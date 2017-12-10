package org.progfun.websocket;

import org.progfun.Market;

/**
 * Abstract API command parser
 */
public abstract class Parser {

    protected Market market;

    public void setMarket(Market market) {
        this.market = market;
    }

    /**
     * This method is called by a connector when a message arrives. It may be
     * called on another thread.
     *
     * @param message
     * @return Action that the Handler should perform as a response to the
     * received message. This interface allows parser to notify that a reconnect
     * is needed, etc. When no action is needed, return null.
     */
    public abstract Action parseMessage(String message);
}
