package com.finolize.app.presentation.screen.home

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finolize.app.data.local.entity.CategoryEntity
import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.data.local.prefs.PreferenceManager
import com.finolize.app.domain.repository.ExpenseRepository
import com.finolize.app.domain.usecase.DeleteExpenseUseCase
import com.finolize.app.domain.usecase.GetExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val totalBalance: Double = 0.0,
    val currency: String = "$",
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val repository: ExpenseRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        getExpensesUseCase(),
        repository.getAllCategories(),
        preferenceManager.currencyFlow
    ) { expenses, categories, currency ->

        val todayExpenses = expenses.filter {
            DateUtils.isToday(it.timestamp)
        }
            HomeUiState(
                expenses = todayExpenses,
                categories = categories,
                totalBalance = todayExpenses.sumOf { it.amount },
                currency = currency,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            deleteExpenseUseCase(expense)
        }
    }
}