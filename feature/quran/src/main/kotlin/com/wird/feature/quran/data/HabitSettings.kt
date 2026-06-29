package com.wird.feature.quran.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.habitDataStore by preferencesDataStore(name = "habit_settings")

data class HabitState(
    val currentStreak: Int,
    val bestStreak: Int,
    val readToday: Boolean,
)

/** Daily-reading streak tracking. */
@Singleton
class HabitSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val LAST_READ = longPreferencesKey("last_read_epoch_day")
        val STREAK = intPreferencesKey("current_streak")
        val BEST = intPreferencesKey("best_streak")
    }

    val state: Flow<HabitState> = context.habitDataStore.data.map { p ->
        val today = LocalDate.now().toEpochDay()
        val last = p[Keys.LAST_READ] ?: -1L
        // A streak only stands if the last read was today or yesterday.
        val rawStreak = p[Keys.STREAK] ?: 0
        val streak = when (last) {
            today, today - 1 -> rawStreak
            else -> 0
        }
        HabitState(
            currentStreak = streak,
            bestStreak = p[Keys.BEST] ?: 0,
            readToday = last == today,
        )
    }

    suspend fun markReadToday() {
        context.habitDataStore.edit { p ->
            val today = LocalDate.now().toEpochDay()
            val last = p[Keys.LAST_READ] ?: -1L
            if (last == today) return@edit
            val newStreak = if (last == today - 1) (p[Keys.STREAK] ?: 0) + 1 else 1
            p[Keys.LAST_READ] = today
            p[Keys.STREAK] = newStreak
            p[Keys.BEST] = maxOf(p[Keys.BEST] ?: 0, newStreak)
        }
    }
}
