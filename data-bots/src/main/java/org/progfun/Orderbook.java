package org.progfun;

import java.util.ArrayList;
import java.util.List;

/**
 * Orderbook with bids and asks
 */
public class Orderbook {

    private final Book bids = new Book();
    private final Book asks = new Book();

    private final List<OrderbookListener> listeners = new ArrayList<>();

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
        bids.add(price, amount, orderCount);
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
        asks.add(price, amount, orderCount);
    }

    /**
     * Remove a bid - all the orders at specific price
     *
     * @param price
     */
    public void removeBid(double price) {
        bids.remove(price);
    }

    /**
     * Remove an ask - all the orders at specific price
     *
     * @param price
     */
    public void removeAsk(double price) {
        asks.remove(price);
    }

    /**
     * Add a new listener, if it is not already registered
     *
     * @param listener
     */
    public void addListener(OrderbookListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove listener. Return true if it was in the list.
     *
     * @param listener
     * @return
     */
    public boolean removeListener(OrderbookListener listener) {
        return listeners.remove(listener);
    }

}
