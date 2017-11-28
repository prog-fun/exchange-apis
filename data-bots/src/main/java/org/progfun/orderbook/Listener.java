package org.progfun.orderbook;

import java.math.BigDecimal;
import org.progfun.Market;

/**
 * Listener for Order book updates
 */
public interface Listener {

    /**
     * A new bid added for the specific price
     * @param market
     * @param bid 
     */
    public void bidAdded(Market market, Order bid);

    /**
     * A new ask added for the specific price
     * @param market
     * @param ask 
     */
    public void askAdded(Market market, Order ask);

    /**
     * A bid with specific price updated: amount or orderCount changed
     * @param market
     * @param bid 
     */
    public void bidUpdated(Market market, Order bid);

    /**
     * An ask with specific price updated: amount of orderCount changed
     * @param market
     * @param ask 
     */
    public void askUpdated(Market market, Order ask);

    /**
     * Bid for specific price removed
     * @param market
     * @param price 
     */
    public void bidRemoved(Market market, BigDecimal price);

    /**
     * Ask for specific price removed
     * @param market
     * @param price 
     */
    public void askRemoved(Market market, BigDecimal price);


}
