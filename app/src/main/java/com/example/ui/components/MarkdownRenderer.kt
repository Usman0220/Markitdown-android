package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    MarkdownHeading(level = block.level, text = block.text)
                }
                is MarkdownBlock.Paragraph -> {
                    MarkdownParagraph(text = block.text)
                }
                is MarkdownBlock.CodeBlock -> {
                    MarkdownCodeBlock(code = block.code, language = block.language)
                }
                is MarkdownBlock.Table -> {
                    MarkdownTableView(headers = block.headers, rows = block.rows)
                }
                is MarkdownBlock.Blockquote -> {
                    MarkdownBlockquote(text = block.text)
                }
                is MarkdownBlock.ListBlock -> {
                    MarkdownListView(items = block.items, isOrdered = block.isOrdered)
                }
                is MarkdownBlock.HorizontalRule -> {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    data class Blockquote(val text: String) : MarkdownBlock()
    data class ListBlock(val items: List<String>, val isOrdered: Boolean) : MarkdownBlock()
    object HorizontalRule : MarkdownBlock()
}

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        when {
            // Fenced code block
            trimmed.startsWith("```") -> {
                val language = trimmed.removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n")))
            }

            // Table
            trimmed.startsWith("|") && trimmed.endsWith("|") -> {
                val tableLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                    tableLines.add(lines[i].trim())
                    i++
                }
                i-- // adjust back for loop increment
                val table = parseTableLines(tableLines)
                if (table != null) {
                    blocks.add(table)
                }
            }

            // Headings
            trimmed.startsWith("#") -> {
                val level = trimmed.takeWhile { it == '#' }.length
                val text = trimmed.drop(level).trim()
                if (level in 1..6 && text.isNotEmpty()) {
                    blocks.add(MarkdownBlock.Heading(level, text))
                } else {
                    blocks.add(MarkdownBlock.Paragraph(line))
                }
            }

            // Blockquote
            trimmed.startsWith(">") -> {
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith(">")) {
                    quoteLines.add(lines[i].trim().removePrefix(">").trim())
                    i++
                }
                i--
                blocks.add(MarkdownBlock.Blockquote(quoteLines.joinToString("\n")))
            }

            // Lists
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                val isOrdered = trimmed.matches(Regex("^\\d+\\.\\s+.*"))
                val items = mutableListOf<String>()
                while (i < lines.size) {
                    val cur = lines[i].trim()
                    if (isOrdered && cur.matches(Regex("^\\d+\\.\\s+.*"))) {
                        items.add(cur.replace(Regex("^\\d+\\.\\s+"), ""))
                    } else if (!isOrdered && (cur.startsWith("- ") || cur.startsWith("* "))) {
                        items.add(cur.substring(2))
                    } else if (cur.isBlank()) {
                        break
                    } else {
                        break
                    }
                    i++
                }
                i--
                blocks.add(MarkdownBlock.ListBlock(items, isOrdered))
            }

            // Horizontal Rule
            trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                blocks.add(MarkdownBlock.HorizontalRule)
            }

            // Empty line
            trimmed.isEmpty() -> {
                // Skip empty lines between blocks
            }

            // Paragraph
            else -> {
                val pLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().isNotEmpty() &&
                    !lines[i].trim().startsWith("```") &&
                    !lines[i].trim().startsWith("#") &&
                    !lines[i].trim().startsWith(">") &&
                    !lines[i].trim().startsWith("|") &&
                    !lines[i].trim().startsWith("- ") &&
                    !lines[i].trim().startsWith("* ") &&
                    !lines[i].trim().matches(Regex("^\\d+\\.\\s+.*")) &&
                    lines[i].trim() != "---"
                ) {
                    pLines.add(lines[i].trim())
                    i++
                }
                i--
                blocks.add(MarkdownBlock.Paragraph(pLines.joinToString(" ")))
            }
        }
        i++
    }

    return blocks
}

private fun parseTableLines(lines: List<String>): MarkdownBlock.Table? {
    if (lines.size < 2) return null

    fun parseRow(rowLine: String): List<String> {
        return rowLine.trim()
            .removeSurrounding("|", "|")
            .split("|")
            .map { it.trim() }
    }

    val headerRow = parseRow(lines[0])
    val separatorRow = parseRow(lines[1])

    // Check if second line is separator like --- | ---
    val isSeparator = separatorRow.all { it.matches(Regex("^-+:?$")) || it.matches(Regex("^:?-+:?$")) || it.isBlank() }
    val dataStartIndex = if (isSeparator) 2 else 1

    val rows = mutableListOf<List<String>>()
    for (idx in dataStartIndex until lines.size) {
        val row = parseRow(lines[idx])
        if (row.any { it.isNotBlank() }) {
            rows.add(row)
        }
    }

    return MarkdownBlock.Table(headerRow, rows)
}

