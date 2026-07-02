package com.wird.feature.recitation.vosk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Downloads the Vosk Arabic model as a foreground service so it keeps running when
 * the app is minimized (a plain coroutine gets killed in the background, which is
 * what made the download fail). Progress is shown in an ongoing notification; the
 * shared [VoskModelManager] state drives the UI when the user returns.
 */
@AndroidEntryPoint
class ModelDownloadService : Service() {

    @Inject lateinit var modelManager: VoskModelManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var running = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        startForegroundCompat(buildNotification("Preparing…", 0, indeterminate = true))

        if (!running) {
            running = true
            scope.launch {
                val progress = launch {
                    modelManager.state.collect { st ->
                        if (st is ModelState.Downloading) {
                            updateNotification("Downloading Arabic voice model", (st.fraction * 100).toInt(), false)
                        }
                    }
                }
                modelManager.download()
                progress.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundCompat(notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(text: String, progress: Int, indeterminate: Boolean): android.app.Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Recitation voice model")
            .setContentText(text)
            .setOngoing(true)
            .setProgress(100, progress, indeterminate)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun updateNotification(text: String, progress: Int, indeterminate: Boolean) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(text, progress, indeterminate))
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Model download",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Downloading the offline recitation voice model" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private companion object {
        const val CHANNEL_ID = "wird_model_download"
        const val NOTIFICATION_ID = 8420
    }
}
