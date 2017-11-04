package org.progfun.connector;

import java.net.URI;
import java.util.logging.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * A WebSocket client. Do NOT use this class directly!
 */
abstract class WSClient extends WebSocketClient {
    private static final Logger LOGGER = Logger.getLogger(WSClient.class.getName());

    WSClient(URI uri) {
        super(uri);
    }
    
    @Override
    public void onOpen(ServerHandshake sh) {
        LOGGER.info("WebSocket connected");
    }

    // onMessage class must be implemented in child class

    @Override
    public void onClose(int i, String string, boolean bln) {
        LOGGER.info("WebSocket closed");
    }

    @Override
    public void onError(Exception excptn) {
        LOGGER.severe("WebSocket error: " + excptn.getMessage());
    }

}
