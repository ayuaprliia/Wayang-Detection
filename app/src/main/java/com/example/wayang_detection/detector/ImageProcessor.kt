package com.example.wayang_detection.detector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Letterbox metadata — tracks how the original image was scaled and padded
 * into the model's 640×640 input. Used to reverse-map detection coordinates
 * back to original image space.
 */
data class LetterboxInfo(
    val scale: Float,        // Scale factor applied to original image
    val padLeft: Float,      // Horizontal padding added (left side)
    val padTop: Float,       // Vertical padding added (top side)
    val scaledWidth: Float,  // Width of actual image content in 640x640
    val scaledHeight: Float  // Height of actual image content in 640x640
)

/**
 * Image preprocessing utilities for TFLite YOLOv11 inference.
 *
 * Key features:
 * - Letterbox preprocessing (aspect-ratio-preserving resize with gray padding)
 * - NHWC Float32 /255.0 normalization
 * - CameraX ImageProxy → Bitmap with rotation correction
 */
object ImageProcessor {

    /**
     * Letterbox a bitmap to the target square size, preserving aspect ratio.
     * This is the standard YOLO/Ultralytics preprocessing:
     * 1. Scale image so longest side fits the target
     * 2. Pad shorter side symmetrically with gray (114, 114, 114)
     *
     * @return Pair of letterboxed bitmap and metadata for coordinate reversal.
     */
    fun letterboxBitmap(bitmap: Bitmap, targetSize: Int): Pair<Bitmap, LetterboxInfo> {
        val srcW = bitmap.width.toFloat()
        val srcH = bitmap.height.toFloat()

        // Scale to fit within targetSize x targetSize
        val scale = minOf(targetSize / srcW, targetSize / srcH)
        val scaledW = (srcW * scale)
        val scaledH = (srcH * scale)
        val padLeft = (targetSize - scaledW) / 2f
        val padTop = (targetSize - scaledH) / 2f

        val letterboxed = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(letterboxed)
        canvas.drawColor(Color.rgb(114, 114, 114)) // YOLO standard gray

        val resized = Bitmap.createScaledBitmap(bitmap, scaledW.toInt(), scaledH.toInt(), true)
        canvas.drawBitmap(resized, padLeft, padTop, null)
        if (resized !== bitmap) resized.recycle()

        val info = LetterboxInfo(scale, padLeft, padTop, scaledW, scaledH)
        return Pair(letterboxed, info)
    }

    /**
     * Convert a Bitmap to a Float32 ByteBuffer suitable for TFLite input.
     * Layout: NHWC (batch=1, height, width, channels=3).
     * Normalization: pixel values divided by 255.0.
     *
     * The bitmap MUST already be the correct size (typically 640x640 after letterboxing).
     */
    fun bitmapToByteBuffer(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): ByteBuffer {
        val bufferSize = 1 * targetWidth * targetHeight * 3 * 4 // float32
        val buffer = ByteBuffer.allocateDirect(bufferSize)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(targetWidth * targetHeight)
        bitmap.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)

        for (pixel in pixels) {
            buffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f) // R
            buffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)  // G
            buffer.putFloat((pixel and 0xFF) / 255.0f)           // B
        }

        buffer.rewind()
        return buffer
    }

    /**
     * Convert CameraX ImageProxy (YUV_420_888) to a Bitmap.
     * Uses CameraX built-in toBitmap() for accurate color conversion.
     * Applies rotation correction from camera sensor metadata.
     */
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val bitmap = imageProxy.toBitmap()

        // Apply rotation from camera sensor metadata
        val rotation = imageProxy.imageInfo.rotationDegrees
        if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            return rotated
        }

        return bitmap
    }
}
