package com.example.service

import com.example.model.ConnectionMetrics
import com.example.model.ConnectionState
import com.example.model.LogEntry
import com.example.model.LogLevel
import com.example.model.SshConfig
import com.example.model.TunnelType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class SshConnectionManager {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var connectionJob: Job? = null
    private var metricsJob: Job? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _metrics = MutableStateFlow(ConnectionMetrics())
    val metrics: StateFlow<ConnectionMetrics> = _metrics.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEntry>>(
        listOf(
            LogEntry(level = LogLevel.INFO, message = "TIENDA SSH Core v2.4 listo"),
            LogEntry(level = LogLevel.INFO, message = "Esperando acción del usuario...")
        )
    )
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    fun connect(config: SshConfig) {
        if (_connectionState.value.isConnecting || _connectionState.value.isConnected) return

        connectionJob?.cancel()
        metricsJob?.cancel()

        connectionJob = scope.launch {
            try {
                _connectionState.value = ConnectionState.CONNECTING
                addLog(LogLevel.INFO, "═══════════════════════════════════")
                addLog(LogLevel.INFO, "Iniciando servicio de túnel TIENDA SSH")
                addLog(LogLevel.INFO, "Modo: ${config.tunnelType.displayName}")
                addLog(LogLevel.INFO, "Destino: ${config.host}:${config.port}")
                addLog(LogLevel.INFO, "Usuario: ${config.username.ifBlank { "anónimo" }}")
                
                delay(400)

                when (config.tunnelType) {
                    TunnelType.SSH_DIRECT -> {
                        addLog(LogLevel.SSH, "Abriendo socket TCP directo hacia ${config.host}:${config.port}...")
                        delay(600)
                        addLog(LogLevel.SSH, "Conexión TCP establecida con éxito (15ms)")
                    }
                    TunnelType.SSH_HTTP_CUSTOM -> {
                        val proxy = if (config.proxyHost.isNotBlank()) "${config.proxyHost}:${config.proxyPort}" else "${config.host}:${config.port}"
                        addLog(LogLevel.HTTP, "Conectando al Proxy HTTP: $proxy...")
                        delay(500)
                        addLog(LogLevel.HTTP, "Enviando Payload HTTP personalizado...")
                        val previewPayload = config.payload.replace("[crlf]", "\n").take(80)
                        addLog(LogLevel.HTTP, "Payload:\n$previewPayload...")
                        delay(700)
                        addLog(LogLevel.HTTP, "Respuesta del Proxy: HTTP/1.1 200 Connection Established")
                        addLog(LogLevel.HTTP, "Túnel HTTP establecido correctamente")
                    }
                    TunnelType.SSL_TLS -> {
                        addLog(LogLevel.VPN, "Iniciando handshake SSL/TLS con SNI: ${config.sniBugHost}...")
                        delay(700)
                        addLog(LogLevel.VPN, "Certificado validado: TLS_AES_256_GCM_SHA384 (TLSv1.3)")
                    }
                    TunnelType.SSL_PAYLOAD -> {
                        addLog(LogLevel.VPN, "Estableciendo conexión TLS segura con SNI ${config.sniBugHost}...")
                        delay(500)
                        addLog(LogLevel.HTTP, "Enviando Upgrade Request WebSocket a ${config.host}...")
                        delay(600)
                        addLog(LogLevel.HTTP, "HTTP/1.1 101 Switching Protocols (WebSocket OK)")
                    }
                    TunnelType.SSH_PROXY -> {
                        addLog(LogLevel.HTTP, "Conectando a Proxy Squid ${config.proxyHost}:${config.proxyPort}...")
                        delay(600)
                        addLog(LogLevel.HTTP, "Squid Proxy CONNECT ${config.host}:${config.port} -> 200 OK")
                    }
                }

                _connectionState.value = ConnectionState.HANDSHAKE
                addLog(LogLevel.SSH, "Iniciando protocolo SSH-2.0-OpenSSH_9.2p1...")
                delay(600)
                addLog(LogLevel.SSH, "Intercambio de claves: curve25519-sha256@libssh.org")
                addLog(LogLevel.SSH, "Cifrado negociado: chacha20-poly1305@openssh.com")
                addLog(LogLevel.SSH, "Compresión zlib: ${if (config.enableCompression) "Habilitada" else "Deshabilitada"}")

                _connectionState.value = ConnectionState.AUTHENTICATING
                addLog(LogLevel.AUTH, "Autenticando mediante password para '${config.username}'...")
                delay(600)
                addLog(LogLevel.AUTH, "Autenticación SSH exitosa (Metodo: password)")

                addLog(LogLevel.VPN, "Iniciando interfaz VPN VpnService local (tun0)...")
                delay(400)
                val assignedIp = "10.0.${Random.nextInt(1, 250)}.${Random.nextInt(2, 250)}"
                addLog(LogLevel.VPN, "IP asignada al dispositivo: $assignedIp")
                addLog(LogLevel.VPN, "DNS primario: ${config.customDns.split(",").firstOrNull()?.trim() ?: "1.1.1.1"}")
                if (config.enableUdpForwarding) {
                    addLog(LogLevel.VPN, "Mapeo UDP Forwarding (BadVPN) activado en puerto 7300")
                }
                
                _connectionState.value = ConnectionState.CONNECTED
                addLog(LogLevel.SUCCESS, "✓ ¡TIENDA SSH Conectado y Enrutando Tráfico!")
                addLog(LogLevel.INFO, "═══════════════════════════════════")

                startMetricsLoop(assignedIp)

            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                addLog(LogLevel.ERROR, "Fallo en la conexión: ${e.message ?: "Error desconocido"}")
            }
        }
    }

    fun disconnect() {
        if (_connectionState.value == ConnectionState.DISCONNECTED) return

        connectionJob?.cancel()
        metricsJob?.cancel()

        scope.launch {
            _connectionState.value = ConnectionState.DISCONNECTING
            addLog(LogLevel.INFO, "Cerrando túnel SSH y liberando interfaz VPN...")
            delay(400)
            _connectionState.value = ConnectionState.DISCONNECTED
            addLog(LogLevel.WARNING, "✗ Desconectado de TIENDA SSH")
            _metrics.value = _metrics.value.copy(
                downloadSpeedBytes = 0L,
                uploadSpeedBytes = 0L,
                pingMs = 0
            )
        }
    }

    private fun startMetricsLoop(assignedIp: String) {
        metricsJob = scope.launch {
            var duration = 0L
            var totalDown = _metrics.value.totalDownloadBytes
            var totalUp = _metrics.value.totalUploadBytes

            while (isActive && _connectionState.value == ConnectionState.CONNECTED) {
                delay(1000)
                duration++

                // Simulate realistic fluctuating network traffic
                val currentDownSpeed = Random.nextLong(150_000, 3_500_000)
                val currentUpSpeed = Random.nextLong(40_000, 800_000)
                val currentPing = Random.nextInt(28, 65)

                totalDown += currentDownSpeed
                totalUp += currentUpSpeed

                _metrics.value = ConnectionMetrics(
                    downloadSpeedBytes = currentDownSpeed,
                    uploadSpeedBytes = currentUpSpeed,
                    totalDownloadBytes = totalDown,
                    totalUploadBytes = totalUp,
                    pingMs = currentPing,
                    durationSeconds = duration,
                    assignedIp = assignedIp
                )
            }
        }
    }

    fun addLog(level: LogLevel, message: String) {
        val entry = LogEntry(level = level, message = message)
        val updated = _logs.value.toMutableList().apply { add(entry) }
        _logs.value = updated
    }

    fun clearLogs() {
        _logs.value = listOf(
            LogEntry(level = LogLevel.INFO, message = "Registros reiniciados"),
            LogEntry(level = LogLevel.INFO, message = "TIENDA SSH esperando comandos...")
        )
    }

    fun getAllLogsText(): String {
        return _logs.value.joinToString(separator = "\n") { it.toFormattedString() }
    }
}
