package com.maku.eyeonsite.platform

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.maku.eyeonsite.ui.theme.Forest
import com.maku.eyeonsite.ui.theme.SavannaGold
import com.maku.eyeonsite.ui.theme.Stone
import com.maku.eyeonsite.ui.theme.White
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun CameraCapture(
    modifier: Modifier,
    onPhotoCaptured: (ByteArray, Double, Double) -> Unit,
    onPermissionDenied: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissions =
        rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
        )

    LaunchedEffect(Unit) {
        if (!permissions.allPermissionsGranted) {
            permissions.launchMultiplePermissionRequest()
        }
    }

    val cameraGranted =
        permissions.permissions.any {
            it.permission == Manifest.permission.CAMERA && it.status.isGranted
        }
    val locationGranted =
        permissions.permissions.any {
            it.permission == Manifest.permission.ACCESS_FINE_LOCATION && it.status.isGranted
        }

    if (!cameraGranted) {
        PermissionRequiredContent(onDenied = onPermissionDenied)
        return
    }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var isCapturing by remember { mutableStateOf(false) }

    LaunchedEffect(lifecycleOwner) {
        val cameraProvider =
            suspendCoroutine<ProcessCameraProvider> { cont ->
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({ cont.resume(future.get()) }, ContextCompat.getMainExecutor(context))
            }
        val preview =
            Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture,
        )
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Shutter button
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (isCapturing) SavannaGold.copy(alpha = 0.6f) else White)
                    .clickable(enabled = !isCapturing) {
                        isCapturing = true
                        capturePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            locationGranted = locationGranted,
                            onCaptured = { bytes, lat, lng ->
                                isCapturing = false
                                onPhotoCaptured(bytes, lat, lng)
                            },
                            onError = { isCapturing = false },
                        )
                    },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) SavannaGold else Forest),
            )
        }
    }
}

@Composable
private fun PermissionRequiredContent(onDenied: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black)
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Camera permission required",
            style = MaterialTheme.typography.titleMedium,
            color = White,
        )
        Text(
            text = "EyeOnSite needs camera access to capture field evidence.",
            style = MaterialTheme.typography.bodyMedium,
            color = Stone,
        )
    }
    LaunchedEffect(Unit) { onDenied() }
}

@SuppressLint("MissingPermission")
private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    locationGranted: Boolean,
    onCaptured: (ByteArray, Double, Double) -> Unit,
    onError: () -> Unit,
) {
    val executor = ContextCompat.getMainExecutor(context)
    val outputFile = File(context.cacheDir, "evidence_${System.currentTimeMillis()}.jpg")

    fun doCapture(
        lat: Double,
        lng: Double,
    ) {
        val options = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        imageCapture.takePicture(
            options,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onCaptured(outputFile.readBytes(), lat, lng)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError()
                }
            },
        )
    }

    if (locationGranted) {
        LocationServices
            .getFusedLocationProviderClient(context)
            .lastLocation
            .addOnSuccessListener { loc -> doCapture(loc?.latitude ?: 0.0, loc?.longitude ?: 0.0) }
            .addOnFailureListener { doCapture(0.0, 0.0) }
    } else {
        doCapture(0.0, 0.0)
    }
}
