package com.finolize.app.domain.repository

import com.finolize.app.data.local.entity.CategoryEntity
import com.finolize.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    suspend fun insertExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)
    suspend fun getExpenseById(id: Long): ExpenseEntity?
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun insertCategory(category: CategoryEntity)
    suspend fun deleteCategory(category: CategoryEntity)
    suspend fun updateCategory(oldName: String, category: CategoryEntity)
}