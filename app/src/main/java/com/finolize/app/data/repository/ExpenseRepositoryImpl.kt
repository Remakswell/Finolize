package com.finolize.app.data.repository

import com.finolize.app.data.local.dao.ExpenseDao
import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao
) : ExpenseRepository {
    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    override suspend fun insertExpense(expense: ExpenseEntity) = dao.insertExpense(expense)
    override suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)
    override suspend fun getExpenseById(id: Long): ExpenseEntity? = dao.getExpenseById(id)
}