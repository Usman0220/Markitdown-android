package com.example.converter

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.util.zip.ZipInputStream

object PptxToMarkdownConverter {

    fun convert(inputStream: InputStream): String {
        val zip = ZipInputStream(inputStream)
        val slideEntries = mutableMapOf<Int, ByteArray>()

        try {
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name.startsWith("ppt/slides/slide") && entry.name.endsWith(".xml")) {
                    val numStr = entry.name.removePrefix("ppt/slides/slide").removeSuffix(".xml")
                    val slideNum = numStr.toIntOrNull() ?: 0
                    slideEntries[slideNum] = zip.readBytes()
                }
                entry = zip.nextEntry
            }
        } finally {
            zip.close()
        }

        if (slideEntries.isEmpty()) {
            return "# Presentation\n\n*(No slides found in PPTX archive)*"
        }

        val sb = StringBuilder()
        sb.append("# Presentation Slides\n\n")

        val sortedSlides = slideEntries.toSortedMap()
        sortedSlides.forEach { (slideNum, xmlBytes) ->
            val paragraphs = parseSlideXml(xmlBytes.inputStream())

            val title = paragraphs.firstOrNull() ?: "Slide $slideNum"
            sb.append("## Slide $slideNum: $title\n\n")

            if (paragraphs.size > 1) {
                for (i in 1 until paragraphs.size) {
                    val p = paragraphs[i].trim()
                    if (p.isNotEmpty()) {
                        sb.append("- ").append(p).append("\n")
                    }
                }
                sb.append("\n")
            }
        }

        return sb.toString().trim()
    }

    private fun parseSlideXml(stream: InputStream): List<String> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        val paragraphs = mutableListOf<String>()
        var currentParagraph = StringBuilder()
        var inText = false

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name ?: ""
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "p") {
                        currentParagraph.clear()
                    } else if (name == "t") {
                        inText = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inText) {
                        currentParagraph.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (name == "t") {
                        inText = false
                    } else if (name == "p") {
                        val text = currentParagraph.toString().trim()
                        if (text.isNotEmpty()) {
                            paragraphs.add(text)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return paragraphs
    }
}
