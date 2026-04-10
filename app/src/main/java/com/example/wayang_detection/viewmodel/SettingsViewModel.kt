package com.example.wayang_detection.viewmodel

import androidx.lifecycle.ViewModel
import com.example.wayang_detection.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for app-wide settings: dev mode, confidence threshold, resolution.
 */
class SettingsViewModel : ViewModel() {

    private val _devModeEnabled = MutableStateFlow(false)
    val devModeEnabled: StateFlow<Boolean> = _devModeEnabled.asStateFlow()

    private val _confidenceThreshold = MutableStateFlow(Constants.DEFAULT_CONFIDENCE_THRESHOLD)
    val confidenceThreshold: StateFlow<Float> = _confidenceThreshold.asStateFlow()

    private val _inputResolution = MutableStateFlow(Constants.DEFAULT_INPUT_RESOLUTION)
    val inputResolution: StateFlow<Int> = _inputResolution.asStateFlow()

    fun toggleDevMode() {
        _devModeEnabled.value = !_devModeEnabled.value
    }

    fun setConfidenceThreshold(value: Float) {
        _confidenceThreshold.value = value.coerceIn(0.1f, 1.0f)
    }

    fun setInputResolution(value: Int) {
        _inputResolution.value = value
    }
}
