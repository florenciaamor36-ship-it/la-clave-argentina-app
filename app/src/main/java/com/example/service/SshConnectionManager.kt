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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import java.util.concurrent.TimeUnit

/** Real SSH session manager. It deliberately never reports CONNECTED before auth succeeds. */
class SshConnectionManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var connectionJob: Job? = null
    private var client: SshClient? = null
    private var session: ClientSession? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    private val _metrics = MutableStateFlow(ConnectionMetrics(assignedIp = "0.0.0.0"))
    val metrics: StateFlow<ConnectionMetrics> = _metrics.asStateFlow()
    private val _logs = MutableStateFlow(listOf(LogEntry(level = LogLevel.INFO, message = "SSH real listo; esperando configuración")))
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()
    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    fun connect(config: SshConfig) {
        if (_connectionState.value.isConnecting || _connectionState.value.isConnected) return
        disconnectInternal()
        connectionJob = scope.launch {
            try {
                val host = config.host.trim()
                val port = config.port.toIntOrNull() ?: error("Puerto SSH inválido")
                require(host.isNotEmpty()) { "Host SSH vacío" }
                require(config.username.isNotBlank()) { "Usuario SSH vacío" }
                require(config.password.isNotEmpty()) { "Contraseña SSH vacía" }
                if (config.tunnelType != TunnelType.SSH_DIRECT) {
                    error("El transporte Payload/proxy todavía no está implementado en esta versión")
                }

                _connectionState.value = ConnectionState.CONNECTING
                addLog(LogLevel.INFO, "Conectando por SSH a $host:$port")
                val ssh = SshClient.setUpDefaultClient()
                ssh.start()
                client = ssh
                _connectionState.value = ConnectionState.HANDSHAKE
                addLog(LogLevel.SSH, "Abriendo transporte SSH real")
                val connected = ssh.connect(config.username.trim(), host, port)
                    .verify(30, TimeUnit.SECONDS)
                session = connected.session
                _connectionState.value = ConnectionState.AUTHENTICATING
                addLog(LogLevel.AUTH, "Autenticando usuario SSH '${config.username.trim()}'")
                connected.session.addPasswordIdentity(config.password)
                connected.session.auth().verify(30, TimeUnit.SECONDS)
                _connectionState.value = ConnectionState.CONNECTED
                _metrics.value = ConnectionMetrics(assignedIp = "0.0.0.0")
                addLog(LogLevel.SUCCESS, "SSH autenticado correctamente")
                addLog(LogLevel.INFO, "Sesión SSH activa; túnel VPN todavía no iniciado")
            } catch (t: Throwable) {
                _connectionState.value = ConnectionState.ERROR
                addLog(LogLevel.ERROR, "SSH falló: ${t.message ?: t.javaClass.simpleName}")
                disconnectInternal()
            }
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        scope.launch {
            _connectionState.value = ConnectionState.DISCONNECTING
            disconnectInternal()
            _connectionState.value = ConnectionState.DISCONNECTED
            _metrics.value = ConnectionMetrics(assignedIp = "0.0.0.0")
            addLog(LogLevel.INFO, "Sesión SSH cerrada")
        }
    }

    private fun disconnectInternal() {
        try { session?.close(false) } catch (_: Throwable) { }
        try { client?.stop() } catch (_: Throwable) { }
        session = null
        client = null
    }

    fun addLog(level: LogLevel, message: String) {
        _logs.value = _logs.value + LogEntry(level, message)
    }

    fun clearLogs() {
        _logs.value = listOf(LogEntry(level = LogLevel.INFO, message = "Registros reiniciados"))
    }

    fun getAllLogsText(): String = _logs.value.joinToString("\n") { it.toFormattedString() }
}
