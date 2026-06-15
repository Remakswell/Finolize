package com.finolize.app.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finolize.app.R

@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    onNavigateToManageCategories: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val currencies = listOf("$", "€", "₴", "₽")
    val languages = listOf("en" to R.string.lang_en, "ru" to R.string.lang_ru, "uk" to R.string.lang_uk)

    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "1.0.0"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Title
        item {
            Text(
                text = stringResource(R.string.nav_settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 2. Section "GENERAL"
        item {
            Text(
                text = stringResource(R.string.general),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column {
                    // Item: Categories
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.manage_categories)) },
                        leadingContent = { Icon(Icons.Default.Category, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        modifier = Modifier.clickable { onNavigateToManageCategories() }
                    )
                }
            }
        }

        // 3. Section "SECURITY"
        item {
            Text(
                text = stringResource(R.string.security),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.biometric_auth),
                            color = if (viewModel.isBiometricHardwareAvailable) Color.Unspecified else Color.Gray
                        )
                    },
                    supportingContent = {
                        Text(
                            text = if (viewModel.isBiometricHardwareAvailable)
                                stringResource(R.string.biometric_desc)
                            else
                                stringResource(R.string.biometric_enable)
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.Fingerprint,
                            null,
                            tint = if (viewModel.isBiometricHardwareAvailable) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = viewModel.isBiometricEnabled,
                            onCheckedChange = { viewModel.toggleBiometric(it) },
                            enabled = viewModel.isBiometricHardwareAvailable
                        )
                    }
                )
            }
        }

        // 4. Section: Language
        item {
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                languages.forEachIndexed { index, (code, nameRes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateLanguage(code) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stringResource(nameRes))
                        RadioButton(
                            selected = viewModel.selectedLanguage == code,
                            onClick = { viewModel.updateLanguage(code) }
                        )
                    }
                    if (index < languages.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }


        // 5. Section: Currency
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.currency),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                currencies.forEachIndexed { index, currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateCurrency(currency) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = when (currency) {
                                "$" -> "USD ($)"
                                "€" -> "EUR (€)"
                                "₴" -> "UAH (₴)"
                                "₽" -> "RUB (₽)"
                                else -> currency
                            }
                        )
                        RadioButton(
                            selected = viewModel.selectedCurrency == currency,
                            onClick = { viewModel.updateCurrency(currency) }
                        )
                    }
                    if (index < currencies.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray.copy(alpha = 0.5f)
                )
                Text(
                    text = "${stringResource(R.string.version)} $versionName",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}