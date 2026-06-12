package com.finolize.app.presentation.screen.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.finolize.app.core.utils.BiometricHelper
import com.finolize.app.data.local.prefs.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    application: Application
) : ViewModel() {

    var selectedLanguage by mutableStateOf(preferenceManager.getLanguage())
        private set
    var selectedCurrency by mutableStateOf(preferenceManager.getCurrency())
        private set

    var isBiometricEnabled by mutableStateOf(preferenceManager.isBiometricEnabled())
        private set

    var isBiometricHardwareAvailable by mutableStateOf(false)
        private set

    init {
        isBiometricHardwareAvailable = BiometricHelper.canAuthenticate(application)

        // Если вдруг биометрия была включена, но пользователь убрал пароль с телефона — выключаем
        if (!isBiometricHardwareAvailable && isBiometricEnabled) {
            toggleBiometric(false)
        }
    }

    fun updateLanguage(langCode: String) {
        selectedLanguage = langCode
        preferenceManager.setLanguage(langCode)
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun updateCurrency(currency: String) {
        selectedCurrency = currency
        preferenceManager.setCurrency(currency)
    }

    fun toggleBiometric(enabled: Boolean) {
        isBiometricEnabled = enabled
        preferenceManager.setBiometricEnabled(enabled)
    }
}