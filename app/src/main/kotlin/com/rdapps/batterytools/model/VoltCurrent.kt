package com.rdapps.batterytools.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class VoltCurrent(
    val voltage: Int = 0,
    val current: Int = 0
) {
    val watt = (voltage / 1000f) * (current / 1000f)
}
