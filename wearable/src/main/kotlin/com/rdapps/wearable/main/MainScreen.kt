package com.rdapps.wearable.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.rdapps.common.model.BatteryStats
import com.rdapps.common.model.ChargingSource
import com.rdapps.common.model.ChargingState
import com.rdapps.common.utils.getReadableTime
import com.rdapps.wearable.R
import kotlinx.coroutines.flow.StateFlow
import java.text.DecimalFormat
import com.rdapps.common.R as CommonR

@Composable
fun MainScreen(
    viewStateFlow: StateFlow<MainViewModel.ViewState>
) {
    val viewState by viewStateFlow.collectAsStateWithLifecycle()

    Scaffold(
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!viewState.isDeviceConnected) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.ic_round_error_24),
                        contentDescription = "Error",
                        modifier = Modifier
                            .size(36.dp)
                    )
                    Text(
                        text = "No device connected",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(top = 4.dp)
                    )
                }
            } else if (!viewState.isAppConnected) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.ic_round_error_24),
                        contentDescription = "Error",
                        modifier = Modifier
                            .size(36.dp)
                    )
                    Text(
                        text = "Connected device doesn't have App installed",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .padding(top = 4.dp)
                    )
                }
            } else {
                BatteryDetails(viewState.batteryStats)
            }
        }
    }
}

@Composable
fun BatteryDetails(batteryStats: BatteryStats) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp)) // Use clip with shape for rounded corners
            .background(Color.Black)
            .padding(10.dp)
    ) {
        // Use standard Compose Column
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally, // Use standard Alignment
            verticalArrangement = Arrangement.Center // Use standard Alignment
        ) {
            val state = batteryStats.chargingState
            if (state is ChargingState.Charging) {
                val decimalFormat = remember {
                    DecimalFormat("#.##")
                }

                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "${batteryStats.currentCharge}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alignByBaseline()
                    )

                    Text(
                        text = "%",
                        color = Color808080,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .alignByBaseline()
                            .padding(start = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconText(
                        iconRes = CommonR.drawable.temperature,
                        text = "${batteryStats.temperature} °C",
                        modifier = Modifier.weight(1f)
                    )
                    IconText(
                        iconRes = CommonR.drawable.current,
                        text = "${batteryStats.voltCurrent.current} mA",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconText(
                        iconRes = CommonR.drawable.voltage,
                        text = "${decimalFormat.format(batteryStats.voltCurrent.voltage / 1000f)} V",
                        modifier = Modifier.weight(1f)
                    )
                    IconText(
                        iconRes = CommonR.drawable.power,
                        text = "${decimalFormat.format(batteryStats.voltCurrent.watt)} W",
                        modifier = Modifier.weight(1f)
                    )
                }

                val (iconRes, source) = when (state.source) {
                    ChargingSource.USB -> Pair(CommonR.drawable.usb, "USB")
                    ChargingSource.AC -> Pair(CommonR.drawable.ac_adapter, "AC adapter")
                    ChargingSource.Wireless -> Pair(CommonR.drawable.wireless, "Wireless pad")
                }

                IconText(
                    iconRes = iconRes,
                    text = source,
                    modifier = Modifier.padding(top = 10.dp)
                )

                if (state.timeRemaining > 0) {
                    IconText(
                        iconRes = CommonR.drawable.ic_round_time_24,
                        text = getReadableTime(state.timeRemaining),
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            } else {
                BatteryPercentView(batteryStats.currentCharge, 60.sp)

                IconText(
                    iconRes = CommonR.drawable.temperature,
                    text = "${batteryStats.temperature} °C"
                )
            }
        }
    }
}

val Color808080 = Color(0xFF808080)

@Composable
fun BatteryPercentView(currentCharge: Int, fontSize: TextUnit) {
    // Use standard Compose Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center, // Use Arrangement for positioning children
        verticalAlignment = Alignment.Bottom // Use standard Alignment
    ) {
        // Use standard Material 3 Text
        Text(
            text = "$currentCharge",
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alignByBaseline()
        )

        Text(
            text = "%",
            color = Color808080, // Use the defined Color constant
            fontSize = fontSize / 2, // Direct division works for TextUnit
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .alignByBaseline()
                .padding(start = 4.dp)
        )
    }
}

@Composable
fun IconText(
    iconRes: Int,
    text: String,
    modifier: Modifier = Modifier // Use standard Modifier
) {
    // Use standard Compose Row
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center, // Use Arrangement for positioning children
        modifier = modifier
    ) {
        // Use standard Compose Image with painterResource
        Image(
            painter = painterResource(id = iconRes),
            modifier = Modifier.size(24.dp),
            contentDescription = text // Provide meaningful content description
        )

        // Use standard Material 3 Text
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Preview
@Composable
private fun MainPreview() {
//    MainScreen()
}