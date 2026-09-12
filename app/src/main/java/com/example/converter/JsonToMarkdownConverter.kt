package com.example.converter

import org.json.JSONArray
import org.json.JSONObject

object JsonToMarkdownConverter {

    fun convert(jsonString: String): String {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return ""

        val sb = StringBuilder()

        try {
            if (trimmed.startsWith("[")) {
                val jsonArray = JSONArray(trimmed)
                sb.append("# JSON Array Data (${jsonArray.length()} items)\n\n")

                // If array of uniform objects, also render markdown table summary!
                if (jsonArray.length() > 0 && jsonArray.get(0) is JSONObject) {
                    val firstObj = jsonArray.getJSONObject(0)
                    val keys = firstObj.keys().asSequence().toList()

                    if (keys.isNotEmpty()) {
                        sb.append("### Summary Table\n\n|")
                        keys.forEach { sb.append(" $it |") }
                        sb.append("\n|")
                        keys.forEach { _ -> sb.append(" --- |") }
                        sb.append("\n")

                        val previewLimit = minOf(jsonArray.length(), 20)
                        for (i in 0 until previewLimit) {
                            val item = jsonArray.optJSONObject(i)
                            if (item != null) {
                                sb.append("|")
                                keys.forEach { key ->
                                    val valStr = item.optString(key, "").replace("|", "\\|").replace("\n", " ")
                                    sb.append(" $valStr |")
                                }
                                sb.append("\n")
                            }
                        }
                        if (jsonArray.length() > 20) {
                            sb.append("\n*Showing first 20 of ${jsonArray.length()} records*\n\n")
                        } else {
                            sb.append("\n\n")
                        }
                    }
                }

                sb.append("### Raw Formatted JSON\n\n```json\n")
                sb.append(jsonArray.toString(2))
                sb.append("\n```\n")

            } else {
                val jsonObject = JSONObject(trimmed)
                sb.append("# JSON Document\n\n")

                // Outline top-level keys
                sb.append("### Document Keys\n\n")
                jsonObject.keys().forEach { key ->
                    val value = jsonObject.get(key)
                    val type = when (value) {
                        is JSONObject -> "Object (${value.length()} keys)"
                        is JSONArray -> "Array (${value.length()} items)"
                        is Number -> "Number ($value)"
                        is Boolean -> "Boolean ($value)"
                        else -> "String"
                    }
                    sb.append("- **`$key`**: $type\n")
                }
                sb.append("\n### Formatted JSON\n\n```json\n")
                sb.append(jsonObject.toString(2))
                sb.append("\n```\n")
            }
        } catch (e: Exception) {
            // Fallback to raw code block if JSON syntax error
            sb.append("# JSON Data\n\n```json\n")
            sb.append(trimmed)
            sb.append("\n```\n")
        }

        return sb.toString().trim()
    }
}
