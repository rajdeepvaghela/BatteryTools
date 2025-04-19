package com.rdapps.batterytools.main

import android.content.ComponentName
import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.rdapps.batterytools.alert.AlertActivity
import com.rdapps.batterytools.alert.AlertService
import com.rdapps.common.R as CommonR

class MessageListener : WearableListenerService() {
    override fun onMessageReceived(event: MessageEvent) {
        super.onMessageReceived(event)
        Log.d(
            TAG,
            "onMessageReceived: path=${event.path} data=${event.data.toString(Charsets.UTF_8)}"
        )

        when (event.path) {
            getString(CommonR.string.stop_alert) -> {
                val intent = Intent().setComponent(ComponentName(this, AlertService::class.java))
                stopService(intent)

                val broadcastIntent = Intent(AlertActivity.ACTION_STOP_ACTIVITY)
                sendBroadcast(broadcastIntent)
            }
        }
    }

    companion object {
        private const val TAG = "MessageListener"
    }
}