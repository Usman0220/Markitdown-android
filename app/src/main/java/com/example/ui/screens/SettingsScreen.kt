package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FormatBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarkItDownViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MarkItDownViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isGeminiAvailable = remember { viewModel.geminiService.isAvailable() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Engine & Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Engine Health Status Section
            Text(
                text = "Conversion Engine Status",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            StatusCard(
                title = "Offline Parsing Engine",
                subtitle = "DOCX, XLSX, PPTX, HTML, CSV, JSON, XML, ZIP",
                status = "Ready • 100% Offline",
                statusColor = Emerald500,
                icon = Icons.Default.CheckCircle
            )

            StatusCard(
                title = "Gemini Multimodal AI",
                subtitle = "Vision OCR, Speech-to-Text & Smart Summaries",
                status = if (isGeminiAvailable) "Connected (gemini-3.5-flash)" else "Key not provided in Secrets",
                statusColor = if (isGeminiAvailable) Emerald500 else Amber500,
                icon = if (isGeminiAvailable) Icons.Default.AutoAwesome else Icons.Default.Key
            )

            StatusCard(
                title = "Local Document Storage",
                subtitle = "Room Persistence with search & offline indexing",
                status = "Active",
                statusColor = Emerald500,
                icon = Icons.Default.Storage
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Conversion Capabilities Table
            Text(
                text = "Format Capabilities",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CapabilityItem(
                        format = "DOCX",
                        desc = "Extracts headings, styles, bold/italic, lists, and tables"
                    )
                    CapabilityItem(
                        format = "XLSX",
                        desc = "Extracts multi-sheet workbooks into structured Markdown tables"
                    )
                    CapabilityItem(
                        format = "PPTX",
                        desc = "Parses slide titles, bullet points, and speaker notes"
                    )
                    CapabilityItem(
                        format = "PDF",
                        desc = "Native text stream extraction + Gemini vision OCR"
                    )
                    CapabilityItem(
                        format = "HTML / URL",
                        desc = "Strips boilerplate and converts tags into GitHub-flavored Markdown"
                    )
                    CapabilityItem(
                        format = "CSV / TSV",
                        desc = "Auto-detects delimiters and builds formatted table grids"
                    )
                    CapabilityItem(
                        format = "JSON / XML",
                        desc = "Syntax indented codeblocks with summary schema tables"
                    )
                    CapabilityItem(
                        format = "ZIP",
                        desc = "Unpacks archives and generates a multi-file Markdown digest"
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Microsoft MarkItDown Attribution & Info
            Text(
                text = "About",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/microsoft/markitdown")
                        )
                        context.startActivity(browserIntent)
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Sky500.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "GitHub",
                            tint = Sky400
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "microsoft/markitdown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Based on Microsoft's universal Python conversion utility. Re-engineered natively for Android with Kotlin & Jetpack Compose.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = "Version 1.0.0 • MarkItDown Android Edition",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    subtitle: String,
    status: String,
    statusColor: Color,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
fun CapabilityItem(format: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FormatBadge(format = format)
        Text(
            text = desc,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
