package com.example.tourscan.ui.language

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LanguageViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val PREFS_NAME = "tourscan_prefs"
        private const val KEY_LANGUAGE = "app_language"
    }

    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun toggleLanguage() {
        val newLang = _language.value.toggle()
        _language.value = newLang
        prefs.edit().putString(KEY_LANGUAGE, newLang.code).apply()
    }

    private fun loadLanguage(): AppLanguage {
        val saved = prefs.getString(KEY_LANGUAGE, AppLanguage.RO.code)
        return AppLanguage.entries.find { it.code == saved } ?: AppLanguage.RO
    }
}
