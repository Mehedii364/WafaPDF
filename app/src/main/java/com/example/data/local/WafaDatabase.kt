package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AnnotationEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.FolderEntity
import com.example.data.model.PdfDocumentEntity

@Database(
  entities = [
    PdfDocumentEntity::class,
    FolderEntity::class,
    BookmarkEntity::class,
    AnnotationEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class WafaDatabase : RoomDatabase() {
  abstract fun pdfDao(): PdfDao

  companion object {
    @Volatile
    private var INSTANCE: WafaDatabase? = null

    fun getDatabase(context: Context): WafaDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          WafaDatabase::class.java,
          "wafa_pdf_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
