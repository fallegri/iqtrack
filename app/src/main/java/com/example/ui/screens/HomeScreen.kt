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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.ai.AiConsultantConfig
import com.example.data.model.AssessmentSession
import com.example.data.model.TestBattery
import com.example.data.model.UserProfile
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
  userProfile: UserProfile,
  selectedBattery: TestBattery,
  aiConfig: AiConsultantConfig,
  onSelectBattery: (TestBattery) -> Unit,
  onStartAssessment: () -> Unit,
  onViewLatestSession: () -> Unit,
  onViewAbout: () -> Unit,
  onEditProfile: () -> Unit,
  onOpenAiSettings: () -> Unit,
  onSelectSession: (AssessmentSession) -> Unit,
  modifier: Modifier = Modifier
) {
  val hasSessions = sessions.isNotEmpty()
  val latestSession = sessions.maxByOrNull { it.completedAt }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Barra de usuario / perfil y acceso rápido
    item {
      Spacer(modifier = Modifier.height(8.dp))
      UserProfileBar(
        profile = userProfile,
        onEditClick = onEditProfile
      )
    }

    // 2. Hero Banner principal con botón "Acerca de"
    item {
      HeroBanner(
        selectedBattery = selectedBattery,
        onStartClick = onStartAssessment,
        onAboutClick = onViewAbout
      )
    }

    // 3. Menú de acción rápida: Ver Último Test y Registrar Datos
    item {
      QuickActionsRow(
        hasLatestSession = hasSessions,
        onViewLatestClick = onViewLatestSession,
        onAboutClick = onViewAbout,
        onEditProfileClick = onEditProfile
      )
    }

    // 3.5 Banner de Consultor IA y Configuración de Modelos
    item {
      AiConsultantHomeBanner(
        aiConfig = aiConfig,
        onConfigureClick = onOpenAiSettings
      )
    }

    // 4. Selector de Batería de Test Psicométrico (Alternativas a realizar)
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Elegir Tipo de Evaluación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${TestBattery.entries.size} baterías",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(TestBattery.entries) { battery ->
            TestBatteryCard(
              battery = battery,
              isSelected = battery == selectedBattery,
              onSelect = { onSelectBattery(battery) }
            )
          }
        }
      }
    }

    // 5. Resumen del motor adaptativo EAP y norma CHC
    item {
      AdaptiveEngineOverviewCard()
    }

    // 6. Historial de Evaluaciones
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Historial de Evaluaciones",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${sessions.size} realizadas",
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
        val isLatest = session.sessionId == latestSession?.sessionId
        SessionHistoryCard(
          session = session,
          isLatest = isLatest,
          onClick = { onSelectSession(session) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun UserProfileBar(
  profile: UserProfile,
  onEditClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
      .clickable { onEditClick() }
      .padding(horizontal = 14.dp, vertical = 10.dp)
      .testTag("user_profile_bar"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .background(PrimaryBlueDark.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = null,
          tint = PrimaryBlueDark,
          modifier = Modifier.size(20.dp)
        )
      }
      Column {
        Text(
          text = if (profile.fullName.isNotBlank()) profile.fullName else "Evaluado Sin Registrar",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (profile.age > 0) "${profile.age} años • ${profile.educationLevel}" else "Toca para registrar tus datos personales",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp
        )
      }
    }

    Icon(
      imageVector = Icons.Default.Edit,
      contentDescription = "Editar perfil",
      tint = PrimaryBlueDark,
      modifier = Modifier.size(18.dp)
    )
  }
}

