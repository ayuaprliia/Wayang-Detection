package com.example.wayang_detection.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wayang_detection.data.model.BoundingBox
import com.example.wayang_detection.data.model.DetectionResult
import com.example.wayang_detection.data.model.DetectionState
import com.example.wayang_detection.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for detection process.
 * Currently uses mock detection — replace performMockDetection()
 * with real TFLite inference later.
 */
class DetectionViewModel : ViewModel() {

    private val _detectionState = MutableStateFlow<DetectionState>(DetectionState.Idle)
    val detectionState: StateFlow<DetectionState> = _detectionState.asStateFlow()

    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private val _inferenceTimeMs = MutableStateFlow(0L)
    val inferenceTimeMs: StateFlow<Long> = _inferenceTimeMs.asStateFlow()

    /**
     * Simulates wayang detection with mock data.
     * TODO: Replace with actual TFLite inference.
     */
    fun performMockDetection() {
        viewModelScope.launch {
            _detectionState.value = DetectionState.Scanning

            // Simulate inference delay
            delay(1500L)

            val mockResults = listOf(
                DetectionResult(
                    characterId = "bima",
                    characterName = "Bima",
                    confidence = 0.952f,
                    boundingBox = BoundingBox(
                        left = 0.15f,
                        top = 0.18f,
                        right = 0.85f,
                        bottom = 0.82f
                    )
                )
            )

            _detectionState.value = DetectionState.Found(mockResults)
            _fps.value = Constants.MOCK_FPS
            _inferenceTimeMs.value = Constants.MOCK_INFERENCE_TIME_MS
        }
    }

    fun resetDetection() {
        _detectionState.value = DetectionState.Idle
        _fps.value = 0
        _inferenceTimeMs.value = 0L
    }
}
