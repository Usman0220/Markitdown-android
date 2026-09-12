package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversionDocument
import com.example.ui.components.FormatBadge
import com.example.ui.theme.Amber500
import com.example.ui.theme.Rose500
import com.example.ui.theme.Sky400
import com.example.ui.viewmodel.MarkItDownViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MarkItDownViewModel,
    onNavigateToViewer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allDocs by viewModel.allDocuments.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()
    val formatFilter by viewModel.formatFilter.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }
    var docToDelete by remember { mutableStateOf<ConversionDocument?>(null) }

    // Filter documents
    val filteredDocs = remember(allDocs, searchQuery, formatFilter) {
        allDocs.filter { doc ->
            val matchesSearch = searchQuery.isBlank() ||
                    doc.title.contains(searchQuery, ignoreCase = true) ||
                    doc.sourceFileName.contains(searchQuery, ignoreCase = true) ||
                    doc.markdownContent.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (formatFilter) {
                "ALL" -> true
                "FAVORITES" -> doc.isFavorite
                "OFFICE" -> doc.sourceFormat in listOf("DOCX", "XLSX", "PPTX")
                "PDF" -> doc.sourceFormat == "PDF"
                "WEB" -> doc.sourceFormat in listOf("HTML", "URL")
                "DATA" -> doc.sourceFormat in listOf("CSV", "JSON", "XML")
                "MEDIA" -> doc.sourceFormat in listOf("IMAGE", "AUDIO")
                else -> doc.sourceFormat.equals(formatFilter, ignoreCase = true)
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Conversion Vault",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    if (allDocs.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                .padding(horizontal = 16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setHistorySearchQuery(it) },
                placeholder = { Text("Search title, filename, or markdown content...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            val filters = listOf("ALL", "FAVORITES", "OFFICE", "PDF", "WEB", "DATA", "MEDIA")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = formatFilter == filter,
                        onClick = { viewModel.setFormatFilter(filter) },
                        label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Document List
            if (filteredDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank() || formatFilter != "ALL") "No matching documents" else "No conversion history yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Convert files, paste text, or load samples to build your archive.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredDocs, key = { it.id }) { doc ->
                        HistoryDocumentCard(
                            document = doc,
                            onClick = {
                                viewModel.selectDocument(doc)
                                onNavigateToViewer()
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(doc) },
                            onDelete = { docToDelete = doc }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Delete single item confirmation
    if (docToDelete != null) {
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            title = { Text("Delete Document?") },
            text = { Text("Are you sure you want to delete '${docToDelete?.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        docToDelete?.let { viewModel.deleteDocument(it) }
                        docToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear all history confirmation
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History?") },
            text = { Text("Are you sure you want to remove all converted documents from history?") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDialog = false
                        viewModel.clearHistory()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HistoryDocumentCard(
    document: ConversionDocument,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FormatBadge(format = document.sourceFormat)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(document.timestamp))
                Text(
                    text = "$dateStr • ${document.wordCount} words",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (document.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Favorite",
                    tint = if (document.isFavorite) Amber500 else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
