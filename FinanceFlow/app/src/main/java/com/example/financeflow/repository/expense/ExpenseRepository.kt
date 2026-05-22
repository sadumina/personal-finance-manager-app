package com.example.financeflow.repository.expense

import com.example.financeflow.model.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val uid get() = auth.currentUser?.uid ?: error("User not logged in")

    fun getExpensesFlow(): Flow<List<Expense>> = callbackFlow {
        val listener = firestore
            .collection("users").document(uid)
            .collection("expenses")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val expenses = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Expense::class.java)?.copy(id = document.id)
                } ?: emptyList()

                trySend(expenses)
            }

        awaitClose { listener.remove() }
    }

    suspend fun addExpense(expense: Expense): String {
        val reference = firestore
            .collection("users").document(uid)
            .collection("expenses")
            .add(expense.copy(userId = uid))
            .await()

        return reference.id
    }

    suspend fun deleteExpense(expenseId: String) {
        firestore
            .collection("users").document(uid)
            .collection("expenses")
            .document(expenseId)
            .delete()
            .await()
    }
}
