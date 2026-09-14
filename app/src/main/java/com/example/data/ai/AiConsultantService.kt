package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AssessmentSession
import com.example.data.model.UserProfile
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Servicio de Consulta Psicométrica con Inteligencia Artificial.
 * Soporta de manera intercambiable:
 * - Google Gemini (Predeterminado, via API REST v1beta)
 * - OpenAI ChatGPT
 * - Anthropic Claude
 * - NVIDIA NIM (https://integrate.api.nvidia.com/v1)
 */
class AiConsultantService(
  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()
) {

  suspend fun generatePsychometricInterpretation(
    session: AssessmentSession,
    profile: UserProfile,
    config: AiConsultantConfig
  ): Result<String> = withContext(Dispatchers.IO) {
    val prompt = buildClinicalPrompt(session, profile)
    val provider = config.selectedProvider
    val key = resolveApiKey(provider, config)

    if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
      return@withContext Result.failure(
        IllegalStateException(
          "No se encontró una API Key configurada para ${provider.displayName}.\n" +
            "Por favor, pulsa en 'Configurar IA' e ingresa tu clave API para habilitar la interpretación."
        )
      )
    }

    try {
      val responseText = when (provider) {
        AiProvider.GEMINI -> callGeminiApi(prompt, key, config.getEffectiveModel(provider))
        AiProvider.CHATGPT -> callOpenAiApi(prompt, key, config.getEffectiveModel(provider), provider.apiBaseUrl)
        AiProvider.CLAUDE -> callClaudeApi(prompt, key, config.getEffectiveModel(provider))
        AiProvider.NVIDIA -> callOpenAiApi(prompt, key, config.getEffectiveModel(provider), provider.apiBaseUrl)
      }
      Result.success(responseText)
    } catch (e: Exception) {
      Log.e("AiConsultantService", "Error calling ${provider.displayName}", e)
      Result.failure(Exception("Error al conectar con ${provider.displayName}: ${e.message ?: "Verifique su conexión y clave API"}"))
    }
  }

  suspend fun askFollowUpQuestion(
    question: String,
    session: AssessmentSession,
    profile: UserProfile,
    previousReport: String,
    config: AiConsultantConfig
  ): Result<String> = withContext(Dispatchers.IO) {
    val prompt = buildFollowUpPrompt(question, session, profile, previousReport)
    val provider = config.selectedProvider
    val key = resolveApiKey(provider, config)

    if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
      return@withContext Result.failure(
        IllegalStateException("Se requiere una API Key configurada para ${provider.displayName}.")
      )
    }

    try {
      val responseText = when (provider) {
        AiProvider.GEMINI -> callGeminiApi(prompt, key, config.getEffectiveModel(provider))
        AiProvider.CHATGPT -> callOpenAiApi(prompt, key, config.getEffectiveModel(provider), provider.apiBaseUrl)
        AiProvider.CLAUDE -> callClaudeApi(prompt, key, config.getEffectiveModel(provider))
        AiProvider.NVIDIA -> callOpenAiApi(prompt, key, config.getEffectiveModel(provider), provider.apiBaseUrl)
      }
      Result.success(responseText)
    } catch (e: Exception) {
      Log.e("AiConsultantService", "Error follow up ${provider.displayName}", e)
      Result.failure(Exception("Error en consulta: ${e.message}"))
    }
  }

  suspend fun testConnection(provider: AiProvider, config: AiConsultantConfig): Result<String> = withContext(Dispatchers.IO) {
    val key = resolveApiKey(provider, config)
    if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
      return@withContext Result.failure(IllegalStateException("La clave API está vacía."))
    }
    val pingPrompt = "Responde únicamente en una sola línea: 'Conexión exitosa con ${provider.displayName}.'"
    try {
      val result = when (provider) {
        AiProvider.GEMINI -> callGeminiApi(pingPrompt, key, config.getEffectiveModel(provider))
        AiProvider.CHATGPT -> callOpenAiApi(pingPrompt, key, config.getEffectiveModel(provider), provider.apiBaseUrl)
        AiProvider.CLAUDE -> callClaudeApi(pingPrompt, key, config.getEffectiveModel(provider))
        AiProvider.NVIDIA -> callOpenAiApi(pingPrompt, key, config.getEffectiveModel(provider), provider.apiBaseUrl)
      }
      Result.success(result.trim())
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun resolveApiKey(provider: AiProvider, config: AiConsultantConfig): String {
    val userKey = config.getEffectiveKey(provider)
    if (userKey.isNotBlank()) return userKey
    if (provider == AiProvider.GEMINI) {
      val buildConfigKey = try {
        BuildConfig.GEMINI_API_KEY
      } catch (_: Throwable) {
        ""
      }
      if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
        return buildConfigKey
      }
    }
    return ""
  }

  private fun callGeminiApi(prompt: String, apiKey: String, model: String): String {
    val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
    val json = JSONObject().apply {
      val contentsArray = JSONArray().apply {
        put(JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().put("text", prompt))
          })
        })
      }
      put("contents", contentsArray)
      put("generationConfig", JSONObject().apply {
        put("temperature", 0.7)
      })
    }

    val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    val request = Request.Builder().url(url).post(requestBody).build()

    client.newCall(request).execute().use { response ->
      val bodyStr = response.body?.string() ?: ""
      if (!response.isSuccessful) {
        val errorMsg = parseErrorMessage(bodyStr, response.code)
        throw Exception("Error ${response.code}: $errorMsg")
      }
      val jsonResp = JSONObject(bodyStr)
      val candidates = jsonResp.optJSONArray("candidates")
      val text = candidates?.optJSONObject(0)
        ?.optJSONObject("content")
        ?.optJSONArray("parts")
        ?.optJSONObject(0)
        ?.optString("text")

      return text ?: "Sin respuesta del modelo."
    }
  }

  private fun callOpenAiApi(prompt: String, apiKey: String, model: String, endpoint: String): String {
    val json = JSONObject().apply {
      put("model", model)
      val messages = JSONArray().apply {
        put(JSONObject().apply {
          put("role", "system")
          put("content", "Eres un neuropsicólogo clínico y especialista en psicometría CHC (Cattell-Horn-Carroll). Proporciona análisis rigurosos, comprensivos y éticos.")
        })
        put(JSONObject().apply {
          put("role", "user")
          put("content", prompt)
        })
      }
      put("messages", messages)
      put("temperature", 0.7)
    }

    val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    val request = Request.Builder()
      .url(endpoint)
      .addHeader("Authorization", "Bearer $apiKey")
      .addHeader("Content-Type", "application/json")
      .post(requestBody)
      .build()

    client.newCall(request).execute().use { response ->
      val bodyStr = response.body?.string() ?: ""
      if (!response.isSuccessful) {
        val errorMsg = parseErrorMessage(bodyStr, response.code)
        throw Exception("Error ${response.code}: $errorMsg")
      }
      val jsonResp = JSONObject(bodyStr)
      val choices = jsonResp.optJSONArray("choices")
      val text = choices?.optJSONObject(0)
        ?.optJSONObject("message")
        ?.optString("content")

      return text ?: "Sin respuesta del modelo."
    }
  }

  private fun callClaudeApi(prompt: String, apiKey: String, model: String): String {
    val url = "https://api.anthropic.com/v1/messages"
    val json = JSONObject().apply {
      put("model", model)
      put("max_tokens", 2500)
      put("system", "Eres un neuropsicólogo clínico y especialista en psicometría CHC (Cattell-Horn-Carroll).")
      val messages = JSONArray().apply {
        put(JSONObject().apply {
          put("role", "user")
          put("content", prompt)
        })
      }
      put("messages", messages)
      put("temperature", 0.7)
    }

    val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    val request = Request.Builder()
      .url(url)
      .addHeader("x-api-key", apiKey)
      .addHeader("anthropic-version", "2023-06-01")
      .addHeader("Content-Type", "application/json")
      .post(requestBody)
      .build()

    client.newCall(request).execute().use { response ->
      val bodyStr = response.body?.string() ?: ""
      if (!response.isSuccessful) {
        val errorMsg = parseErrorMessage(bodyStr, response.code)
        throw Exception("Error ${response.code}: $errorMsg")
      }
      val jsonResp = JSONObject(bodyStr)
      val contentArray = jsonResp.optJSONArray("content")
      val text = contentArray?.optJSONObject(0)?.optString("text")

      return text ?: "Sin respuesta del modelo."
    }
  }

  private fun parseErrorMessage(body: String, code: Int): String {
    return try {
      val obj = JSONObject(body)
      if (obj.has("error")) {
        val errObj = obj.optJSONObject("error")
        if (errObj != null) {
          errObj.optString("message", body)
        } else {
          obj.optString("error", body)
        }
      } else {
        body.take(150)
      }
    } catch (_: Exception) {
      if (code == 401) "Clave API inválida o no autorizada (401)"
      else if (code == 429) "Límite de peticiones excedido (429)"
      else "Código de estado HTTP $code"
    }
  }

  private fun buildClinicalPrompt(session: AssessmentSession, profile: UserProfile): String {
    return """
      Actúa como un Neuropsicólogo Clínico y Consultor Psicométrico experto en el modelo CHC (Cattell-Horn-Carroll) y en Teoría de Respuesta al Ítem (TRI).
      Realiza una interpretación clínica y diagnóstica exhaustiva, rigurosa y constructiva de la siguiente evaluación adaptativa computarizada:

      --- DATOS DEL EVALUADO ---
      - Nombre: ${profile.fullName.ifBlank { "Evaluado/a Anónimo" }}
      - Edad cronológica: ${profile.age} años
      - Nivel de escolaridad: ${profile.educationLevel}
      - Género: ${profile.gender}

      --- RESULTADOS PSICOMÉTRICOS GENERALES ---
      - Cociente Intelectual Escala Completa (FSIQ / WAIS Standard): ${String.format("%.1f", session.fullScaleIq)}
      - Habilidad Latente (Theta θ): ${String.format("%+.2f", session.finalTheta)}
      - Error Estándar de Medición (SE): ${String.format("%.3f", session.standardError)}
      - Intervalo de Confianza al 95%: [${String.format("%.1f", session.confidenceIntervalLow)} - ${String.format("%.1f", session.confidenceIntervalHigh)}]
      - Rango Percentil Normativo: ${String.format("%.1f", session.percentileRank)}°
      - Estado de Validez del Protocolo: ${session.validityStatus}
      - Índice de Atención Sostenida: ${(session.attentionIndex * 100).toInt()}%
      - Latencia Media de Respuesta: ${String.format("%.1f", session.avgResponseTimeMs / 1000.0)} s
      - Reactivos aplicados en la prueba: ${session.totalItems}

      --- DESGLOSE FACTORIAL POR DOMINIO CHC ---
      - Razonamiento Fluido (Gf): CI Factorial = ${String.format("%.1f", session.gfScore)}
      - Procesamiento Visual-Espacial (Gv): CI Factorial = ${String.format("%.1f", session.gvScore)}
      - Memoria de Trabajo Operativa (Gwm): CI Factorial = ${String.format("%.1f", session.gwmScore)}
      - Velocidad de Procesamiento (Gs): CI Factorial = ${String.format("%.1f", session.gsScore)}
      - Inteligencia Cristalizada (Gc): CI Factorial = ${String.format("%.1f", session.gcScore)}

      --- ESTRUCTURA DEL INFORME REQUERIDA (Formato Markdown claro y profesional) ---
      1. **Síntesis Diagnóstica Global:** Categorización clínica del Factor 'g' según normas de Wechsler y significado del intervalo de confianza.
      2. **Análisis del Perfil Cognitivo CHC:**
         - Principales fortalezas cognitivas relativas.
         - Áreas de funcionamiento medio o de oportunidad.
      3. **Discrepancias e Integración Neuropsicológica:**
         - Comparación Gf vs Gc (Potencial adaptativo vs Bagaje semántico adquirido).
         - Relación Gv vs Gwm (Capacidad de visualización vs retención en búfer mental).
         - Impacto de la velocidad de procesamiento (Gs) y atención sostenida.
      4. **Orientación Práctica, Académica y Vocacional:** Contextualizado a la edad y formación del evaluado.
      5. **Estrategias y Ejercicios de Estimulación Cognitiva Personalizados:** 3 a 4 recomendaciones concretas.

      Por favor, utiliza un tono formal, empático, clínicamente sustentado y libre de estigmas, recordando que el CI es una estimación psicométrica puntual sujeta a variables situacionales.
    """.trimIndent()
  }

  private fun buildFollowUpPrompt(
    question: String,
    session: AssessmentSession,
    profile: UserProfile,
    previousReport: String
  ): String {
    return """
      Contexto: El usuario (${profile.fullName.ifBlank { "Evaluado" }}, ${profile.age} años, FSIQ=${String.format("%.1f", session.fullScaleIq)}, Gf=${String.format("%.1f", session.gfScore)}, Gv=${String.format("%.1f", session.gvScore)}, Gwm=${String.format("%.1f", session.gwmScore)}, Gs=${String.format("%.1f", session.gsScore)}, Gc=${String.format("%.1f", session.gcScore)}) ha recibido previamente su informe neuropsicológico y te realiza la siguiente consulta específica:

      PREGUNTA DEL EVALUADO:
      "$question"

      INFORME PREVIO RESUMIDO:
      ${previousReport.take(1000)}

      INSTRUCCIÓN:
      Responde a la duda del usuario en calidad de Neuropsicólogo Consultor. Proporciona una explicación detallada, fundamentada en sus puntuaciones CHC reales, práctica y motivadora.
    """.trimIndent()
  }
}
