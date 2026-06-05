package com.finolize.app.presentation.screen.add_expense

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.domain.model.CategoryList
import com.finolize.app.domain.usecase.AddExpenseUseCase
import com.finolize.app.domain.usecase.GetExpenseByIdUseCase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import com.finolize.app.data.local.prefs.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    // Состояния для полей ввода (переносим из Screen во ViewModel)
    var amount by mutableStateOf("")
    var description by mutableStateOf("")
    var selectedCategoryName by mutableStateOf(CategoryList.categories[0].name)
    var selectedTimestamp by mutableLongStateOf(System.currentTimeMillis())
    var isEditing by mutableStateOf(false)
    private var editingId: Long? = null
    val currency = preferenceManager.getCurrency()

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


    fun saveExpense() {
        val amountDouble = amount.toDoubleOrNull() ?: 0.0
        if (amountDouble <= 0) return

        viewModelScope.launch {
            val expense = ExpenseEntity(
                id = editingId ?: 0, // Если редактируем, используем старый ID
                amount = amountDouble,
                category = selectedCategoryName,
                description = description,
                timestamp = selectedTimestamp
            )
            addExpenseUseCase(expense)
        }
    }
}