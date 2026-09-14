package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.AiConsultantConfig
import com.example.data.ai.AiProvider
import com.example.data.ai.TestLengthMode
import com.example.ui.theme.MetricGreen
import com.example.ui.theme.MetricRed
import com.example.ui.theme.PrimaryBlueDark

@Composable
fun AiSettingsDialog(
  currentConfig: AiConsultantConfig,
  onDismiss: () -> Unit,
  onSaveConfig: (AiConsultantConfig) -> Unit,
  onTestConnection: (AiProvider, AiConsultantConfig, (Boolean, String) -> Unit) -> Unit
) {
  var selectedProvider by remember { mutableStateOf(currentConfig.selectedProvider) }
  var geminiKey by remember { mutableStateOf(currentConfig.geminiApiKey) }
  var openaiKey by remember { mutableStateOf(currentConfig.openaiApiKey) }
  var claudeKey by remember { mutableStateOf(currentConfig.claudeApiKey) }
  var nvidiaKey by remember { mutableStateOf(currentConfig.nvidiaApiKey) }
  var customModel by remember { mutableStateOf(currentConfig.customModelName) }
  var selectedLengthMode by remember { mutableStateOf(currentConfig.testLengthMode) }

  var isKeyVisible by remember { mutableStateOf(false) }
  var isTesting by remember { mutableStateOf(false) }
  var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

  val activeKey = when (selectedProvider) {
    AiProvider.GEMINI -> geminiKey
    AiProvider.CHATGPT -> openaiKey
    AiProvider.CLAUDE -> claudeKey
    AiProvider.NVIDIA -> nvidiaKey
  }

  fun updateActiveKey(newVal: String) {
    when (selectedProvider) {
      AiProvider.GEMINI -> geminiKey = newVal
      AiProvider.CHATGPT -> openaiKey = newVal
      AiProvider.CLAUDE -> claudeKey = newVal
      AiProvider.NVIDIA -> nvidiaKey = newVal
    }
  }

  val tempConfig = AiConsultantConfig(
    selectedProvider = selectedProvider,
    geminiApiKey = geminiKey,
    openaiApiKey = openaiKey,
    claudeApiKey = claudeKey,
    nvidiaApiKey = nvidiaKey,
    customModelName = customModel,
    testLengthMode = selectedLengthMode
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier
      .fillMaxWidth(0.95f)
      .padding(vertical = 16.dp)
      .testTag("ai_settings_dialog"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = PrimaryBlueDark,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Configuración Consultor IA",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Cerrar")
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // --- SECCIÓN 1: SELECCIÓN DE PROVEEDOR IA ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "PROVEEDOR DEL MODELO IA",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          AiProvider.entries.forEach { provider ->
            val isSelected = (selectedProvider == provider)
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) PrimaryBlueDark else MaterialTheme.colorScheme.outlineVariant,
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                  selectedProvider = provider
                  testResult = null
                },
              color = if (isSelected) PrimaryBlueDark.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = isSelected,
                  onClick = {
                    selectedProvider = provider
                    testResult = null
                  }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = provider.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  )
                  Text(
                    text = "Modelo default: ${provider.defaultModel} • ${provider.providerName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }

        // --- SECCIÓN 2: API KEY DEL PROVEEDOR ACTIVO ---
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "CLAVE API (${selectedProvider.displayName})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          OutlinedTextField(
            value = activeKey,
            onValueChange = {
              updateActiveKey(it)
              testResult = null
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("ai_api_key_input"),
            label = { Text("API Key para ${selectedProvider.displayName}") },
            placeholder = { Text(selectedProvider.keyPrefixHint) },
            leadingIcon = {
              Icon(Icons.Default.Key, contentDescription = null)
            },
            trailingIcon = {
              IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                Icon(
                  imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = if (isKeyVisible) "Ocultar clave" else "Mostrar clave"
                )
              }
            },
            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          if (selectedProvider == AiProvider.GEMINI) {
            Text(
              text = "Nota: Si dejas el campo vacío, se empleará la variable de entorno GEMINI_API_KEY por defecto.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          } else if (selectedProvider == AiProvider.NVIDIA) {
            Text(
              text = "Compatible con NVIDIA NIM: https://integrate.api.nvidia.com/v1 (Meta Llama 3.1, Mixtral, etc.)",
              style = MaterialTheme.typography.bodySmall,
              color = PrimaryBlueDark,
              fontSize = 11.sp
            )
          }

          // Botón Probar Conexión
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = {
                isTesting = true
                testResult = null
                onTestConnection(selectedProvider, tempConfig) { success, msg ->
                  isTesting = false
                  testResult = Pair(success, msg)
                }
              },
              enabled = !isTesting,
              modifier = Modifier.testTag("test_ai_connection_button")
            ) {
              if (isTesting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Probando...", fontSize = 12.sp)
              } else {
                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Probar Conexión", fontSize = 12.sp)
              }
            }

            testResult?.let { (success, msg) ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .background(
                    if (success) MetricGreen.copy(alpha = 0.12f) else MetricRed.copy(alpha = 0.12f),
                    RoundedCornerShape(8.dp)
                  )
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                  contentDescription = null,
                  tint = if (success) MetricGreen else MetricRed,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (success) "OK" else "Error",
                  color = if (success) MetricGreen else MetricRed,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
          }

          testResult?.let { (_, msg) ->
            Text(
              text = msg,
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = if (testResult?.first == true) MetricGreen else MetricRed
            )
          }
        }

        // --- SECCIÓN 3: EXTENSIÓN Y PROFUNDIDAD DEL TEST ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "PROFUNDIDAD DE LA PRUEBA ADAPTATIVA (TRI)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          TestLengthMode.entries.forEach { mode ->
            val isSelected = (selectedLengthMode == mode)
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) PrimaryBlueDark else MaterialTheme.colorScheme.outlineVariant,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { selectedLengthMode = mode },
              color = if (isSelected) PrimaryBlueDark.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = isSelected,
                  onClick = { selectedLengthMode = mode }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${mode.title} (${mode.minItems} - ${mode.maxItems} preguntas)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  )
                  Text(
                    text = mode.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSaveConfig(tempConfig)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueDark),
        modifier = Modifier.testTag("save_ai_settings_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Guardar Configuración")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar")
      }
    }
  )
}
