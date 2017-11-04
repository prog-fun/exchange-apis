package org.progfun.orderbook;

import org.progfun.Market;

/**
 * Listener for Order book updates
 */
public interface Listener {

    /**
     * A new bid added for the specific price
     * @param bid 
     */
    public void bidAdded(Market market, Order bid);

    /**
     * A new ask added for the specific price
     * @param ask 
     */
    public void askAdded(Market market, Order ask);

    /**
     * A bid with specific price updated: amount or orderCount changed
     * @param bid 
     */
    public void bidUpdated(Market market, Order bid);

    /**
     * An ask with specific price updated: amount of orderCount changed
     * @param ask 
     */
    public void askUpdated(Market market, Order ask);

    /**
     * Bid for specific price removed
     * @param price 
     */
    public void bidRemoved(Market market, double price);

    /**
     * Ask for specific price removed
     * @param price 
     */
    public void askRemoved(Market market, double price);


}
