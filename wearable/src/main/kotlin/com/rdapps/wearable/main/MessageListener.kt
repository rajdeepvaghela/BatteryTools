package com.rdapps.wearable.main

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.rdapps.common.model.BatteryStats
import com.rdapps.wearable.R
import com.rdapps.wearable.alert.AlertActivity
import com.rdapps.wearable.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.rdapps.common.R as CommonR

class MessageListener : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(event: MessageEvent) {
        super.onMessageReceived(event)
        Log.d(
            TAG,
            "onMessageReceived: path=${event.path} data=${event.data.toString(Charsets.UTF_8)}"
        )

        when (event.path) {
            getString(CommonR.string.message_battery_stats) -> {
                val message = event.data.toString(Charsets.UTF_8)
                val stats = Json.decodeFromString(BatteryStats.serializer(), message)

                scope.launch {
                    dataStore.updateData {
                        stats
                    }
                }

                Log.d(TAG, "onMessageReceived: $stats")
            }

            getString(CommonR.string.alert) -> {
                val message = event.data.toString(Charsets.UTF_8)
                showAlertNotification()

                Log.d(TAG, "onMessageReceived: $message")
            }
        }
    }

    private fun showAlertNotification() {
        val channel = NotificationChannel(
            "ALERT_CHANNEL",
            "Alert Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setBypassDnd(true)
            enableLights(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val notificationManager = getSystemService(NotificationManager::class.java)

        notificationManager.createNotificationChannel(channel)

        startActivity(
            Intent(this, AlertActivity::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            }
        )

        val content = "Device has been unplugged."

        val notification = NotificationCompat.Builder(this, channel.id)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Alert")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(getFullScreenIntent(this))
            .setFullScreenIntent(getFullScreenIntent(this), true)
            .setShowWhen(true)
            .setOngoing(true)
            .build()

        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
    }

    private fun getFullScreenIntent(context: Context): PendingIntent {
        val alertActivityIntent = Intent(context, AlertActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
        }
        return PendingIntent.getActivity(
            context,
            0,
            alertActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        private const val TAG = "MessageListener"
        private const val ALERT_NOTIFICATION_ID = 8910
    }
}