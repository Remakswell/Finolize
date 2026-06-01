package com.finolize.app.domain.usecase

import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.domain.repository.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: ExpenseEntity) {
        repository.insertExpense(expense)
    }
}