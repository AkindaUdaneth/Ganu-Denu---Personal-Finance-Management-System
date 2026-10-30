package com.sliit.se3092.ganudenu

import com.google.firebase.firestore.FirebaseFirestoreException
import com.sliit.se3092.GanuDenu.data.income.IncomeSignInRequiredException
import com.sliit.se3092.GanuDenu.ui.income.incomeError
import org.junit.Assert.assertTrue
import org.junit.Test

/** Firestore error codes initialize Android SparseArray, so run this on Android. */
class IncomeErrorTest {
    @Test fun backendErrorsDescribeAuthRulesAndConnectionSeparately() {
        assertTrue(incomeError(IncomeSignInRequiredException()).contains("signed out"))
        fun error(code: FirebaseFirestoreException.Code) = incomeError(FirebaseFirestoreException("Test", code))
        assertTrue(error(FirebaseFirestoreException.Code.PERMISSION_DENIED).contains("rules"))
        assertTrue(error(FirebaseFirestoreException.Code.UNAUTHENTICATED).contains("Sign in"))
        assertTrue(error(FirebaseFirestoreException.Code.FAILED_PRECONDITION).contains("index"))
        assertTrue(error(FirebaseFirestoreException.Code.UNAVAILABLE).contains("connection"))
    }
}
