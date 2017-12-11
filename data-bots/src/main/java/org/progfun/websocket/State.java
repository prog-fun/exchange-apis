package org.progfun.websocket;

/**
 * Represents state of WebSocket handler
 */
public enum State {
    DISCONNECTED,
    CONNECT_SCHEDULED,
    CONNECTING,
    CONNECTED,
    START_SCHEDULED,
    RUNNING,
    WAIT_CONNECT,
    DISCONNECT_SCHEDULED,
    DISCONNECTING,
    RECONNECT_SCHEDULED,
    REC_DISCONNECTING,
    SHUTDOWN_SCHEDULED,
    SHUTTING_DOWN,
    TERMINATED
}
