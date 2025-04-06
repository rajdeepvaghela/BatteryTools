package com.rdapps.batterytools.settings

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rdapps.batterytools.util.Store
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    @Stable
    data class ViewState(
        val callOnAlert: Boolean = false,
        val callNumber: String = "",
        val smsOnAlert: Boolean = false,
        val smsNumber: String = "",
        val usbAlert: Boolean = false,
        val wirelessChargerAlert: Boolean = false,
        val acChargerAlert: Boolean = false
    )

    val viewStateFlow: StateFlow<ViewState>
        field = MutableStateFlow(ViewState())

    init {
        viewModelScope.launch {
            launch {
                Store.CallOnAlert.getFlow<Boolean>(app).collect { callOnAlert ->
                    viewStateFlow.update {
                        it.copy(
                            callOnAlert = callOnAlert
                        )
                    }
                }
            }

            launch {
                Store.CallNumber.getFlow<String>(app).collect { callNumber ->
                    viewStateFlow.update {
                        it.copy(
                            callNumber = callNumber
                        )
                    }
                }
            }

            launch {
                Store.SmsOnAlert.getFlow<Boolean>(app).collect { smsOnAlert ->
                    viewStateFlow.update {
                        it.copy(
                            smsOnAlert = smsOnAlert
                        )
                    }
                }
            }

            launch {
                Store.SmsNumber.getFlow<String>(app).collect { smsNumber ->
                    viewStateFlow.update {
                        it.copy(
                            smsNumber = smsNumber
                        )
                    }
                }
            }

            launch {
                Store.AlertOnUsb.getFlow<Boolean>(app).collect { usbAlert ->
                    viewStateFlow.update {
                        it.copy(
                            usbAlert = usbAlert
                        )
                    }
                }
            }

            launch {
                Store.AlertOnWirelessCharger.getFlow<Boolean>(app).collect { wirelessChargerAlert ->
                    viewStateFlow.update {
                        it.copy(
                            wirelessChargerAlert = wirelessChargerAlert
                        )
                    }
                }
            }

            launch {
                Store.AlertOnAcCharger.getFlow<Boolean>(app).collect { acChargerAlert ->
                    viewStateFlow.update {
                        it.copy(
                            acChargerAlert = acChargerAlert
                        )
                    }
                }
            }
        }
    }

    fun updateViewState(block: ViewState.() -> ViewState) {
        viewStateFlow.update { it.block() }
    }
}