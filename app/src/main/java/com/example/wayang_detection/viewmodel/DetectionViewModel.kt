package com.example.wayang_detection.viewmodel

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wayang_detection.data.model.DetectionResult
import com.example.wayang_detection.data.model.DetectionState
import com.example.wayang_detection.detector.DetectionSmoother
import com.example.wayang_detection.detector.ImageProcessor
import com.example.wayang_detection.detector.WayangDetector
import com.example.wayang_detection.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ViewModel for wayang detection using YOLOv11 TFLite model.
 * Supports real-time camera detection and single-shot gallery detection.
 *
 * Integrates DetectionSmoother for temporal stability:
 * - EMA bounding box smoothing (reduces jitter)
 * - IoU-based tracking (reduces flickering)
 * - Class voting (reduces misclassification flicker)
 */
class DetectionViewModel(application: Application) : AndroidViewModel(application) {

    private val detector = WayangDetector(application)
    private val smoother = DetectionSmoother()
    private val isProcessing = AtomicBoolean(false)

    /** When true, camera frames are skipped (gallery detection in progress). */
    @Volatile
    var skipFrameProcessing = false
        private set

    /** Current confidence threshold from SettingsViewModel. */
    @Volatile
    var confidenceThreshold = Constants.DEFAULT_CONFIDENCE_THRESHOLD

    private val _detectionState = MutableStateFlow<DetectionState>(DetectionState.Idle)
    val detectionState: StateFlow<DetectionState> = _detectionState.asStateFlow()

    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private val _inferenceTimeMs = MutableStateFlow(0L)
    val inferenceTimeMs: StateFlow<Long> = _inferenceTimeMs.asStateFlow()

    /** Live results for real-time camera overlay (updates every frame). */
    private val _liveResults = MutableStateFlow<List<DetectionResult>>(emptyList())
    val liveResults: StateFlow<List<DetectionResult>> = _liveResults.asStateFlow()

    /** Captured results frozen at moment of capture (for ResultScreen). */
    private val _capturedResults = MutableStateFlow<List<DetectionResult>>(emptyList())
    val capturedResults: StateFlow<List<DetectionResult>> = _capturedResults.asStateFlow()

    /**
     * Process a single camera frame for real-time detection.
     * Drops frames if previous inference is still running (keeps UI smooth).
     * Results are temporally smoothed via DetectionSmoother.
     */
    fun processFrame(imageProxy: ImageProxy) {
        // Drop frame if gallery detection is pending or previous frame still processing
        if (skipFrameProcessing || !isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val startTime = System.currentTimeMillis()

                // Convert ImageProxy (YUV) → Bitmap (with rotation correction)
                val bitmap = ImageProcessor.imageProxyToBitmap(imageProxy)
                imageProxy.close() // Release camera buffer ASAP

                // Run YOLOv11 inference
                val rawResults = detector.detect(bitmap, confidenceThreshold)
                bitmap.recycle()

                // Temporal smoothing — reduces flickering and stabilizes boxes
                val smoothedResults = smoother.smooth(rawResults)

                val elapsed = System.currentTimeMillis() - startTime

                // Update UI state
                _liveResults.value = smoothedResults
                _inferenceTimeMs.value = elapsed
                _fps.value = (1000.0 / elapsed.coerceAtLeast(1L)).toInt()
                _detectionState.value = if (smoothedResults.isNotEmpty())
                    DetectionState.Found(smoothedResults) else DetectionState.Scanning
            } catch (e: Exception) {
                e.printStackTrace()
                imageProxy.close()
            } finally {
                isProcessing.set(false)
            }
        }
    }

    /**
     * Single-shot detection on an image from gallery/URI.
     * No temporal smoothing (single frame, no history needed).
     */
    fun detectFromUri(uri: Uri) {
        skipFrameProcessing = true
        viewModelScope.launch(Dispatchers.Default) {
            _detectionState.value = DetectionState.Scanning
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val results = detector.detect(bitmap, confidenceThreshold)
                    bitmap.recycle()

                    _liveResults.value = results
                    _capturedResults.value = results
                    _detectionState.value = if (results.isNotEmpty())
                        DetectionState.Found(results)
                    else DetectionState.Error("Tidak ada wayang terdeteksi")
                } else {
                    _detectionState.value = DetectionState.Error("Gagal memuat gambar")
                }
            } catch (e: Exception) {
                _detectionState.value = DetectionState.Error("Error: ${e.message}")
            }
        }
    }

    /**
     * Freeze current live results for the ResultScreen.
     * Called when user presses capture button in live mode.
     */
    fun captureCurrentResults() {
        _capturedResults.value = _liveResults.value.toList()
    }

    fun resetDetection() {
        skipFrameProcessing = false
        smoother.reset()
        _detectionState.value = DetectionState.Idle
        _liveResults.value = emptyList()
        _capturedResults.value = emptyList()
        _fps.value = 0
        _inferenceTimeMs.value = 0L
    }

    override fun onCleared() {
        super.onCleared()
        detector.close()
    }
}
