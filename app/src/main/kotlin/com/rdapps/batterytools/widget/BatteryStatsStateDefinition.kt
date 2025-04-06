package com.rdapps.batterytools.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.rdapps.batterytools.model.BatteryStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object BatteryStatsStateDefinition : GlanceStateDefinition<BatteryStats> {

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<BatteryStats> {
        return context.dataStore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(BATTERY_STATS_DATA_STORE_NAME)
    }
}

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