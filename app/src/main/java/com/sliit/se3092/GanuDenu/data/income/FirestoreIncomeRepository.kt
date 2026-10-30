package com.sliit.se3092.GanuDenu.data.income

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.DocumentSnapshot
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreIncomeRepository(private val auth: FirebaseAuth, private val firestore: FirebaseFirestore) : IncomeRepository {
    override fun observeMonth(month: YearMonth) = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { close(IncomeSignInRequiredException()); return@callbackFlow }
        val zone = ZoneId.systemDefault()
        val start = Timestamp(Date.from(month.atDay(1).atStartOfDay(zone).toInstant()))
        val end = Timestamp(Date.from(month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()))
        val registration = firestore.collection("users").document(uid).collection("incomes")
            .whereGreaterThanOrEqualTo("occurredAt", start).whereLessThan("occurredAt", end)
            .orderBy("occurredAt")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null && auth.currentUser?.uid == uid) {
                    val entries = snapshot.documents.mapNotNull { document ->
                        runCatching { decode(document) }.getOrNull()
                    }
                    trySend(IncomeSnapshot(entries, snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites(), snapshot.size() - entries.size))
                }
            }
        val authListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser?.uid != uid) close(IncomeSignInRequiredException())
        }
        auth.addAuthStateListener(authListener)
        awaitClose { registration.remove(); auth.removeAuthStateListener(authListener) }
    }

    override suspend fun save(entry: IncomeEntry) {
        val uid = auth.currentUser?.uid ?: throw IncomeSignInRequiredException()
        // Keep the draft ID on retry so an interrupted acknowledgment cannot duplicate income.
        firestore.collection("users").document(uid).collection("incomes").document(entry.id).set(mapOf(
            "source" to entry.source.key,
            "status" to entry.status.key,
            "originalAmount" to entry.originalAmount,
            "currency" to entry.currency.name,
            "exchangeRateToLkr" to entry.exchangeRateToLkr,
            "amountLkrMinor" to entry.amountLkrMinor,
            "occurredAt" to Timestamp(Date.from(entry.date.atStartOfDay(ZoneId.systemDefault()).toInstant())),
            "note" to entry.note,
            "frequency" to entry.frequency.key,
            "createdAt" to FieldValue.serverTimestamp()
        )).await()
    }

    private fun decode(document: DocumentSnapshot): IncomeEntry {
        fun text(key: String) = requireNotNull(document.getString(key))
        val minor = document.get("amountLkrMinor")
        require(minor is Long) { "Income amounts must use integer minor units." }
        val entry = IncomeEntry(
            document.id, IncomeSource.entries.first { it.key == text("source") },
            IncomeStatus.entries.first { it.key == text("status") }, text("originalAmount"),
            IncomeCurrency.valueOf(text("currency")), text("exchangeRateToLkr"),
            minor,
            requireNotNull(document.getTimestamp("occurredAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            text("note"),
            document.getString("frequency")?.let { key -> IncomeFrequency.entries.first { it.key == key } }
                ?: IncomeFrequency.ONE_OFF
        )
        require(entry.source == IncomeSource.CRYPTO || entry.status != IncomeStatus.PNL)
        require(entry.status == IncomeStatus.PNL || entry.amountLkrMinor > 0)
        val validated = IncomeValidation.toEntry(IncomeDraft(entry.id, entry.source, entry.status,
            entry.originalAmount, entry.currency, entry.exchangeRateToLkr, entry.date.toString(), entry.note, entry.frequency))
        require(validated.amountLkrMinor == entry.amountLkrMinor)
        return entry
    }
}
