package org.progfun;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.progfun.orderbook.Book;
import org.progfun.orderbook.Listener;
import org.progfun.orderbook.Order;

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

    private final Book bids = new Book();
    private final Book asks = new Book();

    private final List<Trade> trades = new ArrayList<>();

    private final List<Listener> listeners = new ArrayList<>();

    // We use semaphore to lock updates while a snapshot is processed
    private final Semaphore mutex = new Semaphore(1);

    /**
     * Create a new market, convert the currencies to upper-case. Throws
     * exception if one of currencies is null or empty
     *
     * @param baseCurrency Base currency that is traded: Bitcoin (BTC), Litecoin
     * (LTC), etc
     * @param quoteCurrency Currency in which the trade is happening: USD, EUR,
     * NOK, etc
     * @throws InvalidFormatException when one of currencies missing
     */
    public Market(String baseCurrency, String quoteCurrency) throws InvalidFormatException {
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

    public Book getBids() {
        return bids;
    }

    public Book getAsks() {
        return asks;
    }

    /**
     * Add a new bid. If a bid with that price is already registered, the amount
     * and orderCount will be added to it.
     *
     * @param price
     * @param amount how much of the base currency buyer wants to buy
     * @param orderCount how many orders have been aggregated in this bid. Use
     * zero if count is not known.
     */
    public void addBid(double price, double amount, int orderCount) {
        if (!lockUpdates()) {
            return;
        }

        Order bid = new Order(price, amount, orderCount);
        Order updatedBid = bids.add(bid);
        // Notify listeners about changes
        for (Listener l : listeners) {
            if (updatedBid != null) {
                // Check if the update resulted in a delete
                if (updatedBid.getAmount() > 0) {
                    l.bidUpdated(this, updatedBid);
                } else {
                    // Order was actually removed
                    l.bidRemoved(this, price);
                }
            } else {
                l.bidAdded(this, bid);
            }
        }

        allowUpdates();
    }

    /**
     * Add a new ask. If an ask with that price is already registered, the
     * amount and orderCount will be added to it.
     *
     * @param price
     * @param amount how much of the base currency seller wants to sell
     * @param orderCount how many orders have been aggregated in this ask Use
     * zero if count is not known.
     */
    public void addAsk(double price, double amount, int orderCount) {
        if (!lockUpdates()) {
            return;
        }

        Order ask = new Order(price, amount, orderCount);
        Order updatedAsk = asks.add(ask);
        // Notify listeners about changes
        for (Listener l : listeners) {
            if (updatedAsk != null) {
                // Check if the update resulted in a delete
                if (updatedAsk.getAmount() > 0) {
                    l.askUpdated(this, updatedAsk);
                } else {
                    // Order was actually removed
                    l.askRemoved(this, price);
                }
            } else {
                l.askAdded(this, ask);
            }
        }

        allowUpdates();
    }

    /**
     * Remove a bid - all the orders at specific price
     *
     * @param price
     */
    public void removeBid(double price) {
        if (!lockUpdates()) {
            return;
        }

        bids.remove(price);
        // Notify listeners about changes
        for (Listener l : listeners) {
            l.bidRemoved(this, price);
        }

        allowUpdates();
    }

    /**
     * Remove an ask - all the orders at specific price
     *
     * @param price
     */
    public void removeAsk(double price) {
        if (!lockUpdates()) {
            return;
        }

        asks.remove(price);
        // Notify listeners about changes
        for (Listener l : listeners) {
            l.askRemoved(this, price);
        }

        allowUpdates();
    }

    /**
     * Lock market for updates. May be useful to avoid updates while processing
     * a snapshot
     *
     * @return true on success, false if interrupted
     */
    public boolean lockUpdates() {
        try {
            mutex.acquire();
            return true;
        } catch (InterruptedException ex) {
            return false;
        }
    }

    /**
     * Allow updates again
     */
    public void allowUpdates() {
        mutex.release();
    }

    /**
     * Add a new listener, if it is not already registered
     *
     * @param listener
     * @return true if listener was added
     */
    public boolean addListener(Listener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove listener. Return true if it was in the list.
     *
     * @param listener
     * @return
     */
    public boolean removeListener(Listener listener) {
        return listeners.remove(listener);
    }
}
