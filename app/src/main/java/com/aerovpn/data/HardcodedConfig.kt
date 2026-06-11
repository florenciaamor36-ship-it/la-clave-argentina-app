package com.aerovpn.data

import com.aerovpn.service.protocol.SSHConfig

/**
 * Hardcoded configuration for emergency/offline access.
 * These are encrypted or obfuscated in a production build.
 */
object HardcodedConfig {
    
    // Server 1: Cloudflare Payload (Official Project NEXUS)
    val server1 = SSHConfig(
        name = "La Clave - Conexión Total",
        serverAddress = "emailmarketing.personal.com.ar",
        serverPort = 80,
        username = "GUSTY",
        password = "12345",
        payload = "GET / HTTP/1.1[crlf]Host: emailmarketing.personal.com.ar[crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]",
        proxyHost = "128.254.190.146",
        proxyPort = 80
    )

    // Server 2: Cloudflare Backup
    val server2 = SSHConfig(
        name = "La Clave - Backup NEXUS",
        serverAddress = "emailmarketing.personal.com.ar",
        serverPort = 80,
        username = "GUSTY",
        password = "12345",
        payload = "CONNECT [host_port] HTTP/1.1[crlf]Host: emailmarketing.personal.com.ar[crlf][crlf]",
        proxyHost = "128.254.190.146",
        proxyPort = 80
    )

    val defaultServer = server1
}
