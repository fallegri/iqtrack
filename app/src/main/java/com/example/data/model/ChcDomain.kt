package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ColorGc
import com.example.ui.theme.ColorGf
import com.example.ui.theme.ColorGs
import com.example.ui.theme.ColorGv
import com.example.ui.theme.ColorGwm

enum class ChcDomain(
  val code: String,
  val fullName: String,
  val shortDescription: String,
  val clinicalConcept: String,
  val color: Color
) {
  GF(
    code = "Gf",
    fullName = "Razonamiento Fluido",
    shortDescription = "Matrices inductivas y deducción abstracta",
    clinicalConcept = "Capacidad de resolver problemas novedosos e identificar patrones abstractos sin depender de aprendizaje previo.",
    color = ColorGf
  ),
  GV(
    code = "Gv",
    fullName = "Procesamiento Visual",
    shortDescription = "Rotación mental, patrones y simetría",
    clinicalConcept = "Habilidad para generar, almacenar, recuperar y transformar imágenes y representaciones espaciales visuales.",
    color = ColorGv
  ),
  GWM(
    code = "Gwm",
    fullName = "Memoria de Trabajo",
    shortDescription = "Retención y manipulación serial de secuencias",
    clinicalConcept = "Capacidad de mantener información activa en la conciencia y transformarla temporalmente bajo control atencional.",
    color = ColorGwm
  ),
  GS(
    code = "Gs",
    fullName = "Velocidad de Procesamiento",
    shortDescription = "Discriminación visual y fluidez temporal",
    clinicalConcept = "Rapidez y fluidez automática para realizar tareas cognitivas elementales con atención sostenida.",
    color = ColorGs
  ),
  GC(
    code = "Gc",
    fullName = "Inteligencia Cristalizada",
    shortDescription = "Analogías conceptuales y razonamiento semántico",
    clinicalConcept = "Conocimiento culturalmente adquirido, amplitud léxica y razonamiento verbal declarativo acumulado.",
    color = ColorGc
  );

  companion object {
    fun fromCode(code: String): ChcDomain {
      return entries.find { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) } ?: GF
    }
  }
}
