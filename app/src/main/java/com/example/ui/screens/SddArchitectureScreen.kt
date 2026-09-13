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
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MetricGreen
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.SecondaryTealDark

@Composable
fun SddArchitectureScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
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
          onClick = onBack,
          modifier = Modifier
            .size(40.dp)
            .testTag("back_from_architecture")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "REVISIÓN Y ADECUACIÓN ARQUITECTÓNICA",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
          )
          Text(
            text = "SDD Móvil (ISO/IEC/IEEE 42010)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    item {
      ArchitectureSummaryCard()
    }

    item {
      Text(
        text = "Matriz de Transformación Arquitectónica",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    item {
      ArchitectureComparisonCard(
        layer = "1. Motor Matemático IRT & Selección CAT",
        webSdd = "Vercel Edge API / Server Actions\n• Requiere HTTP round-trip por ítem\n• Latencia de red (~150-400ms)\n• Dependiente de conexión online continua",
        mobileSdd = "Motor IRT Nativo On-Device (Kotlin)\n• Computación instantánea (< 2ms) EAP con cuadratura Gaussiana\n• Máxima Información de Fisher (MFI)\n• 100% Offline-First sin latencia perceptiva",
        badge = "Zero Latency Edge"
      )
    }

    item {
      ArchitectureComparisonCard(
        layer = "2. Persistencia y Banco de Reactivos",
        webSdd = "Prisma ORM + Neon PostgreSQL + pgvector\n• Base de datos relacional serverless en la nube\n• Dependencia de pool de conexiones remotas",
        mobileSdd = "Android Room Database (SQLite)\n• Banco calibrado de 25 ítems precargados (3PL a, b, c)\n• Historial de sesiones y respuestas local\n• Privacidad psicométrica absoluta del evaluado",
        badge = "Room Persistence"
      )
    }

    item {
      ArchitectureComparisonCard(
        layer = "3. Telemetría de Atención y Validez",
        webSdd = "Browser Canvas & Web rPPG\n• Variabilidad según browser y permisos de cámara\n• Alto consumo de batería y CPU en JS",
        mobileSdd = "Telemetría Táctil y Cadencia Nativa\n• Medición de latencia de toque en milisegundos\n• Detección de Rapid Guessing (< 1.4s)\n• Detección de fatiga cognitiva terminal",
        badge = "Mobile Touch Telemetry"
      )
    }

    item {
      ArchitectureComparisonCard(
        layer = "4. Pipeline RAG y Diagnóstico Clínico",
        webSdd = "Ingesta administrada con scripts Node/Python\n• Reportes clínicos asíncronos en servidor",
        mobileSdd = "Motor RAG Psicométrico Autónomo On-Device\n• Clasificación normativa WAIS/WJ instantánea\n• Análisis ipsativo de fortalezas y debilidades CHC\n• Detección de discrepancias clínicas (Gf vs Gc)",
        badge = "On-Device RAG"
      )
    }

    item {
      IrtMathematicalFormulationCard()
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun ArchitectureSummaryCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Architecture,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp)
        )
        Text(
          text = "DICTAMEN DE REVISIÓN DEL SDD v2.1",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 1.sp
        )
      }

      Text(
        text = "El SDD original plantea una arquitectura Serverless orientada a navegador web (Next.js + Neon + Vercel). Para una aplicación móvil nativa de alto rendimiento y grado clínico, se rediseñó la topología trasladando el cómputo de probabilidad logística, selección MFI y síntesis RAG directamente al silicio del dispositivo Android.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp
      )
    }
  }
}

@Composable
private fun ArchitectureComparisonCard(
  layer: String,
  webSdd: String,
  mobileSdd: String,
  badge: String
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = layer,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Box(
          modifier = Modifier
            .background(PrimaryBlueDark.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlueDark,
            fontSize = 10.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Web original
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
          .padding(10.dp)
      ) {
        Column {
          Text(
            text = "ORIGINAL (Web / Serverless):",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = webSdd,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Adecuación móvil
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(MetricGreen.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
          .border(1.dp, MetricGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
          .padding(10.dp)
      ) {
        Column {
          Text(
            text = "ADECUACIÓN MÓVIL (Android Edge):",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MetricGreen
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = mobileSdd,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp
          )
        }
      }
    }
  }
}

@Composable
private fun IrtMathematicalFormulationCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Code,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = "MODELADO MATEMÁTICO IMPLEMENTADO",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 1.sp
        )
      }

      Text(
        text = "• Modelo 3PL: P(θ) = c + (1 - c) / [1 + exp(-1.702 · a · (θ - b))]\n" +
            "• Información Fisher: I(θ) = [P'(θ)]² / [P(θ)(1 - P(θ))]\n" +
            "• Estimador EAP: θ_EAP = ∫ θ L(θ) π(θ) dθ / ∫ L(θ) π(θ) dθ (Cuadratura 41 nodos)\n" +
            "• Escala Wechsler: CI = 100 + 15 · θ (IC 95% = CI ± 1.96 · 15 · SE)\n" +
            "• Criterio Parada: SE ≤ 0.30 (Confiabilidad r_xx ≥ 0.91)",
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 20.sp
      )
    }
  }
}
