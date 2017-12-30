package org.progfun.websocket;

import org.progfun.Subscription;

/**
 * Listener for socket state changes
 */
public interface SocketStateListener {
    /**
     * Connection to API has been closed
     *
     * @param reason readable reason for termination
     */
    public void onDisconnected(String reason);

    /**
     * WebSocket connection is successfully established
     */
    public void onConnected();

    /**
     * This method is called when a subscription is successful
     *
     * @param subscription
     */
    public void onSubscribed(Subscription subscription);

    /**
     * This method is called when all subscriptions are processed and socket
     * handler is completely ready (Bid wall is stable)
     */
    public void onReady();

    /**
     * Error occurred
     *
     * @param reason readable reason for the error
     */
    public void onError(String reason);
}
