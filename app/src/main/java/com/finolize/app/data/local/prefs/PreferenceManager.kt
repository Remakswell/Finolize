package com.finolize.app.data.local.prefs


import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("finolize_prefs", Context.MODE_PRIVATE)

    private val _currencyFlow = MutableStateFlow(prefs.getString("selected_currency", null) ?: getDefaultCurrencyByLocale())
    val currencyFlow: StateFlow<String> = _currencyFlow

    private val _biometricFlow = MutableStateFlow(prefs.getBoolean("biometric_enabled", false))
    val biometricFlow: StateFlow<Boolean> = _biometricFlow

    fun isFirstRun(): Boolean = prefs.getBoolean("is_first_run", true)

    fun setFirstRun(isFirstRun: Boolean) {
        prefs.edit { putBoolean("is_first_run", isFirstRun) }
    }

    fun getCurrency(): String = _currencyFlow.value

    fun setCurrency(currency: String) {
        prefs.edit { putString("selected_currency", currency) }
        _currencyFlow.value = currency // Оповещаем всех подписчиков!
    }

    fun isBiometricEnabled(): Boolean = _biometricFlow.value

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("biometric_enabled", enabled) }
        _biometricFlow.value = enabled
    }

    private fun getDefaultCurrencyByLocale(): String {
        val locale = java.util.Locale.getDefault()
        return when (locale.language) {
            "uk" -> "₴"
            "ru" -> "₽"
            "pl" -> "zł"
            "de", "es", "fr" -> "€"
            "pt" -> if (locale.country == "BR") "R$" else "€"
            else -> "$"
        }
    }
}