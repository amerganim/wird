package com.wird.feature.prayer.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wird.core.prayertimes.PrayerSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PrayerAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notifier: PrayerNotifier
    @Inject lateinit var scheduler: PrayerAlarmScheduler
    @Inject lateinit var settings: PrayerSettings

    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER).orEmpty()
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                if (settings.prefs.first().notificationsEnabled) {
                    if (prayer.isNotEmpty()) notifier.notifyPrayer(prayer)
                    // Re-arm the next prayer.
                    scheduler.scheduleNext()
                }
            } finally {
                pending.finish()
            }
        }
    }
}
