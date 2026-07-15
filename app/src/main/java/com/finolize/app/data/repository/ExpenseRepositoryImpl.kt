package com.finolize.app.data.repository

import com.finolize.app.data.local.dao.CategoryDao
import com.finolize.app.data.local.dao.ExpenseDao
import com.finolize.app.data.local.entity.CategoryEntity
import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao,
    private val categoryDao: CategoryDao
) : ExpenseRepository {
    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    override suspend fun insertExpense(expense: ExpenseEntity) = dao.insertExpense(expense)
    override suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)
    override suspend fun getExpenseById(id: Long): ExpenseEntity? = dao.getExpenseById(id)
    override fun getAllCategories() = categoryDao.getAllCategories()
    override suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)
    override suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    override suspend fun updateCategory(oldName: String, category: CategoryEntity) {
        categoryDao.insertCategory(category)
        if (oldName != category.name) {
            dao.updateExpenseCategory(oldName, category.name)
        }
    }
}