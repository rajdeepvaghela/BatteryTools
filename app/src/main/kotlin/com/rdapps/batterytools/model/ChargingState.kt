package com.rdapps.batterytools.model

import androidx.compose.runtime.Stable
import com.rdapps.batterytools.util.getReadableTime
import com.rdapps.batterytools.model.ChargingSource
import kotlinx.serialization.Serializable

@Stable
@Serializable
sealed class ChargingState {
    @Serializable
    data object NotCharging : ChargingState()

    @Serializable
    data class Charging(val source: ChargingSource, val timeRemaining: Long) : ChargingState() {
        fun getReadableTimeRemaining() = getReadableTime(timeRemaining)
    }

    @Serializable
    data object NA : ChargingState()
}
