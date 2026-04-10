package com.example.wayang_detection.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wayang_detection.ui.theme.*
import com.example.wayang_detection.util.Constants

/**
 * Quick settings dialog accessed from the gear icon on Home screen.
 * Controls developer mode, confidence threshold, and input resolution.
 */
@Composable
fun SettingsDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    devModeEnabled: Boolean,
    onDevModeToggle: () -> Unit,
    confidenceThreshold: Float,
    onConfidenceChange: (Float) -> Unit,
    inputResolution: Int,
    onResolutionChange: (Int) -> Unit
) {
    if (!isVisible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = "⚙️ Pengaturan Cepat",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Developer Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Developer Mode",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Switch(
                        checked = devModeEnabled,
                        onCheckedChange = { onDevModeToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldPrimary,
                            checkedTrackColor = GoldDark,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = BgOverlay
                        )
                    )
                }

                HorizontalDivider(color = BgOverlay)

                // Confidence Threshold Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Confidence Threshold",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${(confidenceThreshold * 100).toInt()}%",
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = confidenceThreshold,
                        onValueChange = onConfidenceChange,
                        valueRange = 0.1f..1.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldPrimary,
                            activeTrackColor = GoldPrimary,
                            inactiveTrackColor = BgOverlay
                        )
                    )
                }

                HorizontalDivider(color = BgOverlay)

                // Input Resolution
                Column {
                    Text(
                        text = "Input Resolution",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Constants.RESOLUTION_OPTIONS.forEach { res ->
                            FilterChip(
                                selected = inputResolution == res,
                                onClick = { onResolutionChange(res) },
                                label = {
                                    Text(
                                        text = "${res}×${res}",
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = GoldPrimary,
                                    containerColor = BgOverlay,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(color = BgOverlay)

                // App Info
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Model", color = TextMuted, fontSize = 12.sp)
                        Text(Constants.MODEL_NAME, color = TextSecondary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Versi", color = TextMuted, fontSize = 12.sp)
                        Text(Constants.APP_VERSION, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = GoldPrimary)
            }
        }
    )
}
