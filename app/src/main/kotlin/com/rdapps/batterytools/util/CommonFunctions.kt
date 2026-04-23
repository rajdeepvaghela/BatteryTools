package com.rdapps.batterytools.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.rdapps.batterytools.alert.AlertService
import com.rdapps.common.model.BatteryHealth
import com.rdapps.common.model.BatteryStats
import com.rdapps.common.model.ChargingSource
import com.rdapps.common.model.ChargingState
import com.rdapps.common.model.VoltCurrent
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.reflect.KClass
import com.rdapps.common.R as CommonR

@SuppressLint("PrivateApi")
fun Context.getBatteryCapacity(): Int {
    var batteryCapacity = 0.0
    val powerProfileClass = "com.android.internal.os.PowerProfile"
    try {
        val mPowerProfile = Class.forName(powerProfileClass)
            .getConstructor(Context::class.java)
            .newInstance(this)
        batteryCapacity = Class
            .forName(powerProfileClass)
            .getMethod("getBatteryCapacity")
            .invoke(mPowerProfile) as Double
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return batteryCapacity.toInt()
}

fun BatteryManager.parseBatteryStats(context: Context, intent: Intent): BatteryStats {
    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
    val chargePlug: Int = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
    val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
    val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f
    val level = getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: ""

    val batteryHealth = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
        BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.Cold
        BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.Good
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OverHeat
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OverVoltage
        BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.Dead
        else -> BatteryHealth.NA
    }

    val currentNow = abs(getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW))

    val current = if (currentNow / 1000 > 100)
        currentNow / 1000
    else
        currentNow

    val timeRemaining = computeChargeTimeRemaining()

    val chargingState = if (isCharging)
        ChargingState.Charging(
            source = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_USB -> ChargingSource.USB
                BatteryManager.BATTERY_PLUGGED_AC -> ChargingSource.AC
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingSource.Wireless
                else -> ChargingSource.AC
            },
            timeRemaining = timeRemaining
        )
    else
        ChargingState.NotCharging

    return BatteryStats(
        capacity = context.getBatteryCapacity(),
        chargingState = chargingState,
        currentCharge = level,
        voltCurrent = VoltCurrent(
            voltage = voltage,
            current = current
        ),
        temperature = temperature,
        batteryHealth = batteryHealth,
        batteryTech = technology,
        isAlertEnabled = context.isServiceRunning(AlertService::class)
    )
}

@Suppress("DEPRECATION")
fun <T : Service> Context.isServiceRunning(clazz: KClass<T>): Boolean {
    val activityManager = getSystemService(ActivityManager::class.java)
    return activityManager.getRunningServices(Int.MAX_VALUE)
        .any { it.service.className == clazz.qualifiedName }
}

fun BiometricManager.checkBiometricAvailability(): Int {
    return when (canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            // Biometric authentication is available.
            BiometricManager.BIOMETRIC_SUCCESS
        }

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            // No biometric hardware available.
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
        }

        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            // Biometric hardware is currently unavailable.
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE
        }

        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            // User hasn't enrolled any biometrics.
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        }

        else -> {
            // Handle other errors.
            -1
        }
    }
}

fun BiometricManager.canUseBiometric() =
    checkBiometricAvailability() == BiometricManager.BIOMETRIC_SUCCESS

fun CapabilityClient.sendBatteryStats(context: Context, stats: BatteryStats) {
    Log.d("CommonFunctions", "sendBatteryStats: ")

    getCapability(
        context.getString(CommonR.string.wearable_capability),
        CapabilityClient.FILTER_ALL
    ).addOnSuccessListener {
        Log.d("CommonFunctions", "sendBatteryStats: node count=${it.nodes.size}")
        val node = it.nodes.firstOrNull() ?: return@addOnSuccessListener

        val data = Json.encodeToString(stats)
        Wearable.getMessageClient(context)
            .sendMessage(
                node.id,
                context.getString(CommonR.string.message_battery_stats),
                data.encodeToByteArray()
            ).addOnSuccessListener { messageId ->
                Log.d("CommonFunctions", "sendBatteryStats: $messageId")
            }
    }
}
