package com.wird.feature.qibla

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun QiblaRoute(viewModel: QiblaViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    QiblaScreen(uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(uiState: QiblaUiState) {
    val context = LocalContext.current
    var arMode by remember { mutableStateOf(false) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> arMode = granted }

    fun toggleAr() {
        if (arMode) {
            arMode = false
        } else if (hasCamera(context)) {
            arMode = true
        } else {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Qibla") },
                actions = {
                    IconButton(onClick = { toggleAr() }) {
                        Icon(
                            imageVector = if (arMode) Icons.Default.Explore else Icons.Default.CameraAlt,
                            contentDescription = if (arMode) "Compass mode" else "AR mode",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (arMode) {
            QiblaArView(
                qiblaBearing = uiState.qiblaBearing,
                declination = uiState.declination,
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            CompassMode(uiState, Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun CompassMode(uiState: QiblaUiState, modifier: Modifier) {
    val heading = rememberDeviceHeading()
    val trueHeading = (heading.azimuth + uiState.declination + 360f) % 360f
    val pointerAngle = (uiState.qiblaBearing - trueHeading + 360f) % 360f
    val aligned = min(pointerAngle, 360f - pointerAngle) < 6f
    val needsCalibration = heading.accuracy <= SensorManagerAccuracy.LOW

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        Text(
            "${uiState.qiblaBearing.roundToInt()}° from North · ${uiState.locationName}",
            style = MaterialTheme.typography.titleMedium,
        )

        if (!heading.available) {
            Text(
                "This phone has no compass sensor. Use AR mode, or a device with a magnetometer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            return@Column
        }

        Compass(trueHeading = trueHeading, pointerAngle = pointerAngle, aligned = aligned)

        Text(
            text = if (aligned) "Facing the Qibla 🕋" else "Hold the phone up and turn until 🕋 points up",
            style = MaterialTheme.typography.titleLarge,
            color = if (aligned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        Text(
            "Heading ${trueHeading.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (needsCalibration) {
            Text(
                "Low compass accuracy — wave your phone in a figure-8 to calibrate.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Compass(trueHeading: Float, pointerAngle: Float, aligned: Boolean) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(300.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .rotate(-trueHeading),
            ) {
                Text("N", Modifier.align(Alignment.TopCenter), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("E", Modifier.align(Alignment.CenterEnd))
                Text("S", Modifier.align(Alignment.BottomCenter))
                Text("W", Modifier.align(Alignment.CenterStart))
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = pointerAngle },
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 28.dp),
                ) {
                    Text("🕋", fontSize = 30.sp)
                    Icon(
                        imageVector = Icons.Filled.Navigation,
                        contentDescription = "Qibla direction",
                        tint = if (aligned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
        }
    }
}

private object SensorManagerAccuracy {
    const val LOW = 1 // SensorManager.SENSOR_STATUS_ACCURACY_LOW
}

private fun hasCamera(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
