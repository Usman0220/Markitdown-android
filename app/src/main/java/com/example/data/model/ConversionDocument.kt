package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_documents")
data class ConversionDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val sourceFileName: String,
    val sourceFormat: String, // PDF, DOCX, XLSX, PPTX, HTML, CSV, JSON, XML, IMAGE, AUDIO, TEXT, ZIP, URL
    val fileSizeBytes: Long = 0,
    val markdownContent: String,
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val readingTimeMinutes: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val tags: String = ""
)
