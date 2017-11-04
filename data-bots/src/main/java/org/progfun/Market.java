package org.progfun;

import org.progfun.orderbook.Order;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a market trading a pair of assets (Currencies). Example markets:
 * BTC/USD (trading Bitcoin for US Dollar), ETH/EUR, EUR/USD (Exchanging EUR and
 * USD currencies), etc.
 *
 * Market is always a place of exchange for two assets: base asset and quote
 * asset We simplify it by assuming that we always trade one "weird currency"
 * called base currency, and one "normal currency" called quote currency. I.e.,
 * we can buy Bitcoin for US Dollars, and then sell the Bitcoin for US Dollars.
 * Bitcoin would be the base currency that we are trading while US Dollar is
 * used as a quote currency.
 */
public class Market {

    private final String baseCurrency;
    private final String quoteCurrency;
    private final List<Order> orders = new ArrayList<>();
    private final List<Trade> trades = new ArrayList<>();

    /**
     * Create a new market, convert the currencies to upper-case. Throws
     * exception if one of currencies is null or empty
     *
     * @param baseCurrency Base currency that is traded: Bitcoin (BTC), Litecoin
     * (LTC), etc
     * @param quoteCurrency Currency in which the trade is happening: USD, EUR,
     * NOK, etc
     * @throws java.lang.Exception when one of currencies missing
     */
    public Market(String baseCurrency, String quoteCurrency) throws Exception {
        if (baseCurrency == null || "".equals(baseCurrency)) {
            throw new Exception("Base currency must be specified");
        }
        if (quoteCurrency == null || "".equals(quoteCurrency)) {
            throw new Exception("Quote currency must be specified");
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

}
