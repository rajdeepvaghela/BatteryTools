package com.rdapps.batterytools.settings

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.lifecycleScope
import com.rdapps.batterytools.alert.AlertService
import com.rdapps.batterytools.ui.theme.BatteryToolsTheme
import com.rdapps.batterytools.util.Store
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    private val viewModel by viewModels<SettingsViewModel>()

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
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
                SettingsScreen(
                    viewStateFlow = viewModel.viewStateFlow,
                    onViewEvent = ::onViewEvent
                )
            }
        }

        lifecycleScope.launch {
            combine(
                Store.AlertOnAcCharger.getFlow<Boolean>(this@SettingsActivity),
                Store.AlertOnUsb.getFlow<Boolean>(this@SettingsActivity),
                Store.AlertOnWirelessCharger.getFlow<Boolean>(this@SettingsActivity),
            ) { ac, usb, wireless ->
                ac || usb || wireless
            }.collect { isAnyEnabled ->
                if (!isAnyEnabled) {
                    val intent = Intent().setComponent(
                        ComponentName(
                            this@SettingsActivity,
                            AlertService::class.java
                        )
                    )
                    stopService(intent)
                }
            }
        }
    }

    private fun onViewEvent(event: SettingsViewEvent) {
        when (event) {
            is SettingsViewEvent.OnAcChargerAlertChanged -> {
                lifecycleScope.launch {
                    Store.AlertOnAcCharger.set(this@SettingsActivity, event.acChargerAlert)
                }
            }

            is SettingsViewEvent.OnCallNumberChanged -> {
                lifecycleScope.launch {
                    Store.CallNumber.set(this@SettingsActivity, event.callNumber)
                }
            }

            is SettingsViewEvent.OnCallOnAlertChanged -> {
                if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    lifecycleScope.launch {
                        Store.CallOnAlert.set(this@SettingsActivity, event.callOnAlert)
                    }
                } else {
                    requestPermission.launch(Manifest.permission.CALL_PHONE)
                }
            }

            is SettingsViewEvent.OnSmsNumberChanged -> {
                lifecycleScope.launch {
                    Store.SmsNumber.set(this@SettingsActivity, event.smsNumber)
                }
            }

            is SettingsViewEvent.OnSmsOnAlertChanged -> {
                if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    lifecycleScope.launch {
                        Store.SmsOnAlert.set(this@SettingsActivity, event.smsOnAlert)
                    }
                } else {
                    requestPermission.launch(Manifest.permission.SEND_SMS)
                }
            }

            is SettingsViewEvent.OnUsbAlertChanged -> {
                lifecycleScope.launch {
                    Store.AlertOnUsb.set(this@SettingsActivity, event.usbAlert)
                }
            }

            is SettingsViewEvent.OnWirelessChargerAlertChanged -> {
                lifecycleScope.launch {
                    Store.AlertOnWirelessCharger.set(
                        this@SettingsActivity,
                        event.wirelessChargerAlert
                    )
                }
            }
        }
    }
}