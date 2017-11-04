package org.progfun.connector;

/**
 * A parser that will receive messages from the connector and must be able to
 * interpret them as commands for orderbook and other modifications
 */
public interface Parser {

    /**
     * This method is called by a connector when a message arrives. It may be
     * called on another thread.
     *
     * @param message
     */
    public void onMessage(String message);
}
