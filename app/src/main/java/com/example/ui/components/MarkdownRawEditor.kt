package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MarkdownRawEditor(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val lines = remember(content) { content.lines() }
    val lineCount = lines.size
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val matchCount = remember(content, searchQuery) {
        if (searchQuery.isBlank()) 0
        else {
            var count = 0
            var idx = content.indexOf(searchQuery, ignoreCase = true)
            while (idx >= 0) {
                count++
                idx = content.indexOf(searchQuery, idx + searchQuery.length, ignoreCase = true)
            }
            count
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(CodeBackgroundDark)
    ) {
        // Top Search Bar inside raw editor
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Slate400,
                modifier = Modifier.size(18.dp)
            )

            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(
                    color = Slate100,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(Sky400),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Find in Markdown...",
                                color = Slate500,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "$matchCount matches",
                    color = if (matchCount > 0) Sky400 else Rose500,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                IconButton(
                    onClick = { searchQuery = "" },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Slate800, thickness = 1.dp)

        // Editor Body: Line numbers + Code text field
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(verticalScroll)
        ) {
            // Line numbers column
            Column(
                modifier = Modifier
                    .background(Slate950)
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 1..lineCount) {
                    Text(
                        text = "$i",
                        color = Slate600,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.End
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Slate800)
            )

            // Content text editor
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScroll)
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = content,
                    onValueChange = onContentChange,
                    textStyle = TextStyle(
                        color = Slate200,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(Sky400),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Bottom Stats Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UTF-8 • Markdown",
                color = Slate400,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "$lineCount lines • ${content.length} chars",
                color = Slate400,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

val Slate500 = Color(0xFF64748B)
