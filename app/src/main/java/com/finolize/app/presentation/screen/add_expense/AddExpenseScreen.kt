package com.finolize.app.presentation.screen.add_expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finolize.app.R
import com.finolize.app.core.utils.toFormattedDate
import com.finolize.app.domain.model.CategoryList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseId: Long = -1L,
    onNavigateBack: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    var amountText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }
    var currentCategory by remember { mutableStateOf(CategoryList.categories[0]) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = viewModel.selectedTimestamp
    )

    LaunchedEffect(expenseId) {
        if (expenseId != -1L) viewModel.loadExpense(expenseId)
    }

    // Синхронизируем UI с ViewModel при редактировании
    LaunchedEffect(viewModel.isEditing) {
        if (viewModel.isEditing) {
            amountText = viewModel.amount
            descText = viewModel.description
            currentCategory = CategoryList.getCategoryByName(viewModel.selectedCategoryName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Expense" else stringResource(R.string.add_expense)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                value = amountText,
                onValueChange = { input ->
                    val filtered = input.replace(",", ".")
                    val regex = Regex("""^\d{0,6}(\.\d{0,2})?$""")
                    if (filtered.isEmpty() || filtered.matches(regex)) {
                        val doubleValue = filtered.toDoubleOrNull() ?: 0.0
                        if (doubleValue <= 1000000.0) {
                            amountText = filtered
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
                value = descText,
                onValueChange = { descText = it },
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
                    Text(text = viewModel.selectedTimestamp.toFormattedDate()) // Твоя функция из DateUtils
                }
            }

            // 4. Выбор категории
            Text(stringResource(R.string.category), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CategoryList.categories) { cat ->
                    FilterChip(
                        selected = currentCategory == cat,
                        onClick = { currentCategory = cat },
                        label = { Text(cat.name) },
                        leadingIcon = { Icon(cat.icon, null, Modifier.size(18.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. Кнопка сохранения
            Button(
                onClick = {
                    viewModel.amount = amountText
                    viewModel.description = descText
                    viewModel.selectedCategoryName = currentCategory.name
                    viewModel.saveExpense()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amountText.isNotEmpty() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.save))
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