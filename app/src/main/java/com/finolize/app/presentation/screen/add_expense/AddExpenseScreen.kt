package com.finolize.app.presentation.screen.add_expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.finolize.app.R
import com.finolize.app.core.utils.IconMapper
import com.finolize.app.core.utils.toFormattedDate
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseId: Long = -1L,
    onNavigateBack: () -> Unit,
    navController: NavHostController,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = viewModel.selectedTimestamp
    )
    var isNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(expenseId) {
        if (expenseId != -1L) viewModel.loadExpense(expenseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Expense" else stringResource(R.string.add_expense)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Поле ввода суммы
            OutlinedTextField(
                value = viewModel.amount,
                onValueChange = { input ->
                    val filtered = input.replace(",", ".")
                    val regex = Regex("""^\d{0,6}(\.\d{0,2})?$""")
                    if (filtered.isEmpty() || filtered.matches(regex)) {
                        val doubleValue = filtered.toDoubleOrNull() ?: 0.0
                        if (doubleValue <= 1000000.0) {
                            viewModel.amount = filtered
                        }
                    }
                },
                label = { Text(stringResource(R.string.enter_amount)) },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("${viewModel.currency} ") }
            )

            // 2. Поле ввода описания
            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            // 3. выбор даты
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(text = viewModel.selectedTimestamp.toFormattedDate(context)) // Твоя функция из DateUtils
                }
            }

            // 4. Выбор категории
            Text(stringResource(R.string.category), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category -> // Используем список из базы!
                    FilterChip(
                        selected = viewModel.selectedCategoryName == category.name,
                        onClick = { viewModel.selectedCategoryName = category.name },
                        label = { Text(category.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = IconMapper.getIconByName(category.iconName),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(category.colorHex.toColorInt())
                            )
                        }
                    )
                }
                item {
                    IconButton(onClick = { navController.navigate("add_category") }) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Add Category",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. Кнопка сохранения
            Button(
                onClick = {
                    viewModel.saveExpense(onSuccess = { onNavigateBack() })
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isSaving && viewModel.amount.isNotBlank() && (viewModel.amount.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (viewModel.isSaving) {
                    // Вместо текста показываем крутилку
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
                }

            }
        }
    }

    // диалог календаря
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.selectedTimestamp = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}