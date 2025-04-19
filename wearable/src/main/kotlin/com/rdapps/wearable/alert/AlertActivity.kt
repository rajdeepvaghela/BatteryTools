package com.rdapps.wearable.alert

import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.rdapps.wearable.theme.BatteryToolsTheme
import com.rdapps.common.R as CommonR

class AlertActivity : FragmentActivity() {

    private val capabilityClient by lazy {
        Wearable.getCapabilityClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Black.toArgb()
            )
        )

        setContent {
            BatteryToolsTheme {
                AlertScreen(::onViewEvents)
            }
        }
    }

    fun onViewEvents(event: AlertScreenViewEvent) {
        when (event) {
            AlertScreenViewEvent.StopAlert -> {
                stopAlertAndExit()
            }
        }
    }

    private fun stopAlertAndExit() {
        capabilityClient.getCapability(
            getString(CommonR.string.mobile_capability),
            CapabilityClient.FILTER_ALL
        ).addOnSuccessListener {
            Log.d(TAG, "stopAlertAndExit: node count=${it.nodes.size}")
            val node = it.nodes.firstOrNull() ?: return@addOnSuccessListener

            Wearable.getMessageClient(this)
                .sendMessage(
                    node.id,
                    getString(CommonR.string.stop_alert),
                    "stop_alert".encodeToByteArray()
                ).addOnSuccessListener {
                    Log.d(TAG, "stopAlertAndExit: $it")
                    finish()
                }
        }
    }

    companion object {
        private const val TAG = "AlertActivity"
    }
}
