package org.progfun;

/**
 * Subscription Channels
 */
public enum Channel {
    NONE, 
    ORDERBOOK, // Orderbook updates (bids/asks)
    TRADES, // Last trade updates
    PRICES, // Price (candle) updates
    TICKER // Ticker updates
}
