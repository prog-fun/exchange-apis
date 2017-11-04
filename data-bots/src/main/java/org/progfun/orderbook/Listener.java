package org.progfun.orderbook;

/**
 * Listener for Order book updates
 */
public interface Listener {

    /**
     * A new bid added for the specific price
     * @param bid 
     */
    public void bidAdded(Order bid);

    /**
     * A new ask added for the specific price
     * @param ask 
     */
    public void askAdded(Order ask);

    /**
     * A bid with specific price updated: amount or orderCount changed
     * @param bid 
     */
    public void bidUpdated(Order bid);

    /**
     * An ask with specific price updated: amount of orderCount changed
     * @param ask 
     */
    public void askUpdated(Order ask);

    /**
     * Bid for specific price removed
     * @param price 
     */
    public void bidRemoved(double price);

    /**
     * Ask for specific price removed
     * @param price 
     */
    public void askRemoved(double price);


}
