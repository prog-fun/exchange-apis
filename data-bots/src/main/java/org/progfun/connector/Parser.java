package org.progfun.connector;

import org.progfun.Market;

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

    /**
     * An error has occurred in the connector
     * @param excptn 
     */
    public void onError(Exception excptn);

    /**
     * Set a market that the parser should use. Orderbook updates should
     * happen within that market
     * @param market 
     */
    public void setMarket(Market market);
}
