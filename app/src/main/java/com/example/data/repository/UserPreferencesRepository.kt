package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.ai.AiConsultantConfig
import com.example.data.ai.AiProvider
import com.example.data.ai.TestLengthMode
import com.example.data.model.UserProfile

class UserPreferencesRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("catiq_user_prefs", Context.MODE_PRIVATE)

  fun getUserProfile(): UserProfile {
    val isRegistered = prefs.getBoolean(KEY_IS_REGISTERED, false)
    val fullName = prefs.getString(KEY_FULL_NAME, "") ?: ""
    val age = prefs.getInt(KEY_AGE, 25)
    val educationLevel = prefs.getString(KEY_EDUCATION, "Universitario") ?: "Universitario"
    val gender = prefs.getString(KEY_GENDER, "No especificado") ?: "No especificado"

    return UserProfile(
      fullName = fullName,
      age = age,
      educationLevel = educationLevel,
      gender = gender,
      isRegistered = isRegistered
    )
  }

  fun saveUserProfile(profile: UserProfile) {
    prefs.edit()
      .putString(KEY_FULL_NAME, profile.fullName)
      .putInt(KEY_AGE, profile.age)
      .putString(KEY_EDUCATION, profile.educationLevel)
      .putString(KEY_GENDER, profile.gender)
      .putBoolean(KEY_IS_REGISTERED, profile.isRegistered)
      .apply()
  }

  fun getAiConfig(): AiConsultantConfig {
    val providerId = prefs.getString(KEY_AI_PROVIDER, AiProvider.GEMINI.id) ?: AiProvider.GEMINI.id
    val geminiKey = prefs.getString(KEY_GEMINI_KEY, "") ?: ""
    val openaiKey = prefs.getString(KEY_OPENAI_KEY, "") ?: ""
    val claudeKey = prefs.getString(KEY_CLAUDE_KEY, "") ?: ""
    val nvidiaKey = prefs.getString(KEY_NVIDIA_KEY, "") ?: ""
    val customModel = prefs.getString(KEY_CUSTOM_MODEL, "") ?: ""
    val lengthModeId = prefs.getString(KEY_LENGTH_MODE, TestLengthMode.STANDARD.id) ?: TestLengthMode.STANDARD.id

    return AiConsultantConfig(
      selectedProvider = AiProvider.fromId(providerId),
      geminiApiKey = geminiKey,
      openaiApiKey = openaiKey,
      claudeApiKey = claudeKey,
      nvidiaApiKey = nvidiaKey,
      customModelName = customModel,
      testLengthMode = TestLengthMode.fromId(lengthModeId)
    )
  }

  fun saveAiConfig(config: AiConsultantConfig) {
    prefs.edit()
      .putString(KEY_AI_PROVIDER, config.selectedProvider.id)
      .putString(KEY_GEMINI_KEY, config.geminiApiKey)
      .putString(KEY_OPENAI_KEY, config.openaiApiKey)
      .putString(KEY_CLAUDE_KEY, config.claudeApiKey)
      .putString(KEY_NVIDIA_KEY, config.nvidiaApiKey)
      .putString(KEY_CUSTOM_MODEL, config.customModelName)
      .putString(KEY_LENGTH_MODE, config.testLengthMode.id)
      .apply()
  }

  companion object {
    private const val KEY_FULL_NAME = "key_full_name"
    private const val KEY_AGE = "key_age"
    private const val KEY_EDUCATION = "key_education"
    private const val KEY_GENDER = "key_gender"
    private const val KEY_IS_REGISTERED = "key_is_registered"

    private const val KEY_AI_PROVIDER = "key_ai_provider"
    private const val KEY_GEMINI_KEY = "key_gemini_key"
    private const val KEY_OPENAI_KEY = "key_openai_key"
    private const val KEY_CLAUDE_KEY = "key_claude_key"
    private const val KEY_NVIDIA_KEY = "key_nvidia_key"
    private const val KEY_CUSTOM_MODEL = "key_custom_model"
    private const val KEY_LENGTH_MODE = "key_length_mode"
  }
}
