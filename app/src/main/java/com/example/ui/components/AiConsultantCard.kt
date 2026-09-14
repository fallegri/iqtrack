package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiConsultantConfig
import com.example.data.ai.AiProvider
import com.example.ui.theme.MetricGreen
import com.example.ui.theme.MetricRed
import com.example.ui.theme.PrimaryBlueDark

@Composable
fun AiConsultantCard(
  aiConfig: AiConsultantConfig,
  reportText: String,
  isGenerating: Boolean,
  errorMessage: String?,
  followUpList: List<Pair<String, String>>,
  isFollowUpLoading: Boolean,
  onGenerateReport: () -> Unit,
  onOpenSettings: () -> Unit,
  onAskFollowUp: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var questionInput by remember { mutableStateOf("") }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("ai_consultant_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Encabezado con insignia del proveedor de IA
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  colors = listOf(PrimaryBlueDark, Color(0xFF673AB7))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          Column {
            Text(
              text = "Consultor Neuropsicológico IA",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .background(MetricGreen, CircleShape)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${aiConfig.selectedProvider.displayName} (${aiConfig.getEffectiveModel(aiConfig.selectedProvider)})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier.testTag("open_ai_settings_button")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Configurar IA",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Estado 1: Error al generar
      if (errorMessage != null) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(MetricRed.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .border(1.dp, MetricRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MetricRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Aviso de Conexión IA",
              fontWeight = FontWeight.Bold,
              color = MetricRed,
              style = MaterialTheme.typography.bodyMedium
            )
          }
          Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = onOpenSettings,
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueDark),
              modifier = Modifier.height(36.dp)
            ) {
              Text("Configurar Clave API", fontSize = 12.sp)
            }
            OutlinedButton(
              onClick = onGenerateReport,
              modifier = Modifier.height(36.dp)
            ) {
              Text("Reintentar", fontSize = 12.sp)
            }
          }
        }
      }

      // Estado 2: Cargando / Generando informe
      if (isGenerating) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          CircularProgressIndicator(
            color = PrimaryBlueDark,
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp)
          )
          Text(
            text = "Elaborando informe clínico con ${aiConfig.selectedProvider.displayName}...",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Analizando dispersión factorial CHC, correlación Gf-Gc y pautas vocacionales...",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Estado 3: Sin informe previo generado todavía
      if (reportText.isBlank() && !isGenerating && errorMessage == null) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryBlueDark.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Interpretación Personalizada con IA",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Obtén un informe cualitativo completo que contextualiza tus puntuaciones de Cociente Intelectual, identifica patrones en los 5 factores CHC y ofrece recomendaciones prácticas para tu vida académica, profesional y cognitiva.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )
          Button(
            onClick = onGenerateReport,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueDark),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("generate_ai_report_button")
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generar Análisis Clínico IA")
          }
        }
      }

      // Estado 4: Informe disponible
      if (reportText.isNotBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          // Barra de acciones superiores del informe
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "INFORME NARRATIVO GENERADO",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = PrimaryBlueDark
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("Informe Psicométrico IA", reportText)
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(context, "Informe copiado al portapapeles", Toast.LENGTH_SHORT).show()
                }
              ) {
                Icon(
                  Icons.Default.ContentCopy,
                  contentDescription = "Copiar informe",
                  modifier = Modifier.size(18.dp)
                )
              }

              IconButton(
                onClick = onGenerateReport,
                enabled = !isGenerating
              ) {
                Icon(
                  Icons.Default.Refresh,
                  contentDescription = "Regenerar informe",
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          // Contenedor del texto del informe
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
              .padding(14.dp)
          ) {
            Text(
              text = reportText,
              style = MaterialTheme.typography.bodyMedium,
              lineHeight = 22.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          // Historial de Preguntas y Respuestas (Chat Consultativo)
          if (followUpList.isNotEmpty()) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "DIÁLOGO CON EL CONSULTOR IA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
              )
              followUpList.forEach { (q, a) ->
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlueDark.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = "Tú: $q",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueDark
                  )
                  Text(
                    text = a,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                  )
                }
              }
            }
          }

          // Entrada interactiva de consultas al especialista
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "Pregunta al Neuropsicólogo IA sobre tus resultados:",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = questionInput,
                onValueChange = { questionInput = it },
                modifier = Modifier
                  .weight(1f)
                  .testTag("ai_followup_input"),
                placeholder = {
                  Text(
                    "Ej: ¿Cómo potenciar mi memoria de trabajo?",
                    fontSize = 12.sp
                  )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
              )

              Button(
                onClick = {
                  if (questionInput.isNotBlank() && !isFollowUpLoading) {
                    val q = questionInput.trim()
                    questionInput = ""
                    onAskFollowUp(q)
                  }
                },
                enabled = questionInput.isNotBlank() && !isFollowUpLoading,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueDark),
                modifier = Modifier
                  .size(48.dp)
                  .testTag("ai_followup_send_button"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
              ) {
                if (isFollowUpLoading) {
                  CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                  )
                } else {
                  Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar pregunta",
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
