package com.wird.feature.alarm.engine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/** Schedules the wake-up alarm via setAlarmClock (exact, doze-exempt, shows the
 *  system alarm icon). */
@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** Next future occurrence of [hour]:[minute] (today if still ahead, else tomorrow). */
    fun scheduleDaily(hour: Int, minute: Int, label: String) {
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        scheduleAt(target.timeInMillis, label)
    }

    fun scheduleAt(triggerAtMillis: Long, label: String) {
        val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent())
        alarmManager.setAlarmClock(info, fireIntent(label))
    }

    fun cancel() {
        alarmManager.cancel(fireIntent(label = ""))
    }

    private fun fireIntent(label: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_LABEL, label)
        }
        return PendingIntent.getBroadcast(
            context,
            REQ_FIRE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun showIntent(): PendingIntent =
        PendingIntent.getActivity(
            context,
            REQ_SHOW,
            Intent(context, AlarmActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    companion object {
        const val ACTION_FIRE = "com.wird.feature.alarm.FIRE"
        const val EXTRA_LABEL = "label"
        private const val REQ_FIRE = 7301
        private const val REQ_SHOW = 7302
    }
}
