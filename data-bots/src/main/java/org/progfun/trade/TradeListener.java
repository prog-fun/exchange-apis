package org.progfun.trade;

/**
 * Listener for new Trades
 */
public interface TradeListener {

    /**
     * A new trade has been added
     *
     * @param trade the new trade
     */
    public void tradeAdded(Trade trade);
}
