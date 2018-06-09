package org.progfun.price;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains a list of prices for a single market, single resolution!
 */
public class Prices {

    private final List<PriceCandle> prices = new LinkedList<>();

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
        prices.add(price);
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
        return prices;
    }
    
    /**
     * Get price candle with the most recent timestamp
     * @return price candle or null if prices are empty
     */
    public PriceCandle getLatest() {
        long latestTime = -1;
        PriceCandle latestPrice = null;
        for (PriceCandle p : prices) {
            if (p.getOpenTime() > latestTime) {
                latestTime = p.getOpenTime();
                latestPrice = p;
            }
        }
        return latestPrice;
    }

}
