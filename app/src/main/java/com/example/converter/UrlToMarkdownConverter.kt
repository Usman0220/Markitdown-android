package com.example.converter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object UrlToMarkdownConverter {

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    suspend fun convertUrl(url: String): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            var formattedUrl = url.trim()
            if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
                formattedUrl = "https://$formattedUrl"
            }

            val request = Request.Builder()
                .url(formattedUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 MarkItDown/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val contentType = response.header("Content-Type") ?: ""
            val body = response.body?.string() ?: ""

            val markdown = when {
                contentType.contains("json") -> {
                    JsonToMarkdownConverter.convert(body)
                }
                contentType.contains("csv") -> {
                    CsvToMarkdownConverter.convert(body)
                }
                contentType.contains("xml") -> {
                    XmlToMarkdownConverter.convert(body)
                }
                contentType.contains("text/plain") -> {
                    "# ${formattedUrl.substringAfterLast('/')}\n\n$body"
                }
                else -> {
                    // Default to HTML conversion
                    HtmlToMarkdownConverter.convert(body)
                }
            }

            val title = if (markdown.startsWith("# ")) {
                markdown.lines().first().removePrefix("# ").trim()
            } else {
                formattedUrl.substringAfterLast('/').ifBlank { formattedUrl }
            }

            Result.success(title to markdown)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
