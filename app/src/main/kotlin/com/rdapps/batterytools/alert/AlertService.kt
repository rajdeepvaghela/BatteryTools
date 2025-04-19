package com.rdapps.batterytools.alert

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.BatteryManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.rdapps.batterytools.R
import com.rdapps.batterytools.util.Store
import com.rdapps.batterytools.util.parseBatteryStats
import com.rdapps.batterytools.widget.BatteryStatsStateDefinition
import com.rdapps.batterytools.widget.BatteryWidget
import com.rdapps.batterytools.widget.dataStore
import com.rdapps.batterytools.widget.updateWidgetUI
import com.rdapps.common.model.BatteryStats
import com.rdapps.common.model.ChargingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.rdapps.common.R as CommonR

class AlertService : LifecycleService() {

    private val batteryManager by lazy {
        getSystemService(BatteryManager::class.java)
    }

    private val batteryChangeReceiver by lazy {
        BatteryChangeReceiver()
    }

    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    private val audioManager by lazy {
        getSystemService(AudioManager::class.java)
    }

    private val capabilityClient by lazy {
        Wearable.getCapabilityClient(this)
    }

    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            setDataSource(
                /* context = */ this@AlertService,
                /* uri = */ "android.resource://$packageName/${R.raw.instoreantitheftalarm}".toUri()
            )
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            isLooping = true
            prepareAsync()
        }
    }

    private var savedAlarmVolume = 0

    private fun attachForeground() {
        val channel = NotificationChannel(
            "BATTERY_CHANNEL",
            "Battery Channel",
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)

        val content = "Keeping an eye over your phone"

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, AlertActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channel.id)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentText(content)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(true)
            .setOngoing(true)
            .build()

        notificationManager.notify(FOREGROUND_SERVICE_NOTIFICATION_ID, notification)
        startForeground(
            FOREGROUND_SERVICE_NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")

        when (intent?.action) {
            ACTION_DISARM -> {
                mediaPlayer.pause()
                notificationManager.cancel(ALERT_NOTIFICATION_ID)
                return super.onStartCommand(intent, flags, startId)
            }
        }

        IntentFilter(Intent.ACTION_BATTERY_CHANGED).apply {
            ContextCompat.registerReceiver(
                this@AlertService, batteryChangeReceiver, this,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
        attachForeground()

        lifecycleScope.launch(Dispatchers.IO) {
            val stats = dataStore.data.first()
            batteryManager.updateAppWidget(stats.copy(isAlertEnabled = true))
        }

        return super.onStartCommand(intent, flags, startId)
    }

    inner class BatteryChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: intent=$intent")
            intent ?: return

            val stats = batteryManager.parseBatteryStats(this@AlertService, intent)
            Log.d(TAG, "onReceive: charging status = ${stats.chargingState}")

            when (val state = stats.chargingState) {
                is ChargingState.Charging -> {
                    Log.d(TAG, "onReceive: Charging ${state.source}")
                    notificationManager.cancel(ALERT_NOTIFICATION_ID)

                    if (mediaPlayer.isPlaying)
                        mediaPlayer.pause()

                    adjustAlarmVolume(false)
                    stopAlertActivity()
                }

                ChargingState.NotCharging -> {
                    Log.d(TAG, "onReceive: Not Charging")
                    adjustAlarmVolume(true)

                    if (!mediaPlayer.isPlaying)
                        mediaPlayer.start()

                    sendAlertToWatch()
                    showAlertNotification()

                    checkAndSendSms()
                    checkAndMakeCall()
                    startAlertActivity()
                }

                ChargingState.NA -> {}
            }

        }
    }

    private fun startAlertActivity() {
        startActivity(Intent(this, AlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        })
    }

    private fun stopAlertActivity() {
        val intent = Intent(AlertActivity.ACTION_STOP_ACTIVITY)
        sendBroadcast(intent)
    }

    private fun adjustAlarmVolume(startAlert: Boolean) {
        if (startAlert) {
            savedAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
        } else {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, savedAlarmVolume, 0)
        }
    }

    private fun sendAlertToWatch() {
        Log.d(TAG, "sendAlertToWatch: ")

        capabilityClient.getCapability(
            getString(CommonR.string.wearable_capability),
            CapabilityClient.FILTER_ALL
        ).addOnSuccessListener {
            Log.d(TAG, "sendAlertToWatch: node count=${it.nodes.size}")
            val node = it.nodes.firstOrNull() ?: return@addOnSuccessListener

            Wearable.getMessageClient(this)
                .sendMessage(
                    node.id,
                    getString(CommonR.string.alert),
                    "alert".encodeToByteArray()
                ).addOnSuccessListener {
                    Log.d(TAG, "sendAlertToWatch: $it")
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

        notificationManager.createNotificationChannel(channel)

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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        return PendingIntent.getActivity(
            context,
            0,
            alertActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun checkAndMakeCall() = lifecycleScope.launch(Dispatchers.IO) {
        val callOnAlert = Store.CallOnAlert.getFlow<Boolean>(this@AlertService).first()
        val callNumber = Store.CallNumber.getFlow<String>(this@AlertService).first()

        if (!callOnAlert || callNumber.isEmpty())
            return@launch

        delay(1000)

        Log.d(TAG, "makeCall: ")
        try {
            if (ContextCompat.checkSelfPermission(
                    this@AlertService,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = "tel:$callNumber".toUri()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkAndSendSms() = lifecycleScope.launch(Dispatchers.IO) {
        val smsOnAlert = Store.SmsOnAlert.getFlow<Boolean>(this@AlertService).first()
        val smsNumber = Store.SmsNumber.getFlow<String>(this@AlertService).first()

        if (!smsOnAlert || smsNumber.isEmpty())
            return@launch

        Log.d(TAG, "sendSms: ")
        try {
            val smsManager = getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(
                smsNumber,
                null,
                "Your phone got disconnected from charger",
                null,
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        runBlocking(Dispatchers.IO) {
            val stats = dataStore.data.first()
            batteryManager.updateAppWidget(stats.copy(isAlertEnabled = false))
        }
        notificationManager.cancelAll()
        unregisterReceiver(batteryChangeReceiver)
        mediaPlayer.release()
        super.onDestroy()
    }

    private suspend fun BatteryManager.updateAppWidget(stats: BatteryStats) {
        val manager = GlanceAppWidgetManager(this@AlertService)
        val glanceIds = manager.getGlanceIds(BatteryWidget::class.java)
        glanceIds.forEach {
            updateAppWidgetState(
                context = this@AlertService,
                definition = BatteryStatsStateDefinition,
                glanceId = it,
                updateState = { stats }
            )
        }
        updateWidgetUI()
    }

    companion object {
        private const val TAG = "AlertService"
        private const val FOREGROUND_SERVICE_NOTIFICATION_ID = 1234
        private const val ALERT_NOTIFICATION_ID = 8910
        const val ACTION_DISARM = "disarm"
    }

}