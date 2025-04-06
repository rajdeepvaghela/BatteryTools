package com.rdapps.common.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class BatteryStats(
    val capacity: Int = 0,
    val chargingState: ChargingState = ChargingState.NA,
    val currentCharge: Int = 0,
    val voltCurrent: VoltCurrent = VoltCurrent(),
    val temperature: Float = 0f,
    val batteryHealth: BatteryHealth = BatteryHealth.NA,
    val batteryTech: String = "",
    val isAlertEnabled: Boolean = false,
)
