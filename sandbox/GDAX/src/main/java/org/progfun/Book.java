package org.progfun;

import org.progfun.Order;
import java.util.HashMap;
import java.util.Map;

/**
 * One "book" holding either bids or asks
 */
public class Book {
    private Map<Float, Order> orders = new HashMap<>();
    
    /**
     * Add a new bid/ask order. If an order with that price is already registered,
     * the amount and orderCount will be added to it.
     * @param price
     * @param amount
     * @param orderCount 
     */
    void add(float price, float amount, int orderCount) {
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

    void remove(float price) {
        orders.remove(price);
    }

}
