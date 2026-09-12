package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarkItDownViewModel
import com.example.ui.viewmodel.ViewerTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    viewModel: MarkItDownViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentDoc by viewModel.currentDocument.collectAsState()
    val viewerTab by viewModel.viewerTab.collectAsState()

    var showStatsSheet by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }

    if (currentDoc == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("No document loaded", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onNavigateBack) {
                    Text("Back to Dashboard")
                }
            }
        }
        return
    }

    val doc = currentDoc!!

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = doc.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FormatBadge(format = doc.sourceFormat)
                            Text(
                                text = "${doc.wordCount} words • ${doc.readingTimeMinutes} min read",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // AI Actions button
                    IconButton(onClick = { showAiSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = Sky400
                        )
                    }

                    // Document Stats
                    IconButton(onClick = { showStatsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Document Info"
                        )
                    }

                    // Favorite Toggle
                    IconButton(onClick = { viewModel.toggleFavorite(doc) }) {
                        Icon(
                            imageVector = if (doc.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (doc.isFavorite) Amber500 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Copy All Markdown
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Markdown", doc.markdownContent))
                        Toast.makeText(context, "Markdown copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Markdown"
                        )
                    }

                    // Share
                    IconButton(onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_SUBJECT, doc.title)
                            putExtra(Intent.EXTRA_TEXT, doc.markdownContent)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Markdown"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Document"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // View Mode Selector Tab Bar (Rendered vs Raw Markdown)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp)
                ) {
                    Row {
                        TabPill(
                            title = "Rendered View",
                            icon = Icons.Default.Visibility,
                            isSelected = viewerTab == ViewerTab.RENDERED,
                            onClick = { viewModel.setViewerTab(ViewerTab.RENDERED) }
                        )
                        TabPill(
                            title = "Raw Markdown",
                            icon = Icons.Default.Code,
                            isSelected = viewerTab == ViewerTab.RAW_MARKDOWN,
                            onClick = { viewModel.setViewerTab(ViewerTab.RAW_MARKDOWN) }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (viewerTab) {
                    ViewerTab.RENDERED -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                DocumentSummaryCard(document = doc)
                            }
                            item {
                                MarkdownRenderer(markdown = doc.markdownContent)
                            }
                            item {
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }

                    ViewerTab.RAW_MARKDOWN -> {
                        Box(modifier = Modifier.padding(12.dp)) {
                            MarkdownRawEditor(
                                content = doc.markdownContent,
                                onContentChange = { newText ->
                                    viewModel.updateCurrentMarkdown(newText)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Sheets
    if (showStatsSheet) {
        DocumentStatsSheet(
            document = doc,
            onDismiss = { showStatsSheet = false }
        )
    }

    if (showAiSheet) {
        AiAssistSheet(
            markdown = doc.markdownContent,
            geminiService = viewModel.geminiService,
            onApplyPolishedMarkdown = { polished ->
                viewModel.updateCurrentMarkdown(polished)
            },
            onDismiss = { showAiSheet = false }
        )
    }
}

@Composable
fun TabPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DocumentSummaryCard(document: com.example.data.model.ConversionDocument) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormatBadge(format = document.sourceFormat)
                Column {
                    Text(
                        text = document.sourceFileName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${document.charCount} characters • ${document.wordCount} words",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Emerald500.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Converted",
                    color = Emerald400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
