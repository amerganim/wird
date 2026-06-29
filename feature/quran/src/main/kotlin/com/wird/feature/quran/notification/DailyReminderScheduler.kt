package com.wird.feature.quran.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.wird.feature.quran.data.HabitSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/** Schedules a daily inexact-but-idle-allowed alarm for the daily-ayat reminder. */
@Singleton
class DailyReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitSettings: HabitSettings,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    suspend fun reschedule() {
        val prefs = habitSettings.reminder.first()
        if (!prefs.enabled) {
            cancel()
            return
        }
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, prefs.hour)
            set(Calendar.MINUTE, prefs.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        val pendingIntent = pendingIntent()
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        try {
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
        }
    }

    fun cancel() = alarmManager.cancel(pendingIntent())

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(context, DailyReminderReceiver::class.java).apply { action = ACTION_DAILY }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_DAILY = "com.wird.feature.quran.DAILY_REMINDER"
        private const val REQUEST_CODE = 9102
    }
}
