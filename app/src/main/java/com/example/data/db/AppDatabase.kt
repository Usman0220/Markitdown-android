package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ConversionDocumentDao
import com.example.data.model.ConversionDocument

@Database(entities = [ConversionDocument::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversionDocumentDao(): ConversionDocumentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "markitdown_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
