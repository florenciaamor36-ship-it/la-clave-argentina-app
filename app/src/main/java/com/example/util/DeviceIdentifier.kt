package com.example.util

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

object DeviceIdentifier {

    private const val PREFS_NAME = "tienda_ssh_device_prefs"
    private const val KEY_PERSISTENT_DEVICE_ID = "persistent_device_id"
    private const val BACKUP_FILENAME = ".ts_device_signature.id"

    private var cachedId: String? = null

    /**
     * Obtains the persistent device identifier.
     * Combines Settings.Secure.ANDROID_ID with SharedPreferences and a persistent
     * file backup on accessible storage.
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        cachedId?.let { return it }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedId = prefs.getString(KEY_PERSISTENT_DEVICE_ID, null)
        if (!savedId.isNullOrBlank()) {
            cachedId = savedId
            ensureFileBackup(context, savedId)
            return savedId
        }

        // Check persistent file backup
        val backupId = readFromFileBackup(context)
        if (!backupId.isNullOrBlank()) {
            prefs.edit().putString(KEY_PERSISTENT_DEVICE_ID, backupId).apply()
            cachedId = backupId
            return backupId
        }

        // Generate from Android ID or UUID fallback
        val androidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (_: Exception) {
            null
        }

        val rawSeed = if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            androidId
        } else {
            UUID.randomUUID().toString()
        }

        val formattedId = formatSignature(rawSeed)
        prefs.edit().putString(KEY_PERSISTENT_DEVICE_ID, formattedId).apply()
        ensureFileBackup(context, formattedId)
        cachedId = formattedId
        return formattedId
    }

    private fun formatSignature(raw: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(raw.toByteArray(Charsets.UTF_8))
            val hex = hash.joinToString("") { "%02X".format(it) }
            val part1 = hex.substring(0, 4)
            val part2 = hex.substring(4, 8)
            val part3 = hex.substring(8, 12)
            val part4 = hex.substring(12, 16)
            "TS-$part1-$part2-$part3-$part4"
        } catch (_: Exception) {
            val clean = raw.replace("-", "").uppercase(Locale.ROOT)
            if (clean.length >= 12) {
                "TS-${clean.substring(0, 4)}-${clean.substring(4, 8)}-${clean.substring(8, 12)}"
            } else {
                "TS-DEV-" + UUID.randomUUID().toString().substring(0, 8).uppercase(Locale.ROOT)
            }
        }
    }

    private fun readFromFileBackup(context: Context): String? {
        return try {
            // Check app external files dir (Documents)
            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(docsDir, BACKUP_FILENAME)
            if (file.exists() && file.canRead()) {
                val content = file.readText().trim()
                if (content.startsWith("TS-") && content.length >= 10) {
                    return content
                }
            }

            // Check internal files fallback
            val internalFile = File(context.filesDir, BACKUP_FILENAME)
            if (internalFile.exists() && internalFile.canRead()) {
                val content = internalFile.readText().trim()
                if (content.startsWith("TS-") && content.length >= 10) {
                    return content
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun ensureFileBackup(context: Context, id: String) {
        try {
            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (docsDir != null && (docsDir.exists() || docsDir.mkdirs())) {
                val file = File(docsDir, BACKUP_FILENAME)
                file.writeText(id)
            }

            val internalFile = File(context.filesDir, BACKUP_FILENAME)
            internalFile.writeText(id)
        } catch (_: Exception) {
            // Ignore storage permission or sandbox write exceptions
        }
    }

    fun copyToClipboard(context: Context, showToast: Boolean = true): String {
        val id = getDeviceId(context)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ID_DISPOSITIVO_TIENDA_SSH", id)
        clipboard.setPrimaryClip(clip)
        if (showToast) {
            Toast.makeText(context, "✓ ID copiado: $id", Toast.LENGTH_SHORT).show()
        }
        return id
    }

    fun shareDeviceId(context: Context) {
        val id = getDeviceId(context)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Mi ID de Dispositivo TIENDA SSH:\n$id\n(Usa este identificador para activar licencias y perfiles .dtp)"
            )
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir ID de Dispositivo TIENDA SSH").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(shareIntent)
    }
}
