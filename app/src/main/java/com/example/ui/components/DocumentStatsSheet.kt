package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversionDocument
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentStatsSheet(
    document: ConversionDocument,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Document Intelligence",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = document.sourceFileName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FormatBadge(format = document.sourceFormat)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Words",
                    value = "${document.wordCount}",
                    icon = Icons.Default.TextFields,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Reading Time",
                    value = "${document.readingTimeMinutes} min",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Characters",
                    value = "${document.charCount}",
                    icon = Icons.Default.FormatQuote,
                    modifier = Modifier.weight(1f)
                )
            }

            // Markdown Structure Stats
            val headingCount = document.markdownContent.lines().count { it.trim().startsWith("#") }
            val tableCount = document.markdownContent.lines().count { it.trim().startsWith("|") } / 3
            val codeCount = document.markdownContent.lines().count { it.trim().startsWith("```") } / 2

            Text(
                text = "Structure Breakdown",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(label = "Headings", value = "$headingCount found")
                    DetailRow(label = "Markdown Tables", value = "$tableCount detected")
                    DetailRow(label = "Code Blocks", value = "$codeCount blocks")
                    val fileSizeFormatted = if (document.fileSizeBytes > 1024 * 1024) {
                        String.format("%.2f MB", document.fileSizeBytes / (1024.0 * 1024.0))
                    } else {
                        String.format("%.1f KB", document.fileSizeBytes / 1024.0)
                    }
                    DetailRow(label = "Source File Size", value = fileSizeFormatted)
                    val dateFormatted = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(document.timestamp))
                    DetailRow(label = "Converted At", value = dateFormatted)
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Sky400,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}
