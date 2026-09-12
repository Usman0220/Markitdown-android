package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ConversionDocument
import com.example.sample.SampleDocuments
import com.example.ui.components.FormatBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarkItDownViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MarkItDownViewModel,
    onNavigateToViewer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recentDocuments by viewModel.allDocuments.collectAsState()
    val isConverting by viewModel.isConverting.collectAsState()
    val progressMessage by viewModel.conversionProgress.collectAsState()

    var showUrlDialog by remember { mutableStateOf(false) }
    var showPasteDialog by remember { mutableStateOf(false) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileName(context, uri)
            viewModel.convertUri(uri, fileName) {
                onNavigateToViewer()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(listOf(Sky500, Indigo600))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "M↓",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "MarkItDown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    FormatBadge(format = "v1.0", modifier = Modifier.padding(end = 12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Card
            item {
                HeroBannerCard()
            }

            // Converting indicator if active
            if (isConverting) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Sky500.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Sky400,
                                strokeWidth = 2.5.dp
                            )
                            Column {
                                Text(
                                    text = "Converting Document...",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = progressMessage,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Primary Conversion Action Hub
            item {
                Text(
                    text = "Convert to Markdown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "Pick File",
                        subtitle = "PDF, Office, Data, Zip",
                        icon = Icons.Default.UploadFile,
                        accentColor = Sky400,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            filePickerLauncher.launch(arrayOf("*/*"))
                        }
                    )

                    ActionTile(
                        title = "Web URL",
                        subtitle = "Clean article to MD",
                        icon = Icons.Default.Language,
                        accentColor = Indigo400,
                        modifier = Modifier.weight(1f),
                        onClick = { showUrlDialog = true }
                    )

                    ActionTile(
                        title = "Paste Text",
                        subtitle = "HTML, CSV, JSON",
                        icon = Icons.Default.ContentPaste,
                        accentColor = Emerald400,
                        modifier = Modifier.weight(1f),
                        onClick = { showPasteDialog = true }
                    )
                }
            }

            // Format Matrix Carousel
            item {
                Text(
                    text = "Supported Formats",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                val formats = listOf(
                    FormatChipInfo("PDF", "Adobe Acrobat", Rose500, Icons.Default.PictureAsPdf),
                    FormatChipInfo("DOCX", "Word Document", Indigo500, Icons.Default.Description),
                    FormatChipInfo("XLSX", "Excel Sheets", Emerald500, Icons.Default.TableChart),
                    FormatChipInfo("PPTX", "PowerPoint", Amber500, Icons.Default.Slideshow),
                    FormatChipInfo("HTML", "Web Pages", Sky500, Icons.Default.Html),
                    FormatChipInfo("CSV", "Data Tables", Emerald600, Icons.Default.GridOn),
                    FormatChipInfo("JSON", "API Data", Purple500, Icons.Default.Code),
                    FormatChipInfo("ZIP", "Archives", Color(0xFFEAB308), Icons.Default.FolderZip),
                    FormatChipInfo("IMAGE", "Vision OCR", Color(0xFF14B8A6), Icons.Default.Image),
                    FormatChipInfo("AUDIO", "Speech to Text", Color(0xFF8B5CF6), Icons.Default.Mic)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(formats) { f ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(f.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = f.icon,
                                        contentDescription = f.tag,
                                        tint = f.color,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = f.tag,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = f.desc,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Samples to Test MarkItDown
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sample Documents",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "1-tap demo",
                        fontSize = 12.sp,
                        color = Sky400
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val samples = remember { SampleDocuments.getSamples() }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    samples.forEach { sample ->
                        SampleDocumentItem(
                            sample = sample,
                            onClick = {
                                viewModel.loadSample(sample) {
                                    onNavigateToViewer()
                                }
                            }
                        )
                    }
                }
            }

            // Recent Conversions
            if (recentDocuments.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Conversions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentDocuments.take(5).forEach { doc ->
                            RecentDocumentCard(
                                document = doc,
                                onClick = {
                                    viewModel.selectDocument(doc)
                                    onNavigateToViewer()
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog: URL to Markdown
    if (showUrlDialog) {
        UrlInputDialog(
            onDismiss = { showUrlDialog = false },
            onConfirm = { url ->
                showUrlDialog = false
                viewModel.convertUrl(url) {
                    onNavigateToViewer()
                }
            }
        )
    }

    // Dialog: Paste Text to Markdown
    if (showPasteDialog) {
        PasteTextDialog(
            onDismiss = { showPasteDialog = false },
            onConfirm = { text, format, title ->
                showPasteDialog = false
                viewModel.convertRawText(text, format, title) {
                    onNavigateToViewer()
                }
            }
        )
    }
}

@Composable
fun HeroBannerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Optional hero banner background asset
            Image(
                painter = painterResource(id = R.drawable.markitdown_hero_banner_1789180433513),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            // Gradient scrim overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x990F172A),
                                Color(0xF00F172A)
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = "Universal Document Converter",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Convert Office docs, spreadsheets, slides, PDFs, HTML, images & audio into clean Markdown.",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SampleDocumentItem(
    sample: ConversionDocument,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormatBadge(format = sample.sourceFormat)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sample.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${sample.sourceFileName} • ${sample.wordCount} words",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Test Sample",
                tint = Sky400,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun RecentDocumentCard(
    document: ConversionDocument,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormatBadge(format = document.sourceFormat)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(document.timestamp))
                Text(
                    text = "$dateStr • ${document.wordCount} words",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (document.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = Amber500,
                    modifier = Modifier.size(16.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun UrlInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember { mutableStateOf("https://") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Web URL to Markdown") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enter any web page or article URL to convert its content into clean Markdown:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://example.com/article") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.isNotBlank()) onConfirm(url)
                }
            ) {
                Text("Fetch & Convert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PasteTextDialog(
    onDismiss: () -> Unit,
    onConfirm: (text: String, format: String, title: String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("Pasted Document") }
    var selectedFormat by remember { mutableStateOf("AUTO") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paste Content to Markdown") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Format selector chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("AUTO", "HTML", "CSV", "JSON", "XML", "TEXT").forEach { fmt ->
                        FilterChip(
                            selected = selectedFormat == fmt,
                            onClick = { selectedFormat = fmt },
                            label = { Text(fmt, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Paste HTML, CSV rows, JSON payload, or raw text here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) onConfirm(text, selectedFormat, title)
                }
            ) {
                Text("Convert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class FormatChipInfo(
    val tag: String,
    val desc: String,
    val color: Color,
    val icon: ImageVector
)

private fun getFileName(context: android.content.Context, uri: Uri): String {
    var name = "document"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                name = it.getString(nameIndex) ?: "document"
            }
        }
    }
    return name
}
