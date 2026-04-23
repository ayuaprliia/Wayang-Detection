package com.example.wayang_detection.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.wayang_detection.data.model.DetectionResult
import com.example.wayang_detection.data.model.DetectionState
import com.example.wayang_detection.ui.components.BoundingBoxOverlay
import com.example.wayang_detection.ui.components.DevModeOverlay
import com.example.wayang_detection.ui.theme.*
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

/**
 * Detection screen supporting both live camera and gallery modes.
 * Live mode: CameraX ImageAnalysis sends frames for real-time YOLOv11 inference.
 * Gallery mode: single-shot inference on selected image.
 *
 * Bounding box overlay is positioned to match the FIT_CENTER camera preview area,
 * using the frameAspectRatio to compute the active image region.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionScreen(
    mode: String, // "live" or "gallery"
    onBack: () -> Unit,
    onNavigateToResult: () -> Unit,
    detectionState: DetectionState,
    liveResults: List<DetectionResult>,
    onProcessFrame: (ImageProxy) -> Unit,
    onDetectFromUri: (Uri) -> Unit,
    onCaptureResults: () -> Unit,
    onResetDetection: () -> Unit,
    devModeEnabled: Boolean,
    fps: Int,
    inferenceTimeMs: Long,
    inputResolution: Int,
    confidenceThreshold: Float,
    frameAspectRatio: Float
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Background executor for CameraX ImageAnalysis
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    var flashEnabled by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var waitingForGalleryResult by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        if (uri != null) {
            waitingForGalleryResult = true
            onDetectFromUri(uri)
        }
    }

    // Auto-navigate to result for gallery/image detection
    LaunchedEffect(detectionState, waitingForGalleryResult) {
        if (waitingForGalleryResult && detectionState is DetectionState.Found) {
            delay(1000) // Brief display of bounding boxes over image
            waitingForGalleryResult = false
            onNavigateToResult()
        }
    }

    // Request camera permission for live mode / launch gallery picker
    LaunchedEffect(mode) {
        if (mode == "live" && !hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        if (mode == "gallery") {
            galleryLauncher.launch("image/*")
        }
    }

    // Scan button pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
    val scanPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera preview with ImageAnalysis, or selected image
        if (mode == "live" && hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        // FIT_CENTER: show full camera frame without cropping.
                        // This ensures bounding box coordinates (0-1 normalized
                        // to the full analysis frame) align correctly with the preview.
                        scaleType = PreviewView.ScaleType.FIT_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        // ImageAnalysis — sends each frame to ViewModel for YOLOv11 inference
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                    onProcessFrame(imageProxy)
                                }
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (selectedImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(selectedImageUri),
                contentDescription = "Selected image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder/permission needed
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (mode == "live" && !hasCameraPermission) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Izin kamera diperlukan",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary
                            )
                        ) {
                            Text("Berikan Izin")
                        }
                    }
                } else {
                    Text(
                        text = "Memuat...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ── Bounding box overlay with correct positioning ──
        // For live mode, compute the actual preview area within FIT_CENTER to align boxes.
        // The preview shows the camera frame fitted inside the screen with letterboxing;
        // the overlay must exactly cover that active region, not the full screen.
        if (liveResults.isNotEmpty()) {
            if (mode == "live") {
                // Use BoxWithConstraints to measure the full screen size,
                // then compute the actual image area based on frame aspect ratio
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val screenWidth = maxWidth
                    val screenHeight = maxHeight
                    val screenAspect = screenWidth / screenHeight

                    // Compute the image area within FIT_CENTER
                    val (imageWidth, imageHeight) = if (frameAspectRatio > screenAspect) {
                        // Frame is wider than screen → fit to width, pad top/bottom
                        screenWidth to (screenWidth / frameAspectRatio)
                    } else {
                        // Frame is taller than screen → fit to height, pad left/right
                        (screenHeight * frameAspectRatio) to screenHeight
                    }

                    val offsetX = (screenWidth - imageWidth) / 2
                    val offsetY = (screenHeight - imageHeight) / 2

                    // Overlay positioned exactly over the preview image area
                    BoundingBoxOverlay(
                        boundingBoxes = liveResults.map { it.boundingBox to it.characterName },
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .size(width = imageWidth, height = imageHeight)
                    )

                    // Detection labels
                    liveResults.forEach { result ->
                        val labelX = offsetX + imageWidth * result.boundingBox.left
                        val rawY = offsetY + imageHeight * result.boundingBox.top - 28.dp
                        val labelY = if (rawY < 0.dp) 0.dp else rawY

                        Box(
                            modifier = Modifier
                                .offset(x = labelX, y = labelY)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BgPrimary.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${result.characterName} ${(result.confidence * 100).toInt()}%",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // Gallery mode: overlay fills entire image area (Fit already matches)
                BoundingBoxOverlay(
                    boundingBoxes = liveResults.map { it.boundingBox to it.characterName },
                    modifier = Modifier.fillMaxSize()
                )
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    liveResults.forEach { result ->
                        val labelX = maxWidth * result.boundingBox.left
                        val rawY = maxHeight * result.boundingBox.top - 28.dp
                        val labelY = if (rawY < 0.dp) 0.dp else rawY

                        Box(
                            modifier = Modifier
                                .offset(x = labelX, y = labelY)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BgPrimary.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${result.characterName} ${(result.confidence * 100).toInt()}%",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Scanning indicator (only for gallery single-shot inference)
        if (waitingForGalleryResult && detectionState is DetectionState.Scanning) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgPrimary.copy(alpha = 0.8f))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = GoldPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Mendeteksi...",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Dev Mode Overlay (top)
        DevModeOverlay(
            isVisible = devModeEnabled,
            fps = fps,
            inferenceTimeMs = inferenceTimeMs,
            resolution = inputResolution,
            threshold = confidenceThreshold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = if (devModeEnabled) 40.dp else 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                onResetDetection()
                onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (mode == "live") "Mode: Live" else "Mode: Galeri",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        // Bottom controls (live mode)
        if (mode == "live") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(BgPrimary.copy(alpha = 0.85f))
                    .padding(horizontal = 32.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash toggle
                IconButton(onClick = { flashEnabled = !flashEnabled }) {
                    Icon(
                        imageVector = if (flashEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                        contentDescription = "Flash",
                        tint = if (flashEnabled) GoldPrimary else TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Capture button — freezes current detections and navigates to result
                Box(contentAlignment = Alignment.Center) {
                    // Pulse ring
                    Box(
                        modifier = Modifier
                            .size(72.dp * scanPulseScale)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.15f))
                    )
                    // Inner button
                    IconButton(
                        onClick = {
                            if (liveResults.isNotEmpty()) {
                                onCaptureResults()
                                onNavigateToResult()
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(GoldLight, GoldPrimary)
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }
                }

                // Empty spacer to balance the flash toggle icon and keep capture button centered
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}
