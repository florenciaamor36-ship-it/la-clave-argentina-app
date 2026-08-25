package com.example.model

data class ConnectionMetrics(
    val downloadSpeedBytes: Long = 0L,
    val uploadSpeedBytes: Long = 0L,
    val totalDownloadBytes: Long = 0L,
    val totalUploadBytes: Long = 0L,
    val pingMs: Int = 0,
    val durationSeconds: Long = 0L,
    val assignedIp: String = "10.0.8.2",
    val cipher: String = "chacha20-poly1305@openssh.com"
) {
    fun formattedDownloadSpeed(): String = formatSpeed(downloadSpeedBytes)
    fun formattedUploadSpeed(): String = formatSpeed(uploadSpeedBytes)
    fun formattedTotalDownload(): String = formatBytes(totalDownloadBytes)
    fun formattedTotalUpload(): String = formatBytes(totalUploadBytes)
    fun formattedDuration(): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    companion object {
        fun formatSpeed(bytesPerSec: Long): String {
            return when {
                bytesPerSec >= 1024 * 1024 -> String.format("%.2f MB/s", bytesPerSec / (1024.0 * 1024.0))
                bytesPerSec >= 1024 -> String.format("%.1f KB/s", bytesPerSec / 1024.0)
                else -> "$bytesPerSec B/s"
            }
        }

        fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
                bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
                bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
                else -> "$bytes B"
            }
        }
    }
}
