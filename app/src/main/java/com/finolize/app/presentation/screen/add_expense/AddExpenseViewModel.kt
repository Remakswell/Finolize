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

    // Состояния для полей ввода (переносим из Screen во ViewModel)
    var amount by mutableStateOf("")
    var description by mutableStateOf("")
    var selectedCategoryName by mutableStateOf("General")
    var selectedTimestamp by mutableLongStateOf(System.currentTimeMillis())
    var isEditing by mutableStateOf(false)
    private var editingId: Long? = null
    val categories = repository.getAllCategories()
        .map { list ->
            // Сортируем: сначала системные (isSystem == true), потом по алфавиту
            list.sortedWith(compareByDescending<CategoryEntity> { it.isSystem }.thenBy { it.name })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currency = preferenceManager.getCurrency()

    var isSaving by mutableStateOf(false)
        private set

    fun loadExpense(id: Long) {
        if (id == -1L || isEditing) return // Если уже загрузили или ID пустой
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
                id = editingId ?: 0, // Если редактируем, используем старый ID
                amount = amountDouble,
                category = selectedCategoryName,
                description = description,
                timestamp = selectedTimestamp
            )
            addExpenseUseCase(expense)
            onSuccess()
        }
    }
}