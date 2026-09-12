package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiMarkItDownService
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistSheet(
    markdown: String,
    geminiService: GeminiMarkItDownService,
    onApplyPolishedMarkdown: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var currentResult by remember { mutableStateOf<String?>(null) }
    var resultTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val hasApiKey = remember { geminiService.isAvailable() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = Sky400
                    )
                    Text(
                        text = "MarkItDown AI Assistant",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (!hasApiKey) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Amber500.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Notice",
                            tint = Amber500
                        )
                        Text(
                            text = "To enable Gemini AI actions, provide a GEMINI_API_KEY in the Secrets panel.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = Sky400)
                        Text(
                            text = "Analyzing document with Gemini...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (currentResult != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = resultTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            MarkdownRenderer(markdown = currentResult!!)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                currentResult = null
                                resultTitle = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back")
                        }

                        Button(
                            onClick = {
                                onApplyPolishedMarkdown(currentResult!!)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply to Doc")
                        }
                    }
                }
            } else {
                // Action options
                Text(
                    text = "Smart Document Actions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AiActionCard(
                    title = "Generate Executive Summary",
                    description = "Extract key takeaways and action points in concise bullet points",
                    icon = Icons.Default.Summarize,
                    enabled = hasApiKey,
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            val res = geminiService.summarizeMarkdown(markdown)
                            resultTitle = "Executive Summary"
                            currentResult = res
                            isLoading = false
                        }
                    }
                )

                AiActionCard(
                    title = "Polish & Enhance Markdown",
                    description = "Fix table alignments, normalize headings, and refine typography",
                    icon = Icons.Default.AutoFixHigh,
                    enabled = hasApiKey,
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            val res = geminiService.polishMarkdown(markdown)
                            resultTitle = "Polished Markdown"
                            currentResult = res
                            isLoading = false
                        }
                    }
                )

                AiActionCard(
                    title = "Add Table of Contents",
                    description = "Auto-generate an interactive Table of Contents based on headers",
                    icon = Icons.Default.ListAlt,
                    enabled = true,
                    onClick = {
                        // Can be done client-side instantly!
                        val toc = generateClientToc(markdown)
                        resultTitle = "Document with Table of Contents"
                        currentResult = "$toc\n\n---\n\n$markdown"
                    }
                )
            }
        }
    }
}

@Composable
fun AiActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (enabled) Sky500.copy(alpha = 0.15f) else Slate700.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (enabled) Sky400 else Slate500
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else Slate500
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.9f else 0.5f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (enabled) Slate400 else Slate600
            )
        }
    }
}

private fun generateClientToc(markdown: String): String {
    val headings = markdown.lines().filter { it.trim().startsWith("#") }
    if (headings.isEmpty()) return "*(No headings found in document)*"

    val sb = StringBuilder()
    sb.append("## Table of Contents\n\n")
    headings.forEach { h ->
        val trimmed = h.trim()
        val level = trimmed.takeWhile { it == '#' }.length
        val title = trimmed.drop(level).trim()
        val indent = "  ".repeat((level - 1).coerceAtLeast(0))
        val anchor = title.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
        sb.append("$indent- [$title](#$anchor)\n")
    }
    return sb.toString().trim()
}
