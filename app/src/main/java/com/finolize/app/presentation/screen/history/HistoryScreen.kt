package com.finolize.app.presentation.screen.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import com.finolize.app.R
import com.finolize.app.core.utils.IconMapper
import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.presentation.components.DeleteDialog
import com.finolize.app.presentation.components.ExpenseItem
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
        // 1. Headline
        Text(
            text = stringResource(R.string.nav_history),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(16.dp)
        )

        // 2. Search Field
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(R.string.search_transactions)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // 3. Filter by categories
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

        // 5. List of transactions
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (state.groupedExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ManageSearch,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.no_records_found),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                state.groupedExpenses.forEach { monthGroup ->
                    stickyHeader(key = monthGroup.name) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = monthGroup.name.uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${state.currency}${String.format("%.2f", monthGroup.totalAmount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    monthGroup.days.forEach { (day, expenses) ->
                        item {
                            Text(
                                text = day,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        itemsIndexed(items = expenses, key = { _, expense -> expense.id }) { index, expense ->
                            val categoryInfo = state.categories.find { it.name == expense.category }
                            val icon = IconMapper.getIconByName(categoryInfo?.iconName ?: "Category")
                            val color = categoryInfo?.let { Color(it.colorHex.toColorInt()) } ?: Color.Gray

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        expenseToDelete = expense
                                        false
                                    } else false
                                }
                            )

                            Column(modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .animateItem()) {
                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    backgroundContent = {
                                        val bgColor = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                            MaterialTheme.colorScheme.errorContainer else Color.Transparent
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .background(bgColor, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
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

                                if (index < expenses.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (expenseToDelete != null) {
        DeleteDialog(
            onConfirm = {
                expenseToDelete?.let { viewModel.deleteExpense(it) }
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null })
    }
}