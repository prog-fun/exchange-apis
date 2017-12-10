package org.progfun.connector;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.handshake.ServerHandshake;
import org.progfun.Logger;

/**
 * Generic WebSocket client that can send commands to the API and generate event
 * for listener when a message arrives
 */
public class WebSocketConnector {

    WSClient client;

    // Only one listener allowed
    ApiListener listener;

    // Used for logging messages to a file
    private PrintWriter logWriter;

    /**
     * Set listener that will receive all messages from the remote server (API)
     *
     * @param listener
     */
    public void setListener(ApiListener listener) {
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
    public boolean connect(String wssUrl) {
        try {
            URI uri = new URI(wssUrl);
            client = new WSClient(uri) {
                @Override
                public void onMessage(String message) {
                    logMessage(message);
                    if (listener != null) {
                        listener.onMessage(message);
                    }
                }

                @Override
                public void onError(Exception excptn) {
                    // On error we stop the party and notify the listener
                    super.onError(excptn);
                    if (listener != null) {
                        listener.onError(excptn);
                    }
                }

                @Override
                public void onOpen(ServerHandshake sh) {
                    if (listener != null) {
                        listener.onOpen(sh);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    if (listener != null) {
                        listener.onClose(code, reason, remote);
                    }
                }

            };

            // Start connection. Block in this line until connection is 
            // established
            Logger.log("Connecting to Websocket...");
            client.connect();

        } catch (URISyntaxException ex) {
            Logger.log("Invalid WSS URL format: " + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Start to close the connection. This is an asynchronous process: method
     * returns immediately (non-blocking). When connection is closed, connector
     * will receive onClose() callback
     *
     * @return true on success (closing started), false otherwise.
     */
    public boolean close() {
        if (client == null) {
            return false;
        }

        if (client.isOpen()) {
            client.close();
        } else {
            client.closeConnection(0, "Closing invalid connection");
        }
        return true;
    }

    /**
     * Send a message to the WebSocket server (API)
     *
     * @param msg
     * @return true on success, false on error (client not connected, etc)
     */
    public boolean send(String msg) {
        if (client != null) {
            Logger.log("Sending msg to WebSocket API: " + msg);
            client.send(msg);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Start logging all the messages to a text file
     *
     * @param filename path to the log file
     * @return true on success, false otherwise
     */
    public boolean startLogging(String filename) {
        try {
            logWriter = new PrintWriter(new FileOutputStream(filename), true);
            Logger.log("Log file opened: " + filename);
            return true;
        } catch (FileNotFoundException ex) {
            Logger.log("Can' open log file:" + ex.getMessage());
        }
        return false;
    }

    /**
     * Close the log file and stop logging
     *
     * @return true when file was opened and is now closed, false if it was not
     * opened
     */
    public boolean stopLogging() {
        if (logWriter != null) {
            logWriter.close();
            logWriter = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Log message to text file, if log file is opened
     *
     * @param message
     */
    private void logMessage(String message) {
        if (logWriter != null) {
            logWriter.println(message);
        }
    }
}
