package com.example.wayang_detection.detector

import android.content.Context
import android.graphics.Bitmap
import com.example.wayang_detection.data.model.BoundingBox
import com.example.wayang_detection.data.model.DetectionResult
import com.example.wayang_detection.data.repository.WayangRepository
import com.example.wayang_detection.util.Constants
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * YOLOv11 TFLite inference engine for Balinese wayang kulit detection.
 *
 * Model specs:
 * - Input:  (1, 640, 640, 3) Float32 NHWC, normalized /255.0
 * - Output: (1, 20, 8400) where 20 = 4 bbox coords (cx,cy,w,h) + 16 class scores
 * - Labels: 16 Balinese wayang characters (alphabetical order)
 *
 * Pipeline:
 *   Bitmap → Letterbox(640x640) → Float32 Buffer → TFLite → Parse(1,20,8400)
 *   → Confidence Filter → NMS → Reverse Letterbox → List<DetectionResult>
 */
class WayangDetector(context: Context) {

    private val interpreter: Interpreter
    private val labels: List<String>
    private val inputSize = Constants.MODEL_INPUT_SIZE

    init {
        val model = loadModelFile(context, Constants.MODEL_FILE)
        val options = Interpreter.Options().apply {
            setNumThreads(4) // Multi-threaded CPU inference
        }
        interpreter = Interpreter(model, options)
        labels = loadLabels(context, Constants.LABELS_FILE)
    }

    /**
     * Run detection on a bitmap image.
     *
     * @param bitmap Source image (any size — will be letterboxed to 640×640).
     * @param confidenceThreshold Minimum confidence to keep a detection.
     * @param iouThreshold IoU threshold for Non-Maximum Suppression.
     * @return List of detected wayang characters with bounding boxes (normalized 0-1).
     */
    fun detect(
        bitmap: Bitmap,
        confidenceThreshold: Float = Constants.DEFAULT_CONFIDENCE_THRESHOLD,
        iouThreshold: Float = Constants.DEFAULT_IOU_THRESHOLD
    ): List<DetectionResult> {

        // 1. Letterbox preprocessing: preserve aspect ratio, pad with gray
        val (letterboxed, lbInfo) = ImageProcessor.letterboxBitmap(bitmap, inputSize)

        // 2. Convert to Float32 ByteBuffer (NHWC, /255.0)
        val inputBuffer = ImageProcessor.bitmapToByteBuffer(letterboxed, inputSize, inputSize)
        letterboxed.recycle()

        // 3. Prepare output buffer: shape (1, 20, 8400)
        val numRows = Constants.BBOX_COORDS + Constants.NUM_CLASSES // 20
        val numPred = Constants.NUM_PREDICTIONS // 8400
        val outputBuffer = ByteBuffer.allocateDirect(1 * numRows * numPred * 4)
        outputBuffer.order(ByteOrder.nativeOrder())

        // 4. Run TFLite inference
        interpreter.run(inputBuffer, outputBuffer)

        // 5. Parse output tensor — layout is [20][8400] row-major
        outputBuffer.rewind()
        val outputArray = FloatArray(numRows * numPred)
        outputBuffer.asFloatBuffer().get(outputArray)

        // 6. Extract candidate detections in pixel coords (0-640 letterboxed space)
        val rawDetections = mutableListOf<RawDetection>()

        for (j in 0 until numPred) {
            val cx = outputArray[0 * numPred + j]
            val cy = outputArray[1 * numPred + j]
            val w  = outputArray[2 * numPred + j]
            val h  = outputArray[3 * numPred + j]

            // Find best class score
            var maxScore = 0f
            var maxClassIdx = 0
            for (c in 0 until Constants.NUM_CLASSES) {
                val score = outputArray[(Constants.BBOX_COORDS + c) * numPred + j]
                if (score > maxScore) {
                    maxScore = score
                    maxClassIdx = c
                }
            }

            if (maxScore >= confidenceThreshold) {
                // Keep in pixel coords for NMS (IoU is scale-invariant)
                rawDetections.add(
                    RawDetection(
                        left = cx - w / 2f,
                        top = cy - h / 2f,
                        right = cx + w / 2f,
                        bottom = cy + h / 2f,
                        confidence = maxScore,
                        classIdx = maxClassIdx
                    )
                )
            }
        }

        // 7. Non-Maximum Suppression (in pixel space)
        val nmsResults = NmsProcessor.nms(rawDetections, iouThreshold)

        // 8. Reverse letterboxing → normalized coords (0-1) relative to original image
        return nmsResults.mapNotNull { raw ->
            val character = WayangRepository.getByClassId(raw.classIdx)
            character?.let {
                // Remove padding, then normalize by the actual image area within the letterbox
                val left   = ((raw.left - lbInfo.padLeft) / lbInfo.scaledWidth).coerceIn(0f, 1f)
                val top    = ((raw.top - lbInfo.padTop) / lbInfo.scaledHeight).coerceIn(0f, 1f)
                val right  = ((raw.right - lbInfo.padLeft) / lbInfo.scaledWidth).coerceIn(0f, 1f)
                val bottom = ((raw.bottom - lbInfo.padTop) / lbInfo.scaledHeight).coerceIn(0f, 1f)

                DetectionResult(
                    characterId = it.id,
                    characterName = it.name,
                    confidence = raw.confidence,
                    boundingBox = BoundingBox(left, top, right, bottom)
                )
            }
        }
    }

    /** Release TFLite interpreter resources. */
    fun close() {
        interpreter.close()
    }

    private fun loadModelFile(context: Context, filename: String): MappedByteBuffer {
        val assetFd = context.assets.openFd(filename)
        val inputStream = FileInputStream(assetFd.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFd.startOffset,
            assetFd.declaredLength
        )
    }

    private fun loadLabels(context: Context, filename: String): List<String> {
        return context.assets.open(filename).bufferedReader().readLines()
    }
}

/**
 * Internal raw detection result before mapping to WayangCharacter.
 * Coordinates are in pixel space (0-640) within the letterboxed image.
 */
data class RawDetection(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val confidence: Float,
    val classIdx: Int
)
