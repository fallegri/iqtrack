package com.example.engine

import com.example.data.model.ItemResponse
import com.example.data.model.PsychometricItem
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Motor psicométrico IRT (Item Response Theory) nativo para dispositivos móviles.
 * Implementa el modelo logístico 3PL / 2PL, función de información de Fisher,
 * estimación bayesiana EAP (Expected A Posteriori) con cuadratura numérica y selección MFI.
 */
object IrtEngine {
  const val D_SCALING = 1.702 // Constante de escalamiento métrico normal-ogiva
  const val TARGET_SE_STOPPING = 0.30 // Criterio de parada: fiabilidad r_xx >= 0.91
  const val MIN_ITEMS = 5
  const val MAX_ITEMS = 15

  // Puntos de cuadratura Gaussiana para estimación EAP [-4.0, +4.0]
  private val QUAD_POINTS: DoubleArray = DoubleArray(41) { i -> -4.0 + i * 0.2 }
  private val PRIOR_WEIGHTS: DoubleArray = DoubleArray(QUAD_POINTS.size) { i ->
    val x = QUAD_POINTS[i]
    exp(-0.5 * x * x) / sqrt(2 * Math.PI)
  }

  /**
   * Probabilidad 3PL de respuesta correcta P_i(theta)
   */
  fun calculateProbability(theta: Double, a: Double, b: Double, c: Double): Double {
    val exponent = -D_SCALING * a * (theta - b)
    // Protección contra desbordamiento numérico
    val safeExp = when {
      exponent > 35.0 -> exp(35.0)
      exponent < -35.0 -> exp(-35.0)
      else -> exp(exponent)
    }
    return c + (1.0 - c) / (1.0 + safeExp)
  }

  /**
   * Función de Información de Fisher I_i(theta) para el ítem i
   */
  fun calculateFisherInformation(theta: Double, a: Double, b: Double, c: Double): Double {
    val p = calculateProbability(theta, a, b, c)
    val q = 1.0 - p
    if (p <= c || q <= 0.0) return 0.001

    val num = (D_SCALING * a).pow(2) * (p - c).pow(2) * q
    val den = (1.0 - c).pow(2) * p
    return if (den > 0.0) num / den else 0.0
  }

  /**
   * Información total de prueba acumulada en theta: I(theta) = sum(I_i(theta))
   */
  fun calculateTestInformation(theta: Double, administeredItems: List<PsychometricItem>): Double {
    return administeredItems.sumOf { item ->
      calculateFisherInformation(theta, item.discriminationA, item.difficultyB, item.guessingC)
    }
  }

  /**
   * Error estándar condicional de estimación: SE(theta) = 1 / sqrt(I(theta))
   */
  fun calculateStandardError(theta: Double, administeredItems: List<PsychometricItem>): Double {
    val info = calculateTestInformation(theta, administeredItems)
    return if (info > 0.0001) 1.0 / sqrt(info) else 1.0
  }

  /**
   * Estimación Bayesiana EAP (Expected A Posteriori) dado el historial de respuestas
   */
  fun estimateThetaEap(
    administeredItems: List<PsychometricItem>,
    responses: List<Boolean>
  ): Pair<Double, Double> {
    if (administeredItems.isEmpty() || administeredItems.size != responses.size) {
      return Pair(0.0, 1.0) // Prior N(0, 1)
    }

    val logLikelihoods = DoubleArray(QUAD_POINTS.size)

    for (k in QUAD_POINTS.indices) {
      val thetaK = QUAD_POINTS[k]
      var sumLogL = 0.0
      for (i in administeredItems.indices) {
        val item = administeredItems[i]
        val isCorrect = responses[i]
        val p = calculateProbability(thetaK, item.discriminationA, item.difficultyB, item.guessingC)
        val safeP = p.coerceIn(0.0001, 0.9999)
        sumLogL += if (isCorrect) ln(safeP) else ln(1.0 - safeP)
      }
      logLikelihoods[k] = sumLogL
    }

    // Estabilización numérica restando maxLogL
    val maxLog = logLikelihoods.maxOrNull() ?: 0.0
    var denominator = 0.0
    var numerator = 0.0

    val posteriorWeights = DoubleArray(QUAD_POINTS.size)
    for (k in QUAD_POINTS.indices) {
      val weight = exp(logLikelihoods[k] - maxLog) * PRIOR_WEIGHTS[k]
      posteriorWeights[k] = weight
      denominator += weight
      numerator += weight * QUAD_POINTS[k]
    }

    if (denominator <= 0.0) return Pair(0.0, 1.0)

    val thetaEap = numerator / denominator

    // Varianza a posteriori
    var varNumerator = 0.0
    for (k in QUAD_POINTS.indices) {
      val diff = QUAD_POINTS[k] - thetaEap
      varNumerator += posteriorWeights[k] * diff * diff
    }
    val posteriorVariance = varNumerator / denominator
    val seEap = sqrt(max(0.04, posteriorVariance)) // Mínimo SE razonable

    return Pair(thetaEap.coerceIn(-3.5, 3.5), seEap)
  }

  /**
   * Algoritmo de selección adaptativa:
   * Combina Maximum Fisher Information (MFI) con balanceo de dominios CHC (Kingsbury-Zarachara).
   */
  fun selectNextItem(
    currentTheta: Double,
    availableItems: List<PsychometricItem>,
    administeredItemIds: Set<String>,
    domainCounts: Map<String, Int>
  ): PsychometricItem? {
    val candidates = availableItems.filter { it.id !in administeredItemIds }
    if (candidates.isEmpty()) return null

    // Encontrar qué dominios CHC tienen menor exposición para forzar balanceo
    val minCount = domainCounts.values.minOrNull() ?: 0
    val prioritizedDomains = domainCounts.filter { it.value == minCount }.keys

    val domainPool = candidates.filter { it.domainCode in prioritizedDomains }
    val selectionPool = if (domainPool.isNotEmpty()) domainPool else candidates

    // Seleccionar ítem con Máxima Información de Fisher en la habilidad actual
    return selectionPool.maxByOrNull { item ->
      calculateFisherInformation(
        currentTheta,
        item.discriminationA,
        item.difficultyB,
        item.guessingC
      )
    }
  }

  /**
   * Conversión a escala estándar CI (M=100, SD=15)
   */
  fun thetaToIq(theta: Double): Double {
    val rawIq = 100.0 + 15.0 * theta
    return rawIq.coerceIn(40.0, 160.0)
  }

  /**
   * Intervalo de Confianza al 95% (Z = 1.96)
   */
  fun calculateConfidenceInterval(theta: Double, se: Double): Pair<Double, Double> {
    val iq = thetaToIq(theta)
    val margin = 1.96 * 15.0 * se
    val low = max(40.0, iq - margin)
    val high = min(160.0, iq + margin)
    return Pair(low, high)
  }

  /**
   * Percentil Acumulado Phi(theta)
   */
  fun calculatePercentile(theta: Double): Double {
    // Aproximación de Abramowitz & Stegun para la función error Gaussiana
    val z = theta
    val t = 1.0 / (1.0 + 0.2316419 * kotlin.math.abs(z))
    val d = 0.3989422804014337 * exp(-z * z / 2.0)
    val prob = d * t * (0.319381530 + t * (-0.356563782 + t * (1.781477937 + t * (-1.821255978 + t * 1.330274429))))
    val percentile = if (z >= 0.0) (1.0 - prob) * 100.0 else prob * 100.0
    return percentile.coerceIn(0.1, 99.9)
  }
}
