package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO,
    SSH,
    HTTP,
    VPN,
    AUTH,
    SUCCESS,
    WARNING,
    ERROR
}

data class LogEntry(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val message: String
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    fun toFormattedString(): String {
        return "[$formattedTime] [${level.name}] $message"
    }
}
