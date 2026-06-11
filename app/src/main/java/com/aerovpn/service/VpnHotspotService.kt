package com.aerovpn.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.net.ServerSocket
import java.net.Socket
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.thread

class VpnHotspotService : Service() {

    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "START") {
            startProxy()
        } else if (action == "STOP") {
            stopProxy()
        }
        return START_STICKY
    }

    private fun startProxy() {
        if (isRunning) return
        isRunning = true
        thread {
            try {
                serverSocket = ServerSocket(8080)
                Log.d("VpnHotspotService", "Proxy started on port 8080")
                while (isRunning) {
                    val client = serverSocket?.accept() ?: break
                    thread { handleClient(client) }
                }
            } catch (e: Exception) {
                Log.e("VpnHotspotService", "Error in proxy", e)
            }
        }
    }

    private fun handleClient(client: Socket) {
        try {
            val input = client.getInputStream()
            val output = client.getOutputStream()
            
            // Basic HTTP Proxy logic
            // In a real VPN hotspot, we'd use iptables or a proper SOCKS/HTTP proxy library
            // For now, this is a placeholder for the "Single Toggle" functionality
            
            client.close()
        } catch (e: Exception) {
            Log.e("VpnHotspotService", "Error handling client", e)
        }
    }

    private fun stopProxy() {
        isRunning = false
        serverSocket?.close()
        serverSocket = null
        stopSelf()
    }

    override fun onDestroy() {
        stopProxy()
        super.onDestroy()
    }
}
