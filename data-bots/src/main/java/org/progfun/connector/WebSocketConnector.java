package org.progfun.connector;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Generic WebSocket client that can send commands to the API and generate event
 * for listener when a message arrives
 */
public class WebSocketConnector {

    WSClient client;

    // Only one listener allowed
    Parser listener;

    // Used for logging messages to a file
    private PrintWriter logWriter;

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
                    logMessage(message);
                    if (listener != null) {
                        listener.onMessage(message);
                    }
                }

                @Override
                public void onError(Exception excptn) {
                    // On error we stop the party and notify the listener
                    super.onError(excptn);
                    stop();
                    if (listener != null) {
                        listener.onError(excptn);
                    }
                }
            };

            client.connectBlocking();

        } catch (URISyntaxException ex) {
            System.out.println("Invalid WSS URL format: " + ex.getMessage());
            return false;
        } catch (InterruptedException ex) {
            System.out.println("Could not connect: " + ex.getMessage());
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
            System.out.println("Interrupted while closing connection");
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
            System.out.println("Log file opened: " + filename);
            return true;
        } catch (FileNotFoundException ex) {
            System.out.println("Can' open log file:" + ex.getMessage());
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
