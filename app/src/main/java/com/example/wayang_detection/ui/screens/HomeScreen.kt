package com.example.wayang_detection.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wayang_detection.ui.components.ActionCard
import com.example.wayang_detection.ui.components.SettingsDialog
import com.example.wayang_detection.ui.theme.*
import com.example.wayang_detection.util.TimeUtils

/**
 * Home / Dashboard screen with two main action cards (camera + gallery),
 * tips card, and settings access.
 */
@Composable
fun HomeScreen(
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    devModeEnabled: Boolean,
    onDevModeToggle: () -> Unit,
    confidenceThreshold: Float,
    onConfidenceChange: (Float) -> Unit,
    inputResolution: Int,
    onResolutionChange: (Int) -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    var showTip by remember { mutableStateOf(true) }

    // Settings Dialog
    SettingsDialog(
        isVisible = showSettings,
        onDismiss = { showSettings = false },
        devModeEnabled = devModeEnabled,
        onDevModeToggle = onDevModeToggle,
        confidenceThreshold = confidenceThreshold,
        onConfidenceChange = onConfidenceChange,
        inputResolution = inputResolution,
        onResolutionChange = onResolutionChange
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WayangVision",
                color = GoldPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showSettings = true }) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Pengaturan",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Greeting
        Text(
            text = "${TimeUtils.getGreeting()}! ${TimeUtils.getGreetingEmoji()}",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Deteksi wayang kulit Bali dengan kecerdasan buatan",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action Cards
        ActionCard(
            title = "Buka Kamera",
            subtitle = "Deteksi real-time langsung",
            icon = Icons.Rounded.CameraAlt,
            gradientColors = listOf(
                GoldDark,
                GoldPrimary.copy(alpha = 0.8f),
                GoldPrimary
            ),
            onClick = onOpenCamera
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionCard(
            title = "Pilih dari Galeri",
            subtitle = "Upload foto wayang",
            icon = Icons.Rounded.PhotoLibrary,
            gradientColors = listOf(
                Indigo.copy(alpha = 0.8f),
                Indigo,
                Indigo.copy(alpha = 0.9f)
            ),
            onClick = onOpenGallery
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tips Card (dismissable)
        AnimatedVisibility(
            visible = showTip,
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgElevated)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = Amber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tips Deteksi",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Arahkan kamera langsung ke wayang kulit dengan pencahayaan yang cukup untuk hasil deteksi terbaik.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                    IconButton(
                        onClick = { showTip = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Bottom nav space
    }
}
