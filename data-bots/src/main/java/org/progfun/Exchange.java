package org.progfun;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one financial instrument exchange, such as GDAX, Bitfinex, etc.
 */
public class Exchange {

    private final List<Market> markets = new ArrayList<>();

    /**
     * Return all the markets available in the exchange
     *
     * @return
     */
    public List<Market> getMarkets() {
        return markets;
    }

    /**
     * Return all markets available in this exchange related to a specific
     * currency. If there are no markets, return an empty list (never return a
     * null)
     *
     * @param currency "BTC", "USD", etc. Case insensitive
     * @return
     */
    public List<Market> getMatchingMarkets(String currency) {
        ArrayList<Market> matchingMarkets = new ArrayList<>();
        if (currency == null) {
            return matchingMarkets;
        }
        // Check all markets having either base or quote currency matching
        // the desired one
        for (Market m : markets) {
            if (currency.equalsIgnoreCase(m.getBaseCurrency())
                    || currency.equalsIgnoreCase(m.getQuoteCurrency())) {
                matchingMarkets.add(m);
            }
        }
        return matchingMarkets;
    }

    /**
     * Get market for specific currency pair
     *
     * @param baseCurrency case-insensitive base currency
     * @param quoteCurrency case-insensitive quote currency
     * @return the market or null if none found
     */
    public Market getMarket(String baseCurrency, String quoteCurrency) {
        if (baseCurrency == null || quoteCurrency == null) {
            return null;
        }
        for (Market m : markets) {
            if (baseCurrency.equalsIgnoreCase(m.getBaseCurrency())
                    && quoteCurrency.equalsIgnoreCase(m.getQuoteCurrency())) {
                return m;
            }
        }
        return null;
    }

    /**
     * Add a new market to the exchange. If a market with exactly the same
     * currency pair already exists, ignore it
     *
     * @param market
     * @return true if the market was added, false otherwise (if it already
     * existed)
     */
    boolean addMarket(Market market) {
        if (market == null) {
            return false;
        }

        // Check if such market already exists
        if (getMarket(market.getBaseCurrency(), market.getQuoteCurrency()) != null) {
            return false;
        }
        markets.add(market);
        return true;
    }
}
