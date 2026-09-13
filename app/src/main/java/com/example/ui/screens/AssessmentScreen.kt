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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
      .padding(horizontal = 20.dp)
  ) {
    // Barra superior de progreso y telemetría
    Spacer(modifier = Modifier.height(10.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = { showExitDialog = true },
        modifier = Modifier.size(40.dp)
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Salir",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }

      // Dominio CHC
      Box(
        modifier = Modifier
          .background(item.domain.color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
          .border(1.dp, item.domain.color.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
          .padding(horizontal = 12.dp, vertical = 4.dp)
      ) {
        Text(
          text = "${item.domain.code} • ${item.domain.fullName}",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = item.domain.color
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
          .background(timerColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
          .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Timer,
          contentDescription = null,
          tint = timerColor,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${state.remainingTimeSeconds}s",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = timerColor
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Indicador de convergencia de error estándar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Reactivo ${state.itemNumber} de 15",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "SE: ${String.format("%.2f", state.currentSe)} (meta ≤ 0.30)",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Barra de progreso de precisión psicométrica
    val precisionProgress = ((1.0 - (state.currentSe - 0.30).coerceAtLeast(0.0) / 0.70)).toFloat().coerceIn(0.1f, 1f)
    LinearProgressIndicator(
      progress = { precisionProgress },
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp),
      color = PrimaryBlueDark,
      trackColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Contenido desplazable con estímulo y opciones
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Estímulo Gráfico / Cognitivo
      StimulusVisualizer(item = item)

      // Instrucción / Pregunta
      Text(
        text = item.prompt,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp
      )

      // 4 Opciones de Respuesta
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (index in 0..3) {
          val optionText = item.getOption(index)
          val isSelected = (state.selectedOption == index)
          val letter = ('A' + index).toString()

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectOption(index) }
              .testTag("option_${letter.lowercase()}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected)
                item.domain.color.copy(alpha = 0.18f)
              else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) item.domain.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .background(
                    if (isSelected) item.domain.color else MaterialTheme.colorScheme.surface,
                    CircleShape
                  )
                  .border(
                    1.dp,
                    if (isSelected) item.domain.color else MaterialTheme.colorScheme.outline,
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = letter,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                  fontSize = 14.sp
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
    }

    // Botón inferior de confirmación
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Button(
        onClick = onSubmitAnswer,
        enabled = state.selectedOption != null,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("confirm_answer_button"),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueDark),
        shape = RoundedCornerShape(14.dp)
      ) {
        Text(
          text = "Confirmar Respuesta",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = Color.Black
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Telemetría táctil y tiempo de deliberación activos",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        fontSize = 10.sp
      )
    }
  }
}
