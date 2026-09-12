package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiMarkItDownService {

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun isAvailable(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun transcribeImage(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        if (!isAvailable()) {
            return@withContext "# Image\n\n*(Gemini API Key required for AI Vision transcription)*"
        }

        val base64Image = bitmapToBase64(bitmap)
        val prompt = "Transcribe this image into clean, structured GitHub-flavored Markdown. " +
                "Extract all text verbatim, preserve headings (#, ##), format tables as Markdown tables (| Col |), " +
                "format lists (- item), and describe any diagrams, charts, or visual elements inside a blockquote (> Diagram: ...). " +
                "Return ONLY the Markdown content without markdown fence wrappers around the whole response."

        val requestJson = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().apply { put("text", prompt) })
                put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", base64Image)
                    })
                })
            }
            put("contents", JSONArray().apply {
                put(JSONObject().apply { put("parts", partsArray) })
            })
        }

        callGeminiApi(requestJson)
    }

    suspend fun transcribeAudio(audioBytes: ByteArray, mimeType: String): String = withContext(Dispatchers.IO) {
        if (!isAvailable()) {
            return@withContext "# Audio Recording\n\n*(Gemini API Key required for AI Audio transcription)*"
        }

        val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
        val prompt = "Transcribe this audio recording into clean, structured Markdown. " +
                "Break speech into logical paragraphs, note speakers if distinct (e.g. **Speaker 1:** ...), " +
                "and provide a short '## Summary' section at the beginning with key takeaways. " +
                "Return ONLY the Markdown content."

        val requestJson = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().apply { put("text", prompt) })
                put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", mimeType)
                        put("data", base64Audio)
                    })
                })
            }
            put("contents", JSONArray().apply {
                put(JSONObject().apply { put("parts", partsArray) })
            })
        }

        callGeminiApi(requestJson)
    }

    suspend fun summarizeMarkdown(markdown: String): String = withContext(Dispatchers.IO) {
        if (!isAvailable()) return@withContext "API key not configured."

        val prompt = "Provide a concise executive summary and key takeaways of the following Markdown document in bullet points:\n\n$markdown"
        callTextPrompt(prompt)
    }

    suspend fun polishMarkdown(markdown: String): String = withContext(Dispatchers.IO) {
        if (!isAvailable()) return@withContext markdown

        val prompt = "Review and polish this Markdown document. Fix any broken tables, improve heading structure, align lists, and ensure clean formatting. Return ONLY the polished Markdown:\n\n$markdown"
        callTextPrompt(prompt)
    }

    private suspend fun callTextPrompt(prompt: String): String {
        val requestJson = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().apply { put("text", prompt) })
            }
            put("contents", JSONArray().apply {
                put(JSONObject().apply { put("parts", partsArray) })
            })
        }
        return callGeminiApi(requestJson)
    }

    private fun callGeminiApi(requestJson: JSONObject): String {
        val apiKey = getApiKey()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = try {
                val json = JSONObject(responseBody)
                json.optJSONObject("error")?.optString("message") ?: response.message
            } catch (e: Exception) {
                response.message
            }
            return "*(Gemini API error: $errorMsg)*"
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        val candidate = candidates?.optJSONObject(0)
        val content = candidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text") ?: ""

        return text.trim()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
