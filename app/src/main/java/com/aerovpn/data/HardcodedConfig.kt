package com.aerovpn.data

import com.aerovpn.service.protocol.SSHConfig

/**
 * Hardcoded configuration for emergency/offline access.
 * These are encrypted or obfuscated in a production build.
 */
object HardcodedConfig {
    
    // Server 1: Cloudfront Complex Rotate (Based on screenshot)
    val server1 = SSHConfig(
        name = "La Clave - Cloudfront Rotate",
        serverAddress = "emailmarketing.personal.com.ar",
        serverPort = 80,
        username = "GUSTY",
        password = "12345",
        payload = "COPY / HTTP/1.3[crlf]Host: [host][crlf][crlf][lf][lf][instant_split][lf][lf]X / HTTP/1.2[crlf]Host: [host][crlf][lf][crlf]GET / app100123 HTTP/1.1[crlf]Host: [rotate=cloudfront-03.cdn-hub.org;d9l7mhbnyv6wq.cloudfront.net;d2iowtuv61tbhu.cloudfront.net][crlf]Upgrade:websocket[crlf]Connection: Upgrade[crlf][crlf]",
        proxyHost = "128.254.190.146",
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
