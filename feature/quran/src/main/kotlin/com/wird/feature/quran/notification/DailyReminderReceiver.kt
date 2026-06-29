package com.wird.feature.quran.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wird.feature.quran.data.HabitSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DailyReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var notifier: DailyNotifier
    @Inject lateinit var scheduler: DailyReminderScheduler
    @Inject lateinit var settings: HabitSettings

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                if (settings.reminder.first().enabled) {
                    notifier.notifyDaily()
                    scheduler.reschedule() // re-arm tomorrow
                }
            } finally {
                pending.finish()
            }
        }
    }
}
