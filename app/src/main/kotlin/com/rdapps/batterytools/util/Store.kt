package com.rdapps.batterytools.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rdapps.batterytools.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map

enum class Store(val def: Any) {
    CallOnAlert(false),
    SmsOnAlert(false),
    CallNumber(""),
    SmsNumber(""),
    AlertOnUsb(false),
    AlertOnWirelessCharger(false),
    AlertOnAcCharger(false);

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getDataStoreKey(
        clazz: Class<T>,
        isSecure: Boolean = false
    ): Preferences.Key<T> {
        val key = name.lowercase()

        return when (clazz::class.java) {
            Boolean::class.java -> booleanPreferencesKey(key)
            Float::class.java -> floatPreferencesKey(key)
            Long::class.java -> longPreferencesKey(key)
            Int::class.java -> intPreferencesKey(key)
            Double::class.java -> doublePreferencesKey(key)
            else -> stringPreferencesKey(key)
        } as Preferences.Key<T>
    }

    suspend inline fun <reified T : Any> set(
        context: Context,
        value: T,
        isSecure: Boolean = false
    ) {
        context.appDataStore.edit {
            it[getDataStoreKey(clazz = T::class.java, isSecure)] = value
        }
    }

    inline fun <reified T : Any> getFlow(
        context: Context,
        default: T? = null,
        isSecure: Boolean = false,
        clazz: Class<T> = T::class.java
    ): Flow<T> {
        return context.appDataStore.data
            .distinctUntilChangedBy { it[getDataStoreKey(clazz, isSecure)] }
            .map {
                clazz.cast(it[getDataStoreKey(clazz, isSecure)] ?: default ?: def)
                    ?: error("Cast Failure")
            }
    }
}

val Context.appDataStore by preferencesDataStore(name = BuildConfig.DATASTORE_NAME)