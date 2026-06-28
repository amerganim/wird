package com.wird.feature.alarm.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wird.feature.alarm.data.AlarmPrefs
import com.wird.feature.alarm.data.DismissTask

@Composable
fun AlarmRoute(viewModel: AlarmViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AlarmScreen(
        prefs = uiState,
        onEnabledChange = viewModel::setEnabled,
        onTimeChange = viewModel::setTime,
        onDismissTaskChange = viewModel::setDismissTask,
        onUseFajrTimeChange = viewModel::setUseFajrTime,
        onTest = viewModel::testAlarm,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    prefs: AlarmPrefs,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onDismissTaskChange: (DismissTask) -> Unit,
    onUseFajrTimeChange: (Boolean) -> Unit,
    onTest: () -> Unit,
) {
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { onEnabledChange(true) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Wake-up Alarm") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (prefs.useFajrTime) {
                Text(
                    text = "Fajr",
                    fontSize = 64.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp),
                )
                Text(
                    text = "Auto — follows your daily Fajr time",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = formatTime(prefs.hour, prefs.minute),
                    fontSize = 64.sp,
                    modifier = Modifier
                        .clickable { showTimePicker = true }
                        .padding(8.dp),
                )
                Text(
                    text = "Tap the time to change it",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Use Fajr time", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Switch(checked = prefs.useFajrTime, onCheckedChange = onUseFajrTimeChange)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Daily wake-up alarm", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Switch(
                    checked = prefs.enabled,
                    onCheckedChange = { checked ->
                        if (checked && needsNotificationPermission(context)) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onEnabledChange(checked)
                        }
                    },
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Rings full-screen with sound even when the phone is locked. " +
                    "Dismiss tasks (math, shake) are coming next.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))
            Text(
                text = "To dismiss the alarm you must:",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            DismissTask.entries.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismissTaskChange(task) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = prefs.dismissTask == task,
                        onClick = { onDismissTaskChange(task) },
                    )
                    Text(dismissTaskLabel(task))
                }
            }

            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = onTest) {
                Text("Test alarm (rings in 5s)")
            }
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = prefs.hour,
            initialMinute = prefs.minute,
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(state.hour, state.minute)
                    showTimePicker = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = state) },
        )
    }
}

private fun dismissTaskLabel(task: DismissTask): String = when (task) {
    DismissTask.NONE -> "Just tap dismiss"
    DismissTask.MATH -> "Solve a math problem"
    DismissTask.SHAKE -> "Shake the phone"
}

private fun formatTime(hour: Int, minute: Int): String {
    val hour12 = if (hour % 12 == 0) 12 else hour % 12
    val amPm = if (hour < 12) "AM" else "PM"
    return "%d:%02d %s".format(hour12, minute, amPm)
}

private fun needsNotificationPermission(context: android.content.Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED
