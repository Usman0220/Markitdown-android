package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ConversionDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversionDocumentDao {
    @Query("SELECT * FROM conversion_documents ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ConversionDocument>>

    @Query("SELECT * FROM conversion_documents WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<ConversionDocument>>

    @Query("SELECT * FROM conversion_documents WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ConversionDocument?

    @Query("SELECT * FROM conversion_documents WHERE sourceFormat = :format ORDER BY timestamp DESC")
    fun getByFormat(format: String): Flow<List<ConversionDocument>>

    @Query("SELECT * FROM conversion_documents WHERE title LIKE '%' || :query || '%' OR markdownContent LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<ConversionDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: ConversionDocument): Long

    @Update
    suspend fun update(document: ConversionDocument)

    @Delete
    suspend fun delete(document: ConversionDocument)

    @Query("DELETE FROM conversion_documents WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM conversion_documents")
    suspend fun clearAll()
}
