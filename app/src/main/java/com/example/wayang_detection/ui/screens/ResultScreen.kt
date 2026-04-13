package com.example.wayang_detection.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import com.example.wayang_detection.ui.theme.*

/**
 * Result screen showing ALL detected wayang characters.
 * Each detection result is displayed as a separate card with
 * confidence ring, traits, description, philosophy, and encyclopedia link.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    detectionResults: List<DetectionResult>,
    onBack: () -> Unit,
    onViewInEncyclopedia: (String) -> Unit
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

            detectionResults.forEach { result ->
                val character = remember(result.characterId) {
                    WayangRepository.getById(result.characterId)
                }
                if (character != null) {
                    ResultItemCard(
                        result = result,
                        character = character,
                        onViewInEncyclopedia = { onViewInEncyclopedia(result.characterId) }
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
 * philosophy excerpt, and link to encyclopedia detail.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultItemCard(
    result: DetectionResult,
    character: WayangCharacter,
    onViewInEncyclopedia: () -> Unit
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
