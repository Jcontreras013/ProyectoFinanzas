package com.proyectofinanzas.android.ui.expenses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectofinanzas.android.data.AppContainer
import com.proyectofinanzas.android.data.remote.AccountDto
import com.proyectofinanzas.android.data.remote.CreateExpenseRequest
import com.proyectofinanzas.android.data.remote.ExpenseDto
import com.proyectofinanzas.android.data.remote.friendlyErrorMessage
import com.proyectofinanzas.android.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ExpensesListViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<ExpenseDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ExpenseDto>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(AppContainer.apiService.listExpenses(page = 0, size = 30).content)
            } catch (e: Exception) {
                UiState.Error(friendlyErrorMessage(e))
            }
        }
    }
}

class ExpenseDetailViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<ExpenseDto>>(UiState.Loading)
    val state: StateFlow<UiState<ExpenseDto>> = _state.asStateFlow()

    fun load(expenseId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(AppContainer.apiService.getExpense(expenseId))
            } catch (e: Exception) {
                UiState.Error(friendlyErrorMessage(e))
            }
        }
    }
}

class ExpenseCreateViewModel : ViewModel() {
    var accounts by mutableStateOf<List<AccountDto>>(emptyList())
        private set
    var expenseDate by mutableStateOf(LocalDate.now().toString())
    var description by mutableStateOf("")
    var accountId by mutableStateOf("")
    var amount by mutableStateOf("")
    var paymentMethod by mutableStateOf("BANK")
    var isSaving by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var createdExpenseId by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            runCatching { AppContainer.apiService.listAccounts() }
                .onSuccess { list ->
                    accounts = list.filter { it.type == "EXPENSE" && it.allowsPosting && it.isActive }.sortedBy { it.code }
                }
        }
    }

    fun canSubmit(): Boolean = description.isNotBlank() && accountId.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0

    fun submit() {
        if (isSaving || !canSubmit()) return
        viewModelScope.launch {
            isSaving = true
            error = null
            try {
                val expense = AppContainer.apiService.createExpense(
                    CreateExpenseRequest(
                        expenseDate = expenseDate,
                        currency = "HNL",
                        accountId = accountId,
                        description = description,
                        paymentMethod = paymentMethod,
                        amount = amount,
                    )
                )
                createdExpenseId = expense.id
            } catch (e: Exception) {
                error = friendlyErrorMessage(e)
            } finally {
                isSaving = false
            }
        }
    }
}
