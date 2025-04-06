package com.rdapps.wearable.main

import android.app.Application
import android.util.Log
import androidx.datastore.dataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.rdapps.common.R
import com.rdapps.common.model.BatteryStats
import com.rdapps.wearable.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app),
    CapabilityClient.OnCapabilityChangedListener {

    private val context
        get() = getApplication<Application>().applicationContext

    private val nodeClient by lazy {
        Wearable.getNodeClient(context)
    }

    private val capabilityClient by lazy {
        Wearable.getCapabilityClient(context)
    }

    data class ViewState(
        val isDeviceConnected: Boolean = false,
        val connectedNode: Node? = null,
        val batteryStats: BatteryStats = BatteryStats()
    ) {
        val isAppConnected: Boolean
            get() = connectedNode != null
    }

    val viewStateFlow: StateFlow<ViewState>
        field = MutableStateFlow(ViewState())

    init {
        val mobileAppCapability = context.getString(R.string.mobile_capability)

        nodeClient.connectedNodes.addOnSuccessListener { nodeList ->
            viewStateFlow.update {
                it.copy(
                    isDeviceConnected = !nodeList.isNullOrEmpty()
                )
            }
        }

        capabilityClient.getCapability(
            mobileAppCapability,
            CapabilityClient.FILTER_REACHABLE
        ).addOnSuccessListener {
            Log.d(TAG, "capabilityClient.getCapability: ${it?.nodes?.size}")
            val node = it?.nodes?.firstOrNull() ?: return@addOnSuccessListener

            viewStateFlow.update {
                it.copy(
                    connectedNode = node
                )
            }
        }
        capabilityClient.addListener(this, mobileAppCapability)

        viewModelScope.launch {
            context.dataStore.data.collect { stats ->
                viewStateFlow.update {
                    it.copy(
                        batteryStats = stats
                    )
                }
            }
        }

        addCloseable {
            capabilityClient.removeListener(this)
        }
    }

    override fun onCapabilityChanged(info: CapabilityInfo) {
        val node = info.nodes.firstOrNull() ?: run {
            viewStateFlow.update {
                it.copy(
                    isDeviceConnected = false,
                    connectedNode = null
                )
            }
            return
        }

        viewStateFlow.update {
            it.copy(
                isDeviceConnected = true,
                connectedNode = node
            )
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}