package com.example.wayang_detection.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
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
import com.example.wayang_detection.data.model.BoundingBox
import com.example.wayang_detection.data.model.DetectionState
import com.example.wayang_detection.ui.components.BoundingBoxOverlay
import com.example.wayang_detection.ui.components.DevModeOverlay
import com.example.wayang_detection.ui.theme.*

/**
 * Detection screen supporting both live camera and gallery modes.
 * Shows camera preview / selected image with bounding box overlay
 * and developer mode metrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionScreen(
    mode: String, // "live" or "gallery"
    onBack: () -> Unit,
    onDetectionResult: (String, Float) -> Unit,
    detectionState: DetectionState,
    onPerformDetection: () -> Unit,
    onResetDetection: () -> Unit,
    devModeEnabled: Boolean,
    fps: Int,
    inferenceTimeMs: Long,
    inputResolution: Int,
    confidenceThreshold: Float
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        if (uri != null) {
            onPerformDetection()
        }
    }

    // Auto-navigate to result when detection is found
    LaunchedEffect(detectionState) {
        if (detectionState is DetectionState.Found) {
            val result = detectionState.results.firstOrNull()
            if (result != null) {
                kotlinx.coroutines.delay(1500)
                onDetectionResult(result.characterId, result.confidence)
                onResetDetection()
            }
        }
    }

    // Request camera permission for live mode
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
        // Camera preview or selected image
        if (mode == "live" && hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (mode == "gallery" && selectedImageUri != null) {
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

        // Bounding box overlay (when detection found)
        if (detectionState is DetectionState.Found) {
            BoundingBoxOverlay(
                boundingBoxes = detectionState.results.map { it.boundingBox to it.characterName },
                modifier = Modifier.fillMaxSize()
            )

            // Detection label
            detectionState.results.firstOrNull()?.let { result ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgPrimary.copy(alpha = 0.85f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${result.characterName} — ${(result.confidence * 100).toInt()}%",
                        color = GoldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Scanning indicator
        if (detectionState is DetectionState.Scanning) {
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

                // Capture/Scan button
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
                        onClick = onPerformDetection,
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

                // Gallery shortcut
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoLibrary,
                        contentDescription = "Galeri",
                        tint = TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
