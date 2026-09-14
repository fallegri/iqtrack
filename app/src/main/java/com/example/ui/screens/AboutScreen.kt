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
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ColorGf
import com.example.ui.theme.MetricGreen
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.SecondaryTealDark

@Composable
fun AboutScreen(
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
            .testTag("back_from_about")
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
            text = "INFORMACIÓN TÉCNICA",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlueDark,
            letterSpacing = 1.sp
          )
          Text(
            text = "Acerca de CAT-IQ",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    // Sección 1: Descripción del Producto
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(PrimaryBlueDark.copy(alpha = 0.2f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = PrimaryBlueDark,
                modifier = Modifier.size(20.dp)
              )
            }
            Text(
              text = "Descripción del Producto",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "CAT-IQ es una aplicación móvil de evaluación psicométrica adaptativa (Computerized Adaptive Testing) diseñada para medir el Cociente Intelectual (CI) y las capacidades cognitivas individuales con máximo rigor metodológico.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          ProductHighlight(
            icon = Icons.Default.Psychology,
            title = "Taxonomía Cognitiva CHC",
            desc = "Estructurado bajo la Teoría Cattell-Horn-Carroll, evaluando Razonamiento Fluido (Gf), Procesamiento Visual (Gv), Memoria de Trabajo (Gwm), Velocidad (Gs) e Inteligencia Cristalizada (Gc)."
          )

          Spacer(modifier = Modifier.height(8.dp))

          ProductHighlight(
            icon = Icons.Default.Security,
            title = "Privacidad y Procesamiento Nativo On-Device",
            desc = "Todo el motor matemático de calibración, cálculo de información y almacenamiento se ejecuta localmente en el dispositivo sin enviar datos personales a servidores externos."
          )

          Spacer(modifier = Modifier.height(8.dp))

          ProductHighlight(
            icon = Icons.Default.AutoGraph,
            title = "Adaptabilidad en Tiempo Real",
            desc = "El sistema ajusta la dificultad de cada reactivo en función de la trayectoria de aciertos y errores del evaluado, reduciendo significativamente el tiempo de prueba sin perder precisión."
          )
        }
      }
    }

    // Sección 2: Cálculo y Estimación del CI
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(SecondaryTealDark.copy(alpha = 0.2f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = SecondaryTealDark,
                modifier = Modifier.size(20.dp)
              )
            }
            Text(
              text = "Metodología de Cálculo del CI",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "El cálculo del Cociente Intelectual se realiza mediante la Teoría de Respuesta al Ítem (IRT) y modelos bayesianos estandarizados:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          CalculationStepCard(
            stepNumber = "1",
            title = "Modelo Logístico Triparamétrico (IRT 3PL)",
            formula = "P_i(θ) = c_i + (1 - c_i) / (1 + e^(-1.702 · a_i · (θ - b_i)))",
            description = "Modela la probabilidad de acierto de cada reactivo en función del parámetro de habilidad θ, la discriminación del reactivo (a), la dificultad calibrada (b) y la tasa de pseudo-adivinación (c)."
          )

          Spacer(modifier = Modifier.height(10.dp))

          CalculationStepCard(
            stepNumber = "2",
            title = "Función de Información de Fisher",
            formula = "I(θ) = ∑ [(1.702·a_i)² · (P_i - c_i)² · (1 - P_i)] / [(1 - c_i)² · P_i]",
            description = "Determina cuánta precisión métrica aporta cada reactivo administrado en el nivel de habilidad θ actual del evaluado. Permite seleccionar dinámicamente el reactivo que maximiza la información."
          )

          Spacer(modifier = Modifier.height(10.dp))

          CalculationStepCard(
            stepNumber = "3",
            title = "Estimación Bayesiana EAP (Expected A Posteriori)",
            formula = "θ_EAP = ∫ θ · L(θ) · p(θ) dθ / ∫ L(θ) · p(θ) dθ",
            description = "La habilidad latente se calcula integrando la función de verosimilitud de las respuestas con una distribución a priori N(0, 1) mediante 41 nodos de cuadratura numérica de Gauss-Hermite."
          )

          Spacer(modifier = Modifier.height(10.dp))

          CalculationStepCard(
            stepNumber = "4",
            title = "Transformación a Escala Estándar Wechsler",
            formula = "CI = 100 + 15 · θ",
            description = "La métrica θ (media = 0, desviación estándar = 1) se transforma linealmente a la escala clínica universal de CI (media = 100, DT = 15), con rango normal entre 85 y 115."
          )

          Spacer(modifier = Modifier.height(10.dp))

          CalculationStepCard(
            stepNumber = "5",
            title = "Intervalo de Confianza al 95%",
            formula = "IC_95% = CI ± (1.96 · 15 · SE(θ))",
            description = "El Error Estándar de Estimación SE(θ) = 1 / √I(θ) define el margen de error empírico de la puntuación obtenida, garantizando rigor psicométrico."
          )

          Spacer(modifier = Modifier.height(10.dp))

          CalculationStepCard(
            stepNumber = "6",
            title = "Criterio de Parada Adaptativo",
            formula = "SE(θ) ≤ 0.30  ⇒  Fiabilidad r_xx ≥ 0.91",
            description = "La prueba concluye de forma óptima tan pronto como el error estándar se reduce a 0.30 o se alcanza el límite de reactivos, optimizando la precisión sin inducir fatiga cognitiva."
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun ProductHighlight(
  icon: ImageVector,
  title: String,
  desc: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
      .padding(12.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = PrimaryBlueDark,
      modifier = Modifier
        .size(20.dp)
        .padding(top = 2.dp)
    )
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 16.sp
      )
    }
  }
}

@Composable
private fun CalculationStepCard(
  stepNumber: String,
  title: String,
  formula: String,
  description: String
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
      .padding(14.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .background(SecondaryTealDark.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = stepNumber,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = SecondaryTealDark
        )
      }
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
      Text(
        text = formula,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = PrimaryBlueDark
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = description,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      lineHeight = 16.sp
    )
  }
}
