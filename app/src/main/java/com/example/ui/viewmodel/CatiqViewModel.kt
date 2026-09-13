package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AssessmentSession
import com.example.data.model.ChcDomain
import com.example.data.model.ItemResponse
import com.example.data.model.PsychometricItem
import com.example.data.repository.CatiqRepository
import com.example.engine.ClinicalRagReport
import com.example.engine.IrtEngine
import com.example.engine.MobileAttentionTelemetry
import com.example.engine.PsychometricRagEngine
import com.example.engine.TelemetryAnalysis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
  DASHBOARD,
  ACTIVE_TEST,
  REPORT,
  SDD_ARCHITECTURE
}

data class ActiveTestState(
  val sessionId: String = "",
  val currentItem: PsychometricItem? = null,
  val itemNumber: Int = 0,
  val currentTheta: Double = 0.0,
  val currentSe: Double = 1.0,
  val administeredItems: List<PsychometricItem> = emptyList(),
  val responses: List<ItemResponse> = emptyList(),
  val itemStartTimeMs: Long = 0L,
  val selectedOption: Int? = null,
  val remainingTimeSeconds: Int = 30,
  val isTestCompleted: Boolean = false
)

data class ReportState(
  val session: AssessmentSession? = null,
  val responses: List<ItemResponse> = emptyList(),
  val clinicalReport: ClinicalRagReport? = null,
  val telemetry: TelemetryAnalysis? = null
)

class CatiqViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = CatiqRepository.create(application)

  private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _sessions = MutableStateFlow<List<AssessmentSession>>(emptyList())
  val sessions: StateFlow<List<AssessmentSession>> = _sessions.asStateFlow()

  private val _activeTestState = MutableStateFlow(ActiveTestState())
  val activeTestState: StateFlow<ActiveTestState> = _activeTestState.asStateFlow()

  private val _reportState = MutableStateFlow(ReportState())
  val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

  private var allAvailableItems: List<PsychometricItem> = emptyList()
  private var timerJob: Job? = null

  init {
    viewModelScope.launch {
      repository.ensureItemBankSeeded()
      allAvailableItems = repository.getAllItems()
      repository.allSessionsFlow.collect { sessionList ->
        _sessions.value = sessionList
      }
    }
  }

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
  }

  fun startNewAssessment() {
    viewModelScope.launch {
      if (allAvailableItems.isEmpty()) {
        repository.ensureItemBankSeeded()
        allAvailableItems = repository.getAllItems()
      }

      val newSessionId = UUID.randomUUID().toString()
      val initialTheta = 0.0
      val initialSe = 1.0

      // Seleccionar primer ítem de razonamiento inductivo inicial (Gf dificultad media b ≈ 0)
      val firstItem = allAvailableItems.find { it.id == "GF_02" } ?: allAvailableItems.first()

      _activeTestState.value = ActiveTestState(
        sessionId = newSessionId,
        currentItem = firstItem,
        itemNumber = 1,
        currentTheta = initialTheta,
        currentSe = initialSe,
        administeredItems = listOf(firstItem),
        responses = emptyList(),
        itemStartTimeMs = System.currentTimeMillis(),
        selectedOption = null,
        remainingTimeSeconds = firstItem.expectedTimeSeconds,
        isTestCompleted = false
      )

      startItemTimer(firstItem.expectedTimeSeconds)
      _currentScreen.value = AppScreen.ACTIVE_TEST
    }
  }

  private fun startItemTimer(seconds: Int) {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      for (remaining in seconds downTo 0) {
        _activeTestState.value = _activeTestState.value.copy(remainingTimeSeconds = remaining)
        if (remaining == 0) {
          // Si expira el tiempo sin responder, enviar opción no contestada (-1)
          if (_activeTestState.value.selectedOption == null) {
            submitAnswer(-1)
          }
          break
        }
        delay(1000L)
      }
    }
  }

  fun selectOption(optionIndex: Int) {
    _activeTestState.value = _activeTestState.value.copy(selectedOption = optionIndex)
  }

  fun submitCurrentAnswer() {
    val selected = _activeTestState.value.selectedOption ?: return
    submitAnswer(selected)
  }

  private fun submitAnswer(selectedOptionIndex: Int) {
    timerJob?.cancel()
    val state = _activeTestState.value
    val currentItem = state.currentItem ?: return

    val responseDurationMs = System.currentTimeMillis() - state.itemStartTimeMs
    val isCorrect = (selectedOptionIndex == currentItem.correctOptionIndex)

    // Crear lista de respuestas actualizadas
    val updatedAdministered = state.administeredItems
    val currentResponsesBoolean = state.responses.map { it.isCorrect } + isCorrect

    // Actualización de estimación Theta y SE mediante EAP con cuadratura numérica
    val (newTheta, newSe) = IrtEngine.estimateThetaEap(updatedAdministered, currentResponsesBoolean)

    val itemResponse = ItemResponse(
      sessionId = state.sessionId,
      itemId = currentItem.id,
      itemDomain = currentItem.domainCode,
      selectedOptionIndex = selectedOptionIndex,
      isCorrect = isCorrect,
      responseTimeMs = responseDurationMs,
      itemDifficultyB = currentItem.difficultyB,
      itemDiscriminationA = currentItem.discriminationA,
      posteriorTheta = newTheta,
      posteriorSe = newSe,
      itemIndexOrder = state.itemNumber
    )

    val updatedResponses = state.responses + itemResponse

    // Criterio de parada adaptativo:
    // 1. Error estándar SE <= 0.30 (después de al menos MIN_ITEMS = 5)
    // 2. O límite máximo de reactivos alcanzado (MAX_ITEMS = 15)
    val shouldStop = (state.itemNumber >= IrtEngine.MIN_ITEMS && newSe <= IrtEngine.TARGET_SE_STOPPING) ||
        (state.itemNumber >= IrtEngine.MAX_ITEMS)

    if (shouldStop) {
      finalizeAssessment(state.sessionId, newTheta, newSe, updatedResponses, updatedAdministered)
    } else {
      // Balanceo de dominios CHC y Maximum Fisher Information
      val domainCounts = mutableMapOf(
        "Gf" to 0, "Gv" to 0, "Gwm" to 0, "Gs" to 0, "Gc" to 0
      )
      updatedAdministered.forEach { item ->
        domainCounts[item.domainCode] = (domainCounts[item.domainCode] ?: 0) + 1
      }

      val administeredIds = updatedAdministered.map { it.id }.toSet()
      val nextItem = IrtEngine.selectNextItem(
        currentTheta = newTheta,
        availableItems = allAvailableItems,
        administeredItemIds = administeredIds,
        domainCounts = domainCounts
      )

      if (nextItem == null) {
        finalizeAssessment(state.sessionId, newTheta, newSe, updatedResponses, updatedAdministered)
      } else {
        val nextAdministeredList = updatedAdministered + nextItem
        _activeTestState.value = state.copy(
          currentItem = nextItem,
          itemNumber = state.itemNumber + 1,
          currentTheta = newTheta,
          currentSe = newSe,
          administeredItems = nextAdministeredList,
          responses = updatedResponses,
          itemStartTimeMs = System.currentTimeMillis(),
          selectedOption = null,
          remainingTimeSeconds = nextItem.expectedTimeSeconds
        )
        startItemTimer(nextItem.expectedTimeSeconds)
      }
    }
  }

  private fun finalizeAssessment(
    sessionId: String,
    finalTheta: Double,
    finalSe: Double,
    responses: List<ItemResponse>,
    administered: List<PsychometricItem>
  ) {
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val iq = IrtEngine.thetaToIq(finalTheta)
      val (ciLow, ciHigh) = IrtEngine.calculateConfidenceInterval(finalTheta, finalSe)
      val percentile = IrtEngine.calculatePercentile(finalTheta)

      // Telemetría móvil
      val telemetry = MobileAttentionTelemetry.evaluateSession(responses)

      // Cálculo de subpuntuaciones CHC normalizadas
      val domainScores = mutableMapOf<ChcDomain, Double>()
      for (domain in ChcDomain.entries) {
        val domainResponses = responses.filter { it.itemDomain.equals(domain.code, ignoreCase = true) }
        val score = if (domainResponses.isNotEmpty()) {
          val correctCount = domainResponses.count { it.isCorrect }
          val ratio = correctCount.toDouble() / domainResponses.size
          // Centrado en el theta general con ajuste ipsativo
          val domainTheta = finalTheta + (ratio - 0.5) * 1.2
          IrtEngine.thetaToIq(domainTheta)
        } else {
          iq
        }
        domainScores[domain] = score
      }

      // RAG Engine: Generación de informe clínico y análisis ipsativo
      val clinicalReport = PsychometricRagEngine.generateClinicalReport(
        iq = Math.round(iq * 10.0) / 10.0,
        ciLow = Math.round(ciLow * 10.0) / 10.0,
        ciHigh = Math.round(ciHigh * 10.0) / 10.0,
        percentile = Math.round(percentile * 10.0) / 10.0,
        se = Math.round(finalSe * 100.0) / 100.0,
        domainScores = domainScores,
        telemetry = telemetry
      )

      val session = AssessmentSession(
        sessionId = sessionId,
        startedAt = responses.firstOrNull()?.let { now - responses.sumOf { r -> r.responseTimeMs } } ?: now,
        completedAt = now,
        finalTheta = Math.round(finalTheta * 100.0) / 100.0,
        standardError = Math.round(finalSe * 100.0) / 100.0,
        fullScaleIq = Math.round(iq * 10.0) / 10.0,
        confidenceIntervalLow = Math.round(ciLow * 10.0) / 10.0,
        confidenceIntervalHigh = Math.round(ciHigh * 10.0) / 10.0,
        percentileRank = Math.round(percentile * 10.0) / 10.0,
        totalItems = responses.size,
        avgResponseTimeMs = telemetry.avgResponseTimeMs,
        attentionIndex = Math.round(telemetry.attentionIndex * 100.0) / 100.0,
        validityStatus = telemetry.validityStatus,
        gfScore = Math.round((domainScores[ChcDomain.GF] ?: iq) * 10.0) / 10.0,
        gvScore = Math.round((domainScores[ChcDomain.GV] ?: iq) * 10.0) / 10.0,
        gwmScore = Math.round((domainScores[ChcDomain.GWM] ?: iq) * 10.0) / 10.0,
        gsScore = Math.round((domainScores[ChcDomain.GS] ?: iq) * 10.0) / 10.0,
        gcScore = Math.round((domainScores[ChcDomain.GC] ?: iq) * 10.0) / 10.0,
        diagnosticSummary = clinicalReport.detailedNarrative
      )

      // Guardar en base de datos local Room
      repository.saveCompletedSession(session, responses)

      _reportState.value = ReportState(
        session = session,
        responses = responses,
        clinicalReport = clinicalReport,
        telemetry = telemetry
      )

      _activeTestState.value = _activeTestState.value.copy(isTestCompleted = true)
      _currentScreen.value = AppScreen.REPORT
    }
  }

  fun viewSessionReport(session: AssessmentSession) {
    viewModelScope.launch {
      val responses = repository.getResponsesForSession(session.sessionId)
      val telemetry = MobileAttentionTelemetry.evaluateSession(responses)
      val domainScores = mapOf(
        ChcDomain.GF to session.gfScore,
        ChcDomain.GV to session.gvScore,
        ChcDomain.GWM to session.gwmScore,
        ChcDomain.GS to session.gsScore,
        ChcDomain.GC to session.gcScore
      )

      val clinicalReport = PsychometricRagEngine.generateClinicalReport(
        iq = session.fullScaleIq,
        ciLow = session.confidenceIntervalLow,
        ciHigh = session.confidenceIntervalHigh,
        percentile = session.percentileRank,
        se = session.standardError,
        domainScores = domainScores,
        telemetry = telemetry
      )

      _reportState.value = ReportState(
        session = session,
        responses = responses,
        clinicalReport = clinicalReport,
        telemetry = telemetry
      )

      _currentScreen.value = AppScreen.REPORT
    }
  }

  fun deleteSession(sessionId: String) {
    viewModelScope.launch {
      repository.deleteSession(sessionId)
      if (_reportState.value.session?.sessionId == sessionId) {
        _currentScreen.value = AppScreen.DASHBOARD
      }
    }
  }
}
