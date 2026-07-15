package com.finolize.app.presentation.screen.add_expense

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.domain.usecase.AddExpenseUseCase
import com.finolize.app.domain.usecase.GetExpenseByIdUseCase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import com.finolize.app.data.local.entity.CategoryEntity
import com.finolize.app.data.local.prefs.PreferenceManager
import com.finolize.app.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase,
    private val repository: ExpenseRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    var amount by mutableStateOf("")
    var description by mutableStateOf("")
    var isCategoriesLoading by mutableStateOf(true)
        private set
    var selectedCategoryName by mutableStateOf("")
    var selectedTimestamp by mutableLongStateOf(System.currentTimeMillis())
    var isEditing by mutableStateOf(false)
    private var editingId: Long? = null
    val categories = repository.getAllCategories()
        .onEach { isCategoriesLoading = false }
        .map { list ->
            list.sortedWith(compareByDescending<CategoryEntity> { it.isSystem }.thenBy { it.name })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currency = preferenceManager.getCurrency()

    var isSaving by mutableStateOf(false)
        private set

    init {
        selectCategoryByDefault()
    }

    fun selectCategoryByDefault() {
        viewModelScope.launch {
            val firstNotEmptyList = categories.filter { it.isNotEmpty() }.first()
            if (!isEditing && selectedCategoryName.isEmpty()) {
                selectedCategoryName = firstNotEmptyList.first().name
            }
        }
    }

    fun loadExpense(id: Long) {
        if (id == -1L || isEditing) return // If you have already downloaded it or the ID is empty
        viewModelScope.launch {
            getExpenseByIdUseCase(id)?.let { expense ->
                isEditing = true
                editingId = expense.id
                amount = expense.amount.toString()
                description = expense.description
                selectedCategoryName = expense.category
                selectedTimestamp = expense.timestamp
            }
        }
    }


    fun saveExpense(onSuccess: () -> Unit) {
        if (isSaving) return
        val amountDouble = amount.toDoubleOrNull() ?: 0.0
        if (amountDouble <= 0) return

        viewModelScope.launch {
            isSaving = true
            val expense = ExpenseEntity(
                id = editingId ?: 0, // If we edit, we use the old ID
                amount = amountDouble,
                category = selectedCategoryName,
                description = description,
                timestamp = selectedTimestamp
            )
            addExpenseUseCase(expense)
            onSuccess()
        }
    }

    fun onDescriptionChange(newDescription: String) {
        if (newDescription.length <= 100) {
            description = newDescription
        }
    }
}