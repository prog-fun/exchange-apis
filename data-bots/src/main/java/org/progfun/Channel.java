package org.progfun;

/**
 * Subscription Channels
 */
public enum Channel {
    NONE, 
    ORDERBOOK, // Orderbook updates (bids/asks)
    TRADES, // Last trade updates
    PRICES_1MIN, // Price (candle) updates: 1min resolution
    PRICES_5MIN, // Price (candle) updates: 5min resolution
    PRICES_15MIN, // Price (candle) updates: 15min resolution
    PRICES_30MIN, // Price (candle) updates: 30min resolution
    PRICES_1H, // Price (candle) updates: 1h resolution
    PRICES_3H, // Price (candle) updates: 3h resolution
    PRICES_6H, // Price (candle) updates: 6h resolution
    PRICES_12H, // Price (candle) updates: 12h resolution
    PRICES_1D, // Price (candle) updates: 1 day resolution
    PRICES_1W, // Price (candle) updates: 1 week resolution
    TICKER // Ticker updates
}
