package com.finolize.app.presentation.screen.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.finolize.app.R
import com.finolize.app.core.utils.IconMapper
import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.presentation.components.DeleteDialog
import com.finolize.app.presentation.components.ExpenseItem
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    paddingValues: PaddingValues,
    navController: NavHostController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // 1. Заголовок
        Text(
            text = stringResource(R.string.nav_history),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(16.dp)
        )

        // 2. Поле Поиска
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(R.string.search_transactions)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // 3. Фильтр по категориям
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.categories) { category ->
                FilterChip(
                    selected = state.selectedCategory == category.name,
                    onClick = { viewModel.onCategorySelect(category.name) },
                    label = { Text(category.name) }
                )
            }
        }

        // 4. Список транзакций с группировкой и удалением
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (state.groupedExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = stringResource(R.string.no_records_found), color = Color.Gray)
                    }
                }
            } else {
                state.groupedExpenses.forEach { (month, expenses) ->
                    item {
                        Text(
                            text = month,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Список трат в этом месяце
                    items(items = expenses, key = { it.id }) { expense ->
                        val categoryInfo = state.categories.find { it.name == expense.category }
                        val icon = IconMapper.getIconByName(categoryInfo?.iconName ?: "Category")
                        val color = categoryInfo?.let {
                            Color(it.colorHex.toColorInt())
                        } ?: Color.Gray
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    expenseToDelete = expense
                                    false
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color =
                                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                        MaterialTheme.colorScheme.errorContainer else Color.Transparent
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(color, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        ) {
                            ExpenseItem(
                                modifier = Modifier.clickable {
                                    navController.navigate("add_expense?expenseId=${expense.id}")
                                },
                                categoryName = expense.category,
                                categoryIcon = icon,
                                categoryColor = color,
                                amount = String.format("%.2f", expense.amount),
                                timestamp = expense.timestamp,
                                description = expense.description,
                                currency = state.currency
                            )
                        }
                    }
                }
            }
        }
    }

    // Диалог подтверждения удаления
    if (expenseToDelete != null) {
        DeleteDialog(
            onConfirm = {
                expenseToDelete?.let {
                    viewModel.deleteExpense(it)
                }
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null })
    }
}