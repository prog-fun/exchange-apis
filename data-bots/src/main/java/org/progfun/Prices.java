package org.progfun;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains a list of prices for a single market
 */
public class Prices {

    private final Map<Long, PriceCandle> prices = new HashMap<>();

    /**
     * Add a price to the collection. If there is already a price with the same
     * timestamp, it will be replaced with this one.
     *
     * @param price
     */
    public void add(PriceCandle price) {
        if (price == null) {
            return;
        }

        // Either replace an existing price, or add a new price to the list
        prices.put(price.getOpenTime(), price);
    }

    /**
     * Delete all stored prices
     */
    public void clear() {
        prices.clear();
    }

    /**
     * Return number of prices in the collection
     *
     * @return
     */
    public int size() {
        return prices.size();
    }
    
    public Collection<PriceCandle> getAll() {
        return prices.values();
    }

}
