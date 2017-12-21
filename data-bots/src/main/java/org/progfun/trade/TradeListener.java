package org.progfun.trade;

import org.progfun.Market;

/**
 * Listener for new Trades
 */
public interface TradeListener {

    /**
     * A new trade has been added
     *
     * @param market market where the trade was made
     * @param trade the new trade
     */
    public void tradeAdded(Market market, Trade trade);
}
