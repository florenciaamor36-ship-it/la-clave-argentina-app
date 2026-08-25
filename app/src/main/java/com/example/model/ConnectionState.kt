package com.example.model

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    HANDSHAKE,
    AUTHENTICATING,
    CONNECTED,
    DISCONNECTING,
    ERROR;

    val isConnected: Boolean
        get() = this == CONNECTED

    val isConnecting: Boolean
        get() = this == CONNECTING || this == HANDSHAKE || this == AUTHENTICATING

    val isDisconnected: Boolean
        get() = this == DISCONNECTED || this == ERROR
}
