package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChcDomain
import com.example.ui.components.ChcProfileChart
import com.example.ui.components.IqScoreGauge
import com.example.ui.components.ThetaConvergenceChart
import com.example.ui.theme.ColorGf
import com.example.ui.theme.MetricGreen
import com.example.ui.theme.MetricRed
import com.example.ui.theme.MetricYellow
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.viewmodel.ReportState

@Composable
fun ReportScreen(
  state: ReportState,
  onBackToDashboard: () -> Unit,
  modifier: Modifier = Modifier
) {
  val session = state.session
  val clinical = state.clinicalReport
  val telemetry = state.telemetry

  if (session == null || clinical == null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Cargando informe psicométrico...")
    }
    return
  }

  val domainScores = mapOf(
    ChcDomain.GF to session.gfScore,
    ChcDomain.GV to session.gvScore,
    ChcDomain.GWM to session.gwmScore,
    ChcDomain.GS to session.gsScore,
    ChcDomain.GC to session.gcScore
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackToDashboard,
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver al Dashboard",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "INFORME DIAGNÓSTICO",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp
          )
          Text(
            text = "Evaluación CAT-IQ",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    // Gauge de Puntuación FSIQ
    item {
      IqScoreGauge(
        iq = session.fullScaleIq,
        ciLow = session.confidenceIntervalLow,
        ciHigh = session.confidenceIntervalHigh,
        percentile = session.percentileRank,
        se = session.standardError,
        classification = clinical.qualitativeClassification
      )
    }

    // Perfil Multifactorial CHC
    item {
      ChcProfileChart(domainScores = domainScores)
    }

    // Gráfico de Convergencia IRT
    if (state.responses.isNotEmpty()) {
      item {
        ThetaConvergenceChart(responses = state.responses)
      }
    }

    // Análisis RAG Clínico: Fortalezas, Debilidades y Discrepancias
    item {
      RagClinicalNarrativeCard(
        clinical = clinical,
        domainScores = domainScores
      )
    }

    // Auditoría de Telemetría Móvil y Validez del Protocolo
    item {
      TelemetryAuditCard(
        session = session,
        telemetry = telemetry
      )
    }

    // Botón de regreso / finalizar
    item {
      Button(
        onClick = onBackToDashboard,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("back_to_dashboard_button"),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueDark),
        shape = RoundedCornerShape(14.dp)
      ) {
        Text("Volver al Dashboard", fontWeight = FontWeight.Bold, color = Color.Black)
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun RagClinicalNarrativeCard(
  clinical: com.example.engine.ClinicalRagReport,
  domainScores: Map<ChcDomain, Double>
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Psychology,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Text(
          text = "DICTAMEN CLÍNICO RAG (ON-DEVICE)",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 1.sp
        )
      }

      Text(
        text = clinical.detailedNarrative,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp
      )

      // Fortalezas
      if (clinical.cognitiveStrengths.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Fortalezas Cognitivas Intra-individuales:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MetricGreen
          )
          clinical.cognitiveStrengths.forEach { s ->
            Row(
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MetricGreen,
                modifier = Modifier
                  .size(16.dp)
                  .padding(top = 2.dp)
              )
              Text(
                text = s,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      // Debilidades relativas
      if (clinical.cognitiveWeaknesses.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Áreas de Soporte o Vulnerabilidad Relativa:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MetricYellow
          )
          clinical.cognitiveWeaknesses.forEach { w ->
            Row(
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MetricYellow,
                modifier = Modifier
                  .size(16.dp)
                  .padding(top = 2.dp)
              )
              Text(
                text = w,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      // Recomendaciones
      if (clinical.neurocognitiveRecommendations.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Pautas Neurocognitivas Recomendadas:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          clinical.neurocognitiveRecommendations.forEach { rec ->
            Row(
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                  .size(16.dp)
                  .padding(top = 2.dp)
              )
              Text(
                text = rec,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TelemetryAuditCard(
  session: com.example.data.model.AssessmentSession,
  telemetry: com.example.engine.TelemetryAnalysis?
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "AUDITORÍA DE VALIDEZ MÓVIL",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 1.sp
          )
        }

        val isValid = (session.validityStatus == "PROTOCOLO_VÁLIDO" || session.validityStatus == "VÁLIDA")
        Box(
          modifier = Modifier
            .background(
              if (isValid) MetricGreen.copy(alpha = 0.15f) else MetricYellow.copy(alpha = 0.15f),
              RoundedCornerShape(20.dp)
            )
            .border(
              1.dp,
              if (isValid) MetricGreen else MetricYellow,
              RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = session.validityStatus.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isValid) MetricGreen else MetricYellow,
            fontSize = 10.sp
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("Atención Sostenida", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${(session.attentionIndex * 100).toInt()}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Column {
          Text("Latencia Media Táctil", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${String.format("%.1f", session.avgResponseTimeMs / 1000.0)} s", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Column {
          Text("Reactivos Aplicados", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${session.totalItems}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
      }

      if (telemetry != null && telemetry.clinicalNotes.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          telemetry.clinicalNotes.forEach { note ->
            Text(
              text = "• $note",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp
            )
          }
        }
      }
    }
  }
}
