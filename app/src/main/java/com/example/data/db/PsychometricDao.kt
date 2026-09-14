package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AssessmentSession
import com.example.data.model.ItemResponse
import com.example.data.model.PsychometricItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PsychometricDao {
  @Query("SELECT * FROM psychometric_items")
  suspend fun getAllItems(): List<PsychometricItem>

  @Query("SELECT COUNT(*) FROM psychometric_items")
  suspend fun getItemCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItems(items: List<PsychometricItem>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: AssessmentSession)

  @Query("SELECT * FROM assessment_sessions ORDER BY completedAt DESC")
  fun getAllSessionsFlow(): Flow<List<AssessmentSession>>

  @Query("SELECT * FROM assessment_sessions WHERE sessionId = :sessionId LIMIT 1")
  suspend fun getSessionById(sessionId: String): AssessmentSession?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItemResponses(responses: List<ItemResponse>)

  @Query("SELECT * FROM item_responses WHERE sessionId = :sessionId ORDER BY itemIndexOrder ASC")
  suspend fun getResponsesForSession(sessionId: String): List<ItemResponse>

  @Query("UPDATE assessment_sessions SET aiInterpretation = :aiInterpretation WHERE sessionId = :sessionId")
  suspend fun updateAiInterpretation(sessionId: String, aiInterpretation: String)

  @Query("DELETE FROM assessment_sessions WHERE sessionId = :sessionId")
  suspend fun deleteSession(sessionId: String)

  @Query("DELETE FROM item_responses WHERE sessionId = :sessionId")
  suspend fun deleteResponsesForSession(sessionId: String)

  @Query("SELECT DISTINCT itemId FROM item_responses ORDER BY id DESC LIMIT :limit")
  suspend fun getRecentlyAdministeredItemIds(limit: Int = 100): List<String>
}
