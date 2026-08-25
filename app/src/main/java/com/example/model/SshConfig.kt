package com.example.model

data class SshConfig(
    val id: String = "default",
    val profileName: String = "Servidor Predeterminado",
    val host: String = "198.51.100.24",
    val port: String = "22",
    val username: String = "tienda_ssh",
    val password: String = "vpn2026",
    val tunnelType: TunnelType = TunnelType.SSH_HTTP_CUSTOM,
    val payload: String = "CONNECT [host_port] [protocol][crlf]Host: [host][crlf]X-Online-Host: [host][crlf]Connection: Keep-Alive[crlf][crlf]",
    val sniBugHost: String = "ssl.tiendassh.net",
    val proxyHost: String = "198.51.100.24",
    val proxyPort: String = "8080",
    val enableUdpForwarding: Boolean = true,
    val enableCompression: Boolean = true,
    val customDns: String = "1.1.1.1, 1.0.0.1",
    val autoReconnect: Boolean = true,
    val wakeLock: Boolean = true
)
