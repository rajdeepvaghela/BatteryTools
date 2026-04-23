package com.rdapps.batterytools.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.Wearable
import com.rdapps.batterytools.R
import com.rdapps.batterytools.main.MainActivity
import com.rdapps.batterytools.util.parseBatteryStats
import com.rdapps.batterytools.util.sendBatteryStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatteryStateMonitorService : LifecycleService() {

    private val batteryManager by lazy {
        getSystemService(BatteryManager::class.java)
    }

    private val batteryChangeReceiver by lazy {
        BatteryChangeReceiver()
    }

    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        IntentFilter(Intent.ACTION_BATTERY_CHANGED).apply {
            ContextCompat.registerReceiver(
                this@BatteryStateMonitorService, batteryChangeReceiver, this,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
        attachForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand: ")

        batteryManager.getIntProperty(BatteryManager.BATTERY_PLUGGED_AC)
        return START_STICKY
    }

    private fun attachForeground() {

        val channel = NotificationChannel(
            "BATTERY_CHANNEL",
            "Battery Channel",
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val content = "Monitoring Battery for the widget or wearable"

        val notification = NotificationCompat.Builder(this, channel.id)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Battery Monitoring")
            .setContentText(content)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(true)
            .setOngoing(true)
            .build()

        notificationManager.notify(2345, notification)
        startForeground(2345, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
    }

    inner class BatteryChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: intent=$intent")
            intent ?: return

            lifecycleScope.launch(Dispatchers.IO) {
                batteryManager.updateAppWidget(intent)
            }
        }
    }

    private val capabilityClient by lazy {
        Wearable.getCapabilityClient(this)
    }

    private suspend fun BatteryManager.updateAppWidget(intent: Intent) {
        val manager = GlanceAppWidgetManager(this@BatteryStateMonitorService)
        val glanceIds = manager.getGlanceIds(BatteryWidget::class.java)
        val stats = parseBatteryStats(this@BatteryStateMonitorService, intent)
        glanceIds.forEach {
            updateAppWidgetState(
                context = this@BatteryStateMonitorService,
                definition = BatteryStatsStateDefinition,
                glanceId = it,
                updateState = { stats }
            )
        }
        updateWidgetUI()
        capabilityClient.sendBatteryStats(this@BatteryStateMonitorService, stats)
    }

    override fun onDestroy() {
        unregisterReceiver(batteryChangeReceiver)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "BatteryStateMonitorServ"
    }
}