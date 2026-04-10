package com.example.wayang_detection.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wayang_detection.data.model.BoundingBox
import com.example.wayang_detection.data.model.WayangCharacter
import com.example.wayang_detection.data.repository.WayangRepository
import com.example.wayang_detection.ui.components.BoundingBoxOverlay
import com.example.wayang_detection.ui.components.ConfidenceRing
import com.example.wayang_detection.ui.components.TraitChip
import com.example.wayang_detection.ui.theme.*

/**
 * Result screen showing detected wayang with bounding box,
 * confidence ring, character info, and link to encyclopedia.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    characterId: String,
    confidence: Float,
    onBack: () -> Unit,
    onViewInEncyclopedia: (String) -> Unit
) {
    val character = remember { WayangRepository.getById(characterId) }

    if (character == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text("Karakter tidak ditemukan", color = TextSecondary)
        }
        return
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showBottomSheet by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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

            // Image with bounding box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = character.imageResId),
                    contentDescription = character.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                )

                // Bounding box overlay
                BoundingBoxOverlay(
                    boundingBoxes = listOf(
                        BoundingBox(0.1f, 0.1f, 0.9f, 0.9f) to character.name
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                )

                // Detection label badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgPrimary.copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${character.name}, ${(confidence * 100).toInt()}%",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Bottom Sheet with character info
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = BgElevated,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TextMuted)
                    )
                }
            ) {
                ResultSheetContent(
                    character = character,
                    confidence = confidence,
                    onViewInEncyclopedia = { onViewInEncyclopedia(characterId) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultSheetContent(
    character: WayangCharacter,
    confidence: Float,
    onViewInEncyclopedia: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header: name + confidence ring
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎭 ${character.name}",
                    color = GoldPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${character.group} · ${character.category.label}",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
            ConfidenceRing(confidence = confidence)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Trait chips
        if (character.traits.isNotEmpty()) {
            Text(
                text = "Sifat & Karakter",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                character.traits.forEach { trait ->
                    TraitChip(trait = trait)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Description
        Text(
            text = "Tentang ${character.name}",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = character.description,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        if (character.philosophy.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldGlow)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "✨ Filosofi",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = character.philosophy,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Link to encyclopedia
        OutlinedButton(
            onClick = onViewInEncyclopedia,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = GoldPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = GoldPrimary.copy(alpha = 0.5f)
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lihat di Ensiklopedia", fontSize = 14.sp)
        }
    }
}
