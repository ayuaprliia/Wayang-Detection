package com.example.wayang_detection.util

/**
 * App-wide constants for WayangVision.
 */
object Constants {
    const val DEFAULT_CONFIDENCE_THRESHOLD = 0.5f
    const val DEFAULT_INPUT_RESOLUTION = 640
    const val SPLASH_MIN_DURATION_MS = 2500L
    const val MOCK_INFERENCE_TIME_MS = 45L
    const val MOCK_FPS = 28

    val RESOLUTION_OPTIONS = listOf(320, 416, 640)

    const val APP_VERSION = "v1.0.0"
    const val MODEL_NAME = "YOLOv11n-TFLite"
}
