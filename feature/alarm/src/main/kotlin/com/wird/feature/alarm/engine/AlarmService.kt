package com.wird.feature.alarm.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.wird.feature.alarm.R
import com.wird.feature.alarm.data.AlarmSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject lateinit var settings: AlarmSettings
    @Inject lateinit var scheduler: AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null
    private var volumeJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }

        val label = intent?.getStringExtra(AlarmScheduler.EXTRA_LABEL).orEmpty().ifEmpty { "Fajr" }
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification(label))
        acquireWakeLock()
        startSound()
        startVibration()
        launchAlarmScreen(label)

        // Re-arm the next alarm (next Fajr or tomorrow's fixed time).
        scope.launch {
            val prefs = settings.prefs.first()
            if (prefs.enabled) scheduler.scheduleFor(prefs)
        }
        return START_STICKY
    }

    private fun launchAlarmScreen(label: String) {
        val intent = Intent(this, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AlarmScheduler.EXTRA_LABEL, label)
        }
        runCatching { startActivity(intent) }
    }

    private fun buildNotification(label: String): android.app.Notification {
        val fullScreen = PendingIntent.getActivity(
            this,
            0,
            Intent(this, AlarmActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(AlarmScheduler.EXTRA_LABEL, label),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_notification)
            .setContentTitle("$label alarm")
            .setContentText("Time to wake up for $label.")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreen, true)
            .build()
    }

    private fun startSound() {
        runCatching {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                ?: Settings.System.DEFAULT_ALARM_ALERT_URI
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(this@AlarmService, uri)
                isLooping = true
                setVolume(INITIAL_VOLUME, INITIAL_VOLUME)
                prepare()
                start()
            }
            // Gradually rise to full volume over ~30s.
            volumeJob = scope.launch {
                var volume = INITIAL_VOLUME
                while (volume < 1f) {
                    delay(2_500)
                    volume = (volume + 0.1f).coerceAtMost(1f)
                    runCatching { mediaPlayer?.setVolume(volume, volume) }
                }
            }
        }
    }

    private fun startVibration() {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vibrator = vib
        val pattern = longArrayOf(0, 800, 1000)
        vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wird:alarm").apply {
            acquire(5 * 60 * 1000L)
        }
    }

    private fun stopAlarm() {
        volumeJob?.cancel()
        runCatching { mediaPlayer?.stop() }
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        if (wakeLock?.isHeld == true) wakeLock?.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wake-up alarm",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Full-screen wake-up alarm"
                setSound(null, null)
                enableVibration(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    companion object {
        const val ACTION_STOP = "com.wird.feature.alarm.STOP"
        private const val CHANNEL_ID = "wird_alarm"
        private const val NOTIFICATION_ID = 7310
        private const val INITIAL_VOLUME = 0.2f
    }
}
