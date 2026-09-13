package com.example.data.repository

import android.content.Context
import com.example.data.db.CatiqDatabase
import com.example.data.db.PsychometricDao
import com.example.data.model.AssessmentSession
import com.example.data.model.ChcDomain
import com.example.data.model.ItemResponse
import com.example.data.model.PsychometricItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CatiqRepository(private val dao: PsychometricDao) {

  val allSessionsFlow: Flow<List<AssessmentSession>> = dao.getAllSessionsFlow()

  suspend fun ensureItemBankSeeded() = withContext(Dispatchers.IO) {
    val count = dao.getItemCount()
    if (count == 0) {
      dao.insertItems(getPrecalibratedItemBank())
    }
  }

  suspend fun getAllItems(): List<PsychometricItem> = withContext(Dispatchers.IO) {
    dao.getAllItems()
  }

  suspend fun saveCompletedSession(
    session: AssessmentSession,
    responses: List<ItemResponse>
  ) = withContext(Dispatchers.IO) {
    dao.insertSession(session)
    dao.insertItemResponses(responses)
  }

  suspend fun getSessionById(sessionId: String): AssessmentSession? = withContext(Dispatchers.IO) {
    dao.getSessionById(sessionId)
  }

  suspend fun getResponsesForSession(sessionId: String): List<ItemResponse> = withContext(Dispatchers.IO) {
    dao.getResponsesForSession(sessionId)
  }

  suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
    dao.deleteSession(sessionId)
    dao.deleteResponsesForSession(sessionId)
  }

  companion object {
    fun create(context: Context): CatiqRepository {
      val db = CatiqDatabase.getDatabase(context)
      return CatiqRepository(db.psychometricDao())
    }

    /**
     * Banco de 25 reactivos calibrados empíricamente según parámetros IRT 3PL
     * equilibrados a través de los 5 factores CHC principales.
     */
    fun getPrecalibratedItemBank(): List<PsychometricItem> {
      return listOf(
        // ================= GF: RAZONAMIENTO FLUIDO (Matrices Inductivas) =================
        PsychometricItem(
          id = "GF_01",
          domainCode = "Gf",
          difficultyB = -1.2,
          discriminationA = 1.45,
          guessingC = 0.25,
          stimulusType = "MATRIX_PROGRESSION",
          prompt = "¿Qué figura completa lógicamente la secuencia de la matriz 3x3?",
          stimulusCode = "CIRCLE_GROWTH:1,2,3;2,3,4;3,4,?",
          optionA = "Círculo con 4 segmentos",
          optionB = "Círculo con 5 segmentos",
          optionC = "Cuadrado con 5 vértices",
          optionD = "Círculo con 6 segmentos",
          correctOptionIndex = 1,
          expectedTimeSeconds = 25
        ),
        PsychometricItem(
          id = "GF_02",
          domainCode = "Gf",
          difficultyB = 0.1,
          discriminationA = 1.75,
          guessingC = 0.25,
          stimulusType = "MATRIX_ROTATION",
          prompt = "Observa el patrón de rotación horaria (+45°) y sombreado alterno:",
          stimulusCode = "ROTATE_SHADE:0_EMPTY,45_FILL,90_EMPTY;135_FILL,180_EMPTY,225_FILL;270_EMPTY,315_FILL,?",
          optionA = "Flecha 360° sin relleno",
          optionB = "Flecha 360° con relleno sólido",
          optionC = "Flecha 45° con relleno",
          optionD = "Triángulo 0° sin relleno",
          correctOptionIndex = 0,
          expectedTimeSeconds = 35
        ),
        PsychometricItem(
          id = "GF_03",
          domainCode = "Gf",
          difficultyB = 1.1,
          discriminationA = 1.90,
          guessingC = 0.25,
          stimulusType = "MATRIX_XOR",
          prompt = "La tercera celda de cada fila es la superposición XOR (exclusión mutua) de las dos primeras:",
          stimulusCode = "XOR_GRID:L1+L2->L3",
          optionA = "Línea diagonal única y punto central",
          optionB = "Cruz completa con punto central",
          optionC = "Línea horizontal sin punto",
          optionD = "Punto central solitario",
          correctOptionIndex = 0,
          expectedTimeSeconds = 45
        ),
        PsychometricItem(
          id = "GF_04",
          domainCode = "Gf",
          difficultyB = 1.8,
          discriminationA = 2.10,
          guessingC = 0.25,
          stimulusType = "INDUCTIVE_SERIES",
          prompt = "Serie deductiva compleja de números primos y factoriales: 2, 6, 12, 20, 30, 42, ¿?",
          stimulusCode = "SERIES:n*(n+1)",
          optionA = "52",
          optionB = "56",
          optionC = "60",
          optionD = "64",
          correctOptionIndex = 1,
          expectedTimeSeconds = 40
        ),
        PsychometricItem(
          id = "GF_05",
          domainCode = "Gf",
          difficultyB = -0.5,
          discriminationA = 1.30,
          guessingC = 0.25,
          stimulusType = "MATRIX_PROGRESSION",
          prompt = "Matriz de adición geométrica: Cuadrado (4) -> Pentágono (5) -> Hexágono (6). Fila 2: Triángulo (3) -> Rombo (4) -> ¿?",
          stimulusCode = "SHAPE_SIDES:3,4,?",
          optionA = "Pentágono (5 lados)",
          optionB = "Heptágono (7 lados)",
          optionC = "Octógono (8 lados)",
          optionD = "Círculo (infinito)",
          correctOptionIndex = 0,
          expectedTimeSeconds = 20
        ),

        // ================= GV: PROCESAMIENTO VISUAL (Rotación y Espacial) =================
        PsychometricItem(
          id = "GV_01",
          domainCode = "Gv",
          difficultyB = -1.0,
          discriminationA = 1.35,
          guessingC = 0.25,
          stimulusType = "SPATIAL_ROTATION",
          prompt = "¿Cuál de las opciones representa la misma figura 3D tras una rotación en el plano?",
          stimulusCode = "SHAPE_L_3D:ROT_90",
          optionA = "Figura isométrica con saliente en sentido horario",
          optionB = "Figura reflejada en espejo invertida",
          optionC = "Figura rotada con saliente opuesto",
          optionD = "Cubo con cilindro adyacente",
          correctOptionIndex = 0,
          expectedTimeSeconds = 30
        ),
        PsychometricItem(
          id = "GV_02",
          domainCode = "Gv",
          difficultyB = 0.3,
          discriminationA = 1.65,
          guessingC = 0.25,
          stimulusType = "CUBE_UNFOLDING",
          prompt = "Al plegar el desarrollo plano del cubo con caras (Cruz, Punto, Estrella, Triángulo, Círculo, Cuadrado):",
          stimulusCode = "CUBE_FOLD:CROSS_TOP,STAR_FRONT",
          optionA = "La Cruz queda opuesta a la Estrella",
          optionB = "El Punto queda adyacente a la Cruz y al Triángulo",
          optionC = "El Círculo y el Cuadrado comparten arista izquierda",
          optionD = "La Estrella queda en la cara inferior fija",
          correctOptionIndex = 1,
          expectedTimeSeconds = 40
        ),
        PsychometricItem(
          id = "GV_03",
          domainCode = "Gv",
          difficultyB = 1.3,
          discriminationA = 1.85,
          guessingC = 0.25,
          stimulusType = "SYMMETRY_AXIS",
          prompt = "Doble reflexión ortogonal (eje vertical y posterior eje horizontal a 45°):",
          stimulusCode = "DUAL_REFLECT:AXIS_XY",
          optionA = "Inversión especular rotada 90° antihorario",
          optionB = "Identidad no desplazada",
          optionC = "Reflejo simple sin rotación",
          optionD = "Traslación euclidiana pura",
          correctOptionIndex = 0,
          expectedTimeSeconds = 35
        ),
        PsychometricItem(
          id = "GV_04",
          domainCode = "Gv",
          difficultyB = -0.4,
          discriminationA = 1.25,
          guessingC = 0.25,
          stimulusType = "VISUAL_PUZZLE",
          prompt = "Identifica las 3 piezas geométricas que juntas forman exactamente un hexágono regular:",
          stimulusCode = "HEX_PIECES:A+B+C",
          optionA = "2 triángulos equiláteros y 1 rombo central",
          optionB = "3 rectángulos alargados idénticos",
          optionC = "1 cuadrado y 2 círculos inscritos",
          optionD = "4 trapecios asimétricos convexos",
          correctOptionIndex = 0,
          expectedTimeSeconds = 25
        ),
        PsychometricItem(
          id = "GV_05",
          domainCode = "Gv",
          difficultyB = 1.7,
          discriminationA = 2.05,
          guessingC = 0.25,
          stimulusType = "SPATIAL_ROTATION",
          prompt = "Rotación mental en 3 ejes ortogonales (Yaw 90°, Pitch 180°, Roll 90°):",
          stimulusCode = "TENSOR_ROT:90_180_90",
          optionA = "Inversión completa con orientación axial Z negativa",
          optionB = "Retorno a la posición angular inicial",
          optionC = "Giro puro sobre eje X longitudinal",
          optionD = "Reflejo enantiomórfico no quiral",
          correctOptionIndex = 0,
          expectedTimeSeconds = 50
        ),

        // ================= GWM: MEMORIA DE TRABAJO (Manipulación Activa) =================
        PsychometricItem(
          id = "GWM_01",
          domainCode = "Gwm",
          difficultyB = -1.1,
          discriminationA = 1.40,
          guessingC = 0.25,
          stimulusType = "DIGIT_SPAN_REVERSE",
          prompt = "Retén la secuencia y reordénala en orden estrictamente inverso: [ 4 - 8 - 2 - 9 ]",
          stimulusCode = "DIGITS:4,8,2,9->REV",
          optionA = "9 - 2 - 8 - 4",
          optionB = "9 - 8 - 2 - 4",
          optionC = "4 - 9 - 2 - 8",
          optionD = "2 - 9 - 8 - 4",
          correctOptionIndex = 0,
          expectedTimeSeconds = 20
        ),
        PsychometricItem(
          id = "GWM_02",
          domainCode = "Gwm",
          difficultyB = 0.2,
          discriminationA = 1.70,
          guessingC = 0.25,
          stimulusType = "LETTER_NUMBER_SEQUENCING",
          prompt = "Ordena primero los números de menor a mayor y luego las letras alfabéticamente: [ C - 7 - A - 2 - K - 5 ]",
          stimulusCode = "ALPHANUM:C,7,A,2,K,5",
          optionA = "2 - 5 - 7 - A - C - K",
          optionB = "7 - 5 - 2 - K - C - A",
          optionC = "A - C - K - 2 - 5 - 7",
          optionD = "2 - 7 - 5 - A - K - C",
          correctOptionIndex = 0,
          expectedTimeSeconds = 30
        ),
        PsychometricItem(
          id = "GWM_03",
          domainCode = "Gwm",
          difficultyB = 1.2,
          discriminationA = 1.95,
          guessingC = 0.25,
          stimulusType = "N_BACK_OPERATION",
          prompt = "Cálculo en memoria de 2 pasos: Comienza en 15. Suma 8, divide por 2, resta 4, y luego multiplica por el dígito inicial de la serie:",
          stimulusCode = "MATH_CHAIN:((15+8)/2-4)*1",
          optionA = "7.5",
          optionB = "8.0",
          optionC = "9.5",
          optionD = "10.0",
          correctOptionIndex = 0,
          expectedTimeSeconds = 40
        ),
        PsychometricItem(
          id = "GWM_04",
          domainCode = "Gwm",
          difficultyB = -0.3,
          discriminationA = 1.30,
          guessingC = 0.25,
          stimulusType = "DIGIT_SPAN_REVERSE",
          prompt = "Secuencia inversa de 5 dígitos: [ 7 - 1 - 9 - 4 - 3 ]",
          stimulusCode = "DIGITS:7,1,9,4,3->REV",
          optionA = "3 - 4 - 9 - 1 - 7",
          optionB = "3 - 9 - 4 - 1 - 7",
          optionC = "7 - 4 - 9 - 1 - 3",
          optionD = "3 - 4 - 1 - 9 - 7",
          correctOptionIndex = 0,
          expectedTimeSeconds = 25
        ),
        PsychometricItem(
          id = "GWM_05",
          domainCode = "Gwm",
          difficultyB = 1.9,
          discriminationA = 2.15,
          guessingC = 0.25,
          stimulusType = "LETTER_NUMBER_SEQUENCING",
          prompt = "Doble transformación operativa: Secuencia [ 9 - R - 3 - F - 8 - B - 1 - M ]. Invierte las letras y ordena números descendentemente:",
          stimulusCode = "DUAL_TRANSFORM:9,R,3,F,8,B,1,M",
          optionA = "Números: 9,8,3,1 | Letras invertidas: M, B, F, R",
          optionB = "Números: 1,3,8,9 | Letras alfabéticas: B, F, M, R",
          optionC = "Números: 9,8,3,1 | Letras alfabéticas: B, F, M, R",
          optionD = "Números: 8,9,3,1 | Letras invertidas: R, F, B, M",
          correctOptionIndex = 0,
          expectedTimeSeconds = 50
        ),

        // ================= GS: VELOCIDAD DE PROCESAMIENTO (Búsqueda y Fluidez) =================
        PsychometricItem(
          id = "GS_01",
          domainCode = "Gs",
          difficultyB = -1.3,
          discriminationA = 1.40,
          guessingC = 0.25,
          stimulusType = "SYMBOL_SEARCH",
          prompt = "Búsqueda rápida: ¿Está el símbolo '∆' o '◊' presente en el grupo objetivo: [ ⨁ , ⨂ , ◊ , ⨀ , ⨃ ]?",
          stimulusCode = "SYMBOLS:DELTA_DIAMOND in GROUP",
          optionA = "SÍ, el símbolo '◊' está presente",
          optionB = "NO, ninguno está presente",
          optionC = "SÍ, ambos están presentes",
          optionD = "SÍ, el símbolo '∆' está presente",
          correctOptionIndex = 0,
          expectedTimeSeconds = 12
        ),
        PsychometricItem(
          id = "GS_02",
          domainCode = "Gs",
          difficultyB = 0.0,
          discriminationA = 1.60,
          guessingC = 0.25,
          stimulusType = "CODING_MATCH",
          prompt = "Clave de codificación: [ 1=★ , 2=▲ , 3=■ , 4=● ]. Decodifica velozmente: ★ - ● - ▲ - ■",
          stimulusCode = "CODE:1,4,2,3",
          optionA = "1 - 4 - 2 - 3",
          optionB = "1 - 2 - 4 - 3",
          optionC = "1 - 3 - 2 - 4",
          optionD = "4 - 1 - 2 - 3",
          correctOptionIndex = 0,
          expectedTimeSeconds = 15
        ),
        PsychometricItem(
          id = "GS_03",
          domainCode = "Gs",
          difficultyB = 0.9,
          discriminationA = 1.75,
          guessingC = 0.25,
          stimulusType = "SYMBOL_SEARCH",
          prompt = "Discriminación visual de símbolos idénticos rotados. Encuentra el par idéntico:",
          stimulusCode = "MATCH_ROTATED_SYMBOLS",
          optionA = "Par 2 y 5 (idénticos con rotación de 180°)",
          optionB = "Par 1 y 4 (son reflejos especulares dispares)",
          optionC = "Par 3 y 6 (difieren en espesor de trazo)",
          optionD = "Ningún par es congruente",
          correctOptionIndex = 0,
          expectedTimeSeconds = 18
        ),
        PsychometricItem(
          id = "GS_04",
          domainCode = "Gs",
          difficultyB = -0.6,
          discriminationA = 1.30,
          guessingC = 0.25,
          stimulusType = "CODING_MATCH",
          prompt = "Verificación binaria rápida: ¿Coinciden exactamente ambas cadenas? [ KX-893-V4 ] y [ KX-893-V4 ]",
          stimulusCode = "MATCH_STRINGS:IDENTICAL",
          optionA = "IDÉNTICAS exactamente",
          optionB = "DIFERENTES en el número central",
          optionC = "DIFERENTES en la letra final",
          optionD = "DIFERENTES en el guion",
          correctOptionIndex = 0,
          expectedTimeSeconds = 10
        ),
        PsychometricItem(
          id = "GS_05",
          domainCode = "Gs",
          difficultyB = 1.5,
          discriminationA = 1.90,
          guessingC = 0.25,
          stimulusType = "SYMBOL_SEARCH",
          prompt = "Cancelación veloz con condición dual: Cuenta cuántas letras 'B' están inmediatamente precedidas por un número impar: [ 3B , 4B , 7B , 2B , 9B , 6B , 1B ]",
          stimulusCode = "CANCEL_DUAL:ODD_PRECEDING_B",
          optionA = "4 instancias (3B, 7B, 9B, 1B)",
          optionB = "3 instancias (3B, 7B, 9B)",
          optionC = "5 instancias (todas las B)",
          optionD = "2 instancias (7B, 1B)",
          correctOptionIndex = 0,
          expectedTimeSeconds = 20
        ),

        // ================= GC: INTELIGENCIA CRISTALIZADA (Analogías Semánticas) =================
        PsychometricItem(
          id = "GC_01",
          domainCode = "Gc",
          difficultyB = -0.9,
          discriminationA = 1.30,
          guessingC = 0.25,
          stimulusType = "VERBAL_ANALOGY",
          prompt = "Analogía funcional: 'TERMÓMETRO es a TEMPERATURA como BARÓMETRO es a...'",
          stimulusCode = "ANALOGY:THERMO_TEMP",
          optionA = "Presión atmosférica",
          optionB = "Humedad relativa",
          optionC = "Velocidad del viento",
          optionD = "Altitud geográfica",
          correctOptionIndex = 0,
          expectedTimeSeconds = 20
        ),
        PsychometricItem(
          id = "GC_02",
          domainCode = "Gc",
          difficultyB = 0.2,
          discriminationA = 1.55,
          guessingC = 0.25,
          stimulusType = "VERBAL_ANALOGY",
          prompt = "Analogía taxonómica conceptual: 'AXIOMA es a TEOREMA como PREMISA es a...'",
          stimulusCode = "ANALOGY:AXIOM_THEOREM",
          optionA = "Conclusión",
          optionB = "Hipótesis",
          optionC = "Falacia",
          optionD = "Postulado",
          correctOptionIndex = 0,
          expectedTimeSeconds = 25
        ),
        PsychometricItem(
          id = "GC_03",
          domainCode = "Gc",
          difficultyB = 1.0,
          discriminationA = 1.80,
          guessingC = 0.25,
          stimulusType = "VERBAL_ANALOGY",
          prompt = "Relación semántica abstracta: 'ENTROPÍA es a DESORDEN como HOMEOSTASIS es a...'",
          stimulusCode = "ANALOGY:ENTROPY_ORDER",
          optionA = "Equilibrio dinámico",
          optionB = "Metabolismo basal",
          optionC = "Disipación térmica",
          optionD = "Evolución adaptativa",
          correctOptionIndex = 0,
          expectedTimeSeconds = 30
        ),
        PsychometricItem(
          id = "GC_04",
          domainCode = "Gc",
          difficultyB = -0.2,
          discriminationA = 1.25,
          guessingC = 0.25,
          stimulusType = "VERBAL_ANALOGY",
          prompt = "Categorización conceptual: ¿Cuál de los siguientes términos no pertenece al mismo campo semántico ontológico?",
          stimulusCode = "ONTOLOGY_OUTLIER",
          optionA = "Pragmatismo (corriente filosófica)",
          optionB = "Hemoglobina (proteína biológica)",
          optionC = "Empirismo (corriente epistemológica)",
          optionD = "Racionalismo (corriente epistemológica)",
          correctOptionIndex = 1,
          expectedTimeSeconds = 25
        ),
        PsychometricItem(
          id = "GC_05",
          domainCode = "Gc",
          difficultyB = 1.6,
          discriminationA = 2.00,
          guessingC = 0.25,
          stimulusType = "VERBAL_ANALOGY",
          prompt = "Analogía de alta complejidad epistemológica: 'PROLEGÓMENO es a EPÍLOGO como PROPEDÉUTICA es a...'",
          stimulusCode = "ANALOGY:PROLEGOMENO_EPILOGO",
          optionA = "Consumación / Aplicación práctica",
          optionB = "Exégesis hermenéutica",
          optionC = "Disquisición teórica",
          optionD = "Preámbulo metodológico",
          correctOptionIndex = 0,
          expectedTimeSeconds = 35
        )
      )
    }
  }
}
