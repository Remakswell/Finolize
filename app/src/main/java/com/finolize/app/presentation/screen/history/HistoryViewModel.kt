package com.finolize.app.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finolize.app.data.local.entity.CategoryEntity
import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.data.local.prefs.PreferenceManager
import com.finolize.app.domain.repository.ExpenseRepository
import com.finolize.app.domain.usecase.DeleteExpenseUseCase
import com.finolize.app.domain.usecase.GetExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class MonthGroup(
    val name: String,
    val totalAmount: Double,
    val days: Map<String, List<ExpenseEntity>>
)

data class HistoryUiState(
    val groupedExpenses: List<MonthGroup> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val totalAmount: Double = 0.0,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val currency: String = "$"
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val repository: ExpenseRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)

    val state: StateFlow<HistoryUiState> = combine(
        getExpensesUseCase(),
        repository.getAllCategories().map { list ->
            list.sortedWith(compareByDescending<CategoryEntity> { it.isSystem }.thenBy { it.name })
        },
        _searchQuery,
        _selectedCategory,
        preferenceManager.currencyFlow
    ) { expenses, categories, query, category, currency ->

        val filtered = expenses.filter { expense ->
            val matchesQuery = expense.description.contains(query, ignoreCase = true) ||
                    expense.category.contains(query, ignoreCase = true)
            val matchesCategory = category == null || expense.category == category
            matchesQuery && matchesCategory
        }

        val monthFormat = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
        val dayFormat = java.text.SimpleDateFormat("d MMMM, EEEE", java.util.Locale.getDefault())

        val grouped = filtered.groupBy {
            monthFormat.format(Date(it.timestamp))
        }.map { (monthName, expensesInMonth) ->
            MonthGroup(
                name = monthName,
                totalAmount = expensesInMonth.sumOf { it.amount }, // Сумма за месяц
                days = expensesInMonth.groupBy {
                    dayFormat.format(Date(it.timestamp))
                }
            )
        }


        HistoryUiState(
            groupedExpenses = grouped,
            categories = categories,
            totalAmount = filtered.sumOf { it.amount },
            searchQuery = query,
            selectedCategory = category,
            currency = currency
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun onSearchQueryChange(query: String) {
        if (query.length <= 50) {
            _searchQuery.value = query
        }
    }

    fun onCategorySelect(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            deleteExpenseUseCase(expense)
        }
    }
}