package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun FormatBadge(
    format: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (format.uppercase()) {
        "PDF" -> Rose500.copy(alpha = 0.15f) to Rose500
        "DOCX", "DOC" -> Indigo500.copy(alpha = 0.15f) to Indigo500
        "XLSX", "XLS", "CSV", "TSV" -> Emerald500.copy(alpha = 0.15f) to Emerald500
        "PPTX", "PPT" -> Amber500.copy(alpha = 0.15f) to Amber500
        "HTML", "HTM", "URL", "WEB" -> Sky500.copy(alpha = 0.15f) to Sky500
        "JSON", "XML" -> Purple500.copy(alpha = 0.15f) to Purple500
        "IMAGE", "PNG", "JPG" -> Color(0xFF14B8A6).copy(alpha = 0.15f) to Color(0xFF14B8A6)
        "AUDIO", "MP3", "WAV" -> Color(0xFF8B5CF6).copy(alpha = 0.15f) to Color(0xFF8B5CF6)
        "ZIP" -> Color(0xFFEAB308).copy(alpha = 0.15f) to Color(0xFFEAB308)
        else -> Slate400.copy(alpha = 0.15f) to Slate400
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = format.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
