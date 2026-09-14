package com.example.data.ai

/**
 * Proveedores de Modelos de Inteligencia Artificial para el Consultor Psicométrico.
 * Satisface soporte nativo para Gemini (por defecto), ChatGPT (OpenAI), Claude (Anthropic)
 * y NVIDIA NIM (https://integrate.api.nvidia.com/v1).
 */
enum class AiProvider(
  val id: String,
  val displayName: String,
  val providerName: String,
  val defaultModel: String,
  val apiBaseUrl: String,
  val docsUrl: String,
  val keyPrefixHint: String
) {
  GEMINI(
    id = "GEMINI",
    displayName = "Google Gemini (Predeterminado)",
    providerName = "Google DeepMind",
    defaultModel = "gemini-3.5-flash",
    apiBaseUrl = "https://generativelanguage.googleapis.com/v1beta/models/",
    docsUrl = "https://aistudio.google.com/app/apikey",
    keyPrefixHint = "AIzaSy..."
  ),
  CHATGPT(
    id = "CHATGPT",
    displayName = "OpenAI ChatGPT",
    providerName = "OpenAI",
    defaultModel = "gpt-4o-mini",
    apiBaseUrl = "https://api.openai.com/v1/chat/completions",
    docsUrl = "https://platform.openai.com/api-keys",
    keyPrefixHint = "sk-..."
  ),
  CLAUDE(
    id = "CLAUDE",
    displayName = "Anthropic Claude",
    providerName = "Anthropic",
    defaultModel = "claude-3-5-haiku-20241022",
    apiBaseUrl = "https://api.anthropic.com/v1/messages",
    docsUrl = "https://console.anthropic.com/",
    keyPrefixHint = "sk-ant-..."
  ),
  NVIDIA(
    id = "NVIDIA",
    displayName = "NVIDIA NIM",
    providerName = "NVIDIA API Catalog",
    defaultModel = "meta/llama-3.1-70b-instruct",
    apiBaseUrl = "https://integrate.api.nvidia.com/v1/chat/completions",
    docsUrl = "https://integrate.api.nvidia.com/v1",
    keyPrefixHint = "nvapi-..."
  );

  companion object {
    fun fromId(id: String): AiProvider {
      return entries.find { it.id.equals(id, ignoreCase = true) } ?: GEMINI
    }
  }
}
