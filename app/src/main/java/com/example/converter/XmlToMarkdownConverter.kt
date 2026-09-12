package com.example.converter

object XmlToMarkdownConverter {

    fun convert(xmlString: String): String {
        val trimmed = xmlString.trim()
        if (trimmed.isEmpty()) return ""

        val sb = StringBuilder()
        sb.append("# XML Document\n\n")

        val formattedXml = try {
            formatXml(trimmed)
        } catch (e: Exception) {
            trimmed
        }

        sb.append("```xml\n")
        sb.append(formattedXml)
        sb.append("\n```\n")

        return sb.toString().trim()
    }

    private fun formatXml(input: String): String {
        var indent = 0
        val sb = StringBuilder()
        val tokens = input.replace("><", ">\n<").lines()

        for (line in tokens) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("</")) {
                indent = (indent - 1).coerceAtLeast(0)
            }

            sb.append("  ".repeat(indent)).append(trimmed).append("\n")

            if (trimmed.startsWith("<") && !trimmed.startsWith("</") && !trimmed.endsWith("/>") && !trimmed.startsWith("<?") && !trimmed.startsWith("<!") && !trimmed.contains("</")) {
                indent++
            }
        }
        return sb.toString().trimEnd()
    }
}
