package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.model.DtpAccessRules
import com.example.model.DtpProfilePackage
import com.example.model.DtpValidationResult
import com.example.model.RestrictionType
import com.example.model.SshConfig
import com.example.model.TunnelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DtpFileManager {

    private const val FILE_EXTENSION = ".dtp"
    private const val HEADER_MAGIC = "TIENDA_SSH_DTP_V1::"

    /**
     * Serializes a DtpProfilePackage into a Base64-encoded string.
     */
    fun serializeToDtp(pkg: DtpProfilePackage): String = serializeToDtpBase64(pkg)

    /**
     * Serializes a DtpProfilePackage into a Base64-encoded string.
     */
    fun serializeToDtpBase64(pkg: DtpProfilePackage): String {
        val root = JSONObject()
        root.put("version", pkg.version)
        root.put("profileName", pkg.profileName)

        // SshConfig
        val cfgObj = JSONObject()
        val c = pkg.sshConfig
        cfgObj.put("id", c.id)
        cfgObj.put("profileName", c.profileName)
        cfgObj.put("host", c.host)
        cfgObj.put("port", c.port)
        cfgObj.put("username", c.username)
        cfgObj.put("password", c.password)
        cfgObj.put("tunnelType", c.tunnelType.name)
        cfgObj.put("payload", c.payload)
        cfgObj.put("sniBugHost", c.sniBugHost)
        cfgObj.put("proxyHost", c.proxyHost)
        cfgObj.put("proxyPort", c.proxyPort)
        cfgObj.put("enableUdpForwarding", c.enableUdpForwarding)
        cfgObj.put("enableCompression", c.enableCompression)
        cfgObj.put("customDns", c.customDns)
        cfgObj.put("autoReconnect", c.autoReconnect)
        cfgObj.put("wakeLock", c.wakeLock)
        root.put("sshConfig", cfgObj)

        // AccessRules
        val rulesObj = JSONObject()
        val r = pkg.accessRules
        rulesObj.put("restrictionType", r.restrictionType.name)
        r.expirationTimestamp?.let { rulesObj.put("expirationTimestamp", it) }
        r.allowedDeviceId?.let { rulesObj.put("allowedDeviceId", it) }
        
        val multiArray = JSONArray()
        r.allowedDeviceIds.forEach { multiArray.put(it) }
        rulesObj.put("allowedDeviceIds", multiArray)

        r.remoteValidationUrl?.let { rulesObj.put("remoteValidationUrl", it) }
        rulesObj.put("isConfigLocked", r.isConfigLocked)
        rulesObj.put("creatorNote", r.creatorNote)
        rulesObj.put("exportTimestamp", r.exportTimestamp)
        root.put("accessRules", rulesObj)

        val jsonString = root.toString(2)
        val encoded = Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return HEADER_MAGIC + encoded
    }

    /**
     * Deserializes a DtpProfilePackage from Base64 or plain JSON string.
     */
    fun deserializeFromDtp(content: String): DtpProfilePackage? {
        return try {
            var raw = content.trim()
            if (raw.startsWith(HEADER_MAGIC)) {
                raw = raw.removePrefix(HEADER_MAGIC)
            }

            val jsonString = try {
                val decodedBytes = Base64.decode(raw, Base64.DEFAULT)
                String(decodedBytes, Charsets.UTF_8)
            } catch (_: Exception) {
                raw // Fallback to plain JSON
            }

            val root = JSONObject(jsonString)
            val version = root.optInt("version", 1)
            val profileName = root.optString("profileName", "Perfil Importado")

            val cfgObj = root.getJSONObject("sshConfig")
            val sshConfig = SshConfig(
                id = cfgObj.optString("id", System.currentTimeMillis().toString()),
                profileName = cfgObj.optString("profileName", profileName),
                host = cfgObj.optString("host", ""),
                port = cfgObj.optString("port", "22"),
                username = cfgObj.optString("username", ""),
                password = cfgObj.optString("password", ""),
                tunnelType = try {
                    TunnelType.valueOf(cfgObj.optString("tunnelType", TunnelType.SSH_HTTP_CUSTOM.name))
                } catch (_: Exception) {
                    TunnelType.SSH_HTTP_CUSTOM
                },
                payload = cfgObj.optString("payload", ""),
                sniBugHost = cfgObj.optString("sniBugHost", ""),
                proxyHost = cfgObj.optString("proxyHost", ""),
                proxyPort = cfgObj.optString("proxyPort", "8080"),
                enableUdpForwarding = cfgObj.optBoolean("enableUdpForwarding", true),
                enableCompression = cfgObj.optBoolean("enableCompression", true),
                customDns = cfgObj.optString("customDns", "1.1.1.1, 1.0.0.1"),
                autoReconnect = cfgObj.optBoolean("autoReconnect", true),
                wakeLock = cfgObj.optBoolean("wakeLock", true)
            )

            val rulesObj = root.optJSONObject("accessRules")
            val accessRules = if (rulesObj != null) {
                val rawTypeStr = rulesObj.optString("restrictionType", RestrictionType.FREE.name)
                val restType = try {
                    if (rawTypeStr == "REMOTE_API" || rawTypeStr == "VPS_HWID_CHECK") {
                        RestrictionType.VPS_HWID_CHECK
                    } else {
                        RestrictionType.valueOf(rawTypeStr)
                    }
                } catch (_: Exception) {
                    RestrictionType.FREE
                }

                val exp = if (rulesObj.has("expirationTimestamp")) rulesObj.getLong("expirationTimestamp") else null
                val singleId = rulesObj.optString("allowedDeviceId", "").ifBlank { null }

                val allowedList = mutableListOf<String>()
                val arr = rulesObj.optJSONArray("allowedDeviceIds")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val id = arr.getString(i).trim()
                        if (id.isNotEmpty()) allowedList.add(id)
                    }
                }

                val remoteUrl = rulesObj.optString("remoteValidationUrl", "").ifBlank { null }
                val isLocked = rulesObj.optBoolean("isConfigLocked", false)
                val note = rulesObj.optString("creatorNote", "")
                val exportTs = rulesObj.optLong("exportTimestamp", System.currentTimeMillis())

                DtpAccessRules(
                    restrictionType = restType,
                    expirationTimestamp = exp,
                    allowedDeviceId = singleId,
                    allowedDeviceIds = allowedList,
                    remoteValidationUrl = remoteUrl,
                    isConfigLocked = isLocked,
                    creatorNote = note,
                    exportTimestamp = exportTs
                )
            } else {
                DtpAccessRules()
            }

            DtpProfilePackage(
                version = version,
                profileName = profileName,
                sshConfig = sshConfig,
                accessRules = accessRules
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Validates access restrictions against the current device and current timestamp / remote server.
     */
    suspend fun validateAccess(context: Context, rules: DtpAccessRules): DtpValidationResult = withContext(Dispatchers.IO) {
        val currentDeviceId = DeviceIdentifier.getDeviceId(context).trim().uppercase(Locale.ROOT)
        val now = System.currentTimeMillis()

        // 1. Expiration check (Applies to EXPIRATION or any rule with expiration set)
        if (rules.expirationTimestamp != null) {
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val expDateStr = formatter.format(Date(rules.expirationTimestamp))

            if (now > rules.expirationTimestamp) {
                return@withContext DtpValidationResult(
                    isValid = false,
                    title = "Perfil Expirado",
                    message = "Este perfil .dtp venció el $expDateStr. La conexión ha sido abortada por seguridad.",
                    details = "Fecha límite: $expDateStr"
                )
            }
        }

        when (rules.restrictionType) {
            RestrictionType.FREE -> {
                DtpValidationResult(isValid = true, title = "Acceso Autorizado", message = "Perfil libre y activo.")
            }

            RestrictionType.EXPIRATION -> {
                val exp = rules.expirationTimestamp
                if (exp != null) {
                    val remainingMs = exp - now
                    val remainingDays = (remainingMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                    val remainingHours = ((remainingMs / (1000 * 60 * 60)) % 24).coerceAtLeast(0)
                    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val expDateStr = formatter.format(Date(exp))

                    DtpValidationResult(
                        isValid = true,
                        title = "Licencia Activa",
                        message = "Válido hasta: $expDateStr (Quedan $remainingDays d, $remainingHours h)",
                        details = "Expiración: $expDateStr"
                    )
                } else {
                    DtpValidationResult(isValid = true, title = "Acceso Válido", message = "Perfil activo")
                }
            }

            RestrictionType.SINGLE_ID -> {
                val targetId = rules.allowedDeviceId?.trim()?.uppercase(Locale.ROOT)
                if (targetId.isNullOrBlank()) {
                    DtpValidationResult(
                        isValid = false,
                        title = "ID No Configurado",
                        message = "El archivo .dtp no contiene un ID de dispositivo válido."
                    )
                } else if (targetId == currentDeviceId) {
                    DtpValidationResult(
                        isValid = true,
                        title = "Dispositivo Autorizado",
                        message = "ID de hardware validado correctamente ($currentDeviceId)."
                    )
                } else {
                    DtpValidationResult(
                        isValid = false,
                        title = "Dispositivo No Autorizado",
                        message = "Este perfil está asignado exclusivamente a otro dispositivo.\n\nTu ID: $currentDeviceId\nID Autorizado: $targetId",
                        details = "Tu ID: $currentDeviceId"
                    )
                }
            }

            RestrictionType.MULTI_ID -> {
                val normalizedList = rules.allowedDeviceIds.map { it.trim().uppercase(Locale.ROOT) }
                if (normalizedList.contains(currentDeviceId)) {
                    DtpValidationResult(
                        isValid = true,
                        title = "Dispositivo Autorizado",
                        message = "Tu ID ($currentDeviceId) está en la lista de terminales permitidos."
                    )
                } else {
                    DtpValidationResult(
                        isValid = false,
                        title = "Dispositivo No Autorizado",
                        message = "Tu ID no está en la lista de dispositivos autorizados de este archivo.\n\nTu ID: $currentDeviceId",
                        details = "Tu ID: $currentDeviceId"
                    )
                }
            }

            RestrictionType.VPS_HWID_CHECK -> {
                val urlStr = rules.remoteValidationUrl?.trim()
                if (urlStr.isNullOrBlank() || !urlStr.startsWith("http")) {
                    return@withContext DtpValidationResult(
                        isValid = false,
                        title = "URL del VPS Inválida",
                        message = "La dirección de tu VPS para verificar HWIDs no está configurada o es incorrecta."
                    )
                }

                try {
                    // We query the VPS URL. We pass current HWID via query parameter / header and body
                    val targetUrl = if (urlStr.contains("?")) {
                        "$urlStr&hwid=$currentDeviceId"
                    } else {
                        "$urlStr?hwid=$currentDeviceId"
                    }

                    val url = URL(targetUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Accept", "*/*")
                    conn.setRequestProperty("User-Agent", "TIENDA_SSH_HWID_CHECK")
                    conn.setRequestProperty("X-Device-HWID", currentDeviceId)
                    conn.connectTimeout = 7000
                    conn.readTimeout = 7000

                    val responseCode = conn.responseCode
                    if (responseCode in 200..299) {
                        val responseBody = conn.inputStream.bufferedReader().use { it.readText() }.trim()

                        // Check 1: Plain text file on VPS (list of HWIDs per line, comma or space separated)
                        val lines = responseBody.lines().map { it.trim().uppercase(Locale.ROOT) }
                        val isInTextList = lines.any { line ->
                            line.contains(currentDeviceId) || line == currentDeviceId
                        }

                        // Check 2: JSON formatted response from VPS script (e.g. {"status": true} or {"hwids": [...]})
                        var isJsonAuthorized = false
                        try {
                            val jsonResp = JSONObject(responseBody)
                            if (jsonResp.has("hwids")) {
                                val arr = jsonResp.getJSONArray("hwids")
                                for (i in 0 until arr.length()) {
                                    if (arr.getString(i).trim().uppercase(Locale.ROOT) == currentDeviceId) {
                                        isJsonAuthorized = true
                                        break
                                    }
                                }
                            } else if (jsonResp.has("allowed") || jsonResp.has("authorized") || jsonResp.has("status")) {
                                isJsonAuthorized = jsonResp.optBoolean("allowed", jsonResp.optBoolean("authorized", jsonResp.optBoolean("status", false)))
                            }
                        } catch (_: Exception) {
                            // Not a json or simple text
                        }

                        if (isInTextList || isJsonAuthorized) {
                            DtpValidationResult(
                                isValid = true,
                                title = "HWID Autorizado en VPS",
                                message = "✓ Tu HWID ($currentDeviceId) está registrado y activo en el servidor VPS."
                            )
                        } else {
                            DtpValidationResult(
                                isValid = false,
                                title = "HWID No Registrado en VPS",
                                message = "Tu identificador ($currentDeviceId) NO está en la lista de HWIDs permitidos en el VPS.\n\nRegistra tu HWID en el servidor para habilitar el acceso.",
                                details = "HWID: $currentDeviceId"
                            )
                        }
                    } else if (responseCode == 403 || responseCode == 401) {
                        DtpValidationResult(
                            isValid = false,
                            title = "Acceso Denegado por VPS (HTTP $responseCode)",
                            message = "El servidor VPS rechazó la conexión para el HWID ($currentDeviceId)."
                        )
                    } else {
                        DtpValidationResult(
                            isValid = false,
                            title = "Error de Servidor VPS (HTTP $responseCode)",
                            message = "El servidor VPS devolvió un código de error inesperado ($responseCode)."
                        )
                    }
                } catch (e: Exception) {
                    DtpValidationResult(
                        isValid = false,
                        title = "Error de Conexión con VPS",
                        message = "No se pudo consultar la lista de HWIDs en tu servidor VPS (${e.localizedMessage ?: "Fallo de red"}). Verifica tu conexión o la URL configurada.",
                        details = urlStr
                    )
                }
            }
        }
    }

    /**
     * Saves a DtpProfilePackage to a local .dtp file in cache / external files.
     */
    fun exportDtpFile(context: Context, pkg: DtpProfilePackage): File {
        val base64Content = serializeToDtpBase64(pkg)
        val cleanName = pkg.profileName
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            .ifBlank { "config_${System.currentTimeMillis()}" }
        val filename = "$cleanName$FILE_EXTENSION"

        val exportDir = File(context.cacheDir, "exported_dtp").apply { mkdirs() }
        val file = File(exportDir, filename)
        file.writeText(base64Content, Charsets.UTF_8)
        return file
    }

    /**
     * Launches the system share intent for a .dtp file using FileProvider.
     */
    fun shareDtpFile(context: Context, file: File, profileTitle: String) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Perfil TIENDA SSH: $profileTitle")
            putExtra(Intent.EXTRA_TEXT, "Configuración protegida de TIENDA SSH (.dtp):\n${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(shareIntent, "Compartir perfil .dtp").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Reads a .dtp file from a content Uri (e.g. opened from WhatsApp, Telegram, or File picker).
     */
    fun readDtpFromUri(context: Context, uri: Uri): DtpProfilePackage? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                val content = reader.readText()
                deserializeFromDtp(content)
            }
        } catch (_: Exception) {
            null
        }
    }
}
