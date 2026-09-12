package com.example.converter

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.util.zip.ZipInputStream

object DocxToMarkdownConverter {

    fun convert(inputStream: InputStream): String {
        val zip = ZipInputStream(inputStream)
        var documentXml: ByteArray? = null

        try {
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    documentXml = zip.readBytes()
                    break
                }
                entry = zip.nextEntry
            }
        } finally {
            zip.close()
        }

        if (documentXml == null) {
            return "# Document\n\n*(Could not locate word/document.xml in DOCX archive)*"
        }

        return parseDocumentXml(documentXml.inputStream())
    }

    private fun parseDocumentXml(stream: InputStream): String {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        val result = StringBuilder()

        var inParagraph = false
        var inTable = false
        var inTableRow = false
        var inTableCell = false

        var pHeadingLevel = 0
        var isBulletList = false
        var pText = StringBuilder()

        var inRun = false
        var isBold = false
        var isItalic = false
        var isStrike = false
        var runText = StringBuilder()

        // Table structures
        val tableRows = mutableListOf<MutableList<String>>()
        var currentRowCells = mutableListOf<String>()
        var currentCellText = StringBuilder()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (name) {
                        "tbl" -> {
                            inTable = true
                            tableRows.clear()
                        }
                        "tr" -> {
                            inTableRow = true
                            currentRowCells = mutableListOf()
                        }
                        "tc" -> {
                            inTableCell = true
                            currentCellText.clear()
                        }
                        "p" -> {
                            inParagraph = true
                            pHeadingLevel = 0
                            isBulletList = false
                            pText.clear()
                        }
                        "pStyle" -> {
                            val styleVal = parser.getAttributeValue(null, "val") ?: ""
                            when {
                                styleVal.equals("Title", ignoreCase = true) -> pHeadingLevel = 1
                                styleVal.equals("Subtitle", ignoreCase = true) -> pHeadingLevel = 2
                                styleVal.contains("Heading1", ignoreCase = true) || styleVal.contains("Heading 1", ignoreCase = true) -> pHeadingLevel = 1
                                styleVal.contains("Heading2", ignoreCase = true) || styleVal.contains("Heading 2", ignoreCase = true) -> pHeadingLevel = 2
                                styleVal.contains("Heading3", ignoreCase = true) || styleVal.contains("Heading 3", ignoreCase = true) -> pHeadingLevel = 3
                                styleVal.contains("Heading4", ignoreCase = true) || styleVal.contains("Heading 4", ignoreCase = true) -> pHeadingLevel = 4
                                styleVal.contains("Heading5", ignoreCase = true) || styleVal.contains("Heading 5", ignoreCase = true) -> pHeadingLevel = 5
                                styleVal.contains("Heading6", ignoreCase = true) || styleVal.contains("Heading 6", ignoreCase = true) -> pHeadingLevel = 6
                            }
                        }
                        "numPr" -> {
                            isBulletList = true
                        }
                        "r" -> {
                            inRun = true
                            isBold = false
                            isItalic = false
                            isStrike = false
                            runText.clear()
                        }
                        "b" -> {
                            val bVal = parser.getAttributeValue(null, "val")
                            isBold = bVal == null || bVal == "1" || bVal.equals("true", ignoreCase = true)
                        }
                        "i" -> {
                            val iVal = parser.getAttributeValue(null, "val")
                            isItalic = iVal == null || iVal == "1" || iVal.equals("true", ignoreCase = true)
                        }
                        "strike" -> {
                            isStrike = true
                        }
                        "t" -> {
                            val text = parser.nextText()
                            runText.append(text)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (name) {
                        "r" -> {
                            inRun = false
                            var text = runText.toString()
                            if (text.isNotEmpty()) {
                                if (isBold && isItalic) text = "***$text***"
                                else if (isBold) text = "**$text**"
                                else if (isItalic) text = "*$text*"
                                if (isStrike) text = "~~$text~~"

                                if (inTableCell) {
                                    currentCellText.append(text)
                                } else {
                                    pText.append(text)
                                }
                            }
                        }
                        "p" -> {
                            inParagraph = false
                            val paragraphString = pText.toString().trim()
                            if (paragraphString.isNotEmpty()) {
                                when {
                                    pHeadingLevel > 0 -> {
                                        val prefix = "#".repeat(pHeadingLevel)
                                        result.append("\n\n$prefix $paragraphString\n\n")
                                    }
                                    isBulletList -> {
                                        result.append("\n- $paragraphString")
                                    }
                                    else -> {
                                        result.append("\n\n$paragraphString\n")
                                    }
                                }
                            }
                        }
                        "tc" -> {
                            inTableCell = false
                            val cellStr = currentCellText.toString().trim().replace("|", "\\|").replace("\n", " ")
                            currentRowCells.add(cellStr)
                        }
                        "tr" -> {
                            inTableRow = false
                            if (currentRowCells.isNotEmpty()) {
                                tableRows.add(currentRowCells)
                            }
                        }
                        "tbl" -> {
                            inTable = false
                            renderTableToMarkdown(tableRows, result)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return result.toString()
            .lines()
            .map { it.trimEnd() }
            .joinToString("\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun renderTableToMarkdown(rows: List<List<String>>, sb: StringBuilder) {
        if (rows.isEmpty()) return
        val colCount = rows.maxOfOrNull { it.size } ?: 0
        if (colCount == 0) return

        sb.append("\n\n|")
        val header = rows.first()
        for (i in 0 until colCount) {
            val cell = header.getOrNull(i)?.ifBlank { " " } ?: " "
            sb.append(" $cell |")
        }
        sb.append("\n|")
        for (i in 0 until colCount) {
            sb.append(" --- |")
        }
        sb.append("\n")

        for (r in 1 until rows.size) {
            val row = rows[r]
            sb.append("|")
            for (i in 0 until colCount) {
                val cell = row.getOrNull(i)?.ifBlank { " " } ?: " "
                sb.append(" $cell |")
            }
            sb.append("\n")
        }
        sb.append("\n")
    }
}
