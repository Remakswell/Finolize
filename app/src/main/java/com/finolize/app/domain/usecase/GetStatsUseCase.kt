package com.finolize.app.domain.usecase

import com.finolize.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import androidx.compose.ui.graphics.Color
import com.finolize.app.presentation.screen.stats.StatsPeriod
import kotlinx.coroutines.flow.combine
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

        // Объединяем поток расходов и поток категорий
        return combine(
            repository.getAllExpenses(),
            repository.getAllCategories()
        ) { expenses, categories ->
            val filteredExpenses = if (period == StatsPeriod.ALL) expenses
            else expenses.filter { it.timestamp >= startTime }

            val total = filteredExpenses.sumOf { it.amount }
            if (total == 0.0) return@combine emptyList()

            filteredExpenses.groupBy { it.category }
                .map { (name, list) ->
                    val categoryAmount = list.sumOf { it.amount }
                    // Ищем цвет в списке категорий из базы
                    val categoryInfo = categories.find { it.name == name }
                    val color = categoryInfo?.let { Color(android.graphics.Color.parseColor(it.colorHex)) }
                        ?: Color.Gray

                    CategoryStat(
                        categoryName = name,
                        amount = categoryAmount,
                        percentage = (categoryAmount / total).toFloat(),
                        color = color
                    )
                }
                .sortedByDescending { it.amount }
        }
    }

    private fun calculateStartTime(period: StatsPeriod): Long {
        val calendar = Calendar.getInstance()
        when (period) {
            StatsPeriod.TODAY -> {
                // Сбрасываем часы, минуты, секунды и миллисекунды на ноль
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            StatsPeriod.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            StatsPeriod.MONTH -> calendar.add(Calendar.MONTH, -1)
            StatsPeriod.YEAR -> calendar.add(Calendar.YEAR, -1)
            StatsPeriod.ALL -> return 0L
        }
        return calendar.timeInMillis
    }
}