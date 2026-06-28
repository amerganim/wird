package com.wird.feature.alarm.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.alarmDataStore by preferencesDataStore(name = "alarm_settings")

data class AlarmPrefs(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
)

@Singleton
class AlarmSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val ENABLED = booleanPreferencesKey("enabled")
        val HOUR = intPreferencesKey("hour")
        val MINUTE = intPreferencesKey("minute")
    }

    val prefs: Flow<AlarmPrefs> = context.alarmDataStore.data.map { p ->
        AlarmPrefs(
            enabled = p[Keys.ENABLED] ?: false,
            hour = p[Keys.HOUR] ?: DEFAULT_HOUR,
            minute = p[Keys.MINUTE] ?: DEFAULT_MINUTE,
        )
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.alarmDataStore.edit { it[Keys.ENABLED] = enabled }
    }

    suspend fun setTime(hour: Int, minute: Int) {
        context.alarmDataStore.edit {
            it[Keys.HOUR] = hour
            it[Keys.MINUTE] = minute
        }
    }

    companion object {
        const val DEFAULT_HOUR = 5
        const val DEFAULT_MINUTE = 0
    }
}
