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
     */
    public abstract void parseMessage(String message);
}
