package com.wird.feature.alarm.engine

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wird.core.ui.theme.WirdTheme
import com.wird.feature.alarm.data.AlarmSettings
import com.wird.feature.alarm.data.DismissTask
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    @Inject lateinit var settings: AlarmSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showWhenLockedAndTurnScreenOn()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Dismiss-proof: back does nothing — the task must be completed.
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = Unit
        })

        val label = intent.getStringExtra(AlarmScheduler.EXTRA_LABEL).orEmpty().ifEmpty { "Fajr" }
        setContent {
            val task by settings.prefs
                .map { it.dismissTask }
                .collectAsStateWithLifecycle(initialValue = DismissTask.MATH)
            WirdTheme {
                AlarmContent(label = label, task = task, onDismiss = ::dismiss)
            }
        }
    }

    private fun dismiss() {
        startService(
            Intent(this, AlarmService::class.java).apply { action = AlarmService.ACTION_STOP },
        )
        finish()
    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
    }
}

@Composable
private fun AlarmContent(label: String, task: DismissTask, onDismiss: () -> Unit) {
    val now = remember { SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = now, fontSize = 56.sp, color = MaterialTheme.colorScheme.primary)
            Text(text = label, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Wake up for prayer",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            when (task) {
                DismissTask.NONE -> Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                ) { Text("Dismiss") }

                DismissTask.MATH -> MathChallenge(onSolved = onDismiss)
                DismissTask.SHAKE -> ShakeChallenge(onShaken = onDismiss)
            }
        }
    }
}

@Composable
private fun MathChallenge(onSolved: () -> Unit) {
    var problem by remember { mutableStateOf(newProblem()) }
    var answer by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = 24.dp),
    ) {
        Text("Solve to dismiss", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "${problem.first} = ?",
            fontSize = 36.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it.filter(Char::isDigit).take(4) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        Button(
            onClick = {
                if (answer.toIntOrNull() == problem.second) {
                    onSolved()
                } else {
                    problem = newProblem()
                    answer = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Submit") }
    }
}

private fun newProblem(): Pair<String, Int> {
    val a = (3..12).random()
    val b = (3..9).random()
    return "$a × $b" to (a * b)
}

@Composable
private fun ShakeChallenge(onShaken: () -> Unit) {
    val target = 12
    var count by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(SensorManager::class.java)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastShake = 0L
        var fired = false
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val (x, y, z) = event.values
                val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                if (gForce > 1.8f) {
                    val now = System.currentTimeMillis()
                    if (now - lastShake > 250) {
                        lastShake = now
                        count++
                        if (count >= target && !fired) {
                            fired = true
                            onShaken()
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager?.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager?.unregisterListener(listener) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = 24.dp),
    ) {
        Text("Shake the phone to dismiss", style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            progress = { count.toFloat() / target },
            modifier = Modifier.fillMaxWidth(),
        )
        Text("$count / $target")
    }
}
