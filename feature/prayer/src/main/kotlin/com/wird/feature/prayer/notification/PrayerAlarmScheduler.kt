package com.wird.feature.prayer.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.wird.core.prayertimes.PrayerCalculator
import com.wird.core.prayertimes.PrayerSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

/** Schedules a single rolling exact alarm for the next prayer; the receiver
 *  re-arms the following one after each fire. */
@Singleton
class PrayerAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: PrayerSettings,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    suspend fun scheduleNext() {
        val prefs = settings.prefs.first()
        if (!prefs.notificationsEnabled) return
        val next = PrayerCalculator.nextPrayerAfter(prefs, Clock.System.now())
        val triggerAt = next.instant.toEpochMilliseconds()
        val pendingIntent = buildPendingIntent(next.prayer)

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        try {
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel() {
        alarmManager.cancel(buildPendingIntent(prayer = ""))
    }

    private fun buildPendingIntent(prayer: String): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER, prayer)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_PRAYER_ALARM = "com.wird.feature.prayer.PRAYER_ALARM"
        const val EXTRA_PRAYER = "prayer"
        private const val REQUEST_CODE = 4202
    }
}
