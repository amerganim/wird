package com.wird.feature.qibla

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
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
    val magneticHeading = remember { mutableFloatStateOf(0f) }
    val accuracy = remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_UNRELIABLE) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(SensorManager::class.java)
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuth = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                // Low-pass filter that respects the 0/360 wrap-around.
                val delta = ((azimuth - magneticHeading.floatValue + 540f) % 360f) - 180f
                magneticHeading.floatValue = (magneticHeading.floatValue + delta * SMOOTHING + 360f) % 360f
            }

            override fun onAccuracyChanged(sensor: Sensor?, acc: Int) {
                if (sensor?.type == Sensor.TYPE_ROTATION_VECTOR) accuracy.intValue = acc
            }
        }
        sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager?.unregisterListener(listener) }
    }

    val trueHeading = (magneticHeading.floatValue + uiState.declination + 360f) % 360f
    val pointerAngle = (uiState.qiblaBearing - trueHeading + 360f) % 360f
    val aligned = min(pointerAngle, 360f - pointerAngle) < 6f
    val needsCalibration = accuracy.intValue <= SensorManager.SENSOR_STATUS_ACCURACY_LOW

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Qibla") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
            Text(
                "${uiState.qiblaBearing.roundToInt()}° from North · ${uiState.locationName}",
                style = MaterialTheme.typography.titleMedium,
            )

            Compass(trueHeading = trueHeading, pointerAngle = pointerAngle, aligned = aligned)

            Text(
                text = if (aligned) "Facing the Qibla 🕋" else "Turn until 🕋 points up",
                style = MaterialTheme.typography.titleLarge,
                color = if (aligned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
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
}

@Composable
private fun Compass(trueHeading: Float, pointerAngle: Float, aligned: Boolean) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(300.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            // Cardinal directions rotate so N points at true north.
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

            // Qibla pointer, relative to the phone (up = straight ahead).
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

private const val SMOOTHING = 0.15f
