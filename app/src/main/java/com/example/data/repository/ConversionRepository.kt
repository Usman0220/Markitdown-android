package com.example.data.repository

import com.example.data.dao.ConversionDocumentDao
import com.example.data.model.ConversionDocument
import kotlinx.coroutines.flow.Flow

class ConversionRepository(private val dao: ConversionDocumentDao) {
    val allDocuments: Flow<List<ConversionDocument>> = dao.getAll()
    val favoriteDocuments: Flow<List<ConversionDocument>> = dao.getFavorites()

    fun searchDocuments(query: String): Flow<List<ConversionDocument>> = dao.search(query)

    suspend fun getDocumentById(id: Long): ConversionDocument? = dao.getById(id)

    suspend fun saveDocument(document: ConversionDocument): Long = dao.insert(document)

    suspend fun updateDocument(document: ConversionDocument) = dao.update(document)

    suspend fun deleteDocument(document: ConversionDocument) = dao.delete(document)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun toggleFavorite(document: ConversionDocument) {
        dao.update(document.copy(isFavorite = !document.isFavorite))
    }

    suspend fun clearHistory() = dao.clearAll()
}
