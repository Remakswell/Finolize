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

    private val _currencyFlow = MutableStateFlow(prefs.getString("selected_currency", "$") ?: "$")
    val currencyFlow: StateFlow<String> = _currencyFlow

    fun getCurrency(): String = _currencyFlow.value

    fun setCurrency(currency: String) {
        prefs.edit { putString("selected_currency", currency) }
        _currencyFlow.value = currency // Оповещаем всех подписчиков!
    }

    fun getLanguage(): String {
        val systemLang = java.util.Locale.getDefault().language
        val supportedLangs = listOf("en", "ru", "uk")
        val defaultLang = if (systemLang in supportedLangs) systemLang else "en"
        return prefs.getString("selected_language", null) ?: defaultLang
    }

    fun setLanguage(langCode: String) {
        prefs.edit().putString("selected_language", langCode).apply()
    }
}