@Composable
fun MarkdownHeading(level: Int, text: String) {
    val (fontSize, fontWeight, color) = when (level) {
        1 -> Triple(26.sp, FontWeight.Bold, MaterialTheme.colorScheme.onBackground)
        2 -> Triple(20.sp, FontWeight.Bold, MaterialTheme.colorScheme.onBackground)
        3 -> Triple(17.sp, FontWeight.SemiBold, Sky400)
        4 -> Triple(15.sp, FontWeight.SemiBold, MaterialTheme.colorScheme.onBackground)
        else -> Triple(14.sp, FontWeight.Medium, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Column(modifier = Modifier.padding(top = if (level <= 2) 12.dp else 6.dp, bottom = 4.dp)) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            lineHeight = (fontSize.value * 1.3).sp
        )
        if (level <= 2) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = if (level == 1) 1.5.dp else 1.dp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun MarkdownParagraph(text: String) {
    val context = LocalContext.current
    val annotatedString = remember(text) { buildInlineMarkdown(text) }

    Text(
        text = annotatedString,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.clickable {
            // Check for url click annotation if present
        }
    )
}

@Composable
fun MarkdownBlockquote(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Sky400)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MarkdownListView(items: List<String>, isOrdered: Boolean) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(start = 6.dp)
    ) {
        items.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isOrdered) "${index + 1}." else "•",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Sky400,
                    modifier = Modifier.width(if (isOrdered) 24.dp else 16.dp)
                )
                Text(
                    text = buildInlineMarkdown(item),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MarkdownCodeBlock(code: String, language: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CodeBackgroundDark)
            .border(1.dp, Slate800, RoundedCornerShape(10.dp))
    ) {
        Column {
            // Header with language and copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifBlank { "TEXT" }.uppercase(),
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Code", code))
                            copied = true
                            Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                delay(2000)
                                copied = false
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = if (copied) Emerald400 else Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (copied) "Copied" else "Copy",
                        color = if (copied) Emerald400 else Slate400,
                        fontSize = 11.sp
                    )
                }
            }

            // Code content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Slate200,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun MarkdownTableView(headers: List<String>, rows: List<List<String>>) {
    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(modifier = Modifier.horizontalScroll(scrollState)) {
            Column(modifier = Modifier.padding(4.dp)) {
                // Header row
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 8.dp)
                ) {
                    headers.forEachIndexed { colIndex, header ->
                        Box(
                            modifier = Modifier
                                .widthIn(min = 100.dp, max = 220.dp)
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = header,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                // Data rows
                rows.forEachIndexed { rowIndex, row ->
                    val isEven = rowIndex % 2 == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isEven) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            .padding(vertical = 7.dp)
                    ) {
                        headers.indices.forEach { colIndex ->
                            val cell = row.getOrNull(colIndex) ?: ""
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 100.dp, max = 220.dp)
                                    .padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = buildInlineMarkdown(cell),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun buildInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val regex = Regex("(\\*\\*\\*(.*?)\\*\\*\\*|\\*\\*(.*?)\\*\\*|\\*(.*?)\\*|~~(.*?)~~|`(.*?)`|\\[(.*?)\\]\\((.*?)\\))")
        val matches = regex.findAll(text)

        for (match in matches) {
            val range = match.range
            if (range.first > cursor) {
                append(text.substring(cursor, range.first))
            }

            when {
                // Bold and italic: ***text***
                match.value.startsWith("***") && match.value.endsWith("***") -> {
                    val inner = match.groups[2]?.value ?: ""
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                        append(inner)
                    }
                }
                // Bold: **text**
                match.value.startsWith("**") && match.value.endsWith("**") -> {
                    val inner = match.groups[3]?.value ?: ""
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(inner)
                    }
                }
                // Italic: *text*
                match.value.startsWith("*") && match.value.endsWith("*") -> {
                    val inner = match.groups[4]?.value ?: ""
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(inner)
                    }
                }
                // Strikethrough: ~~text~~
                match.value.startsWith("~~") && match.value.endsWith("~~") -> {
                    val inner = match.groups[5]?.value ?: ""
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(inner)
                    }
                }
                // Inline code: `text`
                match.value.startsWith("`") && match.value.endsWith("`") -> {
                    val inner = match.groups[6]?.value ?: ""
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0x3364748B),
                            color = Sky400
                        )
                    ) {
                        append(" $inner ")
                    }
                }
                // Link: [text](url)
                match.value.startsWith("[") -> {
                    val linkText = match.groups[7]?.value ?: ""
                    val url = match.groups[8]?.value ?: ""
                    withStyle(
                        SpanStyle(
                            color = Sky400,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(linkText)
                    }
                }
                else -> {
                    append(match.value)
                }
            }
            cursor = range.last + 1
        }

        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}
