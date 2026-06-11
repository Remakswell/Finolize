package com.finolize.app.data.repository

import com.finolize.app.R
import com.finolize.app.data.local.dao.CategoryDao
import com.finolize.app.data.local.dao.ExpenseDao
import com.finolize.app.data.local.entity.CategoryEntity
import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao,
    private val categoryDao: CategoryDao
) : ExpenseRepository {
    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    override suspend fun insertExpense(expense: ExpenseEntity) = dao.insertExpense(expense)
    override suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)
    override suspend fun getExpenseById(id: Long): ExpenseEntity? = dao.getExpenseById(id)

    override suspend fun prefillCategories(context: android.content.Context) {
        // Проверяем, есть ли уже категории в базе
        val existing = categoryDao.getAllCategories().first()
        if (existing.isEmpty()) {
            val initialCategory = CategoryEntity( name = "General", iconName = "Category", colorHex = "#9E9E9E", isSystem = true )
            categoryDao.insertInitialCategories(listOf(initialCategory))
        }
    }
    override fun getAllCategories() = categoryDao.getAllCategories()
    override suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)
    override suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)
}