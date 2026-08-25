package com.example.model

enum class TunnelType(val displayName: String, val description: String, val defaultPort: String) {
    SSH_DIRECT(
        displayName = "SSH Directo",
        description = "Conexión directa TCP al servidor SSH",
        defaultPort = "22"
    ),
    SSH_HTTP_CUSTOM(
        displayName = "SSH + HTTP Custom",
        description = "Inyección de Payload HTTP a través de Proxy o Bug",
        defaultPort = "80"
    ),
    SSL_TLS(
        displayName = "SSL / TLS (SNI)",
        description = "Túnel cifrado con SNI / Bug Host directo",
        defaultPort = "443"
    ),
    SSL_PAYLOAD(
        displayName = "SSL + WebSocket / CDN",
        description = "Túnel SSL con Payload WebSocket sobre Cloudflare/CDN",
        defaultPort = "443"
    ),
    SSH_PROXY(
        displayName = "SSH + Proxy Squid",
        description = "Conexión SSH a través de proxy Squid remoto",
        defaultPort = "8080"
    )
}
