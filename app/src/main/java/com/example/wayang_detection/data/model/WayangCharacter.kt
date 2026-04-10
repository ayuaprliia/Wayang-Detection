    package com.example.wayang_detection.data.model

import androidx.annotation.DrawableRes

/**
 * Data class representing a Balinese Wayang Kulit character.
 * Used in both the Encyclopedia feature and Detection results.
 */
data class WayangCharacter(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val category: WayangCategory,
    val group: String,
    val traits: List<String> = emptyList(),
    val description: String = "",
    val philosophy: String = "",
    val visualTraits: List<String> = emptyList(),
    @DrawableRes val imageResId: Int,
    val modelClassId: Int
)

/**
 * Categories of Wayang Kulit characters in Balinese tradition.
 */
enum class WayangCategory(val label: String) {
    DEWA("Dewa"),
    PROTAGONIS("Protagonis"),
    ANTAGONIS("Antagonis"),
    PUNAKAWAN("Punakawan")
}
