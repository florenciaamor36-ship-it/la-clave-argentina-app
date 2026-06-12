package com.aerovpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log

/**
 * NetworkStateReceiver — proper dual-mode network monitor.
 */
class NetworkStateReceiver : BroadcastReceiver() {

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isRegistered = false

    companion object {
        private const val TAG = "NetworkStateReceiver"
        
        const val ACTION_NETWORK_CHANGED = "com.aerovpn.ACTION_NETWORK_CHANGED"
        const val ACTION_SCREEN_STATE_CHANGED = "com.aerovpn.ACTION_SCREEN_STATE_CHANGED"
        const val ACTION_POWER_STATE_CHANGED = "com.aerovpn.ACTION_POWER_STATE_CHANGED"

        fun buildIntentFilter(): IntentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
    }

    fun register(context: Context) {
        if (isRegistered) return
        
        context.registerReceiver(this, buildIntentFilter())
        
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                handleNetworkChange(context, true, getNetworkType(network))
            }

            override fun onLost(network: Network) {
                handleNetworkChange(context, false, "None")
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                handleNetworkChange(context, true, getNetworkType(network))
            }
        }
        
        connectivityManager?.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            networkCallback!!
        )
        
        isRegistered = true
    }

    fun unregister(context: Context) {
        if (!isRegistered) return
        
        try {
            context.unregisterReceiver(this)
        } catch (e: Exception) {
            Log.e(TAG, "Unregister receiver failed", e)
        }
        
        networkCallback?.let {
            connectivityManager?.unregisterNetworkCallback(it)
        }
        
        isRegistered = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "onReceive: $action")
        
        val eventAction = when (action) {
            Intent.ACTION_SCREEN_ON, Intent.ACTION_SCREEN_OFF -> ACTION_SCREEN_STATE_CHANGED
            Intent.ACTION_POWER_CONNECTED, Intent.ACTION_POWER_DISCONNECTED -> ACTION_POWER_STATE_CHANGED
            else -> return
        }
        
        notifyVpnService(context, eventAction, mapOf("is_on" to (action == Intent.ACTION_SCREEN_ON || action == Intent.ACTION_POWER_CONNECTED)))
    }

    private fun getNetworkType(network: Network): String {
        val caps = connectivityManager?.getNetworkCapabilities(network) ?: return "Unknown"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other"
        }
    }

    private fun handleNetworkChange(context: Context, isConnected: Boolean, networkType: String) {
        Log.d(TAG, "Network changed: isConnected=$isConnected, type=$networkType")
        notifyVpnService(
            context,
            ACTION_NETWORK_CHANGED,
            extras = mapOf(
                "is_connected" to isConnected,
                "network_type" to networkType
            )
        )
    }

    private fun notifyVpnService(
        context: Context,
        action: String,
        extras: Map<String, Any> = emptyMap()
    ) {
        try {
            val vpnIntent = Intent(context, Class.forName("com.aerovpn.service.AeroVpnService")).apply {
                this.action = action
                extras.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> putExtra(key, value)
                        is String -> putExtra(key, value)
                        is Int -> putExtra(key, value)
                        else -> putExtra(key, value.toString())
                    }
                }
            }
            context.startService(vpnIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to notify VPN service (action=$action)", e)
        }
    }
}
