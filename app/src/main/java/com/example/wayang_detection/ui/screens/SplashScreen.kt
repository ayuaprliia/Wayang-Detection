package com.example.wayang_detection.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wayang_detection.ui.theme.*
import com.example.wayang_detection.util.Constants
import kotlinx.coroutines.delay

/**
 * Splash screen with animated gunungan (kayon) logo and AI model loading progress.
 * Auto-navigates to Home after minimum display time.
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    // Simulate model loading progress
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Memuat Model AI...") }
    var titleVisible by remember { mutableStateOf(false) }

    // Glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "gunungan_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Progress and navigation
    LaunchedEffect(Unit) {
        delay(400)
        titleVisible = true

        // Simulate loading steps
        for (i in 1..100) {
            progress = i / 100f
            delay(Constants.SPLASH_MIN_DURATION_MS / 100)
        }
        statusText = "Siap!"
        delay(300)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(48.dp)
        ) {
            // Gunungan (Kayon) logo drawn with Canvas
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Glow background
                Canvas(modifier = Modifier.size(200.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldPrimary.copy(alpha = glowAlpha * 0.3f),
                                GoldPrimary.copy(alpha = glowAlpha * 0.1f),
                                GoldPrimary.copy(alpha = 0f)
                            ),
                            radius = size.minDimension / 1.5f
                        )
                    )
                }

                // Gunungan shape
                Canvas(modifier = Modifier.size(160.dp)) {
                    val w = size.width
                    val h = size.height

                    // Main triangular gunungan shape
                    val path = Path().apply {
                        moveTo(w / 2, h * 0.05f) // Top point
                        // Right side with curves
                        cubicTo(
                            w * 0.55f, h * 0.2f,
                            w * 0.75f, h * 0.4f,
                            w * 0.8f, h * 0.6f
                        )
                        cubicTo(
                            w * 0.85f, h * 0.75f,
                            w * 0.75f, h * 0.9f,
                            w * 0.65f, h * 0.95f
                        )
                        lineTo(w * 0.35f, h * 0.95f)
                        // Left side
                        cubicTo(
                            w * 0.25f, h * 0.9f,
                            w * 0.15f, h * 0.75f,
                            w * 0.2f, h * 0.6f
                        )
                        cubicTo(
                            w * 0.25f, h * 0.4f,
                            w * 0.45f, h * 0.2f,
                            w / 2, h * 0.05f
                        )
                        close()
                    }

                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GoldLight.copy(alpha = glowAlpha),
                                GoldPrimary.copy(alpha = glowAlpha),
                                GoldDark.copy(alpha = glowAlpha * 0.8f)
                            )
                        ),
                        style = Fill
                    )

                    // Inner decorative lines
                    val innerPath = Path().apply {
                        moveTo(w / 2, h * 0.15f)
                        lineTo(w / 2, h * 0.85f)
                    }
                    drawPath(
                        path = innerPath,
                        color = BgPrimary.copy(alpha = 0.3f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )

                    // Horizontal ornament lines
                    for (i in 1..4) {
                        val y = h * (0.25f + i * 0.12f)
                        val spread = (i * 0.06f)
                        drawLine(
                            color = BgPrimary.copy(alpha = 0.2f),
                            start = Offset(w / 2 - w * spread, y),
                            end = Offset(w / 2 + w * spread, y),
                            strokeWidth = 1.5f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App title
            if (titleVisible) {
                Text(
                    text = "WayangVision",
                    color = TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Deteksi Wayang Kulit Bali",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = GoldPrimary,
                trackColor = BgElevated,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = statusText,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}
