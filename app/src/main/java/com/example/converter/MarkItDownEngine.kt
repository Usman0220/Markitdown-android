package com.example.converter

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.ai.GeminiMarkItDownService
import com.example.data.model.ConversionDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

object MarkItDownEngine {

    suspend fun convertUri(
        context: Context,
        uri: Uri,
        fileName: String,
        geminiService: GeminiMarkItDownService
    ): ConversionDocument = withContext(Dispatchers.IO) {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val mimeType = context.contentResolver.getType(uri) ?: ""

        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Unable to open input stream for $uri")

        val fileSizeBytes = try {
            context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
        } catch (e: Exception) {
            0L
        }

        val format = detectFormat(fileName, mimeType)
        val markdown = convertStream(context, stream, format, fileName, geminiService)

        val wordCount = countWords(markdown)
        val charCount = markdown.length
        val readingTime = (wordCount / 200).coerceAtLeast(1)

        val cleanTitle = fileName.substringBeforeLast('.').ifBlank { "Converted Document" }

        ConversionDocument(
            title = cleanTitle,
            sourceFileName = fileName,
            sourceFormat = format,
            fileSizeBytes = fileSizeBytes,
            markdownContent = markdown,
            wordCount = wordCount,
            charCount = charCount,
            readingTimeMinutes = readingTime,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun convertRawText(
        text: String,
        formatHint: String = "AUTO",
        title: String = "Pasted Content"
    ): ConversionDocument = withContext(Dispatchers.Default) {
        val trimmed = text.trim()
        val format = if (formatHint == "AUTO") detectTextFormat(trimmed) else formatHint

        val markdown = when (format) {
            "HTML" -> HtmlToMarkdownConverter.convert(trimmed)
            "CSV" -> CsvToMarkdownConverter.convert(trimmed)
            "JSON" -> JsonToMarkdownConverter.convert(trimmed)
            "XML" -> XmlToMarkdownConverter.convert(trimmed)
            else -> trimmed
        }

        val wordCount = countWords(markdown)
        val charCount = markdown.length
        val readingTime = (wordCount / 200).coerceAtLeast(1)

        ConversionDocument(
            title = title,
            sourceFileName = "$title.${format.lowercase()}",
            sourceFormat = format,
            fileSizeBytes = text.toByteArray().size.toLong(),
            markdownContent = markdown,
            wordCount = wordCount,
            charCount = charCount,
            readingTimeMinutes = readingTime,
            timestamp = System.currentTimeMillis()
        )
    }

    private suspend fun convertStream(
        context: Context,
        stream: InputStream,
        format: String,
        fileName: String,
        geminiService: GeminiMarkItDownService
    ): String {
        return try {
            when (format) {
                "DOCX" -> stream.use { DocxToMarkdownConverter.convert(it) }
                "XLSX" -> stream.use { XlsxToMarkdownConverter.convert(it) }
                "PPTX" -> stream.use { PptxToMarkdownConverter.convert(it) }
                "PDF" -> stream.use { PdfToMarkdownConverter.convert(context, it, fileName, geminiService) }
                "HTML" -> {
                    val content = stream.bufferedReader().use { it.readText() }
                    HtmlToMarkdownConverter.convert(content)
                }
                "CSV" -> {
                    val content = stream.bufferedReader().use { it.readText() }
                    CsvToMarkdownConverter.convert(content)
                }
                "JSON" -> {
                    val content = stream.bufferedReader().use { it.readText() }
                    JsonToMarkdownConverter.convert(content)
                }
                "XML" -> {
                    val content = stream.bufferedReader().use { it.readText() }
                    XmlToMarkdownConverter.convert(content)
                }
                "ZIP" -> stream.use { ZipToMarkdownConverter.convert(it, fileName) }
                "IMAGE" -> {
                    val bitmap = stream.use { BitmapFactory.decodeStream(it) }
                    if (bitmap != null) {
                        geminiService.transcribeImage(bitmap)
                    } else {
                        "# Image\n\n*(Failed to decode image data)*"
                    }
                }
                "AUDIO" -> {
                    val bytes = stream.use { it.readBytes() }
                    val mime = when (fileName.substringAfterLast('.').lowercase()) {
                        "wav" -> "audio/wav"
                        "ogg" -> "audio/ogg"
                        "flac" -> "audio/flac"
                        "m4a" -> "audio/mp4"
                        else -> "audio/mp3"
                    }
                    geminiService.transcribeAudio(bytes, mime)
                }
                else -> {
                    // Plain text / Code / Logs
                    val content = stream.bufferedReader().use { it.readText() }
                    val ext = fileName.substringAfterLast('.', "").lowercase()
                    if (ext in listOf("py", "kt", "js", "ts", "java", "cpp", "c", "cs", "go", "rs", "sql", "sh", "yaml", "yml")) {
                        "```$ext\n$content\n```"
                    } else {
                        content
                    }
                }
            }
        } catch (e: Exception) {
            "# Error Converting $fileName\n\n*(Conversion error: ${e.message ?: "Unknown error"})*"
        }
    }

    fun detectFormat(fileName: String, mimeType: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when {
            ext == "docx" || mimeType.contains("wordprocessingml") -> "DOCX"
            ext == "xlsx" || ext == "xls" || mimeType.contains("spreadsheetml") -> "XLSX"
            ext == "pptx" || ext == "ppt" || mimeType.contains("presentationml") -> "PPTX"
            ext == "pdf" || mimeType.contains("pdf") -> "PDF"
            ext in listOf("html", "htm") || mimeType.contains("html") -> "HTML"
            ext in listOf("csv", "tsv") || mimeType.contains("csv") -> "CSV"
            ext == "json" || mimeType.contains("json") -> "JSON"
            ext == "xml" || mimeType.contains("xml") -> "XML"
            ext == "zip" || mimeType.contains("zip") -> "ZIP"
            ext in listOf("png", "jpg", "jpeg", "webp", "bmp", "gif") || mimeType.startsWith("image/") -> "IMAGE"
            ext in listOf("mp3", "wav", "m4a", "ogg", "flac") || mimeType.startsWith("audio/") -> "AUDIO"
            else -> "TEXT"
        }
    }

    private fun detectTextFormat(text: String): String {
        val trimmed = text.trim()
        return when {
            trimmed.startsWith("<html", ignoreCase = true) || trimmed.contains("</p>") || trimmed.contains("</div>") || trimmed.contains("</h1>") -> "HTML"
            (trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]")) -> "JSON"
            trimmed.startsWith("<?xml") || (trimmed.startsWith("<") && trimmed.endsWith(">") && !trimmed.contains(" ")) -> "XML"
            trimmed.lines().size > 1 && trimmed.lines().first().contains(",") && trimmed.lines().getOrNull(1)?.contains(",") == true -> "CSV"
            else -> "TEXT"
        }
    }

    fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
}