@Composable
private fun HeroBanner(
  selectedBattery: TestBattery,
  onStartClick: () -> Unit,
  onAboutClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        Brush.linearGradient(
          colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
        ),
        RoundedCornerShape(22.dp)
      )
      .border(1.dp, PrimaryBlueDark.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
      .padding(20.dp)
  ) {
    Column {
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
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = PrimaryBlueDark,
            modifier = Modifier.size(22.dp)
          )
        }
        Column {
          Text(
            text = "CAT-IQ MOBILE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = PrimaryBlueDark,
            letterSpacing = 2.sp
          )
          Text(
            text = "Evaluación Adaptativa",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Batería activa: ${selectedBattery.displayName}. Motor IRT 3PL on-device con selección estocástica y fiabilidad r_xx ≥ 0.91.",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFFCBD5E1),
        lineHeight = 18.sp
      )

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onStartClick,
          modifier = Modifier
            .weight(1.3f)
            .height(48.dp)
            .testTag("start_test_button"),
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueDark),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Iniciar Test", fontWeight = FontWeight.Bold, color = Color.Black)
        }

        OutlinedButton(
          onClick = onAboutClick,
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("view_about_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
        ) {
          Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Acerca de", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
private fun QuickActionsRow(
  hasLatestSession: Boolean,
  onViewLatestClick: () -> Unit,
  onAboutClick: () -> Unit,
  onEditProfileClick: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Card(
      modifier = Modifier
        .weight(1f)
        .clickable(enabled = hasLatestSession) { onViewLatestClick() }
        .testTag("menu_latest_test_button"),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (hasLatestSession) {
          MaterialTheme.colorScheme.surfaceVariant
        } else {
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }
      ),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        if (hasLatestSession) SecondaryTealDark.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
      )
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(SecondaryTealDark.copy(alpha = 0.2f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = if (hasLatestSession) SecondaryTealDark else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
        Column {
          Text(
            text = "Último Test",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (hasLatestSession) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (hasLatestSession) "Ver informe" else "Sin tests",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Card(
      modifier = Modifier
        .weight(1f)
        .clickable { onEditProfileClick() }
        .testTag("menu_user_data_button"),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlueDark.copy(alpha = 0.3f))
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(PrimaryBlueDark.copy(alpha = 0.2f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = PrimaryBlueDark,
            modifier = Modifier.size(18.dp)
          )
        }
        Column {
          Text(
            text = "Mis Datos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Baremos edad",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun TestBatteryCard(
  battery: TestBattery,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  Box(
    modifier = Modifier
      .width(180.dp)
      .background(
        if (isSelected) battery.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        RoundedCornerShape(16.dp)
      )
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) battery.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp)
      )
      .clickable { onSelect() }
      .padding(14.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .background(battery.color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = battery.durationMinutes,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = battery.color
          )
        }
        if (isSelected) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = battery.color,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = battery.displayName,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = battery.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        maxLines = 2,
        lineHeight = 14.sp
      )
    }
  }
}

@Composable
private fun AdaptiveEngineOverviewCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.Speed, contentDescription = null, tint = SecondaryTealDark, modifier = Modifier.size(20.dp))
        Text(
          text = "Eficiencia del Motor Adaptativo",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "La convergencia adaptativa de Fisher reduce la duración de la prueba en un 60% respecto a pruebas clásicas en papel, finalizando automáticamente al alcanzar un SE ≤ 0.30.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 17.sp
      )
    }
  }
}

@Composable
private fun EmptySessionsCard(onStartClick: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .background(PrimaryBlueDark.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Analytics, contentDescription = null, tint = PrimaryBlueDark, modifier = Modifier.size(28.dp))
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "No hay evaluaciones registradas aún",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Selecciona una batería e inicia tu primera prueba psicométrica.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun SessionHistoryCard(
  session: AssessmentSession,
  isLatest: Boolean = false,
  onClick: () -> Unit
) {
  val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
  val formattedDate = dateFormat.format(Date(session.completedAt))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("session_card_${session.sessionId}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = if (isLatest) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlueDark) else null
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .background(
              if (session.fullScaleIq >= 115) MetricGreen.copy(alpha = 0.2f) else PrimaryBlueDark.copy(alpha = 0.2f),
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "${session.fullScaleIq.toInt()}",
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            color = if (session.fullScaleIq >= 115) MetricGreen else PrimaryBlueDark
          )
        }

        Column {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "CI Escala Completa",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleSmall
            )
            if (isLatest) {
              Box(
                modifier = Modifier
                  .background(PrimaryBlueDark, RoundedCornerShape(6.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text("ÚLTIMO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
              }
            }
          }
          Text(
            text = "$formattedDate • ${session.totalItems} reactivos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
          Text(
            text = "Percentil ${session.percentileRank.toInt()}% • SE ${session.standardError}",
            style = MaterialTheme.typography.bodySmall,
            color = PrimaryBlueDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
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

@Composable
private fun AiConsultantHomeBanner(
  aiConfig: AiConsultantConfig,
  onConfigureClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onConfigureClick() }
      .testTag("ai_home_banner"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlueDark.copy(alpha = 0.35f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .background(
              Brush.linearGradient(
                colors = listOf(PrimaryBlueDark, Color(0xFF673AB7))
              ),
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }

        Column {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Consultor IA Psicométrico",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Box(
              modifier = Modifier
                .background(MetricGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
              Text(
                text = aiConfig.selectedProvider.displayName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MetricGreen
              )
            }
          }
          Text(
            text = "Modo ${aiConfig.testLengthMode.title} • Gemini, ChatGPT, Claude, NVIDIA",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }

      IconButton(
        onClick = onConfigureClick,
        modifier = Modifier.testTag("configure_ai_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Configurar IA",
          tint = PrimaryBlueDark
        )
      }
    }
  }
}

