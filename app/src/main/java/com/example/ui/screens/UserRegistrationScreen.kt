package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.SecondaryTealDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserRegistrationScreen(
  currentProfile: UserProfile,
  onSaveProfile: (UserProfile) -> Unit,
  onCancel: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var fullName by remember { mutableStateOf(currentProfile.fullName) }
  var ageText by remember { mutableStateOf(if (currentProfile.age > 0) currentProfile.age.toString() else "25") }
  var educationLevel by remember { mutableStateOf(currentProfile.educationLevel) }
  var gender by remember { mutableStateOf(currentProfile.gender) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val educationOptions = listOf(
    "Secundaria",
    "Técnico",
    "Universitario",
    "Posgrado / Maestría"
  )

  val genderOptions = listOf(
    "Femenino",
    "Masculino",
    "Otro / No binario",
    "Prefiero no decir"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 20.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(14.dp))

    // Cabecera
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (onCancel != null) {
        IconButton(
          onClick = onCancel,
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
      }

      Column {
        Text(
          text = if (currentProfile.isRegistered) "PERFIL DE EVALUADO" else "REGISTRO INICIAL",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Black,
          color = PrimaryBlueDark,
          letterSpacing = 2.sp
        )
        Text(
          text = if (currentProfile.isRegistered) "Actualizar Datos Personales" else "Registro de Datos Personales",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Tarjeta explicativa
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
          ),
          RoundedCornerShape(18.dp)
        )
        .border(1.dp, PrimaryBlueDark.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
        .padding(16.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .background(PrimaryBlueDark.copy(alpha = 0.25f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = PrimaryBlueDark,
            modifier = Modifier.size(24.dp)
          )
        }
        Column {
          Text(
            text = "Estandarización Psicométrica",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Los datos de edad y nivel educativo se emplean para la correcta baremación de percentiles normativos y el informe clínico ipsativo.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1),
            lineHeight = 16.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Campo: Nombre Completo
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlueDark, modifier = Modifier.size(20.dp))
          Text("Nombre Completo o Alias", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = fullName,
          onValueChange = {
            fullName = it
            errorMessage = null
          },
          placeholder = { Text("Ej: Dr. Carlos Mendoza / Andrea Gómez") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_full_name"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlueDark,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Campo: Edad Cronológica
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Badge, contentDescription = null, tint = PrimaryBlueDark, modifier = Modifier.size(20.dp))
          Text("Edad Cronológica (Años)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = ageText,
          onValueChange = {
            if (it.length <= 3 && it.all { char -> char.isDigit() }) {
              ageText = it
              errorMessage = null
            }
          },
          placeholder = { Text("Ej: 28") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_age"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlueDark,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Campo: Nivel Educativo
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.School, contentDescription = null, tint = PrimaryBlueDark, modifier = Modifier.size(20.dp))
          Text("Nivel Educativo Máximo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          educationOptions.forEach { option ->
            val isSelected = (educationLevel == option)
            Box(
              modifier = Modifier
                .background(
                  if (isSelected) PrimaryBlueDark else MaterialTheme.colorScheme.surface,
                  RoundedCornerShape(20.dp)
                )
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) PrimaryBlueDark else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                  shape = RoundedCornerShape(20.dp)
                )
                .clickable { educationLevel = option }
                .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Text(
                text = option,
                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Campo: Género
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Género / Identidad", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          genderOptions.forEach { option ->
            val isSelected = (gender == option)
            Box(
              modifier = Modifier
                .background(
                  if (isSelected) SecondaryTealDark else MaterialTheme.colorScheme.surface,
                  RoundedCornerShape(20.dp)
                )
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) SecondaryTealDark else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                  shape = RoundedCornerShape(20.dp)
                )
                .clickable { gender = option }
                .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Text(
                text = option,
                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }

    if (errorMessage != null) {
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = errorMessage ?: "",
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Botón Guardar
    Button(
      onClick = {
        val parsedAge = ageText.toIntOrNull()
        if (fullName.trim().isEmpty()) {
          errorMessage = "Por favor ingresa tu nombre o identificación."
        } else if (parsedAge == null || parsedAge < 6 || parsedAge > 120) {
          errorMessage = "Por favor ingresa una edad válida (entre 6 y 120 años)."
        } else {
          val newProfile = UserProfile(
            fullName = fullName.trim(),
            age = parsedAge,
            educationLevel = educationLevel,
            gender = gender,
            isRegistered = true
          )
          onSaveProfile(newProfile)
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("save_user_profile_button"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueDark)
    ) {
      Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (currentProfile.isRegistered) "Actualizar Perfil" else "Guardar y Continuar a Evaluaciones",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.Black
      )
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
