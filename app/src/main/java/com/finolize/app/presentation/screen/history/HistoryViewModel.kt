package com.finolize.app.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.data.local.prefs.PreferenceManager
import com.finolize.app.domain.usecase.DeleteExpenseUseCase
import com.finolize.app.domain.usecase.GetExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val groupedExpenses: Map<String, List<ExpenseEntity>> = emptyMap(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val currency: String = "$"
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)

    val state: StateFlow<HistoryUiState> = combine(
        getExpensesUseCase(),
        _searchQuery,
        _selectedCategory,
        preferenceManager.currencyFlow
    ) { expenses, query, category, currency ->

        val filtered = expenses.filter { expense ->
            val matchesQuery = expense.description.contains(query, ignoreCase = true) ||
                    expense.category.contains(query, ignoreCase = true)
            val matchesCategory = category == null || expense.category == category
            matchesQuery && matchesCategory
        }

        HistoryUiState(
            groupedExpenses = filtered.groupBy {
                // Группируем по месяцам для истории (например "May 2024")
                val date = java.util.Date(it.timestamp)
                java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(date)
            },
            searchQuery = query,
            selectedCategory = category,
            currency = currency
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
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