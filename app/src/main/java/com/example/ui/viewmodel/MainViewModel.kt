package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SshConfigRepository
import com.example.model.ConnectionMetrics
import com.example.model.ConnectionState
import com.example.model.DtpAccessRules
import com.example.model.DtpProfilePackage
import com.example.model.DtpValidationResult
import com.example.model.LogEntry
import com.example.model.LogLevel
import com.example.model.RestrictionType
import com.example.model.SshConfig
import com.example.model.TunnelType
import com.example.service.SshConnectionManager
import com.example.util.DeviceIdentifier
import com.example.util.DtpFileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    HOME("Inicio"),
    LOGS("Registro"),
    CONFIG("Ajustes"),
    GUIDE("Guía")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SshConfigRepository(application)
    private val connectionManager = SshConnectionManager()

    val currentConfig: StateFlow<SshConfig> = repository.currentConfig
    val savedProfiles: StateFlow<List<SshConfig>> = repository.savedProfiles
    val connectionState: StateFlow<ConnectionState> = connectionManager.connectionState
    val metrics: StateFlow<ConnectionMetrics> = connectionManager.metrics
    val logs: StateFlow<List<LogEntry>> = connectionManager.logs

    private val _selectedTab = MutableStateFlow(AppTab.HOME)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Active loaded DTP rules (if profile was loaded from restricted .dtp)
    private val _activeDtpRules = MutableStateFlow<DtpAccessRules?>(null)
    val activeDtpRules: StateFlow<DtpAccessRules?> = _activeDtpRules.asStateFlow()

    val deviceId: String = DeviceIdentifier.getDeviceId(application)

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleConnection() {
        val state = connectionState.value
        if (state.isConnected || state.isConnecting) {
            connectionManager.disconnect()
        } else {
            val config = currentConfig.value
            if (config.host.isBlank()) {
                Toast.makeText(getApplication(), "Por favor, ingresa el Host SSH antes de conectar", Toast.LENGTH_SHORT).show()
                _selectedTab.value = AppTab.CONFIG
                return
            }

            // Perform License and Access Restrictions Validation
            val rules = _activeDtpRules.value
            if (rules != null && rules.restrictionType != RestrictionType.FREE) {
                viewModelScope.launch {
                    connectionManager.addLog(LogLevel.AUTH, "Verificando licencia y restricciones del perfil .dtp...")
                    val validation = DtpFileManager.validateAccess(getApplication(), rules)
                    if (!validation.isValid) {
                        connectionManager.addLog(LogLevel.ERROR, "✗ ACCESO DENEGADO: ${validation.title}")
                        connectionManager.addLog(LogLevel.ERROR, validation.message)
                        Toast.makeText(getApplication(), "Acceso denegado: ${validation.title}", Toast.LENGTH_LONG).show()
                        return@launch
                    } else {
                        connectionManager.addLog(LogLevel.SUCCESS, "✓ Licencia autorizada: ${validation.message}")
                        connectionManager.connect(config)
                    }
                }
            } else {
                connectionManager.connect(config)
            }
        }
    }

    fun updateConfig(config: SshConfig) {
        repository.updateConfig(config)
    }

    fun updateField(
        host: String? = null,
        port: String? = null,
        user: String? = null,
        pass: String? = null,
        payload: String? = null,
        sni: String? = null,
        proxyHost: String? = null,
        proxyPort: String? = null,
        tunnelType: TunnelType? = null,
        enableUdp: Boolean? = null,
        enableCompression: Boolean? = null,
        dns: String? = null,
        autoReconnect: Boolean? = null,
        wakeLock: Boolean? = null
    ) {
        val current = currentConfig.value
        val updated = current.copy(
            host = host ?: current.host,
            port = port ?: current.port,
            username = user ?: current.username,
            password = pass ?: current.password,
            payload = payload ?: current.payload,
            sniBugHost = sni ?: current.sniBugHost,
            proxyHost = proxyHost ?: current.proxyHost,
            proxyPort = proxyPort ?: current.proxyPort,
            tunnelType = tunnelType ?: current.tunnelType,
            enableUdpForwarding = enableUdp ?: current.enableUdpForwarding,
            enableCompression = enableCompression ?: current.enableCompression,
            customDns = dns ?: current.customDns,
            autoReconnect = autoReconnect ?: current.autoReconnect,
            wakeLock = wakeLock ?: current.wakeLock
        )
        repository.updateConfig(updated)
    }

    fun saveCurrentAsProfile(name: String) {
        val current = currentConfig.value
        val newProfile = current.copy(
            id = System.currentTimeMillis().toString(),
            profileName = name.ifBlank { "Perfil ${System.currentTimeMillis() % 1000}" }
        )
        repository.saveProfile(newProfile)
        Toast.makeText(getApplication(), "Perfil guardado con éxito", Toast.LENGTH_SHORT).show()
    }

    fun loadProfile(profile: SshConfig) {
        _activeDtpRules.value = null
        repository.updateConfig(profile.copy(id = "default"))
        Toast.makeText(getApplication(), "Cargado: ${profile.profileName}", Toast.LENGTH_SHORT).show()
    }

    fun deleteProfile(profileId: String) {
        repository.deleteProfile(profileId)
        Toast.makeText(getApplication(), "Perfil eliminado", Toast.LENGTH_SHORT).show()
    }

    fun copyLogsToClipboard() {
        val text = connectionManager.getAllLogsText()
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("TIENDA_SSH_LOGS", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "✓ Registros copiados al portapapeles", Toast.LENGTH_SHORT).show()
    }

    fun shareLogs() {
        val text = connectionManager.getAllLogsText()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "--- TIENDA SSH LOGS ---\n$text")
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir registro TIENDA SSH").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(shareIntent)
    }

    fun clearLogs() {
        connectionManager.clearLogs()
        Toast.makeText(getApplication(), "Registros limpiados", Toast.LENGTH_SHORT).show()
    }

    fun insertPayloadTag(tag: String) {
        val current = currentConfig.value.payload
        val updated = current + tag
        updateField(payload = updated)
    }

    fun applyPayloadPreset(presetPayload: String, suggestedPort: String? = null) {
        val current = currentConfig.value
        val updated = current.copy(
            payload = presetPayload,
            port = suggestedPort ?: current.port
        )
        repository.updateConfig(updated)
        Toast.makeText(getApplication(), "Payload aplicado", Toast.LENGTH_SHORT).show()
    }

    fun exportConfigText(): String {
        return repository.exportConfigJson(currentConfig.value)
    }

    fun importConfigText(json: String): Boolean {
        // First try to import as .dtp package
        val dtpPackage = DtpFileManager.deserializeFromDtp(json)
        if (dtpPackage != null) {
            return loadDtpPackage(dtpPackage)
        }

        val config = repository.importConfigJson(json)
        return if (config != null) {
            _activeDtpRules.value = null
            repository.updateConfig(config)
            Toast.makeText(getApplication(), "Configuración importada exitosamente", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(getApplication(), "Error al importar: formato inválido", Toast.LENGTH_SHORT).show()
            false
        }
    }

    // ==========================================
    // DTP SERIALIZATION & EXPORT / IMPORT
    // ==========================================

    fun exportDtpPackage(pkg: DtpProfilePackage, shareImmediately: Boolean) {
        try {
            val file = DtpFileManager.exportDtpFile(getApplication(), pkg)
            if (shareImmediately) {
                DtpFileManager.shareDtpFile(getApplication(), file, pkg.profileName)
            } else {
                Toast.makeText(
                    getApplication(),
                    "✓ Archivo guardado: ${file.name}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                getApplication(),
                "Error al exportar .dtp: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun importDtpFromUri(uri: Uri): Boolean {
        val pkg = DtpFileManager.readDtpFromUri(getApplication(), uri)
        return if (pkg != null) {
            loadDtpPackage(pkg)
        } else {
            Toast.makeText(getApplication(), "Error al leer archivo .dtp", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun importDtpFromText(text: String): Boolean {
        val pkg = DtpFileManager.deserializeFromDtp(text.trim())
        return if (pkg != null) {
            loadDtpPackage(pkg)
        } else {
            Toast.makeText(getApplication(), "Formato de archivo .dtp o Base64 no reconocido", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun loadDtpPackage(pkg: DtpProfilePackage): Boolean {
        _activeDtpRules.value = pkg.accessRules
        repository.updateConfig(pkg.sshConfig.copy(id = "default", profileName = pkg.profileName))

        // Validate immediately to inform user of status
        viewModelScope.launch {
            val validation = DtpFileManager.validateAccess(getApplication(), pkg.accessRules)
            if (validation.isValid) {
                connectionManager.addLog(
                    LogLevel.SUCCESS,
                    "✓ Perfil '${pkg.profileName}' importado (.dtp)"
                )
                if (pkg.accessRules.creatorNote.isNotBlank()) {
                    connectionManager.addLog(LogLevel.INFO, "Nota: ${pkg.accessRules.creatorNote}")
                }
                Toast.makeText(
                    getApplication(),
                    "✓ Perfil '${pkg.profileName}' cargado con éxito",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                connectionManager.addLog(LogLevel.WARNING, "⚠️ Perfil .dtp cargado con advertencias: ${validation.title}")
                connectionManager.addLog(LogLevel.WARNING, validation.message)
                Toast.makeText(
                    getApplication(),
                    "Atención: ${validation.title}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        return true
    }

    fun copyDeviceIdToClipboard() {
        DeviceIdentifier.copyToClipboard(getApplication())
    }

    fun shareDeviceId() {
        DeviceIdentifier.shareDeviceId(getApplication())
    }
}
