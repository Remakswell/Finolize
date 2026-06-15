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
        .map { list ->
            list.sortedWith(compareByDescending<CategoryEntity> { it.isSystem }.thenBy { it.name })
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    var isSaving by mutableStateOf(false)
        private set

    var nameError by mutableStateOf<String?>(null)
        private set

    fun addCategory(name: String, icon: String, color: String, onSuccess: () -> Unit) {
        if (isSaving) return
        val trimmedName = name.trim()
        // Check for duplicate (case-insensitive)
        val exists = categories.value.any { it.name.equals(trimmedName, ignoreCase = true) }

        if (exists) {
            nameError = "exists"
            return
        }
        viewModelScope.launch {
            isSaving = true
            nameError = null
            repository.insertCategory(
                CategoryEntity(name = trimmedName, iconName = icon, colorHex = color)
            )
            onSuccess()
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun updateCategory(id: Long, oldName: String, name: String, icon: String, color: String, onSuccess: () -> Unit) {
        if (isSaving) return
        val trimmedName = name.trim()

        // Check for duplicates only if the name has changed
        if (!trimmedName.equals(oldName, ignoreCase = true)) {
            val exists = categories.value.any { it.name.equals(trimmedName, ignoreCase = true) }
            if (exists) {
                nameError = "exists"
                return
            }
        }

        viewModelScope.launch {
            isSaving = true
            repository.updateCategory(
                oldName,
                CategoryEntity(id = id, name = trimmedName, iconName = icon, colorHex = color)
            )
            isSaving = false
            onSuccess()
        }
    }

    fun clearError() {
        nameError = null
    }
}