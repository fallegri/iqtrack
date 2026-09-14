package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StimulusVisualizer(
  item: PsychometricItem,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        RoundedCornerShape(14.dp)
      )
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp),
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
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    // Cuadrícula 3x3 compacta (~136.dp total)
    Column(
      modifier = Modifier
        .size(136.dp)
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
        .border(1.2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
        .padding(4.dp),
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
                .size(38.dp)
                .background(
                  if (isTarget) item.domain.color.copy(alpha = 0.15f)
                  else MaterialTheme.colorScheme.surfaceVariant,
                  RoundedCornerShape(6.dp)
                )
                .border(
                  width = if (isTarget) 1.5.dp else 0.6.dp,
                  color = if (isTarget) item.domain.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                  shape = RoundedCornerShape(6.dp)
                ),
              contentAlignment = Alignment.Center
            ) {
              if (isTarget) {
                Text(
                  text = "?",
                  fontSize = 20.sp,
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
  Canvas(modifier = Modifier.size(28.dp)) {
    val center = Offset(size.width / 2, size.height / 2)
    when {
      itemCode.contains("CIRCLE_GROWTH") -> {
        val count = row + col + 1
        val radius = size.minDimension / 2.3f
        drawCircle(
          color = domainColor,
          radius = radius,
          center = center,
          style = Stroke(width = 2f)
        )
        for (i in 0 until count) {
          val angle = (i * 360f / count) * (Math.PI / 180f)
          val endX = center.x + (radius * cos(angle)).toFloat()
          val endY = center.y + (radius * sin(angle)).toFloat()
          drawLine(
            color = domainColor,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 2f
          )
        }
      }
      itemCode.contains("ROTATE_SHADE") -> {
        val angleDeg = (row * 3 + col) * 45f
        val isFilled = ((row + col) % 2 == 1)
        val rad = angleDeg * (Math.PI / 180.0)
        val length = 10f
        val arrowTip = Offset(
          center.x + (length * sin(rad)).toFloat(),
          center.y - (length * cos(rad)).toFloat()
        )
        val baseLeft = Offset(
          center.x - (8f * cos(rad) + 6f * sin(rad)).toFloat(),
          center.y - (8f * sin(rad) - 6f * cos(rad)).toFloat()
        )
        val baseRight = Offset(
          center.x + (8f * cos(rad) - 6f * sin(rad)).toFloat(),
          center.y + (8f * sin(rad) + 6f * cos(rad)).toFloat()
        )
        val path = Path().apply {
          moveTo(arrowTip.x, arrowTip.y)
          lineTo(baseLeft.x, baseLeft.y)
          lineTo(baseRight.x, baseRight.y)
          close()
        }
        if (isFilled) {
          drawPath(path, color = domainColor)
        } else {
          drawPath(path, color = domainColor, style = Stroke(width = 2f))
        }
      }
      itemCode.contains("SHAPE_SIDES") -> {
        val sides = (row * 3 + col + 3).coerceIn(3, 8)
        drawPolygon(sides = sides, center = center, radius = 10f, color = domainColor, filled = false)
      }
      else -> {
        val elementCount = ((row + 1) * (col + 1)) % 4 + 1
        for (i in 0 until elementCount) {
          val offset = (i - (elementCount - 1) / 2f) * 6f
          drawCircle(
            color = domainColor,
            radius = 3f,
            center = Offset(center.x + offset, center.y)
          )
        }
      }
    }
  }
}

@Composable
private fun SpatialStimulus(item: PsychometricItem) {
  Canvas(
    modifier = Modifier
      .size(130.dp, 80.dp)
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
      .padding(8.dp)
  ) {
    val c = Offset(size.width / 2, size.height / 2)
    val w = 34f
    val h = 34f
    val depth = 16f

    // Cara frontal
    drawRect(
      color = item.domain.color.copy(alpha = 0.85f),
      topLeft = Offset(c.x - w, c.y - h / 2),
      size = Size(w * 1.3f, h)
    )

    // Cara superior
    val topPath = Path().apply {
      moveTo(c.x - w, c.y - h / 2)
      lineTo(c.x - w + depth, c.y - h / 2 - depth)
      lineTo(c.x - w + depth + w * 1.3f, c.y - h / 2 - depth)
      lineTo(c.x - w + w * 1.3f, c.y - h / 2)
      close()
    }
    drawPath(topPath, color = item.domain.color)

    // Cara lateral
    val rightPath = Path().apply {
      moveTo(c.x - w + w * 1.3f, c.y - h / 2)
      lineTo(c.x - w + depth + w * 1.3f, c.y - h / 2 - depth)
      lineTo(c.x - w + depth + w * 1.3f, c.y + h / 2 - depth)
      lineTo(c.x - w + w * 1.3f, c.y + h / 2)
      close()
    }
    drawPath(rightPath, color = item.domain.color.copy(alpha = 0.6f))
  }
}

@Composable
private fun WorkingMemoryStimulus(item: PsychometricItem) {
  Box(
    modifier = Modifier
      .fillMaxWidth(0.95f)
      .height(60.dp)
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
      .border(1.2.dp, item.domain.color.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
      .padding(horizontal = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = item.stimulusCode.substringAfter(":"),
      style = MaterialTheme.typography.titleMedium.copy(
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.ExtraBold
      ),
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun SpeedStimulus(item: PsychometricItem) {
  Box(
    modifier = Modifier
      .fillMaxWidth(0.95f)
      .height(55.dp)
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
      .border(1.2.dp, item.domain.color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
      .padding(horizontal = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = item.prompt,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
      maxLines = 2
    )
  }
}

@Composable
private fun VerbalAnalogyStimulus(item: PsychometricItem) {
  Box(
    modifier = Modifier
      .fillMaxWidth(0.95f)
      .height(55.dp)
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
      .border(1.2.dp, item.domain.color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
      .padding(horizontal = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = item.prompt,
      style = MaterialTheme.typography.bodySmall.copy(
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        fontWeight = FontWeight.Bold
      ),
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
      maxLines = 2
    )
  }
}

/**
 * Visualizador Gráfico de Alternativas (Options A, B, C, D).
 * En lugar de descripciones de texto ambiguas, renderiza directamente la figura,
 * forma geométrica, ángulo rotado, bloque 3D o token matemático en la tarjeta.
 */
@Composable
fun PsychometricOptionVisualizer(
  item: PsychometricItem,
  optionText: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier
) {
  val domainColor = if (isSelected) item.domain.color else MaterialTheme.colorScheme.primary

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    when {
      // 1. Alternativas de Círculo con Segmentos
      optionText.contains("segmento", ignoreCase = true) || optionText.contains("CIRCLE_SEG", ignoreCase = true) -> {
        val count = when {
          optionText.contains("4") -> 4
          optionText.contains("5") -> 5
          optionText.contains("6") -> 6
          optionText.contains("7") -> 7
          optionText.contains("8") -> 8
          else -> 5
        }
        Canvas(modifier = Modifier.size(36.dp)) {
          val center = Offset(size.width / 2, size.height / 2)
          val radius = size.minDimension / 2.3f
          drawCircle(color = domainColor, radius = radius, center = center, style = Stroke(width = 2.2f))
          for (i in 0 until count) {
            val angle = (i * 360f / count) * (Math.PI / 180f)
            val endX = center.x + (radius * cos(angle)).toFloat()
            val endY = center.y + (radius * sin(angle)).toFloat()
            drawLine(color = domainColor, start = center, end = Offset(endX, endY), strokeWidth = 2.2f)
          }
        }
      }

      // 2. Alternativas de Flecha / Rotación
      optionText.contains("Flecha", ignoreCase = true) || optionText.contains("Triángulo 0°", ignoreCase = true) -> {
        val angleDeg = when {
          optionText.contains("360°") || optionText.contains("0°") -> 0f
          optionText.contains("45°") -> 45f
          optionText.contains("90°") -> 90f
          optionText.contains("135°") -> 135f
          optionText.contains("180°") -> 180f
          optionText.contains("225°") -> 225f
          optionText.contains("270°") -> 270f
          optionText.contains("315°") -> 315f
          else -> 0f
        }
        val isFilled = optionText.contains("relleno", ignoreCase = true) && !optionText.contains("sin relleno", ignoreCase = true)
        Canvas(modifier = Modifier.size(36.dp)) {
          val center = Offset(size.width / 2, size.height / 2)
          val rad = angleDeg * (Math.PI / 180.0)
          val length = 12f
          val arrowTip = Offset(
            center.x + (length * sin(rad)).toFloat(),
            center.y - (length * cos(rad)).toFloat()
          )
          val baseLeft = Offset(
            center.x - (9f * cos(rad) + 7f * sin(rad)).toFloat(),
            center.y - (9f * sin(rad) - 7f * cos(rad)).toFloat()
          )
          val baseRight = Offset(
            center.x + (9f * cos(rad) - 7f * sin(rad)).toFloat(),
            center.y + (9f * sin(rad) + 7f * cos(rad)).toFloat()
          )
          val path = Path().apply {
            moveTo(arrowTip.x, arrowTip.y)
            lineTo(baseLeft.x, baseLeft.y)
            lineTo(baseRight.x, baseRight.y)
            close()
          }
          if (isFilled) {
            drawPath(path, color = domainColor)
          } else {
            drawPath(path, color = domainColor, style = Stroke(width = 2.2f))
          }
        }
      }

      // 3. Formas Poligonales (Pentágono, Cuadrado, Hexágono, etc.)
      optionText.contains("Pentágono", ignoreCase = true) ||
      optionText.contains("Heptágono", ignoreCase = true) ||
      optionText.contains("Octógono", ignoreCase = true) ||
      optionText.contains("Hexágono", ignoreCase = true) ||
      optionText.contains("Triángulo", ignoreCase = true) ||
      optionText.contains("Cuadrado", ignoreCase = true) -> {
        val sides = when {
          optionText.contains("Triángulo", ignoreCase = true) -> 3
          optionText.contains("Cuadrado", ignoreCase = true) -> 4
          optionText.contains("Pentágono", ignoreCase = true) -> 5
          optionText.contains("Hexágono", ignoreCase = true) -> 6
          optionText.contains("Heptágono", ignoreCase = true) -> 7
          optionText.contains("Octógono", ignoreCase = true) -> 8
          else -> 4
        }
        Canvas(modifier = Modifier.size(36.dp)) {
          drawPolygon(sides = sides, center = Offset(size.width / 2, size.height / 2), radius = 13f, color = domainColor, filled = false)
        }
      }

      // 4. Bloques Espaciales 3D / Isométricos
      item.domain == ChcDomain.GV -> {
        val rotationAngle = when {
          optionText.contains("horario", ignoreCase = true) || optionText.contains("90°", ignoreCase = true) -> 90f
          optionText.contains("invertid", ignoreCase = true) || optionText.contains("180°", ignoreCase = true) -> 180f
          optionText.contains("270°", ignoreCase = true) -> 270f
          else -> 0f
        }
        Canvas(modifier = Modifier.size(36.dp)) {
          val c = Offset(size.width / 2, size.height / 2)
          val s = 12f
          drawRect(
            color = domainColor.copy(alpha = 0.8f),
            topLeft = Offset(c.x - s, c.y - s),
            size = Size(s * 1.5f, s * 1.5f)
          )
          val path = Path().apply {
            moveTo(c.x - s, c.y - s)
            lineTo(c.x - s + 6f, c.y - s - 6f)
            lineTo(c.x - s + 6f + s * 1.5f, c.y - s - 6f)
            lineTo(c.x - s + s * 1.5f, c.y - s)
            close()
          }
          drawPath(path, color = domainColor)
        }
      }

      // 5. Patrones XOR (Línea, Cruz, Punto)
      optionText.contains("XOR", ignoreCase = true) ||
      optionText.contains("Cruz", ignoreCase = true) ||
      optionText.contains("Línea", ignoreCase = true) ||
      optionText.contains("Punto", ignoreCase = true) -> {
        Canvas(modifier = Modifier.size(36.dp)) {
          val c = Offset(size.width / 2, size.height / 2)
          when {
            optionText.contains("Cruz", ignoreCase = true) -> {
              drawLine(domainColor, Offset(c.x - 12f, c.y), Offset(c.x + 12f, c.y), strokeWidth = 2.5f)
              drawLine(domainColor, Offset(c.x, c.y - 12f), Offset(c.x, c.y + 12f), strokeWidth = 2.5f)
            }
            optionText.contains("diagonal", ignoreCase = true) -> {
              drawLine(domainColor, Offset(c.x - 10f, c.y - 10f), Offset(c.x + 10f, c.y + 10f), strokeWidth = 2.5f)
              drawCircle(domainColor, radius = 3.5f, center = c)
            }
            optionText.contains("horizontal", ignoreCase = true) -> {
              drawLine(domainColor, Offset(c.x - 12f, c.y), Offset(c.x + 12f, c.y), strokeWidth = 2.5f)
            }
            else -> {
              drawCircle(domainColor, radius = 4f, center = c)
            }
          }
        }
      }

      // 6. Texto / Números / Cadenas concisas
      else -> {
        // Formato conciso en badge elegante
        Text(
          text = cleanOptionText(optionText),
          fontWeight = FontWeight.Bold,
          fontSize = if (optionText.length > 15) 12.sp else 14.sp,
          color = if (isSelected) domainColor else MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          maxLines = 2
        )
      }
    }
  }
}

private fun cleanOptionText(raw: String): String {
  // Limpia textos largos para dejarlos como conceptos breves
  return when {
    raw.contains("(") && raw.contains(")") && raw.length > 25 -> {
      raw.substringBefore("(").trim()
    }
    raw.length > 28 -> raw.take(26) + "…"
    else -> raw
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPolygon(
  sides: Int,
  center: Offset,
  radius: Float,
  color: Color,
  filled: Boolean
) {
  val path = Path()
  val angleStep = (2 * Math.PI / sides)
  for (i in 0 until sides) {
    val angle = i * angleStep - Math.PI / 2
    val x = center.x + (radius * cos(angle)).toFloat()
    val y = center.y + (radius * sin(angle)).toFloat()
    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
  }
  path.close()
  if (filled) {
    drawPath(path, color = color)
  } else {
    drawPath(path, color = color, style = Stroke(width = 2.2f))
  }
}
