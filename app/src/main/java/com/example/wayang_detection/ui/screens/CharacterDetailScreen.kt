package com.example.wayang_detection.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
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
 * traits, description, philosophy, visual traits, and AI Q&A.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CharacterDetailScreen(
    characterId: String,
    onBack: () -> Unit,
    onAskAi: (String, String) -> Unit, // (characterId, question)
    aiResponse: String?,
    aiLoading: Boolean,
    aiError: String?,
    onClearAiResponse: () -> Unit
) {
    val character = remember { WayangRepository.getById(characterId) }
    val scrollState = rememberScrollState()

    // AI question input
    var aiQuestion by remember { mutableStateOf("") }

    // Suggested questions
    val suggestedQuestions = remember(character?.name) {
        listOf(
            "Ceritakan kisah terkenal yang melibatkan ${character?.name ?: "karakter ini"}",
            "Apa makna spiritual ${character?.name ?: "karakter ini"} dalam kehidupan sehari-hari?",
            "Bagaimana ${character?.name ?: "karakter ini"} digambarkan dalam pertunjukan wayang?"
        )
    }

    // Clear AI response when entering screen
    LaunchedEffect(characterId) {
        onClearAiResponse()
    }

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
                    .height(320.dp)
                    .graphicsLayer {
                        translationY = scrollState.value * 0.3f
                    },
                contentAlignment = Alignment.Center
            ) {
                // Dark background behind image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(BgOverlay)
                )
                Image(
                    painter = painterResource(id = character.imageResId),
                    contentDescription = character.name,
                    contentScale = ContentScale.Fit,
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

                // ── AI Q&A Section ──
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Indigo.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Indigo,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🤖 Tanya AI",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Tanyakan apa saja tentang ${character.name} kepada AI",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Suggested questions as chips
                        Text(
                            text = "Pertanyaan yang disarankan:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        suggestedQuestions.forEach { question ->
                            TextButton(
                                onClick = {
                                    aiQuestion = question
                                    onAskAi(characterId, question)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !aiLoading,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "💬 $question",
                                    color = if (aiLoading) TextMuted else Indigo,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom question input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = aiQuestion,
                                onValueChange = { aiQuestion = it },
                                placeholder = {
                                    Text(
                                        "Ketik pertanyaanmu...",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                enabled = !aiLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Indigo,
                                    unfocusedBorderColor = TextMuted.copy(alpha = 0.3f),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (aiQuestion.isNotBlank()) {
                                        onAskAi(characterId, aiQuestion)
                                    }
                                },
                                enabled = aiQuestion.isNotBlank() && !aiLoading,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (aiQuestion.isNotBlank() && !aiLoading) Indigo
                                        else TextMuted.copy(alpha = 0.2f)
                                    )
                            ) {
                                if (aiLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = TextPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = "Kirim",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // AI Response display
                        AnimatedVisibility(
                            visible = aiResponse != null || aiError != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (aiError != null) Coral.copy(alpha = 0.1f)
                                            else BgElevated
                                        )
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.AutoAwesome,
                                                contentDescription = null,
                                                tint = if (aiError != null) Coral else Indigo,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (aiError != null) "Error" else "Jawaban AI",
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
