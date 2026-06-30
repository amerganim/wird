package com.wird.feature.qibla

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.abs
import kotlin.math.roundToInt

private const val HORIZONTAL_FOV = 55f // approximate camera field of view in degrees

@Composable
fun QiblaArView(
    qiblaBearing: Float,
    declination: Float,
    modifier: Modifier = Modifier,
) {
    val heading = rememberDeviceHeading()
    val trueHeading = (heading.azimuth + declination + 360f) % 360f
    // Signed difference in -180..180: positive means qibla is to the right.
    val diff = (((qiblaBearing - trueHeading) + 540f) % 360f) - 180f
    val aligned = abs(diff) < 6f

    BoxWithConstraints(modifier.fillMaxSize()) {
        CameraPreview(Modifier.fillMaxSize())

        val maxWidthDp = maxWidth
        when {
            abs(diff) <= HORIZONTAL_FOV / 2f -> {
                val fraction = diff / (HORIZONTAL_FOV / 2f) // -1..1
                val offsetX = maxWidthDp / 2f * fraction
                Surface(
                    color = if (aligned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = offsetX),
                ) {
                    Text(
                        text = if (aligned) "🕋\nQibla" else "🕋",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            diff < 0 -> EdgeArrow(Alignment.CenterStart, left = true)
            else -> EdgeArrow(Alignment.CenterEnd, left = false)
        }

        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
        ) {
            Text(
                text = if (aligned) {
                    "Facing the Qibla 🕋"
                } else {
                    "Turn ${if (diff < 0) "left" else "right"} ${abs(diff).roundToInt()}°"
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.EdgeArrow(
    alignment: Alignment,
    left: Boolean,
) {
    Icon(
        imageVector = if (left) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .align(alignment)
            .padding(16.dp),
    )
}

@Composable
private fun CameraPreview(modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                runCatching {
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
    )
}
