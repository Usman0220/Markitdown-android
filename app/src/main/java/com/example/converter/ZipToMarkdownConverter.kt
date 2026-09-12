package com.example.converter

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object ZipToMarkdownConverter {

    fun convert(inputStream: InputStream, archiveName: String = "Archive.zip"): String {
        val zip = ZipInputStream(inputStream)
        val sb = StringBuilder()
        sb.append("# Archive: $archiveName\n\n")

        val files = mutableListOf<Pair<String, ByteArray>>()

        try {
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && !entry.name.startsWith("__MACOSX") && !entry.name.startsWith(".")) {
                    val bytes = zip.readBytes()
                    files.add(entry.name to bytes)
                }
                entry = zip.nextEntry
            }
        } finally {
            zip.close()
        }

        if (files.isEmpty()) {
            sb.append("*(Archive is empty or contains no readable files)*\n")
            return sb.toString()
        }

        // Table of contents
        sb.append("## Table of Contents\n\n")
        files.forEachIndexed { index, (fileName, bytes) ->
            val sizeKb = String.format("%.1f KB", bytes.size / 1024.0)
            sb.append("${index + 1}. **`$fileName`** ($sizeKb)\n")
        }
        sb.append("\n---\n\n")

        // Convert individual files
        files.forEach { (fileName, bytes) ->
            val ext = fileName.substringAfterLast('.', "").lowercase()
            sb.append("## File: `$fileName`\n\n")

            val content = when (ext) {
                "docx" -> {
                    try {
                        DocxToMarkdownConverter.convert(ByteArrayInputStream(bytes))
                    } catch (e: Exception) {
                        "*(Error converting Word document: ${e.message})*"
                    }
                }
                "xlsx" -> {
                    try {
                        XlsxToMarkdownConverter.convert(ByteArrayInputStream(bytes))
                    } catch (e: Exception) {
                        "*(Error converting Excel spreadsheet: ${e.message})*"
                    }
                }
                "pptx" -> {
                    try {
                        PptxToMarkdownConverter.convert(ByteArrayInputStream(bytes))
                    } catch (e: Exception) {
                        "*(Error converting PowerPoint presentation: ${e.message})*"
                    }
                }
                "html", "htm" -> {
                    HtmlToMarkdownConverter.convert(String(bytes))
                }
                "csv", "tsv" -> {
                    CsvToMarkdownConverter.convert(String(bytes))
                }
                "json" -> {
                    JsonToMarkdownConverter.convert(String(bytes))
                }
                "xml" -> {
                    XmlToMarkdownConverter.convert(String(bytes))
                }
                "txt", "log", "md" -> {
                    String(bytes)
                }
                "py", "kt", "java", "js", "ts", "cpp", "c", "h", "cs", "go", "rs", "rb", "sh", "sql" -> {
                    "```$ext\n${String(bytes)}\n```"
                }
                else -> {
                    "*(Binary or unsupported format, ${bytes.size} bytes)*"
                }
            }

            sb.append(content).append("\n\n---\n\n")
        }

        return sb.toString().trim()
    }
}
