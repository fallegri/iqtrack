package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AiSettingsDialog
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AssessmentScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.UserRegistrationScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CatiqViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CatiqApp()
      }
    }
  }
}

@Composable
fun CatiqApp(viewModel: CatiqViewModel = viewModel()) {
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val sessions by viewModel.sessions.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val selectedBattery by viewModel.selectedBattery.collectAsStateWithLifecycle()
  val activeTestState by viewModel.activeTestState.collectAsStateWithLifecycle()
  val reportState by viewModel.reportState.collectAsStateWithLifecycle()

  val aiConfig by viewModel.aiConfig.collectAsStateWithLifecycle()
  val aiInterpretation by viewModel.aiInterpretation.collectAsStateWithLifecycle()
  val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()
  val aiError by viewModel.aiError.collectAsStateWithLifecycle()
  val followUpList by viewModel.followUpHistory.collectAsStateWithLifecycle()
  val isFollowUpLoading by viewModel.isFollowUpLoading.collectAsStateWithLifecycle()
  val showAiSettingsDialog by viewModel.showAiSettingsDialog.collectAsStateWithLifecycle()

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    AnimatedContent(
      targetState = currentScreen,
      transitionSpec = { fadeIn() togetherWith fadeOut() },
      label = "screenTransition",
      modifier = Modifier.padding(innerPadding)
    ) { screen ->
      when (screen) {
        AppScreen.DASHBOARD -> HomeScreen(
          sessions = sessions,
          userProfile = userProfile,
          selectedBattery = selectedBattery,
          aiConfig = aiConfig,
          onSelectBattery = { battery -> viewModel.selectBattery(battery) },
          onStartAssessment = { viewModel.startNewAssessment() },
          onViewLatestSession = { viewModel.viewLatestSession() },
          onViewAbout = { viewModel.navigateTo(AppScreen.ABOUT) },
          onEditProfile = { viewModel.navigateTo(AppScreen.USER_REGISTRATION) },
          onOpenAiSettings = { viewModel.openAiSettings() },
          onSelectSession = { session -> viewModel.viewSessionReport(session) }
        )
        AppScreen.ACTIVE_TEST -> AssessmentScreen(
          state = activeTestState,
          onSelectOption = { option -> viewModel.selectOption(option) },
          onSubmitAnswer = { viewModel.submitCurrentAnswer() },
          onCancelTest = { viewModel.navigateTo(AppScreen.DASHBOARD) }
        )
        AppScreen.REPORT -> ReportScreen(
          state = reportState,
          aiConfig = aiConfig,
          aiInterpretation = aiInterpretation,
          isAiGenerating = isAiGenerating,
          aiError = aiError,
          followUpList = followUpList,
          isFollowUpLoading = isFollowUpLoading,
          onGenerateAiReport = { viewModel.generateAiReport() },
          onOpenAiSettings = { viewModel.openAiSettings() },
          onAskAiFollowUp = { question -> viewModel.askAiFollowUp(question) },
          onBackToDashboard = { viewModel.navigateTo(AppScreen.DASHBOARD) }
        )
        AppScreen.ABOUT -> AboutScreen(
          onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
        )
        AppScreen.USER_REGISTRATION -> UserRegistrationScreen(
          currentProfile = userProfile,
          onSaveProfile = { profile -> viewModel.saveUserProfile(profile) },
          onCancel = { viewModel.navigateTo(AppScreen.DASHBOARD) }
        )
      }
    }

    if (showAiSettingsDialog) {
      AiSettingsDialog(
        currentConfig = aiConfig,
        onDismiss = { viewModel.closeAiSettings() },
        onSaveConfig = { newConfig -> viewModel.saveAiConfig(newConfig) },
        onTestConnection = { provider, config, callback ->
          viewModel.testAiConnection(provider, config, callback)
        }
      )
    }
  }
}
