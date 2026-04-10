package com.example.wayang_detection.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wayang_detection.ui.theme.*

/**
 * Animated circular confidence ring showing detection confidence percentage.
 * Animates from 0 to target value on composition.
 */
@Composable
fun ConfidenceRing(
    confidence: Float,
    size: Dp = 80.dp,
    strokeWidth: Dp = 6.dp,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) confidence else 0f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 300),
        label = "confidence_ring"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val ringColor = when {
        confidence >= 0.8f -> Emerald
        confidence >= 0.5f -> Amber
        else -> Coral
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Background ring
            drawArc(
                color = BgElevated,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Progress ring
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        // Percentage text
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            color = ringColor,
            fontSize = if (size >= 80.dp) 16.sp else 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
