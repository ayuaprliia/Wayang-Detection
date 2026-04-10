package com.example.wayang_detection.data.model

/**
 * Result from a single wayang detection inference.
 */
data class DetectionResult(
    val characterId: String,
    val characterName: String,
    val confidence: Float,
    val boundingBox: BoundingBox
)

/**
 * Normalized bounding box coordinates (0.0 to 1.0 range).
 * Relative to the image dimensions.
 */
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

/**
 * Sealed class representing the state of detection process.
 */
sealed class DetectionState {
    data object Idle : DetectionState()
    data object Scanning : DetectionState()
    data class Found(val results: List<DetectionResult>) : DetectionState()
    data class Error(val message: String) : DetectionState()
}
