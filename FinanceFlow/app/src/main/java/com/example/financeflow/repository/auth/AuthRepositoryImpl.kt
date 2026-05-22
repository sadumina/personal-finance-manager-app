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

            auth.signOut()

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

    override suspend fun getCurrentUserProfile(): AuthUserProfile? {
        val firebaseUser = auth.currentUser ?: return null
        val email = firebaseUser.email.orEmpty()
        val fallbackName = firebaseUser.displayName
            ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

        return runCatching {
            val snapshot = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val firestoreName = snapshot.getString("fullName")
                ?: snapshot.getString("name")
                ?: snapshot.getString("username")

            AuthUserProfile(
                name = firestoreName?.takeIf { it.isNotBlank() } ?: fallbackName,
                email = snapshot.getString("email")?.takeIf { it.isNotBlank() } ?: email
            )
        }.getOrDefault(
            AuthUserProfile(
                name = fallbackName,
                email = email.ifBlank { "No email" }
            )
        )
    }

    override fun logout() {

        auth.signOut()
    }
}
