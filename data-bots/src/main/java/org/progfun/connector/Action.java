package org.progfun.connector;

public enum Action {
    NONE,
    CONNECT, // Connect to the remote API (WebSocket)
    START, // Start communication with remote API - send some initial subscribe 
    // commands, etc. START must be done after CONNECT!
//    CONNECT_AND_START, // Use this action type to initalize both steps: connection
//    // first; when connection successful, initialize the process and start it
    DISCONNECT, // Disconnect from the remote API (WebSocket), keep the handler still running
    RECONNECT, // Disconnect from the remote API (WebSocket), then connect again
    SHUTDOWN, // Close the connection and shut down the Handler
    TERMINATE // Terminate the Handler immediately, do not care about closing sockets
};
