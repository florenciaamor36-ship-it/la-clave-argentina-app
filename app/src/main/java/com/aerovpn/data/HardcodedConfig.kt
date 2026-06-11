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
        payload = "COPY / HTTP/1.3[crlf]Host: [host][crlf][crlf]\n[lf][lf][instant_split][lf][lf]\nX / HTTP/1.2[crlf]Host: [host][crlf][lf][crlf]GET /app100123 HTTP/1.1[crlf]Host:[rotate=cloudfront-03.cdn-hub.org;d9l7mhbnyv6wq.cloudfront.net;d2iowtuv61tbhu.cloudfront.net][crlf]Upgrade:websocket[crlf]Connection: Upgrade[crlf][crlf]",
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
        payload = "MKCOL /todosvienenalgrupocastigador  HTTP/1.9[lf]Host: recargas.personal.com.ar[lf]Expect: 100-continue[crlf][crlf][split][crlf][crlf]GET- // HTTP/1.1[crlf]Host: gusty2.internetgm.es[crlf]Connection: Upgrade[crlf]User-Agent: [ua][crlf]Upgrade: websocket[crlf][crlf]",
        proxyHost = "128.254.190.146",
        proxyPort = 80
    )

    val defaultServer = server1
}
