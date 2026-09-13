package com.example

import com.example.data.model.PsychometricItem
import com.example.engine.IrtEngine
import com.example.engine.MobileAttentionTelemetry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IrtEngineTest {

  @Test
  fun testProbability3PL_atDifficulty() {
    // When theta = b:
    // P = c + (1 - c) / (1 + exp(0)) = c + (1 - c)/2
    // For c = 0.25: P = 0.25 + 0.75/2 = 0.625
    val p = IrtEngine.probability3PL(theta = 0.0, a = 1.5, b = 0.0, c = 0.25)
    assertEquals(0.625, p, 0.001)
  }

  @Test
  fun testProbability3PL_extremeTheta() {
    val pHigh = IrtEngine.probability3PL(theta = 4.0, a = 1.5, b = 0.0, c = 0.25)
    assertTrue("High theta should give probability close to 1.0", pHigh > 0.99)

    val pLow = IrtEngine.probability3PL(theta = -4.0, a = 1.5, b = 0.0, c = 0.25)
    assertEquals(0.25, pLow, 0.01) // asymptote at guessing parameter c
  }

  @Test
  fun testFisherInformation_positive() {
    val info = IrtEngine.fisherInformation(theta = 0.0, a = 1.5, b = 0.0, c = 0.25)
    assertTrue("Fisher information must be positive", info > 0.0)
  }

  @Test
  fun testThetaToIqTransformation() {
    // Theta 0 -> IQ 100
    assertEquals(100.0, IrtEngine.thetaToIq(0.0), 0.01)
    // Theta +1 -> IQ 115
    assertEquals(115.0, IrtEngine.thetaToIq(1.0), 0.01)
    // Theta -2 -> IQ 70
    assertEquals(70.0, IrtEngine.thetaToIq(-2.0), 0.01)
  }

  @Test
  fun testConfidenceInterval() {
    val (low, high) = IrtEngine.calculateConfidenceInterval(theta = 0.0, se = 0.3)
    // IQ = 100, margin = 1.96 * 15 * 0.3 = 8.82
    assertEquals(91.18, low, 0.1)
    assertEquals(108.82, high, 0.1)
  }

  @Test
  fun testEapEstimation_allCorrect() {
    val items = listOf(
      PsychometricItem("1", "Gf", 0.0, 1.5, 0.25, "TYPE", "P", "C", "A", "B", "C", "D", 0),
      PsychometricItem("2", "Gf", 0.5, 1.5, 0.25, "TYPE", "P", "C", "A", "B", "C", "D", 0),
      PsychometricItem("3", "Gf", 1.0, 1.5, 0.25, "TYPE", "P", "C", "A", "B", "C", "D", 0)
    )
    val responses = listOf(true, true, true)
    val (theta, se) = IrtEngine.estimateThetaEap(items, responses)

    assertTrue("Consistent correct responses should yield positive theta", theta > 0.5)
    assertTrue("SE should decrease with more items", se < 1.0)
  }
}
