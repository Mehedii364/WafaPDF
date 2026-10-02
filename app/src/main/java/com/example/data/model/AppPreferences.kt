package com.example.data.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String) {
  BANGLA("bn", "বাংলা (Bangla)"),
  ENGLISH("en", "English"),
  ARABIC("ar", "العربية (Arabic)")
}

data class UserSettings(
  val language: AppLanguage = AppLanguage.BANGLA,
  val theme: String = "SYSTEM", // SYSTEM, LIGHT, DARK, SEPIA
  val defaultCompressionPreset: String = "BALANCED",
  val defaultPaperSize: String = "A4",
  val defaultWatermarkText: String = "CONFIDENTIAL",
  val aiProvider: String = "OpenRouter", // OpenRouter, Gemini, Custom
  val aiApiKey: String = "",
  val aiModel: String = "google/gemini-2.5-flash",
  val vaultPin: String = "1234",
  val isVaultLocked: Boolean = true,
  val keepScreenAwake: Boolean = true
)

class AppPreferencesManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("wafa_pdf_prefs", Context.MODE_PRIVATE)

  private val _settings = MutableStateFlow(loadSettings())
  val settings: StateFlow<UserSettings> = _settings.asStateFlow()

  private fun loadSettings(): UserSettings {
    val langCode = prefs.getString("language", AppLanguage.BANGLA.code) ?: AppLanguage.BANGLA.code
    val lang = AppLanguage.entries.find { it.code == langCode } ?: AppLanguage.BANGLA
    return UserSettings(
      language = lang,
      theme = prefs.getString("theme", "SYSTEM") ?: "SYSTEM",
      defaultCompressionPreset = prefs.getString("compression_preset", "BALANCED") ?: "BALANCED",
      defaultPaperSize = prefs.getString("paper_size", "A4") ?: "A4",
      defaultWatermarkText = prefs.getString("watermark_text", "CONFIDENTIAL") ?: "CONFIDENTIAL",
      aiProvider = prefs.getString("ai_provider", "OpenRouter") ?: "OpenRouter",
      aiApiKey = prefs.getString("ai_api_key", "") ?: "",
      aiModel = prefs.getString("ai_model", "google/gemini-2.5-flash") ?: "google/gemini-2.5-flash",
      vaultPin = prefs.getString("vault_pin", "1234") ?: "1234",
      isVaultLocked = prefs.getBoolean("is_vault_locked", true),
      keepScreenAwake = prefs.getBoolean("keep_screen_awake", true)
    )
  }

  fun updateLanguage(lang: AppLanguage) {
    prefs.edit().putString("language", lang.code).apply()
    _settings.value = _settings.value.copy(language = lang)
  }

  fun updateTheme(theme: String) {
    prefs.edit().putString("theme", theme).apply()
    _settings.value = _settings.value.copy(theme = theme)
  }

  fun updateAiConfig(provider: String, apiKey: String, model: String) {
    prefs.edit()
      .putString("ai_provider", provider)
      .putString("ai_api_key", apiKey)
      .putString("ai_model", model)
      .apply()
    _settings.value = _settings.value.copy(
      aiProvider = provider,
      aiApiKey = apiKey,
      aiModel = model
    )
  }

  fun updateVaultPin(newPin: String) {
    prefs.edit().putString("vault_pin", newPin).apply()
    _settings.value = _settings.value.copy(vaultPin = newPin)
  }

  fun setVaultLocked(locked: Boolean) {
    prefs.edit().putBoolean("is_vault_locked", locked).apply()
    _settings.value = _settings.value.copy(isVaultLocked = locked)
  }

  fun updateKeepScreenAwake(keepAwake: Boolean) {
    prefs.edit().putBoolean("keep_screen_awake", keepAwake).apply()
    _settings.value = _settings.value.copy(keepScreenAwake = keepAwake)
  }

  fun updateWatermarkPreset(watermark: String) {
    prefs.edit().putString("watermark_text", watermark).apply()
    _settings.value = _settings.value.copy(defaultWatermarkText = watermark)
  }
}
