package com.rdapps.batterytools.widget

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.unit.ColorProvider
import com.rdapps.batterytools.R
import com.rdapps.batterytools.alert.AlertService
import com.rdapps.batterytools.main.MainActivity
import com.rdapps.batterytools.ui.theme.Color808080
import com.rdapps.batterytools.util.GlanceText
import com.rdapps.batterytools.util.Store
import com.rdapps.batterytools.util.isServiceRunning
import com.rdapps.common.model.BatteryStats
import com.rdapps.common.model.ChargingSource
import com.rdapps.common.model.ChargingState
import com.rdapps.common.utils.getReadableTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat

private const val TAG = "BatteryWidget"

class BatteryWidget : GlanceAppWidget() {

    override val stateDefinition = BatteryStatsStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val batteryStats = currentState<BatteryStats>()
            BatteryWidgetView(batteryStats)
        }
    }
}

suspend fun Context.updateWidgetUI() {
    BatteryWidget().updateAll(this)
}

class BatteryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BatteryWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (!context.isServiceRunning(BatteryStateMonitorService::class)) {
            val serviceIntent = Intent(context, BatteryStateMonitorService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled: ")
        context ?: return
        if (!context.isServiceRunning(BatteryStateMonitorService::class)) {
            val serviceIntent = Intent(context, BatteryStateMonitorService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled: ")
        val serviceIntent = Intent(context, BatteryStateMonitorService::class.java)
        context?.stopService(serviceIntent)
    }
}

@Composable
fun BatteryWidgetView(batteryStats: BatteryStats) {
    val context = LocalContext.current
    val vibrator = remember {
        context.getSystemService(Vibrator::class.java)
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.White)
            .cornerRadius(16.dp)
            .padding(10.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val state = batteryStats.chargingState
            if (state is ChargingState.Charging) {
                val decimalFormat = remember {
                    DecimalFormat("#.##")
                }

                Row(
                    modifier = GlanceModifier.fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    IconText(
                        iconRes = R.drawable.temperature,
                        text = "${batteryStats.temperature} °C",
                        modifier = GlanceModifier.defaultWeight()
                    )
                    IconText(
                        iconRes = R.drawable.current,
                        text = "${batteryStats.voltCurrent.current} mA",
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
                Row(
                    modifier = GlanceModifier.fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    IconText(
                        iconRes = R.drawable.voltage,
                        text = "${decimalFormat.format(batteryStats.voltCurrent.voltage / 1000f)} V",
                        modifier = GlanceModifier.defaultWeight()
                    )
                    IconText(
                        iconRes = R.drawable.power,
                        text = "${decimalFormat.format(batteryStats.voltCurrent.watt)} W",
                        modifier = GlanceModifier.defaultWeight()
                    )
                }

                val (iconRes, source) = when (state.source) {
                    ChargingSource.USB -> Pair(R.drawable.usb, "USB")
                    ChargingSource.AC -> Pair(R.drawable.ac_adapter, "AC adapter")
                    ChargingSource.Wireless -> Pair(R.drawable.wireless, "Wireless pad")
                }

                IconText(
                    iconRes = iconRes,
                    text = source,
                    modifier = GlanceModifier
                        .padding(top = 10.dp)
                )

                if (state.timeRemaining > 0) {
                    IconText(
                        iconRes = R.drawable.ic_round_time_24,
                        text = getReadableTime(state.timeRemaining),
                        modifier = GlanceModifier
                            .padding(top = 10.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                val isAlertSettingEnabled = runBlocking(Dispatchers.IO) {
                    state.source.isAlertSettingEnabled(context)
                }

                if (isAlertSettingEnabled || batteryStats.isAlertEnabled) {
                    if (batteryStats.isAlertEnabled) {
                        Button(
                            text = "Stop Alert",
                            onClick = {
                                vibrator.vibrate(
                                    VibrationEffect.createPredefined(
                                        VibrationEffect.EFFECT_CLICK
                                    )
                                )
                                val intent = Intent(context, AlertService::class.java)
                                context.stopService(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ColorProvider(Color.Red),
                                contentColor = ColorProvider(Color.White)
                            )
                        )
                    } else {
                        Button(
                            text = "Start Alert",
                            onClick = {
                                vibrator.vibrate(
                                    VibrationEffect.createPredefined(
                                        VibrationEffect.EFFECT_CLICK
                                    )
                                )
                                val intent = Intent(context, AlertService::class.java)
                                context.startForegroundService(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ColorProvider(MaterialTheme.colorScheme.primary),
                                contentColor = ColorProvider(MaterialTheme.colorScheme.onPrimary)
                            )
                        )
                    }
                }

            } else {
                BatteryPercentView(batteryStats.currentCharge, 60.sp)

                IconText(
                    iconRes = R.drawable.temperature,
                    text = "${batteryStats.temperature} °C"
                )
            }
        }
    }
}

@Composable
fun BatteryPercentView(currentCharge: Int, fontSize: TextUnit) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.Bottom
    ) {
        GlanceText(
            text = "$currentCharge",
            color = Color.Black,
            fontSize = fontSize,
            font = R.font.poppins_bold
        )

        GlanceText(
            text = "%",
            color = Color808080,
            fontSize = fontSize.div(2),
            font = R.font.poppins_medium,
            modifier = GlanceModifier
                .padding(start = 4.dp, bottom = 10.dp)
        )
    }
}

@Composable
fun IconText(iconRes: Int, text: String, modifier: GlanceModifier = GlanceModifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val icon = Icon.createWithResource(LocalContext.current, iconRes)

        Image(
            provider = ImageProvider(icon),
            modifier = GlanceModifier.size(24.dp),
            contentDescription = null
        )

        GlanceText(
            text = text,
            color = Color.Black,
            fontSize = 14.sp,
            font = R.font.poppins_semibold,
            modifier = GlanceModifier.padding(start = 4.dp)
        )
    }
}

private suspend fun ChargingSource.isAlertSettingEnabled(context: Context): Boolean {
    return (this == ChargingSource.AC &&
            Store.AlertOnAcCharger.getFlow<Boolean>(context).first()) ||
            (this == ChargingSource.Wireless &&
                    Store.AlertOnWirelessCharger.getFlow<Boolean>(context).first()) ||
            (this == ChargingSource.USB && Store.AlertOnUsb.getFlow<Boolean>(context).first())
}

@Composable
@Preview(showBackground = true)
fun BatteryWidgetPreview() {
    BatteryWidgetView(BatteryStats())
}