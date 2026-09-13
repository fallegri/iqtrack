package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChcDomain
import com.example.data.model.PsychometricItem
import com.example.ui.theme.PrimaryBlueDark

@Composable
fun StimulusVisualizer(
  item: PsychometricItem,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        RoundedCornerShape(16.dp)
      )
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    when (item.domain) {
      ChcDomain.GF -> MatrixStimulus(item = item)
      ChcDomain.GV -> SpatialStimulus(item = item)
      ChcDomain.GWM -> WorkingMemoryStimulus(item = item)
      ChcDomain.GS -> SpeedStimulus(item = item)
      ChcDomain.GC -> VerbalAnalogyStimulus(item = item)
    }
  }
}

@Composable
private fun MatrixStimulus(item: PsychometricItem) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = "MATRIZ PSICOMÉTRICA (Gf)",
      style = MaterialTheme.typography.labelSmall,
      color = item.domain.color,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )

    // Cuadrícula 3x3 estilizada
    Column(
      modifier = Modifier
        .size(210.dp)
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
        .padding(6.dp),
      verticalArrangement = Arrangement.SpaceEvenly
    ) {
      for (row in 0..2) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          for (col in 0..2) {
            val isTarget = (row == 2 && col == 2)
            Box(
              modifier = Modifier
                .size(60.dp)
                .background(
                  if (isTarget) item.domain.color.copy(alpha = 0.15f)
                  else MaterialTheme.colorScheme.surfaceVariant,
                  RoundedCornerShape(8.dp)
                )
                .border(
                  width = if (isTarget) 1.5.dp else 0.8.dp,
                  color = if (isTarget) item.domain.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                  shape = RoundedCornerShape(8.dp)
                ),
              contentAlignment = Alignment.Center
            ) {
              if (isTarget) {
                Text(
                  text = "?",
                  fontSize = 26.sp,
                  fontWeight = FontWeight.Black,
                  color = item.domain.color
                )
              } else {
                MatrixCellGraphic(row = row, col = col, itemCode = item.stimulusCode, domainColor = item.domain.color)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MatrixCellGraphic(row: Int, col: Int, itemCode: String, domainColor: Color) {
  Canvas(modifier = Modifier.size(40.dp)) {
    val center = Offset(size.width / 2, size.height / 2)
    when {
      itemCode.contains("CIRCLE_GROWTH") -> {
        val count = row + col + 1
        val radius = (size.minDimension / 2.4f)
        drawCircle(
          color = domainColor,
          radius = radius,
          center = center,
          style = Stroke(width = 2.5f)
        )
        // Dibujar radios según count
        for (i in 0 until count) {
          val angle = (i * 360f / count) * (Math.PI / 180f)
          val endX = center.x + (radius * kotlin.math.cos(angle)).toFloat()
          val endY = center.y + (radius * kotlin.math.sin(angle)).toFloat()
          drawLine(
            color = domainColor,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 2.5f
          )
        }
      }
      itemCode.contains("ROTATE_SHADE") -> {
        val angleDeg = (row * 3 + col) * 45f
        val isFilled = ((row + col) % 2 == 1)
        val path = Path().apply {
          moveTo(center.x, center.y - 14f)
          lineTo(center.x + 12f, center.y + 12f)
          lineTo(center.x - 12f, center.y + 12f)
          close()
        }
        if (isFilled) {
          drawPath(path, color = domainColor)
        } else {
          drawPath(path, color = domainColor, style = Stroke(width = 2.5f))
        }
      }
      else -> {
        // Fallback gráfico formal para matrices geométricas
        val elementCount = (row + 1) * (col + 1) % 4 + 1
        for (i in 0 until elementCount) {
          val offset = (i - 1) * 8f
          drawCircle(
            color = domainColor,
            radius = 4f + (row * 1.5f),
            center = Offset(center.x + offset, center.y)
          )
        }
      }
    }
  }
}

@Composable
private fun SpatialStimulus(item: PsychometricItem) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      text = "PROCESAMIENTO ESPACIAL 3D (Gv)",
      style = MaterialTheme.typography.labelSmall,
      color = item.domain.color,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )

    Canvas(
      modifier = Modifier
        .size(190.dp, 130.dp)
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
        .padding(12.dp)
    ) {
      val c = Offset(size.width / 2, size.height / 2)
      // Dibujar bloque isométrico 3D estilizado
      val w = 45f
      val h = 45f
      val depth = 22f

      // Cara frontal
      drawRect(
        color = item.domain.color.copy(alpha = 0.85f),
        topLeft = Offset(c.x - w, c.y - h / 2),
        size = Size(w * 1.4f, h)
      )

      // Cara superior isométrica
      val topPath = Path().apply {
        moveTo(c.x - w, c.y - h / 2)
        lineTo(c.x - w + depth, c.y - h / 2 - depth)
        lineTo(c.x - w + depth + w * 1.4f, c.y - h / 2 - depth)
        lineTo(c.x - w + w * 1.4f, c.y - h / 2)
        close()
      }
      drawPath(topPath, color = item.domain.color)

      // Cara lateral derecha
      val rightPath = Path().apply {
        moveTo(c.x - w + w * 1.4f, c.y - h / 2)
        lineTo(c.x - w + depth + w * 1.4f, c.y - h / 2 - depth)
        lineTo(c.x - w + depth + w * 1.4f, c.y + h / 2 - depth)
        lineTo(c.x - w + w * 1.4f, c.y + h / 2)
        close()
      }
      drawPath(rightPath, color = item.domain.color.copy(alpha = 0.6f))

      // Eje de rotación angular
      drawCircle(
        color = Color.White,
        radius = 5f,
        center = Offset(c.x + depth, c.y - depth)
      )
    }
  }
}

@Composable
private fun WorkingMemoryStimulus(item: PsychometricItem) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      text = "MANIPULACIÓN OPERATIVA (Gwm)",
      style = MaterialTheme.typography.labelSmall,
      color = item.domain.color,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )

    Box(
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        .border(1.5.dp, item.domain.color.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
        .padding(vertical = 20.dp, horizontal = 16.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = item.stimulusCode.substringAfter(":"),
        style = MaterialTheme.typography.titleLarge.copy(
          fontFamily = FontFamily.Monospace,
          letterSpacing = 3.sp,
          fontWeight = FontWeight.ExtraBold
        ),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun SpeedStimulus(item: PsychometricItem) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      text = "VELOCIDAD Y DISCRIMINACIÓN (Gs)",
      style = MaterialTheme.typography.labelSmall,
      color = item.domain.color,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )

    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        .border(1.5.dp, item.domain.color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        .padding(vertical = 18.dp, horizontal = 16.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = item.prompt,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun VerbalAnalogyStimulus(item: PsychometricItem) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      text = "INTELIGENCIA CRISTALIZADA (Gc)",
      style = MaterialTheme.typography.labelSmall,
      color = item.domain.color,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )

    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        .border(1.5.dp, item.domain.color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        .padding(vertical = 18.dp, horizontal = 16.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = item.prompt,
        style = MaterialTheme.typography.bodyLarge.copy(
          fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
          fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )
    }
  }
}
