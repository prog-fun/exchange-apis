package org.progfun.connector;

import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * A WebSocket client. Do NOT use this class directly!
 */
abstract class WSClient extends WebSocketClient {

    WSClient(URI uri) {
        super(uri);
    }

    @Override
    public void onOpen(ServerHandshake sh) {
        System.out.println("WebSocket connected");
    }

    // onMessage class must be implemented in child class
    @Override
    public void onClose(int i, String string, boolean bln) {
        System.out.println("WebSocket closed");
    }

    @Override
    public void onError(Exception excptn) {
        System.out.println("WebSocket error: " + excptn.getMessage());
    }

}
