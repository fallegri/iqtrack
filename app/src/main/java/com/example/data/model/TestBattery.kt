package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ColorGc
import com.example.ui.theme.ColorGf
import com.example.ui.theme.ColorGs
import com.example.ui.theme.ColorGv
import com.example.ui.theme.ColorGwm
import com.example.ui.theme.PrimaryBlueDark

/**
 * Baterías y escalas de evaluación psicométrica basadas en el modelo CHC (Cattell-Horn-Carroll)
 * y la literatura de evaluación adaptativa (van der Linden 2016, McGrew 2005).
 */
enum class TestBattery(
  val id: String,
  val title: String,
  val subtitle: String,
  val description: String,
  val eligibleDomains: List<String>,
  val accentColor: Color,
  val minItems: Int = 6,
  val maxItems: Int = 15
) {
  FULL_CHC(
    id = "BATTERY_FULL_CHC",
    title = "Batería Integral CHC",
    subtitle = "Cociente Intelectual Escala Completa (FSIQ)",
    description = "Evaluación adaptativa multidominio completa. Mide Gf, Gv, Gwm, Gs y Gc para estimar el factor 'g' general.",
    eligibleDomains = listOf("Gf", "Gv", "Gwm", "Gs", "Gc"),
    accentColor = PrimaryBlueDark,
    minItems = 8,
    maxItems = 15
  ),
  GF_MATRICES(
    id = "BATTERY_GF_MATRICES",
    title = "Razonamiento Fluido (Gf)",
    subtitle = "Matrices Lógicas e Inducción",
    description = "Inspirado en las Matrices Progresivas de Raven y Escala WAIS. Evalúa deducción de reglas y relaciones abstractas no verbales.",
    eligibleDomains = listOf("Gf"),
    accentColor = ColorGf,
    minItems = 5,
    maxItems = 10
  ),
  GV_SPATIAL(
    id = "BATTERY_GV_SPATIAL",
    title = "Procesamiento Visual-Espacial (Gv)",
    subtitle = "Rotación Mental y Perspectiva 3D",
    description = "Evalúa visualización espacial, manipulación isométrica de objetos tridimensionales y simetrías compuestas.",
    eligibleDomains = listOf("Gv"),
    accentColor = ColorGv,
    minItems = 5,
    maxItems = 10
  ),
  EXECUTIVE_GWM_GS(
    id = "BATTERY_EXECUTIVE",
    title = "Batería Ejecutiva (Gwm + Gs)",
    subtitle = "Memoria de Trabajo y Velocidad",
    description = "Mide manipulación secuencial mental inversa, retención operativa y velocidad de discriminación perceptiva.",
    eligibleDomains = listOf("Gwm", "Gs"),
    accentColor = ColorGwm,
    minItems = 5,
    maxItems = 10
  ),
  GC_CRYSTALLIZED(
    id = "BATTERY_GC_CRYSTALLIZED",
    title = "Inteligencia Cristalizada (Gc)",
    subtitle = "Razonamiento Analógico y Semántico",
    description = "Mide relaciones analógicas abstractas, estructuración verbal y categorización conceptual adquirida.",
    eligibleDomains = listOf("Gc"),
    accentColor = ColorGc,
    minItems = 5,
    maxItems = 10
  );

  val displayName: String get() = title
  val color: Color get() = accentColor
  val durationMinutes: String get() = when (this) {
    FULL_CHC -> "8-12 min"
    GF_MATRICES -> "4-6 min"
    GV_SPATIAL -> "4-6 min"
    EXECUTIVE_GWM_GS -> "3-5 min"
    GC_CRYSTALLIZED -> "3-5 min"
  }

  companion object {
    fun fromId(id: String): TestBattery {
      return entries.find { it.id == id } ?: FULL_CHC
    }
  }
}
