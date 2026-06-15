package com.finolize.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import com.finolize.app.domain.usecase.CategoryStat

@Composable
fun PieChart(
    stats: List<CategoryStat>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            stats.forEach { stat ->
                val sweepAngle = stat.percentage * 360f
                drawArc(
                    color = stat.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 40f) // Making a Donut (Donut Chart)
                )
                startAngle += sweepAngle
            }
        }
    }
}