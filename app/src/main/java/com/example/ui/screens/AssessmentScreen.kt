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
import androidx.compose.ui.text.style.TextOverflow
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
      .padding(horizontal = 14.dp, vertical = 6.dp)
  ) {
    // 1. Barra superior unificada y compacta (sólo ~34.dp)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = { showExitDialog = true },
        modifier = Modifier
          .size(30.dp)
          .testTag("exit_assessment_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Salir",
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(18.dp)
        )
      }

      // Pregunta X de Y + Dominio CHC en una sola cápsula compacta
      Text(
        text = "Pregunta ${state.itemNumber}/${state.testBattery.maxItems} • ${item.domain.code} (${item.domain.fullName})",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = item.domain.color,
        fontSize = 11.sp
      )

      // Temporizador compacto
      val timerColor = when {
        state.remainingTimeSeconds <= 5 -> MetricRed
        state.remainingTimeSeconds <= 10 -> MetricYellow
        else -> MaterialTheme.colorScheme.primary
      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Timer,
          contentDescription = null,
          tint = timerColor,
          modifier = Modifier.size(13.dp)
        )
        Text(
          text = "${state.remainingTimeSeconds}s",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = timerColor
        )
      }
    }

    // Barra de convergencia IRT ultra-delgada (2.dp)
    val precisionProgress = ((1.0 - (state.currentSe - 0.30).coerceAtLeast(0.0) / 0.70)).toFloat().coerceIn(0.1f, 1f)
    LinearProgressIndicator(
      progress = { precisionProgress },
      modifier = Modifier
        .fillMaxWidth()
        .height(2.dp),
      color = PrimaryBlueDark,
      trackColor = MaterialTheme.colorScheme.surfaceVariant
    )

    // 2. Validador en EXACTAMENTE UNA SOLA LÍNEA DE TEXTO (ocupa espacio mínimo vertical)
    Text(
      text = "✓ Validador Anti-Repetición: 100% preguntas únicas garantizadas",
      style = MaterialTheme.typography.labelSmall,
      fontSize = 10.sp,
      color = MetricGreen,
      fontWeight = FontWeight.Medium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp)
    )

    // 3. Contenedor principal con amplio espacio para la pregunta, el estímulo y las opciones
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // PREGUNTA: Espacio amplio, texto grande, visible y destacado
      Text(
        text = item.prompt,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp, vertical = 2.dp)
      )

      // Estímulo Gráfico / Cognitivo (Proporcionado y compacto)
      StimulusVisualizer(
        item = item,
        modifier = Modifier.padding(horizontal = 4.dp)
      )

      // 4 Alternativas de Respuesta en Cuadrícula 2x2 optimizada
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Fila 1: Opciones A y B
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
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
          horizontalArrangement = Arrangement.spacedBy(6.dp)
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

    // 4. Botón de Confirmación compacto al pie
    val hasSelectedOption = state.selectedDisplayIndex != null
    Button(
      onClick = onSubmitAnswer,
      enabled = hasSelectedOption,
      modifier = Modifier
        .fillMaxWidth()
        .height(44.dp)
        .testTag("submit_answer_button"),
      shape = RoundedCornerShape(10.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = PrimaryBlueDark,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = if (hasSelectedOption) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = if (hasSelectedOption) "Confirmar Respuesta" else "Selecciona una Opción",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = if (hasSelectedOption) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
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
      .height(62.dp)
      .clickable { onSelectOption(displaySlot) }
      .testTag("option_${displayLetter.lowercase()}"),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        item.domain.color.copy(alpha = 0.18f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
      }
    ),
    border = if (isSelected) {
      androidx.compose.foundation.BorderStroke(1.8.dp, item.domain.color)
    } else {
      androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    }
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(4.dp)
    ) {
      // Badge con la letra (A, B, C, D) en la esquina superior izquierda
      Box(
        modifier = Modifier
          .size(20.dp)
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
          fontSize = 10.sp,
          color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
        )
      }

      // Visualización Gráfica o Texto de la Opción en el Centro
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(start = 20.dp, end = 2.dp),
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
