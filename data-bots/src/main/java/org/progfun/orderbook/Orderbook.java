package org.progfun.orderbook;

import java.util.ArrayList;
import java.util.List;

/**
 * Orderbook with bids and asks
 */
public class Orderbook {

    private final Book bids = new Book();
    private final Book asks = new Book();

    private final List<Listener> listeners = new ArrayList<>();

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
        Order bid = new Order(price, amount, orderCount);
        Order updatedBid = bids.add(bid);
        // Notify listeners about changes
        for (Listener l : listeners) {
            if (updatedBid != null) {
                l.bidUpdated(updatedBid);
            } else {
                l.bidAdded(bid);
            }
        }
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
        Order ask = new Order(price, amount, orderCount);
        Order updatedAsk = asks.add(ask);
        // Notify listeners about changes
        for (Listener l : listeners) {
            if (updatedAsk != null) {
                l.askUpdated(updatedAsk);
            } else {
                l.askAdded(ask);
            }
        }
    }

    /**
     * Remove a bid - all the orders at specific price
     *
     * @param price
     */
    public void removeBid(double price) {
        bids.remove(price);
        // Notify listeners about changes
        for (Listener l : listeners) {
            l.bidRemoved(price);
        }
    }

    /**
     * Remove an ask - all the orders at specific price
     *
     * @param price
     */
    public void removeAsk(double price) {
        asks.remove(price);
        // Notify listeners about changes
        for (Listener l : listeners) {
            l.askRemoved(price);
        }
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
