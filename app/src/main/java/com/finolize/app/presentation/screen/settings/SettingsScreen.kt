package com.finolize.app.presentation.screen.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finolize.app.R
import androidx.core.net.toUri
import android.widget.Toast

@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    onNavigateToManageCategories: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showCurrencyDialog by remember { mutableStateOf(false) }

    val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
    } else {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    val versionName = packageInfo.versionName ?: "1.0.0"

    val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()
    }

    val errorNoEmail = stringResource(R.string.error_no_email_app)

    val shareMessage = stringResource(
        R.string.share_message,
        "https://play.google.com/store/apps/details?id=${context.packageName}"
    )
    val feedbackSubject = stringResource(R.string.feedback_subject, versionName)

    val rateApp = {
        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri())
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=${context.packageName}".toUri())
            context.startActivity(webIntent)
        }
    }

    val shareApp = {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        context.startActivity(Intent.createChooser(shareIntent, null))
    }

    val contactSupport = {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:finolize@gmail.com".toUri()
            putExtra(Intent.EXTRA_SUBJECT, feedbackSubject)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, errorNoEmail, Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.nav_settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        // --- GENERAL ---
        item {
            SettingsSectionTitle(stringResource(R.string.general))
            Card(shape = MaterialTheme.shapes.extraLarge) {
                Column {
                    SettingsMenuItem(
                        title = stringResource(R.string.manage_categories),
                        icon = Icons.Default.Category,
                        onClick = onNavigateToManageCategories
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SettingsMenuItem(
                        title = stringResource(R.string.currency),
                        subtitle = getCurrencyName(viewModel.selectedCurrency),
                        icon = Icons.Default.Payments,
                        onClick = { showCurrencyDialog = true }
                    )
                }
            }
        }

        // --- SECURITY ---
        item {
            SettingsSectionTitle(stringResource(R.string.security))
            Card(shape = MaterialTheme.shapes.extraLarge) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.biometric_auth),
                            color = if (viewModel.isBiometricHardwareAvailable) Color.Unspecified else Color.Gray
                        )
                    },
                    supportingContent = {
                        Text(text = if (viewModel.isBiometricHardwareAvailable)
                            stringResource(R.string.biometric_desc) else stringResource(R.string.biometric_enable))
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.Fingerprint, null,
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

        // --- SUPPORT ---
        item {
            SettingsSectionTitle(stringResource(R.string.support))
            Card(shape = MaterialTheme.shapes.extraLarge) {
                Column {
                    SettingsMenuItem(
                        title = stringResource(R.string.rate_us),
                        icon = Icons.Default.StarRate,
                        onClick = rateApp
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SettingsMenuItem(
                        title = stringResource(R.string.share_app),
                        icon = Icons.Default.Share,
                        onClick = shareApp
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SettingsMenuItem(
                        title = stringResource(R.string.contact_support),
                        icon = Icons.Default.Email,
                        onClick = contactSupport
                    )
                }
            }
        }

        // --- ABOUT ---
        item {
            SettingsSectionTitle(stringResource(R.string.about))
            Card(shape = MaterialTheme.shapes.extraLarge) {
                SettingsMenuItem(
                    title = stringResource(R.string.privacy_policy),
                    icon = Icons.Default.Info,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/finolize-app/privacy"))
                        context.startActivity(intent)
                    }
                )
            }
        }

        // Version info
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "${stringResource(R.string.version)} $versionName ($versionCode)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }

    if (showCurrencyDialog) {
        CurrencyDialog(
            currentCurrency = viewModel.selectedCurrency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = {
                viewModel.updateCurrency(it)
                showCurrencyDialog = false
            }
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 16.dp)
    )
}

@Composable
fun SettingsMenuItem(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(text = title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    )
}

@Composable
fun CurrencyDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val currencies = listOf("$", "€", "₴", "₽", "zł", "R$")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.currency)) },
        text = {
            Column {
                currencies.forEach { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(currency) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currency == currentCurrency, onClick = { onSelect(currency) })
                        Spacer(Modifier.width(12.dp))
                        Text(getCurrencyName(currency))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

private fun getCurrencyName(code: String) = when (code) {
    "$" -> "USD ($)"
    "€" -> "EUR (€)"
    "₴" -> "UAH (₴)"
    "₽" -> "RUB (₽)"
    "zł" -> "PLN (zł)"
    "R$" -> "BRL (R$)"
    else -> code
}