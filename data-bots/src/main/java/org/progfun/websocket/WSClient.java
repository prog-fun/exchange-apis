package org.progfun.websocket;

import java.net.URI;
import java.net.UnknownHostException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.progfun.Logger;

/**
 * A WebSocket client. Do NOT use this class directly!
 */
abstract class WSClient extends WebSocketClient {

    WSClient(URI uri) {
        super(uri);
    }

    @Override
    public void onOpen(ServerHandshake sh) {
        Logger.log("WebSocket connected");
    }

    // onMessage must be implemented in child class

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Logger.log("WebSocket closed");
    }

    @Override
    public void onError(Exception ex) {
        if (ex instanceof UnknownHostException) {
            Logger.log("WSClient: Could not connect to Websocket @ " + ex.getMessage());
        }
    }

}
