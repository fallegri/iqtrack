package com.example.data.ai

data class AiConsultantConfig(
  val selectedProvider: AiProvider = AiProvider.GEMINI,
  val geminiApiKey: String = "",
  val openaiApiKey: String = "",
  val claudeApiKey: String = "",
  val nvidiaApiKey: String = "",
  val customModelName: String = "",
  val testLengthMode: TestLengthMode = TestLengthMode.STANDARD
) {
  fun getEffectiveKey(provider: AiProvider): String {
    return when (provider) {
      AiProvider.GEMINI -> geminiApiKey.trim()
      AiProvider.CHATGPT -> openaiApiKey.trim()
      AiProvider.CLAUDE -> claudeApiKey.trim()
      AiProvider.NVIDIA -> nvidiaApiKey.trim()
    }
  }

  fun getEffectiveModel(provider: AiProvider): String {
    return if (customModelName.isNotBlank() && selectedProvider == provider) {
      customModelName.trim()
    } else {
      provider.defaultModel
    }
  }
}

/**
 * Modo de profundidad y extensión de reactivos adaptativos.
 */
enum class TestLengthMode(
  val id: String,
  val title: String,
  val minItems: Int,
  val maxItems: Int,
  val targetSe: Double,
  val description: String
) {
  SCREENING(
    id = "SCREENING",
    title = "Rápido / Screening",
    minItems = 8,
    maxItems = 12,
    targetSe = 0.35,
    description = "Evaluación breve y ágil (8-12 ítems). Estimación rápida de habilidad general."
  ),
  STANDARD(
    id = "STANDARD",
    title = "Estándar Clínico CAT",
    minItems = 12,
    maxItems = 18,
    targetSe = 0.28,
    description = "Recomendado por TRI (12-18 ítems). Convergencia por Máxima Información de Fisher (r ≥ 0.92)."
  ),
  EXHAUSTIVE(
    id = "EXHAUSTIVE",
    title = "Batería Exhaustiva / Multidominio",
    minItems = 20,
    maxItems = 30,
    targetSe = 0.22,
    description = "Evaluación diagnóstica en profundidad (20-30 ítems). Alta precisión individual por cada factor CHC."
  );

  companion object {
    fun fromId(id: String): TestLengthMode {
      return entries.find { it.id.equals(id, ignoreCase = true) } ?: STANDARD
    }
  }
}
