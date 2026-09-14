package com.example

import com.example.data.bank.PsychometricItemBank
import com.example.data.model.ItemResponse
import com.example.data.model.PsychometricItem
import com.example.engine.ItemRepetitionValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemRepetitionValidatorTest {

  private val sampleItem1 = PsychometricItem(
    id = "GF_01",
    domainCode = "Gf",
    difficultyB = 0.0,
    discriminationA = 1.5,
    guessingC = 0.25,
    stimulusType = "MATRIX",
    prompt = "¿Qué figura completa la matriz?",
    stimulusCode = "CIRCLE_1",
    optionA = "A",
    optionB = "B",
    optionC = "C",
    optionD = "D",
    correctOptionIndex = 0
  )

  private val sampleItem2 = PsychometricItem(
    id = "GF_02",
    domainCode = "Gf",
    difficultyB = 0.5,
    discriminationA = 1.6,
    guessingC = 0.25,
    stimulusType = "MATRIX",
    prompt = "¿Qué figura completa la matriz?",
    stimulusCode = "CIRCLE_2",
    optionA = "A",
    optionB = "B",
    optionC = "C",
    optionD = "D",
    correctOptionIndex = 1
  )

  private val duplicateContentItem = PsychometricItem(
    id = "GF_CLONE",
    domainCode = "Gf",
    difficultyB = 0.0,
    discriminationA = 1.5,
    guessingC = 0.25,
    stimulusType = "MATRIX",
    prompt = "¿Qué figura completa la matriz?",
    stimulusCode = "circle_1", // Same fingerprint as sampleItem1
    optionA = "A",
    optionB = "B",
    optionC = "C",
    optionD = "D",
    correctOptionIndex = 0
  )

  @Test
  fun testInSessionDuplicateId_isRejected() {
    val inSession = listOf(sampleItem1)
    val result = ItemRepetitionValidator.validateCandidate(
      candidate = sampleItem1,
      inSessionAdministered = inSession
    )
    assertFalse("Candidate with same ID must be rejected", result.isValid)
    assertTrue("Result should be DuplicateId", result is ItemRepetitionValidator.ValidationResult.DuplicateId)
  }

  @Test
  fun testInSessionDuplicateContent_isRejected() {
    val inSession = listOf(sampleItem1)
    val result = ItemRepetitionValidator.validateCandidate(
      candidate = duplicateContentItem,
      inSessionAdministered = inSession
    )
    assertFalse("Candidate with identical stimulus fingerprint must be rejected", result.isValid)
    assertTrue("Result should be DuplicateContent", result is ItemRepetitionValidator.ValidationResult.DuplicateContent)
  }

  @Test
  fun testInSessionDifferentItem_isValid() {
    val inSession = listOf(sampleItem1)
    val result = ItemRepetitionValidator.validateCandidate(
      candidate = sampleItem2,
      inSessionAdministered = inSession
    )
    assertTrue("Candidate with distinct ID and distinct content must be valid", result.isValid)
  }

  @Test
  fun testCrossSessionRecency_filtersRecentItems() {
    val available = listOf(sampleItem1, sampleItem2)
    val crossSessionRecent = setOf(sampleItem1.id)

    val eligible = ItemRepetitionValidator.filterEligibleCandidates(
      availablePool = available,
      inSessionAdministered = emptyList(),
      crossSessionExcludedIds = crossSessionRecent,
      requiredCount = 1
    )

    assertEquals(1, eligible.size)
    assertEquals(sampleItem2.id, eligible.first().id)
  }

  @Test
  fun testBankIntegrity_105PrecalibratedItemsHaveZeroDuplicates() {
    val bank = PsychometricItemBank.allItems
    val report = ItemRepetitionValidator.validateBankIntegrity(bank)

    assertEquals("Total items should be 105", 105, report.totalItemsChecked)
    assertEquals("Duplicate count must be 0", 0, report.duplicateIdCount)
    assertEquals("Invalid items count must be 0", 0, report.invalidItemsCount)
    assertTrue("Bank integrity report must be clean with 0 issues: ${report.issues}", report.isClean)
  }

  @Test
  fun testSessionAudit_guaranteesZeroRepetitions() {
    val cleanResponses = listOf(
      ItemResponse(id = 1, sessionId = "s1", itemId = "GF_01", itemDomain = "Gf", selectedOptionIndex = 0, isCorrect = true, responseTimeMs = 1000, itemDifficultyB = 0.0, itemDiscriminationA = 1.0, posteriorTheta = 0.0, posteriorSe = 0.5, itemIndexOrder = 1),
      ItemResponse(id = 2, sessionId = "s1", itemId = "GV_01", itemDomain = "Gv", selectedOptionIndex = 1, isCorrect = true, responseTimeMs = 1200, itemDifficultyB = 0.2, itemDiscriminationA = 1.0, posteriorTheta = 0.2, posteriorSe = 0.45, itemIndexOrder = 2),
      ItemResponse(id = 3, sessionId = "s1", itemId = "GWM_01", itemDomain = "Gwm", selectedOptionIndex = 2, isCorrect = true, responseTimeMs = 1500, itemDifficultyB = 0.4, itemDiscriminationA = 1.0, posteriorTheta = 0.4, posteriorSe = 0.40, itemIndexOrder = 3)
    )

    val audit = ItemRepetitionValidator.auditSession(cleanResponses)
    assertTrue("Clean session must guarantee 0 repetitions", audit.isZeroRepetitionGuaranteed)
    assertEquals(0, audit.duplicateCount)

    val dirtyResponses = cleanResponses + cleanResponses.first().copy(id = 4, itemIndexOrder = 4)
    val dirtyAudit = ItemRepetitionValidator.auditSession(dirtyResponses)
    assertFalse("Dirty session must be detected as non-zero repetition", dirtyAudit.isZeroRepetitionGuaranteed)
    assertEquals(1, dirtyAudit.duplicateCount)
  }
}
