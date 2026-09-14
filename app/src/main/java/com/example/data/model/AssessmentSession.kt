package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assessment_sessions")
data class AssessmentSession(
  @PrimaryKey val sessionId: String,
  val startedAt: Long,
  val completedAt: Long,
  val finalTheta: Double,
  val standardError: Double,
  val fullScaleIq: Double,
  val confidenceIntervalLow: Double,
  val confidenceIntervalHigh: Double,
  val percentileRank: Double,
  val totalItems: Int,
  val avgResponseTimeMs: Long,
  val attentionIndex: Double, // 0.0 to 1.0 (based on latency stability & cadence)
  val validityStatus: String, // "VÁLIDA", "ADVERTENCIA_ADIVINACIÓN", "FATIGA_DETECTADA"
  val gfScore: Double,
  val gvScore: Double,
  val gwmScore: Double,
  val gsScore: Double,
  val gcScore: Double,
  val diagnosticSummary: String,
  val aiInterpretation: String = ""
)
