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
    PRICES_2H, // Price (candle) updates: 2h resolution
    PRICES_3H, // Price (candle) updates: 3h resolution
    PRICES_4H, // Price (candle) updates: 4h resolution
    PRICES_6H, // Price (candle) updates: 6h resolution
    PRICES_12H, // Price (candle) updates: 12h resolution
    PRICES_1D, // Price (candle) updates: 1 day resolution
    PRICES_1W, // Price (candle) updates: 1 week resolution
    TICKER; // Ticker updates

    /**
     * Take price resolution string ("5m" etc), convert it to Channel object
     *
     * @param res
     * @return Channel object or null if the resolution string is not supported
     */
    public static Channel priceStringToChannel(String res) {
        if (res == null) {
            return null;
        }
        switch (res.toLowerCase()) {
            case "1m":
                return PRICES_1MIN;
            case "5m":
                return PRICES_5MIN;
            case "15m":
                return PRICES_15MIN;
            case "30m":
                return PRICES_30MIN;
            case "1h":
                return PRICES_1H;
            case "2h":
                return PRICES_2H;
            case "3h":
                return PRICES_3H;
            case "4h":
                return PRICES_4H;
            case "6h":
                return PRICES_6H;
            case "12h":
                return PRICES_12H;
            case "1d":
                return PRICES_1D;
            case "1w":
                return PRICES_1W;
            default:
                return null;
        }
    }
}
