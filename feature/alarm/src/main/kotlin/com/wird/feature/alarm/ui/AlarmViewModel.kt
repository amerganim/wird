package com.wird.feature.alarm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.feature.alarm.data.AlarmPrefs
import com.wird.feature.alarm.data.AlarmSettings
import com.wird.feature.alarm.data.DismissTask
import com.wird.feature.alarm.engine.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val settings: AlarmSettings,
    private val scheduler: AlarmScheduler,
) : ViewModel() {

    val uiState: StateFlow<AlarmPrefs> = settings.prefs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlarmPrefs(
            enabled = false,
            hour = AlarmSettings.DEFAULT_HOUR,
            minute = AlarmSettings.DEFAULT_MINUTE,
            dismissTask = DismissTask.MATH,
            useFajrTime = false,
        ),
    )

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.setEnabled(enabled)
            if (enabled) {
                scheduler.scheduleFor(settings.prefs.first())
            } else {
                scheduler.cancel()
            }
        }
    }

    fun setTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settings.setTime(hour, minute)
            rescheduleIfEnabled()
        }
    }

    fun setDismissTask(task: DismissTask) {
        viewModelScope.launch { settings.setDismissTask(task) }
    }

    fun setUseFajrTime(useFajr: Boolean) {
        viewModelScope.launch {
            settings.setUseFajrTime(useFajr)
            rescheduleIfEnabled()
        }
    }

    private suspend fun rescheduleIfEnabled() {
        val prefs = settings.prefs.first()
        if (prefs.enabled) scheduler.scheduleFor(prefs)
    }

    fun testAlarm() {
        scheduler.scheduleAt(System.currentTimeMillis() + 5_000L, "Test")
    }

    private companion object {
        const val LABEL = "Fajr"
    }
}
