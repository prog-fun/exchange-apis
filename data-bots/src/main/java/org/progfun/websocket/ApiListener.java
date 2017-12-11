package org.progfun.websocket;

import org.java_websocket.handshake.ServerHandshake;

/**
 * Listener for messages and status changes from the remote API (WebSocket or
 * otherwise)
 */
public interface ApiListener {

    /**
     * This method is called by a connector when a message arrives. It may be
     * called on another thread.
     *
     * @param message
     */
    public void onMessage(String message);

    /**
     * An error has occurred in the connector
     *
     * @param ex
     */
    public void onError(Exception ex);

    /**
     * Connection to API has been closed
     *
     * @param code code for connection termination reason
     * @param reason readable reason for termination
     * @param remote when true, remote end initiated the termination
     */
    public void onClose(int code, String reason, boolean remote);

    /**
     * WebSocket connection is successfully established
     * @param sh 
     */
    public void onOpen(ServerHandshake sh);

}
