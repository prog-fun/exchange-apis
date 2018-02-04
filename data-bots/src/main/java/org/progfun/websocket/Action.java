package org.progfun.websocket;

public enum Action {
    NONE,
    CONNECT, // Connect to the remote API (WebSocket)
    START, // Initialize communication with remote API, must be done after CONNECT!
    SUBSCRIBE, // Perform all subscriptions to necessary channels
    DISCONNECT, // Disconnect from the remote API (WebSocket), keep the handler still running
    RECONNECT, // Disconnect from the remote API (WebSocket), then connect again
    SHUTDOWN, // Close the connection and shut down the Handler
    TERMINATE, // Terminate the Handler immediately, do not care about closing sockets
    PARSE_MSG, // Parse a message received from the remote API
    EXECUTE_METHOD // Execute a specific method on the main Handler thread
};
