package org.progfun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents one financial instrument exchange, such as GDAX, Bitfinex, etc.
 * The class is NOT Thread safe!
 */
public class Exchange {

    private final HashMap<CurrencyPair, Market> markets = new HashMap<>();
    private String symbol;

    /**
     * Set symbol identifying the exchange
     *
     * @param symbol the symbol of the exchange: GDAX, BITF, etc.
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Get symbol identifying the exchange: BITF, GDAX, etc.
     *
     * @return
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Return all the markets available in the exchange
     *
     * @return
     */
    public Market[] getMarkets() {
        Market[] m = new Market[markets.size()];
        markets.values().toArray(m);
        return m;
    }

    /**
     * Get first market in the list. Handy if we use only one market.
     *
     * @return first market or null if there are no markets
     */
    public Market getFirstMarket() {
        if (markets.isEmpty()) {
            return null;
        }
        return markets.values().iterator().next();
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
        for (CurrencyPair cp : markets.keySet()) {
            if (currency.equalsIgnoreCase(cp.getBaseCurrency())
                    || currency.equalsIgnoreCase(cp.getQuoteCurrency())) {
                matchingMarkets.add(markets.get(cp));
            }
        }
        return matchingMarkets;
    }

    /**
     * Get market for specific currency pair
     *
     * @param cp base and quote currency pair
     * @return the market or null if none found
     */
    public Market getMarket(CurrencyPair cp) {
        return markets.get(cp);
    }

    /**
     * Get market for specific currency pair
     *
     * @param baseCurrency case-insensitive base currency
     * @param quoteCurrency case-insensitive quote currency
     * @return the market or null if none found
     */
    public Market getMarket(String baseCurrency, String quoteCurrency) {
        try {
            CurrencyPair cp = new CurrencyPair(baseCurrency, quoteCurrency);
            return getMarket(cp);
        } catch (InvalidFormatException ex) {
            return null;
        }
    }

    /**
     * Add a new market to the exchange. If a market with exactly the same
     * currency pair already exists, ignore it
     *
     * @param market
     * @return true if the market was added, false otherwise (if it already
     * existed)
     */
    public boolean addMarket(Market market) {
        if (market == null) {
            return false;
        }

        // Check if such market already exists
        CurrencyPair cp = market.getCurrencyPair();
        if (markets.containsKey(cp)) {
            return false;
        }
        markets.put(cp, market);
        return true;
    }

    /**
     * Clear all data from the exchange
     */
    public void clearData() {
        for (Market m : markets.values()) {
            m.clearData();
        }
    }

    /**
     * Delete all trades, in all markets
     */
    public void clearTrades() {
        for (Market m : markets.values()) {
            m.clearTrades();
        }
    }
}
