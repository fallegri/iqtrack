package com.example.engine

import com.example.data.model.ItemResponse
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class TelemetryAnalysis(
  val attentionIndex: Double, // 0.0 a 1.0 (calidad de atención sostenida)
  val avgResponseTimeMs: Long,
  val rapidGuessCount: Int,
  val cognitiveFatigueDetected: Boolean,
  val validityStatus: String,
  val clinicalNotes: List<String>
)

object MobileAttentionTelemetry {
  // Umbral de tiempo mínimo de deliberación cognitiva (ms)
  private const val RAPID_GUESS_THRESHOLD_MS = 1400L

  fun evaluateSession(responses: List<ItemResponse>): TelemetryAnalysis {
    if (responses.isEmpty()) {
      return TelemetryAnalysis(
        attentionIndex = 1.0,
        avgResponseTimeMs = 0L,
        rapidGuessCount = 0,
        cognitiveFatigueDetected = false,
        validityStatus = "INSUFICIENTE",
        clinicalNotes = listOf("No hay suficientes reactivos para computar telemetría.")
      )
    }

    val totalTime = responses.sumOf { it.responseTimeMs }
    val avgTime = totalTime / responses.size

    // 1. Detección de adivinación rápida (Rapid Guessing Behavior - RGB)
    // Se considera adivinación si el sujeto responde en menos de 1.4s en un reactivo con dificultad moderada/alta (b > 0.0)
    var rapidGuesses = 0
    for (res in responses) {
      if (res.responseTimeMs < RAPID_GUESS_THRESHOLD_MS && res.itemDifficultyB > -0.2) {
        rapidGuesses++
      }
    }

    // 2. Detección de fatiga cognitiva
    // Compara la segunda mitad del test contra la primera mitad
    val mid = responses.size / 2
    var fatigueDetected = false
    if (responses.size >= 8) {
      val firstHalfAvg = responses.take(mid).map { it.responseTimeMs }.average()
      val secondHalfAvg = responses.drop(mid).map { it.responseTimeMs }.average()
      val firstHalfAccuracy = responses.take(mid).count { it.isCorrect }.toDouble() / mid
      val secondHalfAccuracy = responses.drop(mid).count { it.isCorrect }.toDouble() / (responses.size - mid)

      // Si la latencia se dispara un 60%+ o la precisión cae más de 35% en ítems comparables
      if (secondHalfAvg > firstHalfAvg * 1.65 && secondHalfAccuracy < firstHalfAccuracy - 0.35) {
        fatigueDetected = true
      }
    }

    // 3. Índice de atención sostenida y estabilidad (0.0 a 1.0)
    var penalty = 0.0
    // Penalización por respuestas impulsivas
    penalty += (rapidGuesses * 0.18)
    // Penalización si el tiempo promedio es extremadamente bajo (< 2.5s)
    if (avgTime < 2500L) {
      penalty += 0.25
    }
    // Penalización por fatiga
    if (fatigueDetected) {
      penalty += 0.15
    }

    val attentionIndex = (1.0 - penalty).coerceIn(0.20, 1.0)

    // 4. Estado de validez psicométrica
    val notes = mutableListOf<String>()
    val validityStatus: String = when {
      rapidGuesses >= 3 -> {
        notes.add("Alerta psicométrica: Se detectaron $rapidGuesses reactivos contestados en menos de 1.4s (patrón de adivinación rápida).")
        "ADVERTENCIA_ADIVINACIÓN"
      }
      fatigueDetected -> {
        notes.add("Degradación de cadencia: Disminución de rendimiento y fluctuación atencional hacia la fase terminal de la prueba.")
        "FATIGA_DETECTADA"
      }
      avgTime < 3000L -> {
        notes.add("Cadencia hiperacelerada: El tiempo de reflexión fue notablemente inferior al percentil normativo.")
        "CADENCIA_ACELERADA"
      }
      else -> {
        notes.add("Protocolo de alta validez: Cadencia atencional uniforme, tiempos de deliberación acordes a la dificultad.")
        "PROTOCOLO_VÁLIDO"
      }
    }

    return TelemetryAnalysis(
      attentionIndex = attentionIndex,
      avgResponseTimeMs = avgTime,
      rapidGuessCount = rapidGuesses,
      cognitiveFatigueDetected = fatigueDetected,
      validityStatus = validityStatus,
      clinicalNotes = notes
    )
  }
}
