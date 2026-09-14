package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiConsultantConfig
import com.example.data.ai.AiConsultantService
import com.example.data.ai.AiProvider
import com.example.data.ai.TestLengthMode
import com.example.data.model.AssessmentSession
import com.example.data.model.ChcDomain
import com.example.data.model.ItemResponse
import com.example.data.model.PsychometricItem
import com.example.data.model.TestBattery
import com.example.data.model.UserProfile
import com.example.data.repository.CatiqRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.engine.ClinicalRagReport
import com.example.engine.IrtEngine
import com.example.engine.ItemRepetitionValidator
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
import kotlin.math.abs

enum class AppScreen {
  DASHBOARD,
  ACTIVE_TEST,
  REPORT,
  ABOUT,
  USER_REGISTRATION
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
  val selectedDisplayIndex: Int? = null,
  val optionDisplayOrder: List<Int> = listOf(0, 1, 2, 3),
  val remainingTimeSeconds: Int = 30,
  val isTestCompleted: Boolean = false,
  val testBattery: TestBattery = TestBattery.FULL_CHC,
  val recentExcludedItemIds: Set<String> = emptySet(),
  val zeroRepetitionGuaranteed: Boolean = true
)

data class ReportState(
  val session: AssessmentSession? = null,
  val responses: List<ItemResponse> = emptyList(),
  val clinicalReport: ClinicalRagReport? = null,
  val telemetry: TelemetryAnalysis? = null,
  val repetitionAudit: ItemRepetitionValidator.SessionRepetitionAudit? = null
)

class CatiqViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = CatiqRepository.create(application)
  private val userPrefs = UserPreferencesRepository(application)

  private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _sessions = MutableStateFlow<List<AssessmentSession>>(emptyList())
  val sessions: StateFlow<List<AssessmentSession>> = _sessions.asStateFlow()

  private val _userProfile = MutableStateFlow(userPrefs.getUserProfile())
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  private val _selectedBattery = MutableStateFlow(TestBattery.FULL_CHC)
  val selectedBattery: StateFlow<TestBattery> = _selectedBattery.asStateFlow()

  private val _activeTestState = MutableStateFlow(ActiveTestState())
  val activeTestState: StateFlow<ActiveTestState> = _activeTestState.asStateFlow()

  private val _reportState = MutableStateFlow(ReportState())
  val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

  private val aiConsultantService = AiConsultantService()

  private val _aiConfig = MutableStateFlow(userPrefs.getAiConfig())
  val aiConfig: StateFlow<AiConsultantConfig> = _aiConfig.asStateFlow()

  private val _isAiGenerating = MutableStateFlow(false)
  val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

  private val _aiError = MutableStateFlow<String?>(null)
  val aiError: StateFlow<String?> = _aiError.asStateFlow()

  private val _aiInterpretation = MutableStateFlow("")
  val aiInterpretation: StateFlow<String> = _aiInterpretation.asStateFlow()

  private val _followUpHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
  val followUpHistory: StateFlow<List<Pair<String, String>>> = _followUpHistory.asStateFlow()

  private val _isFollowUpLoading = MutableStateFlow(false)
  val isFollowUpLoading: StateFlow<Boolean> = _isFollowUpLoading.asStateFlow()

  private val _showAiSettingsDialog = MutableStateFlow(false)
  val showAiSettingsDialog: StateFlow<Boolean> = _showAiSettingsDialog.asStateFlow()

  private var allAvailableItems: List<PsychometricItem> = emptyList()
  private var timerJob: Job? = null

  init {
    viewModelScope.launch {
      repository.ensureItemBankSeeded()
      val itemsFromDb = repository.getAllItems()
      allAvailableItems = ItemRepetitionValidator.deduplicatePool(itemsFromDb)
      // Auditoría preventiva de banco
      ItemRepetitionValidator.validateBankIntegrity(allAvailableItems)
      repository.allSessionsFlow.collect { sessionList ->
        _sessions.value = sessionList
      }
    }
  }

  fun navigateTo(screen: AppScreen) {
    _currentScreen.value = screen
  }

  fun saveUserProfile(profile: UserProfile) {
    userPrefs.saveUserProfile(profile)
    _userProfile.value = profile
    _currentScreen.value = AppScreen.DASHBOARD
  }

  fun selectBattery(battery: TestBattery) {
    _selectedBattery.value = battery
  }

  fun viewLatestSession() {
    val latest = _sessions.value.maxByOrNull { it.completedAt }
    if (latest != null) {
      viewSessionReport(latest)
    }
  }

  fun startNewAssessment(battery: TestBattery = _selectedBattery.value) {
    viewModelScope.launch {
      if (allAvailableItems.isEmpty()) {
        repository.ensureItemBankSeeded()
        allAvailableItems = ItemRepetitionValidator.deduplicatePool(repository.getAllItems())
      }

      // Consultar historial de reactivos recientes para control inter-sesión (evitar repetición entre pruebas)
      val recentIds = try {
        repository.getRecentlyAdministeredItemIds(limit = 60).toSet()
      } catch (e: Exception) {
        emptySet()
      }

      val newSessionId = UUID.randomUUID().toString()
      val initialTheta = 0.0
      val initialSe = 1.0

      // Filtrar banco de reactivos para la batería seleccionada
      val batteryPool = allAvailableItems.filter { it.domainCode in battery.eligibleDomains }
      val pool = if (batteryPool.isNotEmpty()) batteryPool else allAvailableItems

      // Aplicar Validador Anti-Repetición para el ítem inicial:
      // Excluye reactivos respondidos en sesiones recientes mientras el banco lo permita
      val eligibleCandidates = ItemRepetitionValidator.filterEligibleCandidates(
        availablePool = pool,
        inSessionAdministered = emptyList(),
        crossSessionExcludedIds = recentIds,
        requiredCount = 2
      )

      val firstCandidates = eligibleCandidates.filter { abs(it.difficultyB) <= 0.7 }
      val firstItem = if (firstCandidates.isNotEmpty()) {
        firstCandidates.random()
      } else if (eligibleCandidates.isNotEmpty()) {
        eligibleCandidates.random()
      } else {
        pool.random()
      }

      // Aleatorización estocástica del orden de opciones (A, B, C, D)
      val initialOptionOrder = listOf(0, 1, 2, 3).shuffled()

      _activeTestState.value = ActiveTestState(
        sessionId = newSessionId,
        currentItem = firstItem,
        itemNumber = 1,
        currentTheta = initialTheta,
        currentSe = initialSe,
        administeredItems = listOf(firstItem),
        responses = emptyList(),
        itemStartTimeMs = System.currentTimeMillis(),
        selectedDisplayIndex = null,
        optionDisplayOrder = initialOptionOrder,
        remainingTimeSeconds = firstItem.expectedTimeSeconds,
        isTestCompleted = false,
        testBattery = battery,
        recentExcludedItemIds = recentIds,
        zeroRepetitionGuaranteed = true
      )

      startItemTimer(firstItem.expectedTimeSeconds)
      _currentScreen.value = AppScreen.ACTIVE_TEST
    }
  }

  private fun startItemTimer(seconds: Int) {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      var remaining = seconds
      while (remaining > 0) {
        delay(1000)
        remaining--
        _activeTestState.value = _activeTestState.value.copy(remainingTimeSeconds = remaining)
      }
      // Al expirar el tiempo límite se registra omisión/tiempo agotado
      submitCurrentAnswer(isTimeExpired = true)
    }
  }

  fun selectOption(displayIndex: Int) {
    _activeTestState.value = _activeTestState.value.copy(selectedDisplayIndex = displayIndex)
  }

  fun submitCurrentAnswer(isTimeExpired: Boolean = false) {
    timerJob?.cancel()
    val state = _activeTestState.value
    val currentItem = state.currentItem ?: return

    val responseTimeMs = (System.currentTimeMillis() - state.itemStartTimeMs).coerceAtLeast(500)

    val actualSelectedOption = if (isTimeExpired || state.selectedDisplayIndex == null) {
      -1
    } else {
      val displayIdx = state.selectedDisplayIndex
      if (displayIdx in 0..3 && displayIdx < state.optionDisplayOrder.size) {
        state.optionDisplayOrder[displayIdx]
      } else {
        -1
      }
    }

    val isCorrect = (actualSelectedOption == currentItem.correctOptionIndex)
    val updatedAdministered = state.administeredItems

    viewModelScope.launch {
      // Estimación Bayesiana EAP (Expected A Posteriori)
      val allCorrectList = state.responses.map { it.isCorrect } + isCorrect
      val (newTheta, newSe) = IrtEngine.estimateThetaEap(
        administeredItems = updatedAdministered,
        responses = allCorrectList
      )

      val response = ItemResponse(
        sessionId = state.sessionId,
        itemId = currentItem.id,
        itemDomain = currentItem.domainCode,
        itemDifficultyB = currentItem.difficultyB,
        itemDiscriminationA = currentItem.discriminationA,
        selectedOptionIndex = actualSelectedOption,
        isCorrect = isCorrect,
        responseTimeMs = responseTimeMs,
        posteriorTheta = newTheta,
        posteriorSe = newSe,
        itemIndexOrder = state.itemNumber
      )

      val updatedResponses = state.responses + response

      // Criterio de parada adaptativo según batería y modo de longitud configurado
      val battery = state.testBattery
      val lengthMode = _aiConfig.value.testLengthMode
      val minItems = if (battery == TestBattery.FULL_CHC) lengthMode.minItems else battery.minItems
      val maxItems = if (battery == TestBattery.FULL_CHC) lengthMode.maxItems else battery.maxItems
      val targetSe = if (battery == TestBattery.FULL_CHC) lengthMode.targetSe else 0.30

      val shouldStop = IrtEngine.checkStoppingCriterion(
        currentSe = newSe,
        itemsAdministered = updatedAdministered.size,
        minItems = minItems,
        maxItems = maxItems,
        targetSe = targetSe
      )

      if (shouldStop) {
        finalizeAssessment(state.sessionId, newTheta, newSe, updatedResponses, updatedAdministered, battery)
        return@launch
      }

      // Siguiente ítem con validador anti-repetición intra-sesión e inter-sesión
      val domainCounts = updatedAdministered.groupingBy { it.domainCode }.eachCount()
      val nextItem = IrtEngine.selectNextItem(
        currentTheta = newTheta,
        availableItems = allAvailableItems,
        administeredItems = updatedAdministered,
        domainCounts = domainCounts,
        allowedDomains = battery.eligibleDomains,
        crossSessionExcludedIds = state.recentExcludedItemIds
      )

      if (nextItem == null) {
        finalizeAssessment(state.sessionId, newTheta, newSe, updatedResponses, updatedAdministered, battery)
      } else {
        // Validador estricto de garantía de no duplicación
        val validation = ItemRepetitionValidator.validateCandidate(nextItem, updatedAdministered)
        val safeItem = if (validation.isValid) {
          nextItem
        } else {
          // Si por alguna razón no pasó, seleccionar el primer candidato disponible no administrado
          ItemRepetitionValidator.filterEligibleCandidates(
            availablePool = allAvailableItems.filter { it.domainCode in battery.eligibleDomains },
            inSessionAdministered = updatedAdministered,
            crossSessionExcludedIds = emptySet()
          ).firstOrNull() ?: nextItem
        }

        val nextAdministeredList = updatedAdministered + safeItem
        val nextOptionOrder = listOf(0, 1, 2, 3).shuffled()

        _activeTestState.value = state.copy(
          currentItem = safeItem,
          itemNumber = state.itemNumber + 1,
          currentTheta = newTheta,
          currentSe = newSe,
          administeredItems = nextAdministeredList,
          responses = updatedResponses,
          itemStartTimeMs = System.currentTimeMillis(),
          selectedDisplayIndex = null,
          optionDisplayOrder = nextOptionOrder,
          remainingTimeSeconds = safeItem.expectedTimeSeconds
        )
        startItemTimer(safeItem.expectedTimeSeconds)
      }
    }
  }

  private fun finalizeAssessment(
    sessionId: String,
    finalTheta: Double,
    finalSe: Double,
    responses: List<ItemResponse>,
    administered: List<PsychometricItem>,
    battery: TestBattery
  ) {
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val iq = IrtEngine.thetaToIq(finalTheta)
      val (ciLow, ciHigh) = IrtEngine.calculateConfidenceInterval(finalTheta, finalSe)
      val percentile = IrtEngine.calculatePercentile(finalTheta)

      // Telemetría de atención móvil
      val telemetry = MobileAttentionTelemetry.evaluateSession(responses)

      // Cálculo de subpuntuaciones CHC normalizadas
      val domainScores = mutableMapOf<ChcDomain, Double>()
      for (domain in ChcDomain.entries) {
        val domainResponses = responses.filter { it.itemDomain.equals(domain.code, ignoreCase = true) }
        val score = if (domainResponses.isNotEmpty()) {
          val correctCount = domainResponses.count { it.isCorrect }
          val ratio = correctCount.toDouble() / domainResponses.size
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
        diagnosticSummary = "${battery.displayName}: ${clinicalReport.detailedNarrative}"
      )

      repository.saveCompletedSession(session, responses)

      val audit = ItemRepetitionValidator.auditSession(responses)

      _reportState.value = ReportState(
        session = session,
        responses = responses,
        clinicalReport = clinicalReport,
        telemetry = telemetry,
        repetitionAudit = audit
      )

      _aiInterpretation.value = session.aiInterpretation
      _followUpHistory.value = emptyList()
      _aiError.value = null

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

      val audit = ItemRepetitionValidator.auditSession(responses)

      _reportState.value = ReportState(
        session = session,
        responses = responses,
        clinicalReport = clinicalReport,
        telemetry = telemetry,
        repetitionAudit = audit
      )

      _aiInterpretation.value = session.aiInterpretation
      _followUpHistory.value = emptyList()
      _aiError.value = null

      _currentScreen.value = AppScreen.REPORT
    }
  }

  fun openAiSettings() {
    _showAiSettingsDialog.value = true
  }

  fun closeAiSettings() {
    _showAiSettingsDialog.value = false
  }

  fun saveAiConfig(config: AiConsultantConfig) {
    userPrefs.saveAiConfig(config)
    _aiConfig.value = config
  }

  fun testAiConnection(
    provider: AiProvider,
    config: AiConsultantConfig,
    callback: (Boolean, String) -> Unit
  ) {
    viewModelScope.launch {
      val result = aiConsultantService.testConnection(provider, config)
      result.onSuccess { msg ->
        callback(true, msg)
      }.onFailure { err ->
        callback(false, err.message ?: "Error desconocido al conectar con ${provider.displayName}")
      }
    }
  }

  fun generateAiReport() {
    val session = _reportState.value.session ?: return
    val profile = _userProfile.value
    val config = _aiConfig.value

    _isAiGenerating.value = true
    _aiError.value = null

    viewModelScope.launch {
      val result = aiConsultantService.generatePsychometricInterpretation(
        session = session,
        profile = profile,
        config = config
      )

      _isAiGenerating.value = false
      result.onSuccess { text ->
        _aiInterpretation.value = text
        _aiError.value = null
        repository.updateAiInterpretation(session.sessionId, text)
      }.onFailure { error ->
        _aiError.value = error.message ?: "Ocurrió un error inesperado al consultar el modelo IA."
      }
    }
  }

  fun askAiFollowUp(question: String) {
    val session = _reportState.value.session ?: return
    val profile = _userProfile.value
    val config = _aiConfig.value
    val currentReport = _aiInterpretation.value

    if (question.isBlank()) return

    _isFollowUpLoading.value = true
    viewModelScope.launch {
      val result = aiConsultantService.askFollowUpQuestion(
        question = question,
        session = session,
        profile = profile,
        previousReport = currentReport,
        config = config
      )

      _isFollowUpLoading.value = false
      result.onSuccess { answer ->
        _followUpHistory.value = _followUpHistory.value + Pair(question, answer)
      }.onFailure { err ->
        _followUpHistory.value = _followUpHistory.value + Pair(question, "Error al procesar consulta: ${err.message}")
      }
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
