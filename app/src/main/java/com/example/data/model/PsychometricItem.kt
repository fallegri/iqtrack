package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "psychometric_items")
data class PsychometricItem(
  @PrimaryKey val id: String,
  val domainCode: String, // "Gf", "Gv", "Gwm", "Gs", "Gc"
  val difficultyB: Double, // IRT b parameter: -2.5 to 2.5
  val discriminationA: Double, // IRT a parameter: 0.8 to 2.2
  val guessingC: Double, // IRT c parameter: 0.0 to 0.25
  val stimulusType: String, // "MATRIX_REASONING", "SPATIAL_ROTATION", "DIGIT_SPAN", "SYMBOL_SPEED", "VERBAL_ANALOGY"
  val prompt: String,
  val stimulusCode: String, // Code describing pattern/grid/digits for custom drawing
  val optionA: String,
  val optionB: String,
  val optionC: String,
  val optionD: String,
  val correctOptionIndex: Int, // 0 to 3
  val expectedTimeSeconds: Int = 30
) {
  val domain: ChcDomain
    get() = ChcDomain.fromCode(domainCode)

  fun getOption(index: Int): String {
    return when (index) {
      0 -> optionA
      1 -> optionB
      2 -> optionC
      3 -> optionD
      else -> ""
    }
  }
}
