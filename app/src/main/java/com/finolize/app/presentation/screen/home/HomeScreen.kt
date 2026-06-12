package com.finolize.app.presentation.screen.home


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.finolize.app.R
import com.finolize.app.core.utils.IconMapper
import com.finolize.app.data.local.entity.ExpenseEntity
import com.finolize.app.presentation.components.DeleteDialog
import com.finolize.app.presentation.components.ExpenseItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt
import com.finolize.app.core.utils.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
    val fullDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }

    val cardHeight = 132.dp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Заголовок (дата)
        item {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = stringResource(R.string.today),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = fullDate.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        // 2. Блок баланса (Шиммер или Реальная карточка)
        item {
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight)
                        .clip(RoundedCornerShape(28.dp))
                        .shimmerEffect()
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = cardHeight),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)) {
                        Text(
                            text = stringResource(R.string.todays_expenses).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${state.currency}${
                                String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    state.totalBalance
                                )
                            }",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                }
            }
        }

        // 3. Заголовок истории
        item {
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.today_history),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }

        // 4. Список трат (Шиммер или Реальный список)
        if (state.isLoading) {
            items(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
            }
        } else if (state.expenses.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "☕", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_expenses_today),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(items = state.expenses, key = { it.id }) { expense ->
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

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val bgColor =
                            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
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
                        amount = String.format(Locale.getDefault(), "%.2f", expense.amount),
                        timestamp = expense.timestamp,
                        description = expense.description,
                        currency = state.currency
                    )
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
            onDismiss = { expenseToDelete = null }
        )
    }
}