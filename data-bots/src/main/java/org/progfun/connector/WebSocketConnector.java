package org.progfun.connector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Generic WebSocket client that can send commands to the API and generate event
 * for listener when a message arrives
 */
public class WebSocketConnector {

    WSClient client;

    private static final Logger LOGGER = Logger.getLogger(WebSocketConnector.class.getName());

    // Only one listener allowed
    Parser listener;

    /**
     * Set listener that will receive all messages from the remote server (API)
     *
     * @param listener
     */
    public void setListener(Parser listener) {
        this.listener = listener;
    }

    /**
     * Unregister the listener
     */
    public void removeListener() {
        this.listener = null;
    }

    /**
     * Start a WebSocket client in non-blocking mode. It will generate events in
     * another thread
     *
     * @param wssUrl The wss:// URL to the WebSocket server
     * @return true if client was successfully started, false otherwise
     */
    public boolean start(String wssUrl) {
        try {
            URI uri = new URI(wssUrl);
            client = new WSClient(uri) {
                @Override
                public void onMessage(String message) {
                    if (listener != null) {
                        listener.onMessage(message);
                    }
                }
                @Override
                public void onError(Exception excptn) {
                    // On error we stop the party
                    super.onError(excptn);
                    stop();
                }
                
            };

        } catch (URISyntaxException ex) {
            LOGGER.severe("Invalid WSS URL format: " + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Stop the connection
     *
     * @return true on success, false otherwise
     */
    public boolean stop() {
        if (client == null) {
            return false;
        }

        try {
            client.closeBlocking();
        } catch (InterruptedException ex) {
            LOGGER.info("Interrupted while closing connection");
        }
        return true;
    }

}
