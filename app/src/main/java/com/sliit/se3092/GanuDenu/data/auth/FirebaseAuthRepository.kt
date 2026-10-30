package com.sliit.se3092.GanuDenu.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    override val currentIdentity: AuthIdentity?
        get() = auth.currentUser?.let { AuthIdentity(it.uid, it.email.orEmpty()) }

    override val identities = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(currentIdentity) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun loadProfile(): UserProfile? {
        val identity = requireNotNull(currentIdentity) { "Please sign in again." }
        // Require server confirmation before treating a pending profile write as saved.
        val snapshot = firestore.collection("users").document(identity.uid).get(Source.SERVER).await()
        return if (snapshot.exists()) UserProfile(
            uid = identity.uid,
            name = snapshot.getString("name").orEmpty(),
            phone = snapshot.getString("phone").orEmpty(),
            email = identity.email,
            preferredCurrency = snapshot.getString("preferredCurrency") ?: "LKR"
        ) else null
    }

    override suspend fun createProfile(name: String, phone: String): UserProfile {
        val identity = requireNotNull(currentIdentity) { "Please sign in again." }
        val reference = firestore.collection("users").document(identity.uid)
        // Retrying after a network interruption must not overwrite an existing profile.
        firestore.runTransaction { transaction ->
            if (!transaction.get(reference).exists()) {
                transaction.set(reference, mapOf(
                    "name" to name,
                    "phone" to phone,
                    "email" to identity.email,
                    "preferredCurrency" to "LKR",
                    "createdAt" to FieldValue.serverTimestamp()
                ))
            }
        }.await()
        return requireNotNull(loadProfile()) { "Profile was not saved. Please retry." }
    }

    override suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override fun signOut() = auth.signOut()
}
