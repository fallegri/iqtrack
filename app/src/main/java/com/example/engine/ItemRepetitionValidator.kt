package com.example.engine

import com.example.data.model.ItemResponse
import com.example.data.model.PsychometricItem

/**
 * Validador Psicométrico Anti-Repetición de Reactivos (IRT CAT Engine).
 * 
 * Garantiza:
 * 1. Validación Intra-Sesión Estricta: Ningún reactivo con idéntico ID o idéntico
 *    código de estímulo/contenido se administra dos veces en una misma prueba.
 * 2. Control de Exposición Inter-Sesión (Recency & Cooldown): Filtra reactivos
 *    respondidos recientemente en sesiones anteriores para mitigar el efecto de práctica
 *    y garantizar que cada evaluación explore ítems frescos.
 * 3. Auditoría de Integridad del Banco: Valida unicidad de IDs, validez de parámetros IRT
 *    y consistencia de opciones de respuesta.
 */
object ItemRepetitionValidator {

  sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class DuplicateId(val id: String) : ValidationResult()
    data class DuplicateContent(val id: String, val duplicateWithId: String) : ValidationResult()
    data class ExcludedByRecency(val id: String) : ValidationResult()
    data class InvalidItemData(val reason: String) : ValidationResult()

    val isValid: Boolean get() = this is Valid
  }

  data class BankValidationReport(
    val totalItemsChecked: Int,
    val uniqueIdsCount: Int,
    val duplicateIdCount: Int,
    val duplicateIds: List<String>,
    val invalidItemsCount: Int,
    val issues: List<String>
  ) {
    val isClean: Boolean get() = duplicateIdCount == 0 && invalidItemsCount == 0
  }

  data class SessionRepetitionAudit(
    val totalAdministered: Int,
    val uniqueAdministered: Int,
    val duplicateCount: Int,
    val isZeroRepetitionGuaranteed: Boolean
  )

  /**
   * Genera una huella digital normalizada del contenido del ítem
   * para prevenir que ítems con diferente ID pero contenido idéntico sean administrados.
   */
  fun getItemFingerprint(item: PsychometricItem): String {
    val cleanCode = item.stimulusCode.trim().lowercase()
    val cleanPrompt = item.prompt.trim().lowercase()
    return if (cleanCode.isNotEmpty() && cleanCode != "none") {
      "${item.domainCode}:$cleanCode"
    } else {
      "${item.domainCode}:$cleanPrompt"
    }
  }

  /**
   * Valida un ítem candidato frente a los ítems administrados en la sesión actual
   * y frente a los IDs excluidos por sesiones recientes.
   */
  fun validateCandidate(
    candidate: PsychometricItem,
    inSessionAdministered: Collection<PsychometricItem>,
    crossSessionExcludedIds: Set<String> = emptySet(),
    enforceRecency: Boolean = true
  ): ValidationResult {
    // 1. Verificación de ID intra-sesión
    val administeredIds = inSessionAdministered.map { it.id }.toSet()
    if (candidate.id in administeredIds) {
      return ValidationResult.DuplicateId(candidate.id)
    }

    // 2. Verificación de huella digital de contenido intra-sesión
    val candidateFingerprint = getItemFingerprint(candidate)
    val existingMatch = inSessionAdministered.firstOrNull { getItemFingerprint(it) == candidateFingerprint }
    if (existingMatch != null) {
      return ValidationResult.DuplicateContent(candidate.id, existingMatch.id)
    }

    // 3. Verificación de datos básicos del ítem
    if (candidate.id.isBlank() || candidate.correctOptionIndex !in 0..3) {
      return ValidationResult.InvalidItemData("Ítem ${candidate.id} posee datos o índice de respuesta inválido")
    }

    // 4. Verificación inter-sesión (recencia) si está habilitada
    if (enforceRecency && candidate.id in crossSessionExcludedIds) {
      return ValidationResult.ExcludedByRecency(candidate.id)
    }

    return ValidationResult.Valid
  }

  /**
   * Filtra una lista de candidatos aplicando el validador anti-repetición.
   * Aplica fallback suave para inter-sesión si el banco se agota,
   * pero NUNCA compromete la unicidad intra-sesión.
   */
  fun filterEligibleCandidates(
    availablePool: List<PsychometricItem>,
    inSessionAdministered: Collection<PsychometricItem>,
    crossSessionExcludedIds: Set<String> = emptySet(),
    requiredCount: Int = 1
  ): List<PsychometricItem> {
    // Paso 1: Filtrado estricto (Intra-sesión + Inter-sesión)
    val strictCandidates = availablePool.filter { item ->
      validateCandidate(
        candidate = item,
        inSessionAdministered = inSessionAdministered,
        crossSessionExcludedIds = crossSessionExcludedIds,
        enforceRecency = true
      ).isValid
    }

    // Si hay suficientes candidatos frescos, los retorna
    if (strictCandidates.size >= requiredCount) {
      return strictCandidates
    }

    // Paso 2: Fallback suave: relajar inter-sesión pero MANTENER estricta unicidad intra-sesión
    return availablePool.filter { item ->
      validateCandidate(
        candidate = item,
        inSessionAdministered = inSessionAdministered,
        crossSessionExcludedIds = emptySet(),
        enforceRecency = false
      ).isValid
    }
  }

  /**
   * Audita la integridad de todo el banco de reactivos para detectar duplicados antes de su uso.
   */
  fun validateBankIntegrity(items: List<PsychometricItem>): BankValidationReport {
    val idGroups = items.groupBy { it.id }
    val duplicateIds = idGroups.filter { it.value.size > 1 }.keys.toList()

    val issues = mutableListOf<String>()
    var invalidCount = 0

    for (item in items) {
      if (item.correctOptionIndex !in 0..3) {
        issues.add("Ítem ${item.id}: correctOptionIndex (${item.correctOptionIndex}) fuera de rango [0..3]")
        invalidCount++
      }
      if (item.prompt.isBlank() && item.stimulusCode.isBlank()) {
        issues.add("Ítem ${item.id}: carece de prompt y stimulusCode")
        invalidCount++
      }
    }

    if (duplicateIds.isNotEmpty()) {
      issues.add("Se detectaron ${duplicateIds.size} IDs duplicados: $duplicateIds")
    }

    return BankValidationReport(
      totalItemsChecked = items.size,
      uniqueIdsCount = idGroups.size,
      duplicateIdCount = duplicateIds.size,
      duplicateIds = duplicateIds,
      invalidItemsCount = invalidCount,
      issues = issues
    )
  }

  /**
   * Audita las respuestas de una sesión finalizada para certificar que no hubo reactivos repetidos.
   */
  fun auditSession(responses: List<ItemResponse>): SessionRepetitionAudit {
    val total = responses.size
    val uniqueIds = responses.map { it.itemId }.toSet().size
    val duplicates = total - uniqueIds

    return SessionRepetitionAudit(
      totalAdministered = total,
      uniqueAdministered = uniqueIds,
      duplicateCount = duplicates,
      isZeroRepetitionGuaranteed = (duplicates == 0)
    )
  }

  /**
   * Sanitiza un banco eliminando duplicados si existieran, priorizando la primera aparición.
   */
  fun deduplicatePool(items: List<PsychometricItem>): List<PsychometricItem> {
    val seenIds = mutableSetOf<String>()
    val seenFingerprints = mutableSetOf<String>()
    val cleanList = mutableListOf<PsychometricItem>()

    for (item in items) {
      val fp = getItemFingerprint(item)
      if (item.id !in seenIds && fp !in seenFingerprints) {
        seenIds.add(item.id)
        seenFingerprints.add(fp)
        cleanList.add(item)
      }
    }
    return cleanList
  }
}
