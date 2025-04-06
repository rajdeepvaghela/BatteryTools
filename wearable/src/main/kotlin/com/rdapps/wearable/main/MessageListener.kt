package com.rdapps.wearable.main

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.rdapps.common.model.BatteryStats
import com.rdapps.wearable.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class MessageListener : WearableListenerService() {

    override fun onMessageReceived(event: MessageEvent) {
        super.onMessageReceived(event)
        Log.d(TAG, "onMessageReceived: ${event.data.toString(Charsets.UTF_8)}")

        val message = event.data.toString(Charsets.UTF_8)
        val stats = Json.decodeFromString(BatteryStats.serializer(), message)

        runBlocking(Dispatchers.IO) {
            dataStore.updateData {
                stats
            }
        }

        Log.d(TAG, "onMessageReceived: $stats")
    }

    companion object {
        private const val TAG = "MessageListener"
    }
}