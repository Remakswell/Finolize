package com.finolize.app.presentation.screen.stats


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finolize.app.R
import com.finolize.app.presentation.components.PieChart

@Composable
fun StatsScreen(
    paddingValues: PaddingValues,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Заголовок
        Text(
            text = stringResource(R.string.nav_stats),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 2. Переключатель периодов (Tabs)
        TabRow(
            selectedTabIndex = state.selectedPeriod.ordinal,
            containerColor = Color.Transparent,
            divider = {}, // Убираем линию внизу для чистоты
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedPeriod.ordinal]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            StatsPeriod.entries.forEach { period ->
                Tab(
                    selected = state.selectedPeriod == period,
                    onClick = { viewModel.selectPeriod(period) },
                    text = {
                        Text(
                            text = period.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isEmpty) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "No expenses for this period", color = Color.Gray)
            }
        } else {
            // 3. График с суммой в центре
            Box(
                contentAlignment = Alignment.Center, // Центрируем всё внутри
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                PieChart(
                    stats = state.stats,
                    modifier = Modifier.size(220.dp) // Чуть увеличим
                )

                // ВЫВОДИМ СУММУ В ЦЕНТРЕ
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "${state.currency}${String.format("%.2f", state.totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // 4. Список категорий
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.stats) { stat ->
                    StatItem(stat, state.currency)
                }
            }
        }
    }
}

@Composable
fun StatItem(stat: com.finolize.app.domain.usecase.CategoryStat, currency: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(stat.color)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = stat.categoryName, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "${currency}${String.format("%.2f", stat.amount)}", fontWeight = FontWeight.Bold)
            Text(text = "${(stat.percentage * 100).toInt()}%", fontSize = 12.sp, color = Color.Gray)
        }
    }
}