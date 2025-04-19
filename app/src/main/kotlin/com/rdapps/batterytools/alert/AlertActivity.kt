package com.rdapps.batterytools.alert

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.rdapps.batterytools.ui.theme.BatteryToolsTheme
import com.rdapps.batterytools.util.canUseBiometric
import java.util.concurrent.Executor

class AlertActivity : FragmentActivity() {

    private val executor: Executor by lazy {
        ContextCompat.getMainExecutor(this)
    }

    private val biometricManager: BiometricManager by lazy {
        BiometricManager.from(this)
    }

    private val keyguardManager by lazy {
        getSystemService(KeyguardManager::class.java)
    }

    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            stopAlertAndExit()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
        }
    }

    private val stopActivityBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
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

        val filter = IntentFilter(ACTION_STOP_ACTIVITY)
        ContextCompat.registerReceiver(
            this,
            stopActivityBroadcast,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    fun onViewEvents(event: AlertScreenViewEvent) {
        when (event) {
            AlertScreenViewEvent.StopAlert -> {
                if (keyguardManager.isKeyguardLocked && biometricManager.canUseBiometric()) {
                    val biometricPrompt = BiometricPrompt(this, executor, callback)

                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Biometric Authentication")
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                        .build()

                    biometricPrompt.authenticate(promptInfo)
                } else {
                    stopAlertAndExit()
                }
            }
        }
    }

    private fun stopAlertAndExit() {
        val intent = Intent().setComponent(ComponentName(this, AlertService::class.java))
        stopService(intent)
        finish()
    }

    companion object {
        private const val TAG = "AlertActivity"
        const val ACTION_STOP_ACTIVITY: String =
            "com.rdapps.batterytools.AlertActivity:STOP_ACTIVITY"
    }
}
