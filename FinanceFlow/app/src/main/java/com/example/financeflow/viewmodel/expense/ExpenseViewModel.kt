package com.example.financeflow.viewmodel.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeflow.model.Expense
import com.example.financeflow.repository.expense.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class ExpenseUiState(
    val selectedMonth: String = currentMonthLabel(),
    val expenses: List<Expense> = emptyList(),
    val visibleExpenses: List<Expense> = emptyList(),
    val totalOptional: Double = 0.0,
    val totalMust: Double = 0.0,
    val todaySpending: Double = 0.0,
    val optionalBudget: Double = 100000.0,
    val showAddExpense: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val optionalRemaining: Double
        get() = (optionalBudget - totalOptional).coerceAtLeast(0.0)
}

data class AddExpenseFormState(
    val expenseType: String = "must",
    val amount: String = "",
    val amountError: String? = null,
    val category: String = "Food",
    val description: String = "",
    val descriptionError: String? = null,
    val paymentMethod: String = "Card",
    val date: String = currentDateLabel(),
    val dateError: String? = null
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(currentMonthLabel())
    private val showAddExpense = MutableStateFlow(false)
    private val isSaving = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val formState = MutableStateFlow(AddExpenseFormState())

    val addExpenseForm: StateFlow<AddExpenseFormState> = formState

    val uiState: StateFlow<ExpenseUiState> = combine(
        repository.getExpensesFlow(),
        selectedMonth,
        showAddExpense,
        isSaving,
        errorMessage
    ) { expenses, month, addVisible, saving, error ->
        val monthKey = labelToMonthKey(month)
        val today = currentDateLabel()
        val visible = expenses.filter { it.monthKey == monthKey }
        ExpenseUiState(
            selectedMonth = month,
            expenses = expenses,
            visibleExpenses = visible,
            totalOptional = visible.filter { it.expenseType.equals("optional", true) }.sumOf { it.amount },
            totalMust = visible.filter { it.expenseType.equals("must", true) }.sumOf { it.amount },
            todaySpending = visible.filter { it.date == today }.sumOf { it.amount },
            showAddExpense = addVisible,
            isSaving = saving,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseUiState()
    )

    fun selectMonth(month: String) {
        selectedMonth.value = month
    }

    fun openAddExpense(category: String? = null) {
        formState.value = AddExpenseFormState(
            category = category ?: "Food",
            expenseType = if (category == "Rent") "must" else "optional"
        )
        showAddExpense.value = true
    }

    fun closeAddExpense() {
        showAddExpense.value = false
        errorMessage.value = null
    }

    fun updateExpenseType(type: String) {
        formState.value = formState.value.copy(expenseType = type)
    }

    fun updateAmount(amount: String) {
        val filtered = amount
            .filter { it.isDigit() || it == '.' }
            .let { value ->
                val firstDot = value.indexOf('.')
                if (firstDot == -1) value else {
                    value.take(firstDot + 1) + value.drop(firstDot + 1).replace(".", "")
                }
            }
            .take(10)

        formState.value = formState.value.copy(
            amount = filtered,
            amountError = null
        )
    }

    fun updateCategory(category: String) {
        formState.value = formState.value.copy(category = category)
    }

    fun updateDescription(description: String) {
        formState.value = formState.value.copy(
            description = description.take(80),
            descriptionError = null
        )
    }

    fun updatePaymentMethod(paymentMethod: String) {
        formState.value = formState.value.copy(paymentMethod = paymentMethod)
    }

    fun updateDate(date: String) {
        formState.value = formState.value.copy(
            date = date.take(20),
            dateError = null
        )
    }

    fun saveExpense() {
        val form = formState.value
        val amount = form.amount.toDoubleOrNull()
        val amountError = when {
            form.amount.isBlank() -> "Amount is required"
            amount == null -> "Enter a valid amount"
            amount <= 0.0 -> "Amount must be more than zero"
            amount > 10_000_000.0 -> "Amount is too high"
            else -> null
        }
        val descriptionError = if (form.description.length > 80) "Keep description under 80 characters" else null
        val dateError = if (parseExpenseDate(form.date) == null) "Use date like May 22, 2026" else null

        if (amountError != null || descriptionError != null || dateError != null) {
            formState.value = form.copy(
                amountError = amountError,
                descriptionError = descriptionError,
                dateError = dateError
            )
            errorMessage.value = "Please fix the highlighted fields"
            return
        }

        if (amount == null || amount <= 0.0) {
            errorMessage.value = "Enter a valid amount"
            return
        }

        viewModelScope.launch {
            isSaving.value = true
            errorMessage.value = null
            try {
                repository.addExpense(
                    Expense(
                        amount = amount,
                        category = form.category,
                        description = form.description.ifBlank { form.category },
                        expenseType = form.expenseType,
                        paymentMethod = form.paymentMethod,
                        date = form.date,
                        monthKey = dateLabelToMonthKey(form.date),
                        timestamp = System.currentTimeMillis()
                    )
                )
                showAddExpense.value = false
                formState.value = AddExpenseFormState()
            } catch (exception: Exception) {
                errorMessage.value = exception.message ?: "Unable to save expense"
            } finally {
                isSaving.value = false
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expenseId)
            } catch (exception: Exception) {
                errorMessage.value = exception.message ?: "Unable to delete expense"
            }
        }
    }
}

fun formatLkr(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 0
    return "LKR ${formatter.format(amount)}"
}

private fun currentMonthLabel(): String =
    YearMonth.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))

private fun currentDateLabel(): String =
    LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))

private fun labelToMonthKey(label: String): String =
    YearMonth.parse(label, DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)).toString()

private fun dateLabelToMonthKey(label: String): String =
    parseExpenseDate(label)?.let { YearMonth.from(it).toString() } ?: YearMonth.now().toString()

private fun parseExpenseDate(label: String): LocalDate? =
    runCatching {
        LocalDate.parse(label, DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))
    }.getOrNull()
