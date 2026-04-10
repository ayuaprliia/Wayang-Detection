package com.example.wayang_detection.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wayang_detection.data.model.WayangCategory
import com.example.wayang_detection.data.model.WayangCharacter
import com.example.wayang_detection.data.repository.WayangRepository
import com.example.wayang_detection.ui.components.TraitChip
import com.example.wayang_detection.ui.theme.*

/**
 * Full character detail screen with hero image, info sections,
 * traits, description, philosophy, and visual traits.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CharacterDetailScreen(
    characterId: String,
    onBack: () -> Unit
) {
    val character = remember { WayangRepository.getById(characterId) }
    val scrollState = rememberScrollState()

    if (character == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPrimary),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Karakter tidak ditemukan", color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBack) {
                    Text("Kembali", color = GoldPrimary)
                }
            }
        }
        return
    }

    val categoryColor = when (character.category) {
        WayangCategory.DEWA -> DewaColor
        WayangCategory.PROTAGONIS -> ProtagColor
        WayangCategory.ANTAGONIS -> AntagColor
        WayangCategory.PUNAKAWAN -> PunaColor
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
                text = character.name,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Hero image with parallax
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .graphicsLayer {
                        translationY = scrollState.value * 0.3f
                    }
            ) {
                Image(
                    painter = painterResource(id = character.imageResId),
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                )

                // Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    androidx.compose.ui.graphics.Color.Transparent,
                                    BgPrimary
                                )
                            )
                        )
                )
            }

            // Content sections
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                // Name & alias
                Text(
                    text = character.name,
                    color = GoldPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                if (character.aliases.isNotEmpty()) {
                    Text(
                        text = character.aliases.joinToString(" · "),
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = character.category.label,
                        color = categoryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Info table card
                InfoSection(character)

                Spacer(modifier = Modifier.height(20.dp))

                // Traits
                if (character.traits.isNotEmpty()) {
                    SectionHeader("💎 Sifat & Karakter")
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
                if (character.description.isNotEmpty()) {
                    SectionHeader("📖 Kisah & Cerita")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = character.description,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }

                // Philosophy
                if (character.philosophy.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldGlow)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "✨ Filosofi",
                                color = GoldPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = character.philosophy,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Visual traits
                if (character.visualTraits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader("🎨 Ciri Visual")
                    Spacer(modifier = Modifier.height(8.dp))
                    character.visualTraits.forEach { trait ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "●",
                                color = GoldPrimary,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(top = 5.dp, end = 8.dp)
                            )
                            Text(
                                text = trait,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InfoSection(character: WayangCharacter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📋 Informasi Dasar",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow("Peran", character.category.label)
            InfoRow("Kelompok", character.group)
            if (character.aliases.isNotEmpty()) {
                InfoRow("Alias", character.aliases.joinToString(", "))
            }
            InfoRow("ID Model", character.modelClassId.toString())
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
}
