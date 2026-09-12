package com.example.converter

import java.util.regex.Pattern

object HtmlToMarkdownConverter {

    fun convert(htmlContent: String): String {
        if (htmlContent.isBlank()) return ""

        var html = htmlContent
            // Strip scripts and styles
            .replace(Regex("(?is)<script.*?>.*?</script>"), "")
            .replace(Regex("(?is)<style.*?>.*?</style>"), "")
            .replace(Regex("(?is)<!--.*?-->"), "")

        // Extract title if present
        val titleMatcher = Pattern.compile("(?is)<title>(.*?)</title>").matcher(html)
        val title = if (titleMatcher.find()) titleMatcher.group(1)?.trim() else null

        // Handle pre/code blocks before tags are stripped
        val codeBlocks = mutableListOf<String>()
        html = html.replace(Regex("(?is)<pre.*?>\\s*<code(?:\\s+class=[\"'](?:language-)?([a-zA-Z0-9_-]+)[\"'])?.*?>(.*?)</code>\\s*</pre>")) { matchResult ->
            val lang = matchResult.groups[1]?.value ?: ""
            val code = unescapeHtml(matchResult.groups[2]?.value ?: "")
            val index = codeBlocks.size
            codeBlocks.add("```$lang\n$code\n```")
            "___CODE_BLOCK_${index}___"
        }

        // Inline code
        html = html.replace(Regex("(?is)<code>(.*?)</code>")) { matchResult ->
            "`${unescapeHtml(matchResult.groups[1]?.value ?: "").trim()}`"
        }

        // Tables
        html = convertTables(html)

        // Headers
        html = html.replace(Regex("(?is)<h1.*?>(.*?)</h1>"), "\n\n# $1\n\n")
        html = html.replace(Regex("(?is)<h2.*?>(.*?)</h2>"), "\n\n## $1\n\n")
        html = html.replace(Regex("(?is)<h3.*?>(.*?)</h3>"), "\n\n### $1\n\n")
        html = html.replace(Regex("(?is)<h4.*?>(.*?)</h4>"), "\n\n#### $1\n\n")
        html = html.replace(Regex("(?is)<h5.*?>(.*?)</h5>"), "\n\n##### $1\n\n")
        html = html.replace(Regex("(?is)<h6.*?>(.*?)</h6>"), "\n\n###### $1\n\n")

        // Blockquotes
        html = html.replace(Regex("(?is)<blockquote.*?>(.*?)</blockquote>")) { matchResult ->
            val content = matchResult.groups[1]?.value ?: ""
            val cleanContent = content.replace(Regex("<.*?>"), "").trim()
            "\n\n> $cleanContent\n\n"
        }

        // Bold and Italic
        html = html.replace(Regex("(?is)<(strong|b).*?>(.*?)</\\1>"), "**$2**")
        html = html.replace(Regex("(?is)<(em|i).*?>(.*?)</\\1>"), "*$2*")
        html = html.replace(Regex("(?is)<(del|s|strike).*?>(.*?)</\\1>"), "~~$2~~")

        // Links
        html = html.replace(Regex("(?is)<a\\s+[^>]*?href=[\"'](.*?)[\"'][^>]*?>(.*?)</a>")) { matchResult ->
            val url = matchResult.groups[1]?.value ?: ""
            val text = matchResult.groups[2]?.value?.replace(Regex("<.*?>"), "")?.trim() ?: url
            if (text.isNotBlank()) "[$text]($url)" else ""
        }

        // Images
        html = html.replace(Regex("(?is)<img\\s+[^>]*?src=[\"'](.*?)[\"'][^>]*?alt=[\"'](.*?)[\"'][^>]*?>")) { matchResult ->
            val src = matchResult.groups[1]?.value ?: ""
            val alt = matchResult.groups[2]?.value ?: "image"
            "![$alt]($src)"
        }
        html = html.replace(Regex("(?is)<img\\s+[^>]*?src=[\"'](.*?)[\"'][^>]*?>")) { matchResult ->
            val src = matchResult.groups[1]?.value ?: ""
            "![image]($src)"
        }

        // Unordered lists
        html = html.replace(Regex("(?is)<li.*?>(.*?)</li>")) { matchResult ->
            val item = matchResult.groups[1]?.value?.replace(Regex("<.*?>"), "")?.trim() ?: ""
            "\n- $item"
        }

        // Horizontal rules
        html = html.replace(Regex("(?is)<hr\\s*/?>"), "\n\n---\n\n")

        // Line breaks and paragraphs
        html = html.replace(Regex("(?is)<br\\s*/?>"), "\n")
        html = html.replace(Regex("(?is)<p.*?>(.*?)</p>"), "\n\n$1\n\n")

        // Strip remaining HTML tags
        html = html.replace(Regex("<.*?>"), " ")

        // Restore code blocks
        codeBlocks.forEachIndexed { index, block ->
            html = html.replace("___CODE_BLOCK_${index}___", "\n\n$block\n\n")
        }

        // Unescape entities and normalize newlines
        var markdown = unescapeHtml(html)
            .lines()
            .map { it.trimEnd() }
            .joinToString("\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()

        if (!title.isNullOrBlank() && !markdown.startsWith("# ")) {
            markdown = "# $title\n\n$markdown"
        }

        return markdown
    }

    private fun convertTables(html: String): String {
        return html.replace(Regex("(?is)<table.*?>(.*?)</table>")) { tableMatch ->
            val tableContent = tableMatch.groups[1]?.value ?: ""
            val rows = mutableListOf<List<String>>()

            val rowMatcher = Pattern.compile("(?is)<tr.*?>(.*?)</tr>").matcher(tableContent)
            while (rowMatcher.find()) {
                val rowHtml = rowMatcher.group(1) ?: ""
                val cells = mutableListOf<String>()
                val cellMatcher = Pattern.compile("(?is)<t[hd].*?>(.*?)</t[hd]>").matcher(rowHtml)
                while (cellMatcher.find()) {
                    val cellText = unescapeHtml(cellMatcher.group(1)?.replace(Regex("<.*?>"), "")?.trim() ?: "")
                        .replace("|", "\\|")
                        .replace("\n", " ")
                    cells.add(cellText)
                }
                if (cells.isNotEmpty()) {
                    rows.add(cells)
                }
            }

            if (rows.isEmpty()) return@replace ""

            val colCount = rows.maxOfOrNull { it.size } ?: 0
            if (colCount == 0) return@replace ""

            val sb = StringBuilder("\n\n")
            // Header row
            val headerRow = rows.first()
            sb.append("|")
            for (i in 0 until colCount) {
                val text = headerRow.getOrNull(i)?.ifBlank { " " } ?: " "
                sb.append(" $text |")
            }
            sb.append("\n|")
            // Separator row
            for (i in 0 until colCount) {
                sb.append(" --- |")
            }
            sb.append("\n")

            // Data rows
            for (rowIndex in 1 until rows.size) {
                val row = rows[rowIndex]
                sb.append("|")
                for (i in 0 until colCount) {
                    val text = row.getOrNull(i)?.ifBlank { " " } ?: " "
                    sb.append(" $text |")
                }
                sb.append("\n")
            }
            sb.append("\n")
            sb.toString()
        }
    }

    private fun unescapeHtml(text: String): String {
        return text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")
            .replace("&bull;", "•")
            .replace("&hellip;", "…")
    }
}
