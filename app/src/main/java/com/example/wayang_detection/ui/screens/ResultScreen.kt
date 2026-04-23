package com.example.wayang_detection.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wayang_detection.data.model.DetectionResult
import com.example.wayang_detection.data.model.WayangCategory
import com.example.wayang_detection.data.model.WayangCharacter
import com.example.wayang_detection.data.repository.WayangRepository
import com.example.wayang_detection.ui.components.ConfidenceRing
import com.example.wayang_detection.ui.components.TraitChip
import com.example.wayang_detection.ui.components.BoundingBoxOverlay
import com.example.wayang_detection.ui.theme.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale

/**
 * Result screen showing ALL detected wayang characters.
 * Each detection result is displayed as a separate card with
 * confidence ring, traits, description, philosophy, encyclopedia link,
 * and AI-powered elaboration button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    detectionResults: List<DetectionResult>,
    capturedImage: android.graphics.Bitmap?,
    onBack: () -> Unit,
    onViewInEncyclopedia: (String) -> Unit,
    onAskAiElaborate: (String) -> Unit,
    aiResponse: String?,
    aiLoading: Boolean,
    aiError: String?,
    onClearAiResponse: () -> Unit
) {
    if (detectionResults.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPrimary),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tidak ada hasil deteksi",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onBack) {
                    Text("Kembali", color = GoldPrimary)
                }
            }
        }
        return
    }

    // Track which character the AI is elaborating on
    var aiTargetCharacterId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Hasil Deteksi",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Summary header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "🎭", fontSize = 24.sp)
            Text(
                text = "${detectionResults.size} Karakter Terdeteksi",
                color = GoldPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Scrollable list of result cards
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            if (capturedImage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        bitmap = capturedImage.asImageBitmap(),
                        contentDescription = "Annotated Image",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                    BoundingBoxOverlay(
                        boundingBoxes = detectionResults.map { it.boundingBox to it.characterName },
                        modifier = Modifier.matchParentSize()
                    )

                    // Detection labels positioned near each bounding box
                    BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                        detectionResults.forEach { result ->
                            val labelX = maxWidth * result.boundingBox.left
                            val rawY = maxHeight * result.boundingBox.top - 28.dp
                            val labelY = if (rawY < 0.dp) 0.dp else rawY

                            Box(
                                modifier = Modifier
                                    .offset(x = labelX, y = labelY)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BgPrimary.copy(alpha = 0.85f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${result.characterName} ${(result.confidence * 100).toInt()}%",
                                    color = GoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            detectionResults.forEach { result ->
                val character = remember(result.characterId) {
                    WayangRepository.getById(result.characterId)
                }
                if (character != null) {
                    ResultItemCard(
                        result = result,
                        character = character,
                        onViewInEncyclopedia = { onViewInEncyclopedia(result.characterId) },
                        onAskAi = {
                            aiTargetCharacterId = result.characterId
                            onClearAiResponse()
                            onAskAiElaborate(result.characterId)
                        },
                        aiResponse = if (aiTargetCharacterId == result.characterId) aiResponse else null,
                        aiLoading = if (aiTargetCharacterId == result.characterId) aiLoading else false,
                        aiError = if (aiTargetCharacterId == result.characterId) aiError else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Individual result card for a single detected wayang character.
 * Shows confidence ring, category badge, traits, description,
 * philosophy excerpt, AI elaboration, and link to encyclopedia detail.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultItemCard(
    result: DetectionResult,
    character: WayangCharacter,
    onViewInEncyclopedia: () -> Unit,
    onAskAi: () -> Unit,
    aiResponse: String?,
    aiLoading: Boolean,
    aiError: String?
) {
    val categoryColor = when (character.category) {
        WayangCategory.DEWA -> DewaColor
        WayangCategory.PROTAGONIS -> ProtagColor
        WayangCategory.ANTAGONIS -> AntagColor
        WayangCategory.PUNAKAWAN -> PunaColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: name + confidence ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name,
                        color = GoldPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${character.group} · ${character.category.label}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                ConfidenceRing(
                    confidence = result.confidence,
                    size = 56.dp,
                    strokeWidth = 5.dp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(categoryColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = character.category.label,
                    color = categoryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trait chips
            if (character.traits.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    character.traits.forEach { trait ->
                        TraitChip(trait = trait)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Description
            Text(
                text = "Tentang ${character.name}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = character.description,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )

            // Philosophy highlight
            if (character.philosophy.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldGlow)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "✨ Filosofi",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = character.philosophy,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── AI Elaboration Section ──
            Button(
                onClick = onAskAi,
                modifier = Modifier.fillMaxWidth(),
                enabled = !aiLoading,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Indigo.copy(alpha = 0.15f),
                    contentColor = Indigo
                )
            ) {
                if (aiLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Indigo,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI sedang berpikir...", fontSize = 13.sp)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("✨ Jelaskan Lebih Lanjut dengan AI", fontSize = 13.sp)
                }
            }

            // AI Response
            AnimatedVisibility(
                visible = aiResponse != null || aiError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (aiError != null) Coral.copy(alpha = 0.1f)
                                else Indigo.copy(alpha = 0.08f)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (aiError != null) Coral else Indigo,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (aiError != null) "Error" else "Penjelasan AI",
                                    color = if (aiError != null) Coral else Indigo,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = aiError ?: aiResponse ?: "",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Link to encyclopedia
            OutlinedButton(
                onClick = onViewInEncyclopedia,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GoldPrimary
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = GoldPrimary.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lihat di Ensiklopedia", fontSize = 13.sp)
            }
        }
    }
}
