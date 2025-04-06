package com.rdapps.batterytools.widget

import android.content.Context
import androidx.datastore.dataStore

const val BATTERY_STATS_DATA_STORE_NAME = "batteryStats.json"

val Context.dataStore by dataStore(
    BATTERY_STATS_DATA_STORE_NAME,
    BatteryStatsSerializer
)