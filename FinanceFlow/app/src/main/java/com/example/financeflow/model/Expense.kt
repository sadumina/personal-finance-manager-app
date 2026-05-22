package com.example.financeflow.model

data class Expense(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val expenseType: String = "optional",
    val paymentMethod: String = "",
    val date: String = "",
    val monthKey: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
