package com.finolize.app.presentation.screen.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.finolize.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.finolize.app.core.utils.IconMapper
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    categoryId: Long = -1L,
    onNavigateBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    var oldName by remember { mutableStateOf("") }

    val icons = listOf(
        "ShoppingCart", "Restaurant", "Bus", "Movie", "Favorite",
        "Home", "Work", "School", "Gym", "Medical", "Gas",
        "Bills", "Travel", "Coffee", "Gift", "Pets"
    )

    val colors = listOf(
        Color(0xFFF44336),
        Color(0xFF2196F3),
        Color(0xFF4CAF50),
        Color(0xFFFFEB3B),
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFFE91E63),
        Color(0xFF03A9F4),
        Color(0xFF795548),
        Color(0xFF9E9E9E),
        Color(0xFF009688),
        Color(0xFF8BC34A),
        Color(0xFF3F51B5),
        Color(0xFFCDDC39),
        Color(0xFFFF5722),
        Color(0xFF333333)
    )

    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(icons.first()) }
    var selectedColor by remember { mutableStateOf(colors.first()) }
    var isNavigating by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(categoryId, categories) {
        if (categoryId != -1L && categories.isNotEmpty()) {
            categories.find { it.id == categoryId }?.let { category ->
                name = category.name
                oldName = category.name
                selectedIcon = category.iconName
                selectedColor = Color(category.colorHex.toColorInt())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (categoryId == -1L) stringResource(R.string.new_category) else stringResource(R.string.edit_category)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            onNavigateBack()
                        }
                    }) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // SCROLLABLE CONTENT
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { input ->
                        if (input.length <= 20) {
                            name = input
                            viewModel.clearError()
                        }
                    },
                    label = { Text(stringResource(R.string.category_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = viewModel.nameError != null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    supportingText = {
                        if (viewModel.nameError != null) {
                            Text(
                                text = stringResource(R.string.category_exists),
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(text = "${name.length} / 20", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.select_icon),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Grid of icons by Row (4 per row)
                icons.chunked(4).forEach { rowIcons ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        rowIcons.forEach { iconName ->
                            val icon = IconMapper.getIconByName(iconName)
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedIcon == iconName) selectedColor.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedIcon = iconName },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (selectedIcon == iconName) selectedColor else Color.Gray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.select_color),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Сетка цветов через Row (по 4 в ряд)
                colors.chunked(4).forEach { colorRow ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        colorRow.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == color) {
                                    Icon(Icons.Default.Check, null, tint = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // CREATE BUTTON
            Button(
                onClick = {
                    val colorHex = String.format("#%06X", (0xFFFFFF and selectedColor.toArgb()))
                    if (categoryId == -1L) {
                        viewModel.addCategory(name, selectedIcon, colorHex, onSuccess = { onNavigateBack() })
                    } else {
                        viewModel.updateCategory(categoryId, oldName, name, selectedIcon, colorHex, onSuccess = { onNavigateBack() })
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !viewModel.isSaving && name.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.create_category), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}