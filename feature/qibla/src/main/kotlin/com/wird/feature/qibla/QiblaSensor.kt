package com.wird.feature.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

data class DeviceHeading(
    val azimuth: Float, // magnetic azimuth of where the device points, degrees 0..360
    val accuracy: Int,
    val available: Boolean,
)

/**
 * Device heading from the (geomagnetic) rotation vector. Falls back to
 * TYPE_GEOMAGNETIC_ROTATION_VECTOR on devices without a gyroscope (so no
 * TYPE_ROTATION_VECTOR), e.g. many entry-level phones. Uses the "pointing"
 * coordinate remap so it works held upright (and for AR).
 */
@Composable
fun rememberDeviceHeading(): DeviceHeading {
    val context = LocalContext.current
    val azimuth = remember { mutableFloatStateOf(0f) }
    val accuracy = remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_UNRELIABLE) }
    val available = remember { mutableIntStateOf(-1) } // -1 unknown, 0 no, 1 yes

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(SensorManager::class.java)
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        available.intValue = if (sensor != null) 1 else 0

        val displayRotation = displayRotation(context)
        val rotationMatrix = FloatArray(9)
        val remapped = FloatArray(9)
        val orientation = FloatArray(3)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val (axisX, axisY) = when (displayRotation) {
                    Surface.ROTATION_90 -> SensorManager.AXIS_Z to SensorManager.AXIS_MINUS_X
                    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Z
                    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Z to SensorManager.AXIS_X
                    else -> SensorManager.AXIS_X to SensorManager.AXIS_Z
                }
                SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remapped)
                SensorManager.getOrientation(remapped, orientation)
                val deg = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                // Wrap-aware low-pass filter.
                val delta = ((deg - azimuth.floatValue + 540f) % 360f) - 180f
                azimuth.floatValue = (azimuth.floatValue + delta * SMOOTHING + 360f) % 360f
            }

            override fun onAccuracyChanged(s: Sensor?, acc: Int) {
                accuracy.intValue = acc
            }
        }
        if (sensor != null) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose { sensorManager?.unregisterListener(listener) }
    }

    return DeviceHeading(
        azimuth = azimuth.floatValue,
        accuracy = accuracy.intValue,
        available = available.intValue != 0,
    )
}

private const val SMOOTHING = 0.2f

private fun displayRotation(context: Context): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display?.rotation ?: Surface.ROTATION_0
    } else {
        @Suppress("DEPRECATION")
        (context.getSystemService(WindowManager::class.java)).defaultDisplay.rotation
    }
