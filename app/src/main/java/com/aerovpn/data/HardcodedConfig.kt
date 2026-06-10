package com.aerovpn.data

import com.aerovpn.service.protocol.SSHConfig

/**
 * Hardcoded configuration for emergency/offline access.
 * These are encrypted or obfuscated in a production build.
 */
object HardcodedConfig {
    
    // Server 1: Cloudfront Rotate
    val server1 = SSHConfig(
        name = "La Clave - Cloudfront",
        serverAddress = "emailmarketing.personal.com.ar",
        serverPort = 80,
        username = "GUSTY",
        password = "12345",
        payload = "CONNECT [host_port] [protocol][crlf]Host: d279v0k7n2z8z8.cloudfront.net[crlf]X-Online-Host: d279v0k7n2z8z8.cloudfront.net[crlf]Connection: Keep-Alive[crlf]User-Agent: [ua][crlf]Referer: [host][crlf][crlf]",
        proxyHost = "128.254.190.146", // Assuming VPS as proxy for payload handling
        proxyPort = 80
    )

    // Server 2: MKCOL
    val server2 = SSHConfig(
        name = "La Clave - MKCOL",
        serverAddress = "emailmarketing.personal.com.ar",
        serverPort = 80,
        username = "GUSTY",
        password = "12345",
        payload = "MKCOL http://emailmarketing.personal.com.ar/ HTTP/1.1[crlf]Host: emailmarketing.personal.com.ar[crlf]Connection: Keep-Alive[crlf][crlf]",
        proxyHost = "128.254.190.146",
        proxyPort = 80
    )

    val defaultServer = server1
}
