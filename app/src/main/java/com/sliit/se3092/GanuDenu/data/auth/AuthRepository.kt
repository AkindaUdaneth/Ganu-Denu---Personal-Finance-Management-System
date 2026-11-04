package com.sliit.se3092.GanuDenu.data.auth

import kotlinx.coroutines.flow.Flow

data class AuthIdentity(val uid: String, val email: String)

data class UserProfile(
    val uid: String,
    val name: String,
    val phone: String,
    val email: String,
    val preferredCurrency: String = "LKR"
)

interface AuthRepository {
    val identities: Flow<AuthIdentity?>
    val currentIdentity: AuthIdentity?
    suspend fun signIn(email: String, password: String)
    suspend fun register(email: String, password: String)
    suspend fun signInWithGoogle(idToken: String)
    suspend fun loadProfile(): UserProfile?
    suspend fun createProfile(name: String, phone: String): UserProfile
    suspend fun sendPasswordReset(email: String)
    fun signOut()
}
