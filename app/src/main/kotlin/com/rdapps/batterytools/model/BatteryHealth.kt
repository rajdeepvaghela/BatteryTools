package com.rdapps.batterytools.model

import androidx.compose.runtime.Stable

@Stable
enum class BatteryHealth {
    Cold,
    Good,
    OverHeat,
    OverVoltage,
    Dead,
    NA
}