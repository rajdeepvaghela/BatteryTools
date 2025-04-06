package com.rdapps.batterytools.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.rdapps.batterytools.alert.AlertService
import com.rdapps.batterytools.settings.SettingsActivity
import com.rdapps.batterytools.ui.theme.BatteryToolsTheme
import com.rdapps.batterytools.util.getBatteryCapacity
import com.rdapps.batterytools.util.isServiceRunning
import com.rdapps.batterytools.util.parseBatteryStats
import com.rdapps.batterytools.widget.BatteryStateMonitorService
import com.rdapps.batterytools.widget.BatteryWidget
import com.rdapps.batterytools.widget.dataStore
import com.rdapps.batterytools.widget.updateWidgetUI
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val batteryManager by lazy {
        getSystemService(BatteryManager::class.java)
    }

    private val batteryChangeReceiver by lazy {
        BatteryChangeReceiver()
    }

    private val alertService by lazy {
        Intent(this, AlertService::class.java)
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.White.toArgb(),
                Color.Black.toArgb()
            )
        )

        setContent {
            BatteryToolsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onViewEvents = ::onViewEvents
                    )
                }
            }
        }

        IntentFilter(Intent.ACTION_BATTERY_CHANGED).apply {
            ContextCompat.registerReceiver(
                this@MainActivity, batteryChangeReceiver, this,
                ContextCompat.RECEIVER_EXPORTED
            )
        }

        viewModel.updateBatteryCapacity(getBatteryCapacity())

        lifecycleScope.launch {
            updateWidgetUI()
            val manager = GlanceAppWidgetManager(this@MainActivity)
            val glanceIds = manager.getGlanceIds(BatteryWidget::class.java)
            if (glanceIds.isNotEmpty() && !isServiceRunning(BatteryStateMonitorService::class))
                startForegroundService(
                    Intent(
                        this@MainActivity,
                        BatteryStateMonitorService::class.java
                    )
                )
        }
    }

    private fun onViewEvents(event: MainScreenViewEvent) {
        when (event) {
            MainScreenViewEvent.OnStartAlertClicked -> {
                startForegroundService(alertService)
            }

            MainScreenViewEvent.OnStopAlertClicked -> {
                stopService(alertService)
            }

            MainScreenViewEvent.OnSettingsClicked -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }

    inner class BatteryChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            lifecycleScope.launch {
                dataStore.updateData {
                    batteryManager.parseBatteryStats(this@MainActivity, intent)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryChangeReceiver)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}