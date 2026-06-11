package com.finolize.app.presentation.screen.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finolize.app.data.local.entity.CategoryEntity
import com.finolize.app.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(private val repository: ExpenseRepository) :
    ViewModel() {
    val categories = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var isSaving by mutableStateOf(false)
        private set

    fun addCategory(name: String, icon: String, color: String, onSuccess: () -> Unit) {
        if (isSaving) return
        viewModelScope.launch {
            isSaving = true
            repository.insertCategory(
                CategoryEntity(name = name, iconName = icon, colorHex = color)
            )
            onSuccess()
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}