package com.example.converter

object CsvToMarkdownConverter {

    fun convert(csvContent: String): String {
        if (csvContent.isBlank()) return ""

        val delimiter = detectDelimiter(csvContent)
        val rows = parseCsv(csvContent, delimiter)
        if (rows.isEmpty()) return ""

        val colCount = rows.maxOfOrNull { it.size } ?: 0
        if (colCount == 0) return ""

        val sb = StringBuilder()

        // First row as headers
        val header = rows.first()
        sb.append("|")
        for (i in 0 until colCount) {
            val cell = header.getOrNull(i)?.trim()?.replace("|", "\\|") ?: "Column ${i + 1}"
            sb.append(" ").append(if (cell.isEmpty()) "Column ${i + 1}" else cell).append(" |")
        }
        sb.append("\n|")
        for (i in 0 until colCount) {
            sb.append(" --- |")
        }
        sb.append("\n")

        // Rows
        for (rowIndex in 1 until rows.size) {
            val row = rows[rowIndex]
            if (row.all { it.isBlank() }) continue // Skip blank lines
            sb.append("|")
            for (i in 0 until colCount) {
                val cell = row.getOrNull(i)?.trim()?.replace("|", "\\|") ?: ""
                sb.append(" ").append(cell).append(" |")
            }
            sb.append("\n")
        }

        return sb.toString().trim()
    }

    private fun detectDelimiter(content: String): Char {
        val firstLine = content.lines().firstOrNull { it.isNotBlank() } ?: return ','
        val commaCount = firstLine.count { it == ',' }
        val tabCount = firstLine.count { it == '\t' }
        val semiCount = firstLine.count { it == ';' }

        return when {
            tabCount > commaCount && tabCount > semiCount -> '\t'
            semiCount > commaCount && semiCount > tabCount -> ';'
            else -> ','
        }
    }

    private fun parseCsv(content: String, delimiter: Char): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        val currentCell = StringBuilder()
        var insideQuotes = false

        val chars = content.toCharArray()
        var i = 0
        while (i < chars.size) {
            val c = chars[i]

            when {
                c == '"' -> {
                    if (insideQuotes && i + 1 < chars.size && chars[i + 1] == '"') {
                        currentCell.append('"')
                        i++ // Skip escaped quote
                    } else {
                        insideQuotes = !insideQuotes
                    }
                }
                c == delimiter && !insideQuotes -> {
                    currentRow.add(currentCell.toString())
                    currentCell.clear()
                }
                (c == '\r' || c == '\n') && !insideQuotes -> {
                    if (c == '\r' && i + 1 < chars.size && chars[i + 1] == '\n') {
                        i++
                    }
                    currentRow.add(currentCell.toString())
                    currentCell.clear()
                    if (currentRow.any { it.isNotBlank() }) {
                        rows.add(currentRow)
                    }
                    currentRow = mutableListOf()
                }
                else -> {
                    currentCell.append(c)
                }
            }
            i++
        }

        if (currentCell.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentCell.toString())
            if (currentRow.any { it.isNotBlank() }) {
                rows.add(currentRow)
            }
        }

        return rows
    }
}
