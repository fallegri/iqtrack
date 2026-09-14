package com.example.data.bank

import com.example.data.model.PsychometricItem

/**
 * Banco Central de Reactivos Psicométricos Calibrados (IRT 3PL).
 * Contiene 105 reactivos distribuidos equitativamente en los 5 factores CHC:
 * - Gf: Razonamiento Fluido (21 reactivos)
 * - Gv: Procesamiento Visual-Espacial (21 reactivos)
 * - Gwm: Memoria de Trabajo Operativa (21 reactivos)
 * - Gs: Velocidad de Procesamiento Perceptivo (21 reactivos)
 * - Gc: Inteligencia Cristalizada y Semántica (21 reactivos)
 */
object PsychometricItemBank {
  val allItems: List<PsychometricItem> by lazy {
    GfItemBank.items +
      GvItemBank.items +
      GwmItemBank.items +
      GsItemBank.items +
      GcItemBank.items
  }

  val totalCount: Int get() = allItems.size
}
