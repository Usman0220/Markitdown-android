package com.example.converter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.example.ai.GeminiMarkItDownService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.regex.Pattern

object PdfToMarkdownConverter {

    suspend fun convert(
        context: Context,
        inputStream: InputStream,
        fileName: String,
        geminiService: GeminiMarkItDownService? = null
    ): String = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "temp_pdf_${System.currentTimeMillis()}.pdf")
        try {
            FileOutputStream(tempFile).use { out ->
                inputStream.copyTo(out)
            }

            val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val pageCount = renderer.pageCount

            val sb = StringBuilder()
            val docTitle = fileName.removeSuffix(".pdf").removeSuffix(".PDF")
            sb.append("# $docTitle\n\n")

            // If Gemini is available and has API key, we can use multimodal vision on pages
            val canUseGeminiVision = geminiService != null && geminiService.isAvailable()

            if (canUseGeminiVision) {
                // Process pages with Gemini OCR / layout analysis
                for (pageIndex in 0 until minOf(pageCount, 10)) { // limit up to 10 pages for response size
                    val page = renderer.openPage(pageIndex)
                    val width = (page.width * 1.5).toInt()
                    val height = (page.height * 1.5).toInt()
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    sb.append("## Page ${pageIndex + 1}\n\n")
                    val pageMarkdown = geminiService?.transcribeImage(bitmap) ?: ""
                    if (pageMarkdown.isNotBlank()) {
                        sb.append(pageMarkdown).append("\n\n")
                    } else {
                        sb.append("*(No text detected)*\n\n")
                    }
                    bitmap.recycle()
                }
            } else {
                // Native fast extraction: parse PDF text streams directly
                val rawBytes = tempFile.readBytes()
                val extractedText = extractTextFromPdfBytes(rawBytes)

                if (extractedText.isNotBlank()) {
                    sb.append(extractedText)
                } else {
                    // Fallback structured metadata breakdown
                    sb.append("> **Document Info**: PDF document contains **$pageCount pages**.\n\n")
                    for (i in 0 until minOf(pageCount, 20)) {
                        val page = renderer.openPage(i)
                        sb.append("### Page ${i + 1}\n\n")
                        sb.append("- **Dimensions**: ${page.width} x ${page.height} pt\n")
                        page.close()
                    }
                    sb.append("\n*Tip: Connect Gemini API in Settings to enable OCR for scanned pages.*\n")
                }
            }

            renderer.close()
            pfd.close()

            sb.toString().trim()
        } catch (e: Exception) {
            "# PDF Conversion\n\n*(Error reading PDF: ${e.message})*"
        } finally {
            tempFile.delete()
        }
    }

    private fun extractTextFromPdfBytes(bytes: ByteArray): String {
        val content = String(bytes, Charsets.ISO_8859_1)
        val sb = StringBuilder()

        // Extract text between BT (Begin Text) and ET (End Text)
        val btEtPattern = Pattern.compile("BT(.*?)ET", Pattern.DOTALL)
        val matcher = btEtPattern.matcher(content)

        val textBlocks = mutableListOf<String>()
        while (matcher.find()) {
            val stream = matcher.group(1) ?: continue
            // Extract Tj and TJ text operators
            val tjPattern = Pattern.compile("\\((.*?)\\)\\s*Tj", Pattern.DOTALL)
            val tjMatcher = tjPattern.matcher(stream)
            val blockSb = StringBuilder()
            while (tjMatcher.find()) {
                val str = tjMatcher.group(1) ?: ""
                blockSb.append(unescapePdfString(str)).append(" ")
            }

            val arrayPattern = Pattern.compile("\\[(.*?)\\]\\s*TJ", Pattern.DOTALL)
            val arrayMatcher = arrayPattern.matcher(stream)
            while (arrayMatcher.find()) {
                val arr = arrayMatcher.group(1) ?: ""
                val itemMatcher = Pattern.compile("\\((.*?)\\)").matcher(arr)
                while (itemMatcher.find()) {
                    blockSb.append(unescapePdfString(itemMatcher.group(1) ?: "")).append(" ")
                }
            }

            val trimmed = blockSb.toString().trim()
            if (trimmed.isNotBlank()) {
                textBlocks.add(trimmed)
            }
        }

        if (textBlocks.isNotEmpty()) {
            textBlocks.forEach { block ->
                if (block.length < 60 && !block.contains(".") && block.split(" ").size in 1..8) {
                    // Possible heading
                    sb.append("\n\n### ").append(block).append("\n\n")
                } else {
                    sb.append(block).append("\n\n")
                }
            }
        }

        return sb.toString().trim()
    }

    private fun unescapePdfString(s: String): String {
        return s.replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\(", "(")
            .replace("\\)", ")")
            .replace("\\\\", "\\")
    }
}
