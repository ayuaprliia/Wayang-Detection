package com.example.wayang_detection.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wayang_detection.ui.theme.GoldPrimary

/**
 * Gold-bordered chip for displaying character traits.
 * Used in ResultScreen and CharacterDetailScreen.
 */
@Composable
fun TraitChip(
    trait: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = trait,
        color = GoldPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .border(
                width = 1.dp,
                color = GoldPrimary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
