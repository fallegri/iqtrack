package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PsychometricItem
import com.example.ui.components.PsychometricOptionVisualizer
import com.example.ui.components.StimulusVisualizer
import com.example.ui.theme.MetricGreen
import com.example.ui.theme.MetricRed
import com.example.ui.theme.MetricYellow
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.viewmodel.ActiveTestState

@Composable
fun AssessmentScreen(
  state: ActiveTestState,
  onSelectOption: (Int) -> Unit,
  onSubmitAnswer: () -> Unit,
  onCancelTest: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showExitDialog by remember { mutableStateOf(false) }
  val item = state.currentItem

  if (showExitDialog) {
    AlertDialog(
      onDismissRequest = { showExitDialog = false },
      title = { Text("¿Cancelar evaluación?") },
      text = { Text("Si sales ahora se perderá el progreso de la sesión psicométrica adaptativa.") },
      confirmButton = {
        Button(
          onClick = {
            showExitDialog = false
            onCancelTest()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MetricRed)
        ) {
          Text("Abandonar Test")
        }
      },
      dismissButton = {
        TextButton(onClick = { showExitDialog = false }) {
          Text("Continuar")
        }
      }
    )
  }

  if (item == null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Inicializando banco de reactivos...")
    }
    return
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp)
  ) {
    // 1. Barra superior: Salir, Dominio CHC y Temporizador
    Spacer(modifier = Modifier.height(8.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = { showExitDialog = true },
        modifier = Modifier
          .size(36.dp)
          .testTag("exit_assessment_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Salir",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }

      // Dominio CHC Pill
      Box(
        modifier = Modifier
          .background(item.domain.color.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
          .border(1.dp, item.domain.color.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
          .padding(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Text(
          text = "${item.domain.code} • ${item.domain.fullName}",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = item.domain.color,
          fontSize = 11.sp
        )
      }

      // Temporizador de reactivo
      val timerColor = when {
        state.remainingTimeSeconds <= 5 -> MetricRed
        state.remainingTimeSeconds <= 10 -> MetricYellow
        else -> MaterialTheme.colorScheme.primary
      }

      Row(
        modifier = Modifier
          .background(timerColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
          .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Timer,
          contentDescription = null,
          tint = timerColor,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${state.remainingTimeSeconds}s",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = timerColor
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // 2. Progreso métrico de convergencia IRT
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Reactivo ${state.itemNumber} de ${state.testBattery.maxItems} • ${state.testBattery.displayName}",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "SE: ${String.format("%.2f", state.currentSe)} (meta ≤ 0.30)",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    val precisionProgress = ((1.0 - (state.currentSe - 0.30).coerceAtLeast(0.0) / 0.70)).toFloat().coerceIn(0.1f, 1f)
    LinearProgressIndicator(
      progress = { precisionProgress },
      modifier = Modifier
        .fillMaxWidth()
        .height(4.dp),
      color = PrimaryBlueDark,
      trackColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Spacer(modifier = Modifier.height(8.dp))

    // 3. Contenedor principal con estímulo, prompt y alternativas en cuadrícula 2x2
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Estímulo Gráfico / Cognitivo (Compacto)
      StimulusVisualizer(item = item)

      // Instrucción / Pregunta breve
      Text(
        text = item.prompt,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp,
        modifier = Modifier.fillMaxWidth()
      )

      // 4 Alternativas de Respuesta en Cuadrícula 2x2
      // Diseñado para caber completamente en pantalla móvil sin requerir scroll
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Fila 1: Opciones A y B
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OptionCard(
            displaySlot = 0,
            item = item,
            state = state,
            onSelectOption = onSelectOption,
            modifier = Modifier.weight(1f)
          )
          OptionCard(
            displaySlot = 1,
            item = item,
            state = state,
            onSelectOption = onSelectOption,
            modifier = Modifier.weight(1f)
          )
        }

        // Fila 2: Opciones C y D
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OptionCard(
            displaySlot = 2,
            item = item,
            state = state,
            onSelectOption = onSelectOption,
            modifier = Modifier.weight(1f)
          )
          OptionCard(
            displaySlot = 3,
            item = item,
            state = state,
            onSelectOption = onSelectOption,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 4. Botón de Confirmación de Respuesta
    val hasSelectedOption = state.selectedDisplayIndex != null
    Button(
      onClick = onSubmitAnswer,
      enabled = hasSelectedOption,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("submit_answer_button"),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = PrimaryBlueDark,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = if (hasSelectedOption) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (hasSelectedOption) "Confirmar Respuesta" else "Selecciona una Opción",
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = if (hasSelectedOption) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.height(10.dp))
  }
}

@Composable
private fun OptionCard(
  displaySlot: Int,
  item: PsychometricItem,
  state: ActiveTestState,
  onSelectOption: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val actualIndex = state.optionDisplayOrder.getOrElse(displaySlot) { displaySlot }
  val optionText = item.getOption(actualIndex)
  val isSelected = (state.selectedDisplayIndex == displaySlot)
  val displayLetter = ('A' + displaySlot).toString()

  Card(
    modifier = modifier
      .height(78.dp)
      .clickable { onSelectOption(displaySlot) }
      .testTag("option_${displayLetter.lowercase()}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        item.domain.color.copy(alpha = 0.20f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
      }
    ),
    border = if (isSelected) {
      androidx.compose.foundation.BorderStroke(2.dp, item.domain.color)
    } else {
      androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(6.dp)
    ) {
      // Badge con la letra (A, B, C, D) en la esquina superior izquierda
      Box(
        modifier = Modifier
          .size(22.dp)
          .background(
            if (isSelected) item.domain.color else MaterialTheme.colorScheme.surface,
            CircleShape
          )
          .align(Alignment.TopStart),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = displayLetter,
          fontWeight = FontWeight.Black,
          fontSize = 11.sp,
          color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
        )
      }

      // Visualización Gráfica o Token de la Opción en el Centro
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(start = 22.dp),
        contentAlignment = Alignment.Center
      ) {
        PsychometricOptionVisualizer(
          item = item,
          optionText = optionText,
          isSelected = isSelected,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}
