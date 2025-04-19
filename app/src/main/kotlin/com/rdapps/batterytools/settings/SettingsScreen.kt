package com.rdapps.batterytools.settings

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SettingsScreen

sealed interface SettingsViewEvent {
    data class OnCallOnAlertChanged(val callOnAlert: Boolean) : SettingsViewEvent
    data class OnCallNumberChanged(val callNumber: String) : SettingsViewEvent
    data class OnSmsOnAlertChanged(val smsOnAlert: Boolean) : SettingsViewEvent
    data class OnSmsNumberChanged(val smsNumber: String) : SettingsViewEvent
    data class OnUsbAlertChanged(val usbAlert: Boolean) : SettingsViewEvent
    data class OnWirelessChargerAlertChanged(val wirelessChargerAlert: Boolean) : SettingsViewEvent
    data class OnAcChargerAlertChanged(val acChargerAlert: Boolean) : SettingsViewEvent
}

@Composable
fun SettingsScreen(
    viewStateFlow: StateFlow<SettingsViewModel.ViewState>,
    onViewEvent: (SettingsViewEvent) -> Unit
) {
    val viewState by viewStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val vibrator = remember {
        context.getSystemService(Vibrator::class.java)
    }
    Column(
        modifier = Modifier
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            // Call on Alert
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Call on Alert")
                Switch(
                    checked = viewState.callOnAlert,
                    onCheckedChange = {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        onViewEvent(SettingsViewEvent.OnCallOnAlertChanged(it))
                    }
                )
            }

            var callNumber by remember(viewState.callNumber) {
                mutableStateOf(viewState.callNumber)
            }

            // add a check here for calling permission
            AnimatedVisibility(viewState.callOnAlert) {
                // Call Number
                OutlinedTextField(
                    value = callNumber,
                    onValueChange = {
                        callNumber = it
                        onViewEvent(SettingsViewEvent.OnCallNumberChanged(it))
                    },
                    label = { Text("Call Number") },
                    placeholder = { Text("+91 9999988888") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Column {
            // SMS on Alert
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SMS on Alert")
                Switch(
                    checked = viewState.smsOnAlert,
                    onCheckedChange = {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        onViewEvent(SettingsViewEvent.OnSmsOnAlertChanged(it))
                    }
                )
            }

            var smsNumber by remember(viewState.smsNumber) {
                mutableStateOf(viewState.smsNumber)
            }

            AnimatedVisibility(viewState.smsOnAlert) {
                // SMS Number
                OutlinedTextField(
                    value = smsNumber,
                    onValueChange = {
                        smsNumber = it
                        onViewEvent(SettingsViewEvent.OnSmsNumberChanged(it))
                    },
                    label = { Text("SMS Number") },
                    placeholder = { Text("+91 9999988888") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp)
        )

        // Alert for...
        Text("Enable Alert for:")

        Column {
            // USB
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = viewState.usbAlert,
                    onCheckedChange = {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        onViewEvent(SettingsViewEvent.OnUsbAlertChanged(it))
                    }
                )
                Text("USB")
            }

            // Wireless Charger
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = viewState.wirelessChargerAlert,
                    onCheckedChange = {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        onViewEvent(SettingsViewEvent.OnWirelessChargerAlertChanged(it))
                    }
                )
                Text("Wireless Charger")
            }

            // AC Charger
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = viewState.acChargerAlert,
                    onCheckedChange = {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        onViewEvent(SettingsViewEvent.OnAcChargerAlertChanged(it))
                    }
                )
                Text("AC Charger")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    MaterialTheme {
        SettingsScreen(MutableStateFlow(SettingsViewModel.ViewState(callOnAlert = true))) {}
    }
}