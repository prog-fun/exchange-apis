package org.progfun;

import java.util.Objects;

/**
 * Class represents pair of currencies: Base and Quote currency
 */
public class CurrencyPair {

    private final String baseCurrency;
    private final String quoteCurrency;

    /**
     * @param baseCurrency Base currency that is traded: Bitcoin (BTC), Litecoin
     * (LTC), etc
     * @param quoteCurrency Currency in which the trade is happening: USD, EUR,
     * NOK, etc
     */
    public CurrencyPair(String baseCurrency, String quoteCurrency)
            throws InvalidFormatException {
        if (baseCurrency == null || "".equals(baseCurrency)) {
            throw new InvalidFormatException("Base currency must be specified");
        }
        if (quoteCurrency == null || "".equals(quoteCurrency)) {
            throw new InvalidFormatException("Quote currency must be specified");
        }
        this.baseCurrency = baseCurrency.toUpperCase();
        this.quoteCurrency = quoteCurrency.toUpperCase();
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    /**
     * Comparison of CurrencyPairs
     *
     * @param obj
     * @return true if this currency pair is equal to obj
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CurrencyPair)) {
            return false;
        }
        CurrencyPair cp = (CurrencyPair) obj;
        return this.baseCurrency.equals(cp.baseCurrency)
                && this.quoteCurrency.equals(cp.quoteCurrency);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.baseCurrency);
        hash = 79 * hash + Objects.hashCode(this.quoteCurrency);
        return hash;
    }

    @Override
    public String toString() {
        return "{" + baseCurrency + ", " + quoteCurrency + '}';
    }
    
    
}
