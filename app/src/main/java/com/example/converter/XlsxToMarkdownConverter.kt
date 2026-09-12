package com.example.converter

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.util.zip.ZipInputStream

object XlsxToMarkdownConverter {

    fun convert(inputStream: InputStream): String {
        val zip = ZipInputStream(inputStream)
        val sharedStrings = mutableListOf<String>()
        val sheetEntries = mutableMapOf<String, ByteArray>()
        val sheetNames = mutableListOf<String>()

        try {
            var entry = zip.nextEntry
            while (entry != null) {
                when {
                    entry.name == "xl/sharedStrings.xml" -> {
                        val bytes = zip.readBytes()
                        parseSharedStrings(bytes.inputStream(), sharedStrings)
                    }
                    entry.name == "xl/workbook.xml" -> {
                        val bytes = zip.readBytes()
                        parseWorkbook(bytes.inputStream(), sheetNames)
                    }
                    entry.name.startsWith("xl/worksheets/sheet") && entry.name.endsWith(".xml") -> {
                        sheetEntries[entry.name] = zip.readBytes()
                    }
                }
                entry = zip.nextEntry
            }
        } finally {
            zip.close()
        }

        if (sheetEntries.isEmpty()) {
            return "# Spreadsheet\n\n*(No worksheets found in XLSX archive)*"
        }

        val sb = StringBuilder()

        // Sort sheets: sheet1.xml, sheet2.xml, etc.
        val sortedEntries = sheetEntries.entries.sortedBy { it.key }
        sortedEntries.forEachIndexed { index, (entryName, xmlBytes) ->
            val sheetTitle = sheetNames.getOrNull(index) ?: "Sheet ${index + 1}"
            sb.append("## $sheetTitle\n\n")

            val table = parseSheetXml(xmlBytes.inputStream(), sharedStrings)
            if (table.isNotEmpty()) {
                val colCount = table.maxOfOrNull { it.size } ?: 0
                if (colCount > 0) {
                    val header = table.first()
                    sb.append("|")
                    for (c in 0 until colCount) {
                        val name = header.getOrNull(c)?.ifBlank { "Col ${c + 1}" } ?: "Col ${c + 1}"
                        sb.append(" $name |")
                    }
                    sb.append("\n|")
                    for (c in 0 until colCount) {
                        sb.append(" --- |")
                    }
                    sb.append("\n")

                    for (r in 1 until table.size) {
                        val row = table[r]
                        sb.append("|")
                        for (c in 0 until colCount) {
                            val valStr = row.getOrNull(c)?.ifBlank { " " } ?: " "
                            sb.append(" $valStr |")
                        }
                        sb.append("\n")
                    }
                    sb.append("\n\n")
                }
            } else {
                sb.append("*(Empty sheet)*\n\n")
            }
        }

        return sb.toString().trim()
    }

    private fun parseSharedStrings(stream: InputStream, list: MutableList<String>) {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var inT = false
        val currentString = StringBuilder()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "t") inT = true
                    else if (parser.name == "si") currentString.clear()
                }
                XmlPullParser.TEXT -> {
                    if (inT) currentString.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") inT = false
                    else if (parser.name == "si") list.add(currentString.toString())
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseWorkbook(stream: InputStream, sheetNames: MutableList<String>) {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "sheet") {
                val name = parser.getAttributeValue(null, "name")
                if (!name.isNullOrBlank()) {
                    sheetNames.add(name)
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseSheetXml(stream: InputStream, sharedStrings: List<String>): List<List<String>> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        val rows = mutableListOf<MutableList<String>>()
        var currentRow = mutableListOf<String>()
        var currentCellType = ""
        var currentCellValue = StringBuilder()
        var currentCellRef = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (name) {
                        "row" -> currentRow = mutableListOf()
                        "c" -> {
                            currentCellType = parser.getAttributeValue(null, "t") ?: ""
                            currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                            currentCellValue.clear()
                        }
                        "v" -> {}
                    }
                }
                XmlPullParser.TEXT -> {
                    currentCellValue.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (name) {
                        "c" -> {
                            var cellVal = currentCellValue.toString().trim()
                            if (currentCellType == "s") {
                                val idx = cellVal.toIntOrNull()
                                cellVal = if (idx != null && idx in sharedStrings.indices) {
                                    sharedStrings[idx]
                                } else cellVal
                            } else if (currentCellType == "b") {
                                cellVal = if (cellVal == "1") "TRUE" else "FALSE"
                            }
                            // Calculate column index from ref like "C5"
                            val colLetter = currentCellRef.filter { it.isLetter() }
                            val targetColIndex = columnLettersToIndex(colLetter)
                            while (currentRow.size < targetColIndex) {
                                currentRow.add("")
                            }
                            currentRow.add(cellVal.replace("|", "\\|").replace("\n", " "))
                        }
                        "row" -> {
                            if (currentRow.any { it.isNotBlank() }) {
                                rows.add(currentRow)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return rows
    }

    private fun columnLettersToIndex(letters: String): Int {
        if (letters.isBlank()) return 0
        var result = 0
        for (char in letters.uppercase()) {
            result = result * 26 + (char - 'A' + 1)
        }
        return (result - 1).coerceAtLeast(0)
    }
}
