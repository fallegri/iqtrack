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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssessmentSession
import com.example.ui.theme.ColorGf
import com.example.ui.theme.ColorGv
import com.example.ui.theme.ColorGwm
import com.example.ui.theme.MetricGreen
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.SecondaryTealDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
  sessions: List<AssessmentSession>,
  onStartAssessment: () -> Unit,
  onViewArchitecture: () -> Unit,
  onSelectSession: (AssessmentSession) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(10.dp))
      HeroBanner(
        onStartClick = onStartAssessment,
        onArchitectureClick = onViewArchitecture
      )
    }

    item {
      AdaptiveEngineOverviewCard()
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Evaluaciones Registradas",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${sessions.size} sesiones",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    if (sessions.isEmpty()) {
      item {
        EmptySessionsCard(onStartClick = onStartAssessment)
      }
    } else {
      items(sessions, key = { it.sessionId }) { session ->
        SessionHistoryCard(
          session = session,
          onClick = { onSelectSession(session) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun HeroBanner(
  onStartClick: () -> Unit,
  onArchitectureClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        Brush.linearGradient(
          colors = listOf(
            Color(0xFF1E1B4B),
            Color(0xFF0F172A)
          )
        ),
        RoundedCornerShape(24.dp)
      )
      .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
      .padding(22.dp)
  ) {
    Column {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(Color(0xFF38BDF8).copy(alpha = 0.2f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(22.dp)
          )
        }
        Column {
          Text(
            text = "CAT-IQ MOBILE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = Color(0xFF38BDF8),
            letterSpacing = 2.sp
          )
          Text(
            text = "Evaluación Psicométrica Adaptativa",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Motor IRT 3PL calibrado en dispositivo y taxonomía CHC con soporte RAG clínico y telemetría de atención.",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFCBD5E1),
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(20.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Button(
          onClick = onStartClick,
          modifier = Modifier
            .weight(1.3f)
            .height(50.dp)
            .testTag("start_test_button"),
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueDark),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Iniciar Test", fontWeight = FontWeight.Bold, color = Color.Black)
        }

        OutlinedButton(
          onClick = onArchitectureClick,
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("view_architecture_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
        ) {
          Icon(Icons.Default.Architecture, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("SDD Móvil", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
private fun AdaptiveEngineOverviewCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Text(
        text = "ESPECIFICACIÓN TÉCNICA EN DISPOSITIVO",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        TechFeatureItem(
          icon = Icons.Default.Analytics,
          label = "Motor IRT",
          value = "3PL EAP Gauss"
        )
        TechFeatureItem(
          icon = Icons.Default.Speed,
          label = "Criterio Parada",
          value = "SE ≤ 0.30"
        )
        TechFeatureItem(
          icon = Icons.Default.CheckCircle,
          label = "Modelo",
          value = "CHC 5 Factores"
        )
      }
    }
  }
}

@Composable
private fun TechFeatureItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 11.sp
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun EmptySessionsCard(onStartClick: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = Icons.Default.Analytics,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Sin evaluaciones previas",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Inicia tu primera evaluación CAT-IQ para calcular tu Cociente Intelectual con precisión adaptativa.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
    }
  }
}

@Composable
private fun SessionHistoryCard(
  session: AssessmentSession,
  onClick: () -> Unit
) {
  val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
  val dateStr = dateFormat.format(Date(session.completedAt))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("session_card_${session.sessionId}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .background(
              MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "${session.fullScaleIq.toInt()}",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Column {
          Text(
            text = "CI ${session.fullScaleIq.toInt()} • PR ${session.percentileRank.toInt()}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${session.totalItems} reactivos • SE ${session.standardError}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = dateStr,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontSize = 11.sp
          )
        }
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Ver informe",
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
