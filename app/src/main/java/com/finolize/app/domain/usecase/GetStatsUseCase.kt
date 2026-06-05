package com.finolize.app.domain.usecase

import com.finolize.app.domain.model.CategoryList
import com.finolize.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.compose.ui.graphics.Color
import com.finolize.app.presentation.screen.stats.StatsPeriod
import java.util.Calendar
import javax.inject.Inject

data class CategoryStat(
    val categoryName: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

class GetStatsUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(period: StatsPeriod): Flow<List<CategoryStat>> {
        val startTime = calculateStartTime(period)
        return repository.getAllExpenses().map { expenses ->
            // 1. Фильтруем по времени
            val filteredExpenses = if (period == StatsPeriod.ALL) {
                expenses
            } else {
                expenses.filter { it.timestamp >= startTime }
            }

            val total = filteredExpenses.sumOf { it.amount }
            if (total == 0.0) return@map emptyList()

            // 2. Группируем и считаем проценты
            filteredExpenses.groupBy { it.category }
                .map { (name, list) ->
                    val categoryAmount = list.sumOf { it.amount }
                    val categoryInfo = CategoryList.getCategoryByName(name)
                    CategoryStat(
                        categoryName = name,
                        amount = categoryAmount,
                        percentage = (categoryAmount / total).toFloat(),
                        color = categoryInfo.color
                    )
                }
                .sortedByDescending { it.amount }
        }
    }

    private fun calculateStartTime(period: StatsPeriod): Long {
        val calendar = Calendar.getInstance()
        when (period) {
            StatsPeriod.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            StatsPeriod.MONTH -> calendar.add(Calendar.MONTH, -1)
            StatsPeriod.YEAR -> calendar.add(Calendar.YEAR, -1)
            StatsPeriod.ALL -> return 0L
        }
        return calendar.timeInMillis
    }
}