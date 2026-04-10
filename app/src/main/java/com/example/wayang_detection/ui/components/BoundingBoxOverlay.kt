package com.example.wayang_detection.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.wayang_detection.data.model.BoundingBox
import com.example.wayang_detection.ui.theme.GoldPrimary

/**
 * Canvas overlay drawing golden bounding boxes for detected wayang.
 * Features an animated pulse effect on the border.
 */
@Composable
fun BoundingBoxOverlay(
    boundingBoxes: List<Pair<BoundingBox, String>>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "box_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        boundingBoxes.forEach { (box, label) ->
            val rect = Rect(
                left = box.left * canvasWidth,
                top = box.top * canvasHeight,
                right = box.right * canvasWidth,
                bottom = box.bottom * canvasHeight
            )

            // Outer glow
            drawRect(
                color = GoldPrimary.copy(alpha = pulseAlpha * 0.15f),
                topLeft = Offset(rect.left - 4f, rect.top - 4f),
                size = androidx.compose.ui.geometry.Size(
                    rect.width + 8f,
                    rect.height + 8f
                )
            )

            // Main bounding box border
            drawRect(
                color = GoldPrimary.copy(alpha = pulseAlpha),
                topLeft = Offset(rect.left, rect.top),
                size = androidx.compose.ui.geometry.Size(rect.width, rect.height),
                style = Stroke(
                    width = 3f,
                    pathEffect = PathEffect.cornerPathEffect(8f)
                )
            )

            // Corner brackets (top-left)
            val cornerLen = minOf(rect.width, rect.height) * 0.15f
            val bracketStroke = Stroke(width = 5f)
            val bracketColor = Color.White.copy(alpha = pulseAlpha)

            // Top-left
            drawLine(bracketColor, Offset(rect.left, rect.top), Offset(rect.left + cornerLen, rect.top), strokeWidth = bracketStroke.width)
            drawLine(bracketColor, Offset(rect.left, rect.top), Offset(rect.left, rect.top + cornerLen), strokeWidth = bracketStroke.width)
            // Top-right
            drawLine(bracketColor, Offset(rect.right, rect.top), Offset(rect.right - cornerLen, rect.top), strokeWidth = bracketStroke.width)
            drawLine(bracketColor, Offset(rect.right, rect.top), Offset(rect.right, rect.top + cornerLen), strokeWidth = bracketStroke.width)
            // Bottom-left
            drawLine(bracketColor, Offset(rect.left, rect.bottom), Offset(rect.left + cornerLen, rect.bottom), strokeWidth = bracketStroke.width)
            drawLine(bracketColor, Offset(rect.left, rect.bottom), Offset(rect.left, rect.bottom - cornerLen), strokeWidth = bracketStroke.width)
            // Bottom-right
            drawLine(bracketColor, Offset(rect.right, rect.bottom), Offset(rect.right - cornerLen, rect.bottom), strokeWidth = bracketStroke.width)
            drawLine(bracketColor, Offset(rect.right, rect.bottom), Offset(rect.right, rect.bottom - cornerLen), strokeWidth = bracketStroke.width)
        }
    }
}
