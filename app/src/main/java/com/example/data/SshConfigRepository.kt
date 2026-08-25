package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.SshConfig
import com.example.model.TunnelType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class SshConfigRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("tienda_ssh_prefs", Context.MODE_PRIVATE)

    private val _currentConfig = MutableStateFlow(loadConfig())
    val currentConfig: StateFlow<SshConfig> = _currentConfig.asStateFlow()

    private val _savedProfiles = MutableStateFlow<List<SshConfig>>(loadProfiles())
    val savedProfiles: StateFlow<List<SshConfig>> = _savedProfiles.asStateFlow()

    fun updateConfig(newConfig: SshConfig) {
        _currentConfig.value = newConfig
        saveConfig(newConfig)
    }

    fun saveProfile(profile: SshConfig) {
        val currentList = _savedProfiles.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == profile.id }
        if (index != -1) {
            currentList[index] = profile
        } else {
            currentList.add(profile)
        }
        _savedProfiles.value = currentList
        saveProfilesToPrefs(currentList)
    }

    fun deleteProfile(profileId: String) {
        val currentList = _savedProfiles.value.filterNot { it.id == profileId }
        _savedProfiles.value = currentList
        saveProfilesToPrefs(currentList)
    }

    private fun saveConfig(config: SshConfig) {
        prefs.edit().apply {
            putString(KEY_HOST, config.host)
            putString(KEY_PORT, config.port)
            putString(KEY_USER, config.username)
            putString(KEY_PASS, config.password)
            putString(KEY_TUNNEL_TYPE, config.tunnelType.name)
            putString(KEY_PAYLOAD, config.payload)
            putString(KEY_SNI, config.sniBugHost)
            putString(KEY_PROXY_HOST, config.proxyHost)
            putString(KEY_PROXY_PORT, config.proxyPort)
            putBoolean(KEY_UDP, config.enableUdpForwarding)
            putBoolean(KEY_COMPRESSION, config.enableCompression)
            putString(KEY_DNS, config.customDns)
            putBoolean(KEY_AUTO_RECONNECT, config.autoReconnect)
            putBoolean(KEY_WAKELOCK, config.wakeLock)
            apply()
        }
    }

    private fun loadConfig(): SshConfig {
        val tunnelTypeName = prefs.getString(KEY_TUNNEL_TYPE, TunnelType.SSH_HTTP_CUSTOM.name) ?: TunnelType.SSH_HTTP_CUSTOM.name
        val tunnelType = try {
            TunnelType.valueOf(tunnelTypeName)
        } catch (e: Exception) {
            TunnelType.SSH_HTTP_CUSTOM
        }

        return SshConfig(
            id = "default",
            profileName = "Perfil Principal",
            host = prefs.getString(KEY_HOST, "198.51.100.24") ?: "198.51.100.24",
            port = prefs.getString(KEY_PORT, "22") ?: "22",
            username = prefs.getString(KEY_USER, "tienda_ssh") ?: "tienda_ssh",
            password = prefs.getString(KEY_PASS, "vpn2026") ?: "vpn2026",
            tunnelType = tunnelType,
            payload = prefs.getString(KEY_PAYLOAD, "CONNECT [host_port] [protocol][crlf]Host: [host][crlf]X-Online-Host: [host][crlf]Connection: Keep-Alive[crlf][crlf]")
                ?: "CONNECT [host_port] [protocol][crlf]Host: [host][crlf]X-Online-Host: [host][crlf]Connection: Keep-Alive[crlf][crlf]",
            sniBugHost = prefs.getString(KEY_SNI, "ssl.tiendassh.net") ?: "ssl.tiendassh.net",
            proxyHost = prefs.getString(KEY_PROXY_HOST, "198.51.100.24") ?: "198.51.100.24",
            proxyPort = prefs.getString(KEY_PROXY_PORT, "8080") ?: "8080",
            enableUdpForwarding = prefs.getBoolean(KEY_UDP, true),
            enableCompression = prefs.getBoolean(KEY_COMPRESSION, true),
            customDns = prefs.getString(KEY_DNS, "1.1.1.1, 1.0.0.1") ?: "1.1.1.1, 1.0.0.1",
            autoReconnect = prefs.getBoolean(KEY_AUTO_RECONNECT, true),
            wakeLock = prefs.getBoolean(KEY_WAKELOCK, true)
        )
    }

    private fun loadProfiles(): List<SshConfig> {
        val jsonString = prefs.getString(KEY_PROFILES, null) ?: return getDefaultProfiles()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<SshConfig>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(fromJsonObject(obj))
            }
            if (list.isEmpty()) getDefaultProfiles() else list
        } catch (e: Exception) {
            getDefaultProfiles()
        }
    }

    private fun saveProfilesToPrefs(list: List<SshConfig>) {
        val jsonArray = JSONArray()
        list.forEach { config ->
            jsonArray.put(toJsonObject(config))
        }
        prefs.edit().putString(KEY_PROFILES, jsonArray.toString()).apply()
    }

    private fun getDefaultProfiles(): List<SshConfig> {
        return listOf(
            SshConfig(
                id = "p1",
                profileName = "TIENDA Server Premium 1 (Payload HTTP)",
                host = "us1.tiendassh.net",
                port = "80",
                username = "tienda_user",
                password = "user2026",
                tunnelType = TunnelType.SSH_HTTP_CUSTOM,
                payload = "CONNECT [host_port] [protocol][crlf]Host: [host][crlf]X-Online-Host: [host][crlf]Connection: Keep-Alive[crlf][crlf]",
                proxyHost = "198.51.100.24",
                proxyPort = "8080"
            ),
            SshConfig(
                id = "p2",
                profileName = "TIENDA SSL Cloudflare (WS/CDN)",
                host = "cf-latam.tiendassh.net",
                port = "443",
                username = "tienda_vip",
                password = "vip2026",
                tunnelType = TunnelType.SSL_PAYLOAD,
                sniBugHost = "m.facebook.com",
                payload = "GET / HTTP/1.1[crlf]Host: cf-latam.tiendassh.net[crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]"
            ),
            SshConfig(
                id = "p3",
                profileName = "TIENDA Direct SSH Gaming (Baja Latencia)",
                host = "br-speed.tiendassh.net",
                port = "22",
                username = "gamer_ssh",
                password = "fast2026",
                tunnelType = TunnelType.SSH_DIRECT,
                enableUdpForwarding = true
            )
        )
    }

    fun exportConfigJson(config: SshConfig): String {
        return toJsonObject(config).toString(2)
    }

    fun importConfigJson(jsonString: String): SshConfig? {
        return try {
            val obj = JSONObject(jsonString)
            fromJsonObject(obj)
        } catch (e: Exception) {
            null
        }
    }

    private fun toJsonObject(config: SshConfig): JSONObject {
        return JSONObject().apply {
            put("id", config.id)
            put("profileName", config.profileName)
            put("host", config.host)
            put("port", config.port)
            put("username", config.username)
            put("password", config.password)
            put("tunnelType", config.tunnelType.name)
            put("payload", config.payload)
            put("sniBugHost", config.sniBugHost)
            put("proxyHost", config.proxyHost)
            put("proxyPort", config.proxyPort)
            put("enableUdpForwarding", config.enableUdpForwarding)
            put("enableCompression", config.enableCompression)
            put("customDns", config.customDns)
            put("autoReconnect", config.autoReconnect)
            put("wakeLock", config.wakeLock)
        }
    }

    private fun fromJsonObject(obj: JSONObject): SshConfig {
        val tunnelType = try {
            TunnelType.valueOf(obj.optString("tunnelType", TunnelType.SSH_HTTP_CUSTOM.name))
        } catch (e: Exception) {
            TunnelType.SSH_HTTP_CUSTOM
        }
        return SshConfig(
            id = obj.optString("id", System.currentTimeMillis().toString()),
            profileName = obj.optString("profileName", "Perfil Importado"),
            host = obj.optString("host", "198.51.100.24"),
            port = obj.optString("port", "22"),
            username = obj.optString("username", ""),
            password = obj.optString("password", ""),
            tunnelType = tunnelType,
            payload = obj.optString("payload", "CONNECT [host_port] [protocol][crlf]Host: [host][crlf]Connection: Keep-Alive[crlf][crlf]"),
            sniBugHost = obj.optString("sniBugHost", ""),
            proxyHost = obj.optString("proxyHost", ""),
            proxyPort = obj.optString("proxyPort", "8080"),
            enableUdpForwarding = obj.optBoolean("enableUdpForwarding", true),
            enableCompression = obj.optBoolean("enableCompression", true),
            customDns = obj.optString("customDns", "1.1.1.1, 1.0.0.1"),
            autoReconnect = obj.optBoolean("autoReconnect", true),
            wakeLock = obj.optBoolean("wakeLock", true)
        )
    }

    companion object {
        private const val KEY_HOST = "key_host"
        private const val KEY_PORT = "key_port"
        private const val KEY_USER = "key_user"
        private const val KEY_PASS = "key_pass"
        private const val KEY_TUNNEL_TYPE = "key_tunnel_type"
        private const val KEY_PAYLOAD = "key_payload"
        private const val KEY_SNI = "key_sni"
        private const val KEY_PROXY_HOST = "key_proxy_host"
        private const val KEY_PROXY_PORT = "key_proxy_port"
        private const val KEY_UDP = "key_udp"
        private const val KEY_COMPRESSION = "key_compression"
        private const val KEY_DNS = "key_dns"
        private const val KEY_AUTO_RECONNECT = "key_auto_reconnect"
        private const val KEY_WAKELOCK = "key_wakelock"
        private const val KEY_PROFILES = "key_profiles"
    }
}
