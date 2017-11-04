package org.progfun;

import java.util.NavigableSet;
import java.util.TreeMap;

/**
 * One "book" holding either bids or asks
 */
public class Book {

    private final TreeMap<Double, Order> orders = new TreeMap<>();

    /**
     * Add a new bid/ask order. If an order with that price is already
     * registered, the amount and orderCount will be added to it.
     *
     * @param price
     * @param amount
     * @param orderCount
     */
    public void add(double price, double amount, int orderCount) {
        Order o = orders.get(price);
        if (o == null) {
            // First order for this price
            o = new Order(price, amount, orderCount);
            orders.put(price, o);
        } else {
            // Existing order, update amount and count
            o.increase(amount, orderCount);
        }
    }

    /**
     * Remove an order with a specific price
     *
     * @param price
     */
    public void remove(double price) {
        orders.remove(price);
    }

    /**
     * Return number of orders - the different price levels in the book.
     *
     * @return
     */
    public int size() {
        return orders.size();
    }

    /**
     * Get an ordered array of prices
     *
     * @param ascending when true, values are sorted in ascending order,
     * descending otherwise
     * @return n-th highest order or null if index out of bounds
     */
    public Double[] getOrderedPrices(boolean ascending) {
        NavigableSet<Double> ns = ascending ? orders.navigableKeySet()
                : orders.descendingKeySet();
        Double[] prices = new Double[ns.size()];
        ns.toArray(prices);
        return prices;
    }

    /**
     * Get order for specific price or null if it does not exist
     *
     * @param price
     * @return
     */
    public Order getOrderForPrice(double price) {
        return orders.get(price);
    }

}
