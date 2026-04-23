package com.example.wayang_detection.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wayang_detection.ui.theme.Emerald

/**
 * Developer mode overlay showing real-time performance metrics.
 * Displayed at the top of DetectionScreen when dev mode is enabled.
 */
@Composable
fun DevModeOverlay(
    isVisible: Boolean,
    fps: Int,
    inferenceTimeMs: Long,
    resolution: Int,
    threshold: Float,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "DEV  ●  ⏱ ${inferenceTimeMs}ms  │  📐 ${resolution}²  │  Threshold: ${(threshold * 100).toInt()}%",
                color = Emerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}
