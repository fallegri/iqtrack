package com.example.engine

import com.example.data.model.ChcDomain

data class ClinicalRagReport(
  val qualitativeClassification: String,
  val percentileDescription: String,
  val cognitiveStrengths: List<String>,
  val cognitiveWeaknesses: List<String>,
  val clinicalDiscrepancies: List<String>,
  val neurocognitiveRecommendations: List<String>,
  val detailedNarrative: String
)

object PsychometricRagEngine {

  fun generateClinicalReport(
    iq: Double,
    ciLow: Double,
    ciHigh: Double,
    percentile: Double,
    se: Double,
    domainScores: Map<ChcDomain, Double>,
    telemetry: TelemetryAnalysis
  ): ClinicalRagReport {

    // 1. Clasificación según la distribución gaussiana normada (M=100, SD=15)
    val classification = when {
      iq >= 130.0 -> "Muy Superior (Extremadamente Alto)"
      iq >= 120.0 -> "Superior (Muy Alto)"
      iq >= 110.0 -> "Promedio Alto (Brillante)"
      iq >= 90.0 -> "Promedio (Rango Normativo Típico)"
      iq >= 80.0 -> "Promedio Bajo"
      iq >= 70.0 -> "Limítrofe (Zona Fronteriza)"
      else -> "Muy Bajo (Extremadamente Bajo)"
    }

    val percentileDesc = when {
      percentile >= 98.0 -> "Rendimiento cognitivo superior al 98% de la población general tipificada (percentil $percentile)."
      percentile >= 90.0 -> "Rendimiento destacado, por encima del 90% del grupo normativo de referencia."
      percentile >= 50.0 -> "Rendimiento cognitivo alineado sólidamente con la media poblacional (percentil $percentile)."
      percentile >= 25.0 -> "Rendimiento dentro de los límites de variabilidad normal típica."
      else -> "Rendimiento por debajo del percentil 25, sugiriendo áreas de soporte específico."
    }

    // 2. Análisis Ipsativo CHC (comparación de cada dominio con respecto a la media del sujeto)
    val meanDomainScore = if (domainScores.isNotEmpty()) domainScores.values.average() else 100.0
    val strengths = mutableListOf<String>()
    val weaknesses = mutableListOf<String>()

    for ((domain, score) in domainScores) {
      val diff = score - meanDomainScore
      if (diff >= 7.5) {
        strengths.add("${domain.fullName} (${domain.code}): Índice $score. Demuestra alta eficiencia en ${domain.shortDescription.lowercase()}.")
      } else if (diff <= -7.5) {
        weaknesses.add("${domain.fullName} (${domain.code}): Índice $score. Muestra menor solvencia relativa en ${domain.shortDescription.lowercase()}.")
      }
    }

    // 3. Detección de Discrepancias Clínicas Clásicas
    val discrepancies = mutableListOf<String>()
    val gf = domainScores[ChcDomain.GF] ?: 100.0
    val gc = domainScores[ChcDomain.GC] ?: 100.0
    val gwm = domainScores[ChcDomain.GWM] ?: 100.0
    val gs = domainScores[ChcDomain.GS] ?: 100.0
    val gv = domainScores[ChcDomain.GV] ?: 100.0

    // Discrepancia Fluida vs Cristalizada (Gf vs Gc)
    if (kotlin.math.abs(gf - gc) >= 15.0) {
      if (gf > gc) {
        discrepancies.add("Disociación Gf > Gc (+${(gf - gc).toInt()} pts): Mayor solvencia de razonamiento abstracto e inductivo en comparación con el léxico y conocimiento consolidado.")
      } else {
        discrepancies.add("Disociación Gc > Gf (+${(gc - gf).toInt()} pts): Sólido bagaje conceptual y semántico formal que compensa una resolución abstracta más conservadora.")
      }
    }

    // Discrepancia Eficiencia Cognitiva (Gwm vs Gs)
    if (kotlin.math.abs(gwm - gs) >= 15.0) {
      if (gwm > gs) {
        discrepancies.add("Discrepancia de Eficiencia Gwm > Gs: Gran capacidad de retención y manipulación mental operativa frente a un ritmo de discriminación psicomotriz más pausado.")
      } else {
        discrepancies.add("Discrepancia de Eficiencia Gs > Gwm: Elevada fluidez y velocidad perceptiva, pero con menor capacidad de buffer en retención de trabajo compleja.")
      }
    }

    // 4. Recomendaciones neurocognitivas
    val recommendations = mutableListOf<String>()
    if (gwm < 95.0) {
      recommendations.add("Entrenamiento en memoria de trabajo activa (tareas n-back duales y fragmentación mnemónica de secuencias numéricas).")
    }
    if (gs < 95.0) {
      recommendations.add("Ejercicios de rastreo y cancelación rápida de símbolos para potenciar la fluidez y velocidad perceptomotriz.")
    }
    if (gf >= 115.0) {
      recommendations.add("Aprovechar la elevada aptitud de razonamiento fluido para la formulación de modelos deductivos complejos y programación analítica.")
    }
    if (telemetry.rapidGuessCount >= 2) {
      recommendations.add("Regulación de la reflexividad cognitiva: Implementar pausas deliberadas de 3 a 5 segundos antes de seleccionar alternativas complejas.")
    }
    if (recommendations.isEmpty()) {
      recommendations.add("Mantenimiento cognitivo armónico mediante retos multifactoriales equilibrados en dominios espaciales y de razonamiento inductivo.")
    }

    // 5. Narrativa diagnóstica estructurada según directrices psicométricas
    val narrativeBuilder = StringBuilder()
    narrativeBuilder.append("El evaluado presenta un Cociente Intelectual General (FSIQ) estimado de $iq ")
    narrativeBuilder.append("con un Intervalo de Confianza del 95% entre $ciLow y $ciHigh (Error Estándar SE = $se). ")
    narrativeBuilder.append("Este valor sitúa su rendimiento global en la categoría '$classification'.\n\n")

    narrativeBuilder.append("En cuanto a la validez del protocolo CAT-IQ, el análisis de telemetría móvil arrojó ")
    narrativeBuilder.append("un índice de atención sostenida de ${(telemetry.attentionIndex * 100).toInt()}% ")
    narrativeBuilder.append("con un tiempo medio de respuesta táctil de ${(telemetry.avgResponseTimeMs / 1000.0)} segundos por reactivo. ")
    narrativeBuilder.append("Estado de validez: ${telemetry.validityStatus}.\n\n")

    if (discrepancies.isNotEmpty()) {
      narrativeBuilder.append("Aspectos destacados del perfil CHC:\n")
      discrepancies.forEach { d -> narrativeBuilder.append("• $d\n") }
    }

    return ClinicalRagReport(
      qualitativeClassification = classification,
      percentileDescription = percentileDesc,
      cognitiveStrengths = strengths,
      cognitiveWeaknesses = weaknesses,
      clinicalDiscrepancies = discrepancies,
      neurocognitiveRecommendations = recommendations,
      detailedNarrative = narrativeBuilder.toString()
    )
  }
}
