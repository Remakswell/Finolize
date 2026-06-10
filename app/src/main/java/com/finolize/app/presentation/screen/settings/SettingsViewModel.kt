package com.finolize.app.presentation.screen.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.finolize.app.data.local.prefs.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    var selectedLanguage by mutableStateOf(preferenceManager.getLanguage())
        private set
    var selectedCurrency by mutableStateOf(preferenceManager.getCurrency())
        private set

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
}