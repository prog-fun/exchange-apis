package org.progfun.orderbook;

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
     * The amount can be negative - meaning that the order has shrunk in size.
     *
     * @param order the new order to add to the book
     * @return if this was a new order for the specific price, return null;
     * if an order with that price was already registered and is
     * now updated, return order with updated amount and count;
     * If amount becomes negative, the order is removed from the book and
     * an order with this price and amount=0, count=0 is returned
     * If count becomes negative, count is reset to null
     * if order == null return null
     */
    public Order add(Order order) {
        if (order == null) {
            return null;
        }

        Order o = orders.get(order.getPrice());
        if (o == null) {
            // First order for this price
            orders.put(order.getPrice(), order);
            return null;
        } else {
            // Existing order, update amount and count
            o.increase(order.getAmount(), order.getCount());
            // Check if amount became zero, then we remove the order
            if (o.getAmount() <= 0) {
                orders.remove(o.getPrice());
                o.setAmount(0);
                o.setCount(null);
            }
            return o;
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
