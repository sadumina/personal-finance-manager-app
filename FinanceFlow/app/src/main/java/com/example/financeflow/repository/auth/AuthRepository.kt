package com.example.financeflow.repository.auth

data class AuthUserProfile(
    val name: String,
    val email: String
)

interface AuthRepository {

    val isUserLoggedIn: Boolean

    suspend fun login(
        email: String,
        password: String
    ): Result<Unit>

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Result<Unit>

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    suspend fun getCurrentUserProfile(): AuthUserProfile?

    fun logout()
}
