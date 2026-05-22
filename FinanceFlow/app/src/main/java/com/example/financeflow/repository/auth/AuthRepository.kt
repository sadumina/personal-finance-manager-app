package com.example.financeflow.repository.auth

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

    fun logout()
}
