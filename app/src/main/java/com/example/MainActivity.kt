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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AssessmentScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SddArchitectureScreen
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
  val activeTestState by viewModel.activeTestState.collectAsStateWithLifecycle()
  val reportState by viewModel.reportState.collectAsStateWithLifecycle()

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
          onStartAssessment = { viewModel.startNewAssessment() },
          onViewArchitecture = { viewModel.navigateTo(AppScreen.SDD_ARCHITECTURE) },
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
          onBackToDashboard = { viewModel.navigateTo(AppScreen.DASHBOARD) }
        )
        AppScreen.SDD_ARCHITECTURE -> SddArchitectureScreen(
          onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
        )
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}

