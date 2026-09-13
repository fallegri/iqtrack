package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_responses")
data class ItemResponse(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String,
  val itemId: String,
  val itemDomain: String,
  val selectedOptionIndex: Int,
  val isCorrect: Boolean,
  val responseTimeMs: Long,
  val itemDifficultyB: Double,
  val itemDiscriminationA: Double,
  val posteriorTheta: Double,
  val posteriorSe: Double,
  val itemIndexOrder: Int
)
