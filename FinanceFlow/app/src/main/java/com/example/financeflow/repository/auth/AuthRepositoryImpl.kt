package com.example.financeflow.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit> {

        return try {

            auth.signInWithEmailAndPassword(email, password)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Result<Unit> {

        return try {

            val result = auth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = result.user ?: error("Account created, but user session was not returned")
            val userId = firebaseUser.uid

            firebaseUser.updateProfile(
                userProfileChangeRequest {
                    displayName = fullName.trim()
                }
            ).await()

            val userData = hashMapOf(
                "uid" to userId,
                "fullName" to fullName.trim(),
                "email" to email,
                "phone" to phone,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .set(userData, SetOptions.merge())
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {

        auth.signOut()
    }
}
