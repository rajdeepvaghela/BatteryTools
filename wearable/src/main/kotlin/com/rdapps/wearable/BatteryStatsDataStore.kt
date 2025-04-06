package com.rdapps.wearable

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.rdapps.common.model.BatteryStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

const val BATTERY_STATS_DATA_STORE_NAME = "batteryStats.json"

val Context.dataStore by dataStore(
    BATTERY_STATS_DATA_STORE_NAME,
    BatteryStatsSerializer
)

object BatteryStatsSerializer : Serializer<BatteryStats> {
    override val defaultValue: BatteryStats = BatteryStats()

    override suspend fun readFrom(input: InputStream): BatteryStats {
        return Json.decodeFromString(
            deserializer = BatteryStats.serializer(),
            string = input.readBytes().decodeToString()
        )
    }

    override suspend fun writeTo(t: BatteryStats, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(
                    serializer = BatteryStats.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}