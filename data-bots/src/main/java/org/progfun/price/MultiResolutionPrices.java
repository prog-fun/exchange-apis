package org.progfun.price;

import java.util.HashMap;
import java.util.Map;
import org.progfun.Channel;

/**
 * Holds prices in different resolutions: 1min, 15min, etc
 */
public class MultiResolutionPrices {

    private final Map<Channel, Prices> prices = new HashMap<>();

    /**
     * Get all stored prices for a given resolution
     *
     * @param resolution
     * @return Prices object containing all prices, or null if no prices stored
     * for this resolution
     */
    public Prices get(Channel resolution) {
        return prices.get(resolution);
    }

    /**
     * Add a price candle with a given resolution
     * @param resolution
     * @param price 
     */
    public void add(Channel resolution, PriceCandle price) {
        Prices p = prices.get(resolution);
        if (p == null) {
            // First price for this resolution
            p = new Prices();
            prices.put(resolution, p);
        }
        p.add(price);
    }

    /**
     * Clear all prices, for all resolutions
     */
    public void clear() {
        prices.clear();
    }

    /**
     * Clear prices for one specific resolution
     * @param resolution 
     */
    public void remove(Channel resolution) {
        prices.remove(resolution);
    }

}
