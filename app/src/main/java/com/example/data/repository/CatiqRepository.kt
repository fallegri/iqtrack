package com.example.data.repository

import android.content.Context
import com.example.data.bank.PsychometricItemBank
import com.example.data.db.CatiqDatabase
import com.example.data.db.PsychometricDao
import com.example.data.model.AssessmentSession
import com.example.data.model.ItemResponse
import com.example.data.model.PsychometricItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CatiqRepository(private val dao: PsychometricDao) {

  val allSessionsFlow: Flow<List<AssessmentSession>> = dao.getAllSessionsFlow()

  suspend fun ensureItemBankSeeded() = withContext(Dispatchers.IO) {
    val count = dao.getItemCount()
    val bank = getPrecalibratedItemBank()
    if (count < bank.size) {
      dao.insertItems(bank)
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

  suspend fun updateAiInterpretation(sessionId: String, interpretation: String) = withContext(Dispatchers.IO) {
    dao.updateAiInterpretation(sessionId, interpretation)
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

  suspend fun getRecentlyAdministeredItemIds(limit: Int = 100): List<String> = withContext(Dispatchers.IO) {
    dao.getRecentlyAdministeredItemIds(limit)
  }

  companion object {
    fun create(context: Context): CatiqRepository {
      val db = CatiqDatabase.getDatabase(context)
      return CatiqRepository(db.psychometricDao())
    }

    /**
     * Retorna el banco completo de 105 reactivos psicométricos calibrados IRT 3PL.
     */
    fun getPrecalibratedItemBank(): List<PsychometricItem> {
      return PsychometricItemBank.allItems
    }
  }
}
