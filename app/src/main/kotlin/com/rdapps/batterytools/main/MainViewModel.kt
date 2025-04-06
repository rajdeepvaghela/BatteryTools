package com.rdapps.batterytools.main

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rdapps.batterytools.model.BatteryStats
import com.rdapps.batterytools.model.ChargingSource
import com.rdapps.batterytools.model.ChargingState
import com.rdapps.batterytools.util.Store
import com.rdapps.batterytools.widget.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    @Stable
    data class ViewState(
        val batteryStat: BatteryStats = BatteryStats(),
        val isAlertOnAcCharger: Boolean = false,
        val isAlertOnWirelessCharger: Boolean = false,
        val isAlertOnUsb: Boolean = false
    ) {
        val isAlertSettingEnabled: Boolean =
            (batteryStat.chargingState is ChargingState.Charging) &&
                    ((batteryStat.chargingState.source == ChargingSource.AC && isAlertOnAcCharger) ||
                            (batteryStat.chargingState.source == ChargingSource.Wireless && isAlertOnWirelessCharger) ||
                            (batteryStat.chargingState.source == ChargingSource.USB && isAlertOnUsb))

    }

    val viewStateFlow: StateFlow<ViewState>
        field = MutableStateFlow(ViewState())

    init {
        viewModelScope.launch {
            app.dataStore.data.collect { stats ->
                viewStateFlow.update {
                    it.copy(
                        batteryStat = stats
                    )
                }
            }
        }

        viewModelScope.launch {
            launch {
                Store.AlertOnAcCharger.getFlow<Boolean>(app).collect { value ->
                    viewStateFlow.update {
                        it.copy(
                            isAlertOnAcCharger = value
                        )
                    }
                }
            }

            launch {
                Store.AlertOnWirelessCharger.getFlow<Boolean>(app).collect { value ->
                    viewStateFlow.update {
                        it.copy(
                            isAlertOnWirelessCharger = value
                        )
                    }
                }
            }

            launch {
                Store.AlertOnUsb.getFlow<Boolean>(app).collect { value ->
                    viewStateFlow.update {
                        it.copy(
                            isAlertOnUsb = value
                        )
                    }
                }
            }
        }
    }

    fun updateBatteryCapacity(batteryCapacity: Int) {
        viewStateFlow.update {
            it.copy(
                batteryStat = it.batteryStat.copy(
                    capacity = batteryCapacity
                )
            )
        }
    }

}