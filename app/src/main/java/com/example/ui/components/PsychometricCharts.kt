package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChcDomain
import com.example.data.model.ItemResponse
import com.example.ui.theme.MetricGreen
import com.example.ui.theme.MetricPurple
import com.example.ui.theme.MetricYellow
import com.example.ui.theme.PrimaryBlueDark

@Composable
fun IqScoreGauge(
  iq: Double,
  ciLow: Double,
  ciHigh: Double,
  percentile: Double,
  se: Double,
  classification: String,
  modifier: Modifier = Modifier
) {
  val animatedIq by animateFloatAsState(
    targetValue = iq.toFloat(),
    animationSpec = tween(durationMillis = 1200),
    label = "iqAnimation"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "COCIENTE INTELECTUAL GENERAL (FSIQ)",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.2.sp
    )

    Spacer(modifier = Modifier.height(14.dp))

    Box(
      modifier = Modifier.size(170.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(160.dp)) {
        val strokeWidth = 14.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Fondo del arco (240 grados)
        drawArc(
          color = Color(0xFF334155).copy(alpha = 0.4f),
          startAngle = 150f,
          sweepAngle = 240f,
          useCenter = false,
          topLeft = Offset(center.x - radius, center.y - radius),
          size = Size(radius * 2, radius * 2),
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progreso del CI (escala de 40 a 160 -> rango de 120 puntos)
        val normalized = ((animatedIq - 40f) / 120f).coerceIn(0f, 1f)
        val sweep = normalized * 240f

        val gaugeColor = when {
          animatedIq >= 120f -> MetricPurple
          animatedIq >= 90f -> PrimaryBlueDark
          animatedIq >= 80f -> MetricYellow
          else -> Color(0xFFEF4444)
        }

        drawArc(
          color = gaugeColor,
          startAngle = 150f,
          sweepAngle = sweep,
          useCenter = false,
          topLeft = Offset(center.x - radius, center.y - radius),
          size = Size(radius * 2, radius * 2),
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "${animatedIq.toInt()}",
          fontSize = 44.sp,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "CI Estándar",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Categoría cualitativa
    Box(
      modifier = Modifier
        .background(
          MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
          RoundedCornerShape(30.dp)
        )
        .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
      Text(
        text = classification,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Fila con intervalo de confianza 95% y percentil
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      MetricBadge(
        title = "IC 95% (±1.96 SE)",
        value = "${ciLow.toInt()} - ${ciHigh.toInt()}"
      )
      MetricBadge(
        title = "Rango Percentil",
        value = "PR ${percentile.toInt()}"
      )
      MetricBadge(
        title = "Error Estándar",
        value = "SE $se"
      )
    }
  }
}

@Composable
private fun MetricBadge(title: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 10.sp
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun ChcProfileChart(
  domainScores: Map<ChcDomain, Double>,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
      .padding(18.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "PERFIL COGNITIVO MULTIFACTORIAL (CHC)",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp
      )
      Text(
        text = "Media = 100",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    ChcDomain.entries.forEach { domain ->
      val score = domainScores[domain] ?: 100.0
      ChcBarRow(domain = domain, score = score)
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun ChcBarRow(domain: ChcDomain, score: Double) {
  val animatedScore by animateFloatAsState(
    targetValue = score.toFloat(),
    animationSpec = tween(1000),
    label = "chcBar"
  )

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .background(domain.color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "${domain.fullName} (${domain.code})",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Text(
        text = "${animatedScore.toInt()}",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = domain.color
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Barra de progreso con marcador en la media poblacional (100)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(12.dp)
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
    ) {
      val fraction = ((animatedScore - 50f) / 100f).coerceIn(0.05f, 1f)
      Box(
        modifier = Modifier
          .fillMaxWidth(fraction)
          .height(12.dp)
          .background(domain.color, RoundedCornerShape(6.dp))
      )

      // Línea de referencia en 100 (fraction = (100 - 50) / 100 = 0.5)
      Box(
        modifier = Modifier
          .fillMaxWidth(0.5f)
          .height(12.dp),
        contentAlignment = Alignment.CenterEnd
      ) {
        Box(
          modifier = Modifier
            .width(2.dp)
            .height(12.dp)
            .background(Color.White.copy(alpha = 0.7f))
        )
      }
    }
  }
}

@Composable
fun ThetaConvergenceChart(
  responses: List<ItemResponse>,
  modifier: Modifier = Modifier
) {
  if (responses.size < 2) return

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
      .padding(18.dp)
  ) {
    Text(
      text = "TRAYECTORIA DE CONVERGENCIA IRT (θ & SE)",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(12.dp))

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
        .padding(12.dp)
    ) {
      val w = size.width
      val h = size.height

      // Eje central theta = 0.0
      val zeroY = h / 2
      drawLine(
        color = Color(0xFF64748B).copy(alpha = 0.4f),
        start = Offset(0f, zeroY),
        end = Offset(w, zeroY),
        strokeWidth = 1.5f
      )

      val stepX = w / (responses.size - 1).coerceAtLeast(1)

      // Rango de theta para dibujo: [-3.0, +3.0]
      fun thetaToY(theta: Double): Float {
        val normalized = ((theta.toFloat() + 3.0f) / 6.0f).coerceIn(0f, 1f)
        return h - (normalized * h)
      }

      // Banda de error estándar SE (±1.96 SE)
      val bandPath = Path()
      val upperPoints = mutableListOf<Offset>()
      val lowerPoints = mutableListOf<Offset>()

      for (i in responses.indices) {
        val x = i * stepX
        val theta = responses[i].posteriorTheta
        val se = responses[i].posteriorSe
        upperPoints.add(Offset(x, thetaToY(theta + se)))
        lowerPoints.add(Offset(x, thetaToY(theta - se)))
      }

      if (upperPoints.isNotEmpty()) {
        bandPath.moveTo(upperPoints.first().x, upperPoints.first().y)
        upperPoints.forEach { bandPath.lineTo(it.x, it.y) }
        lowerPoints.asReversed().forEach { bandPath.lineTo(it.x, it.y) }
        bandPath.close()

        drawPath(
          path = bandPath,
          color = PrimaryBlueDark.copy(alpha = 0.18f)
        )
      }

      // Línea de trayectoria Theta
      val linePath = Path()
      for (i in responses.indices) {
        val x = i * stepX
        val y = thetaToY(responses[i].posteriorTheta)
        if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        // Punto con color de acierto/error
        val pointColor = if (responses[i].isCorrect) MetricGreen else Color(0xFFEF4444)
        drawCircle(color = pointColor, radius = 4.5f, center = Offset(x, y))
      }

      drawPath(
        path = linePath,
        color = PrimaryBlueDark,
        style = Stroke(width = 2.5f)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "Reactivo 1",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = "Área azul: Margen SE | Verde: Correcto | Rojo: Error",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
      )
      Text(
        text = "Reactivo ${responses.size}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
