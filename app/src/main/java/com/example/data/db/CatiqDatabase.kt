package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AssessmentSession
import com.example.data.model.ItemResponse
import com.example.data.model.PsychometricItem

@Database(
  entities = [
    PsychometricItem::class,
    AssessmentSession::class,
    ItemResponse::class
  ],
  version = 2,
  exportSchema = false
)
abstract class CatiqDatabase : RoomDatabase() {
  abstract fun psychometricDao(): PsychometricDao

  companion object {
    @Volatile
    private var INSTANCE: CatiqDatabase? = null

    fun getDatabase(context: Context): CatiqDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          CatiqDatabase::class.java,
          "catiq_psychometric_db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
