package com.rdapps.batterytools.main

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rdapps.batterytools.R
import com.rdapps.common.model.BatteryHealth
import com.rdapps.common.model.ChargingSource
import com.rdapps.common.model.ChargingState
import com.rdapps.common.model.VoltCurrent
import com.rdapps.batterytools.ui.theme.BatteryToolsTheme
import com.rdapps.batterytools.ui.theme.Color261C90
import com.rdapps.batterytools.ui.theme.Color808080
import java.text.DecimalFormat

private const val TAG = "MainScreen"

sealed interface MainScreenViewEvent {
    object OnStartAlertClicked : MainScreenViewEvent
    object OnStopAlertClicked : MainScreenViewEvent
    object OnSettingsClicked : MainScreenViewEvent
}

@Composable
fun MainScreen(onViewEvents: (MainScreenViewEvent) -> Unit) {
    val viewModel: MainViewModel = viewModel()

    val viewState by viewModel.viewStateFlow.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Log.d(TAG, "MainActivityScreen: composed")
            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
            ) {

                val (percentView, temperatureView) = createRefs()

                BatteryPercentView(
                    viewState.batteryStat.currentCharge,
                    modifier = Modifier
                        .constrainAs(percentView) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .fillMaxWidth(0.4f))

                BatteryTemperatureView(
                    viewState.batteryStat.temperature,
                    modifier = Modifier.constrainAs(temperatureView) {
                        start.linkTo(percentView.end)
                        end.linkTo(parent.end)
                        top.linkTo(percentView.top)
                        bottom.linkTo(percentView.bottom)
                    })
            }
            ChargingStatsView(
                chargingState = viewState.batteryStat.chargingState,
                voltCurrent = viewState.batteryStat.voltCurrent,
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )

            AnimatedVisibility(
                visible = viewState.isAlertSettingEnabled || viewState.batteryStat.isAlertEnabled,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 20.dp)
            ) {
                if (viewState.batteryStat.isAlertEnabled) {
                    Button(
                        onClick = {
                            onViewEvents(MainScreenViewEvent.OnStopAlertClicked)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Stop Alert", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = {
                            onViewEvents(MainScreenViewEvent.OnStartAlertClicked)
                        }
                    ) {
                        Text(text = "Start Alert", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            BatteryStaticStatsView(
                capacity = viewState.batteryStat.capacity,
                batteryHealth = viewState.batteryStat.batteryHealth,
                batteryTech = viewState.batteryStat.batteryTech,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 20.dp)
            )
        }

        Icon(
            painter = painterResource(R.drawable.settings),
            contentDescription = "settings",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable(
                    indication = ripple(bounded = false),
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onViewEvents(MainScreenViewEvent.OnSettingsClicked)
                }
                .padding(10.dp)
        )
    }
}

@Composable
fun BatteryStaticStatsView(
    capacity: Int, batteryHealth: BatteryHealth, batteryTech: String, modifier: Modifier = Modifier
) {
    Log.d(TAG, "BatteryStaticStatsView: composed")
    Row(
        modifier = modifier
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color = Color.Black.copy(alpha = 0.05f))
            .padding(10.dp)
    ) {
        @Composable
        fun text(text: String, color: Color) = Text(
            text = text, fontWeight = FontWeight.SemiBold, color = color
        )

        Column {
            text("Capacity", Color.Black)
            Spacer(modifier = Modifier.height(10.dp))
            text("Tech", Color.Black)
            Spacer(modifier = Modifier.height(10.dp))
            text("Health", Color.Black)
        }
        Column(modifier = Modifier.padding(start = 20.dp)) {
            text("$capacity mAh", Color261C90)
            Spacer(modifier = Modifier.height(10.dp))
            text(batteryTech, Color261C90)
            Spacer(modifier = Modifier.height(10.dp))
            text(batteryHealth.name, Color261C90)
        }
    }
}

@Composable
fun BatteryTemperatureView(
    temperature: Float, modifier: Modifier = Modifier
) {
    Log.d(TAG, "BatteryTemperatureView: composed")
    Column(
        modifier = modifier
    ) {
        IconText(
            R.drawable.temperature,
            "$temperature °C",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun BatteryPercentView(
    currentCharge: Int, modifier: Modifier = Modifier
) {

    Log.d(TAG, "BatteryPercentView: composed")
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = "$currentCharge",
                fontSize = 60.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alignByBaseline()
            )
            Text(
                text = "%",
                fontSize = 30.sp,
                color = Color808080,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .alignByBaseline()
                    .padding(start = 4.dp)
            )
        }
        LinearProgressIndicator(
            progress = { currentCharge / 100f },
            modifier = Modifier.clip(RoundedCornerShape(10.dp)),
        )
    }

}

@Composable
fun ChargingStatsView(
    chargingState: ChargingState, voltCurrent: VoltCurrent, modifier: Modifier = Modifier
) {
    Log.d(TAG, "ChargingStatsView: composed")
    when (val state = chargingState) {
        is ChargingState.Charging -> {

            val (iconRes, source) = when (state.source) {
                ChargingSource.USB -> Pair(R.drawable.usb, "USB")
                ChargingSource.AC -> Pair(R.drawable.ac_adapter, "AC adapter")
                ChargingSource.Wireless -> Pair(R.drawable.wireless, "Wireless pad")
            }

            val chargingText = buildAnnotatedString {
                withStyle(style = SpanStyle(Color808080)) {
                    append("Charging via ")
                }
                withStyle(style = SpanStyle(Color.Black)) {
                    append(source)
                }
            }

            Column(modifier = modifier.fillMaxWidth()) {
                IconText(
                    iconRes = iconRes,
                    text = chargingText,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                val remainingText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color808080)) {
                        append("Remaining Time: ")
                    }
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        append(state.getReadableTimeRemaining())
                    }
                }
                Text(
                    text = remainingText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(4.dp)
                )
                WattView(voltCurrent)
            }
        }

        ChargingState.NotCharging -> {
            Column(modifier = modifier.fillMaxWidth()) {
                IconText(
                    R.drawable.unplugged,
                    "Discharging",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        ChargingState.NA -> {}
    }
}

@Composable
fun WattView(voltCurrent: VoltCurrent) {
    val decimalFormat = remember {
        Log.d(TAG, "WattView: decimalFormat initialized")
        DecimalFormat("#.##")
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        Log.d(TAG, "WattView: composed")

        val (voltageView, currentView, wattView) = createRefs()

        IconText(
            R.drawable.voltage,
            "${decimalFormat.format(voltCurrent.voltage / 1000f)} V",
            modifier = Modifier.constrainAs(voltageView) {
                start.linkTo(parent.start)
                end.linkTo(currentView.start)
            })

        IconText(
            R.drawable.current,
            "${voltCurrent.current} mA",
            modifier = Modifier.constrainAs(currentView) {
                start.linkTo(voltageView.end)
                end.linkTo(wattView.start)
            })

        IconText(
            R.drawable.power,
            "${decimalFormat.format(voltCurrent.watt)} W",
            modifier = Modifier.constrainAs(wattView) {
                start.linkTo(currentView.end)
                end.linkTo(parent.end)
            })

        createHorizontalChain(voltageView, currentView, wattView, chainStyle = ChainStyle.Spread)
    }
}

@Composable
fun IconText(iconRes: Int, text: CharSequence, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = modifier
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color808080
        )

        if (text is AnnotatedString) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp)
            )
        } else if (text is String) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BatteryToolsTheme {
        MainScreen {}
    }
}