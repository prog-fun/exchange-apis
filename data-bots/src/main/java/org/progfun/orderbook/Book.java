package org.progfun.orderbook;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeMap;

/**
 * One "book" holding either bids or asks.
 * If iterator interface used, the ordering is undefined.
 * Use other methods to get orders sorted by price.
 */
public class Book implements Iterable<Order> {

    private final TreeMap<Double, Order> orders = new TreeMap<>();

    /**
     * Add a new bid/ask order. If an order with that price is already
     * registered, the amount and orderCount will be added to it. The amount can
     * be negative - meaning that the order has shrunk in size.
     *
     * @param order the new order to add to the book
     * @return if this was a new order for the specific price, return null; if
     * an order with that price was already registered and is now updated,
     * return order with updated amount and count; If amount becomes negative,
     * the order is removed from the book and an order with this price and
     * amount=0, count=0 is returned If count becomes negative, count is reset
     * to null if order == null return null
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

    /**
     * Return a copy of the orders, limited by price
     *
     * @param limitPercent how many percent away from the best price the
     * threshold will be. 
     * @param ascending when true, lowest prices are included (asks). When
     * false, highest prices are included (bids).
     * @return
     */
    public Book getPriceLimitedOrders(double limitPercent, boolean ascending) {
        Book b = new Book();
        if (orders.isEmpty()) {
            return b;
        }
        
        // Get the best price and calculate threshold
        NavigableSet<Double> ns = ascending ? orders.navigableKeySet()
                : orders.descendingKeySet();
        Double bestPrice = ns.first();   
        double limit = (ascending ? 1 : -1) * limitPercent;
        double threshold = getPriceThreshold(bestPrice, limit);
        
        for (Double price : ns) {
            if ((ascending && price > threshold) 
                    || (!ascending && price < threshold)) {
                // Threshold reached
                break;
            }
            b.add(orders.get(price));
        }
        
        return b;
    }

    /**
     * Calculate the price threshold - N percent away from the best price
     *
     * @param bestPrice
     * @param limitPercent how many percent away from the best price the
     * threshold can be. Use negative value for bids, positive for asks
     * @return
     */
    public static double getPriceThreshold(double bestPrice, double limitPercent) {
        return bestPrice + (bestPrice * limitPercent) / 100.0;
    }

    @Override
    public Iterator<Order> iterator() {
        return orders.values().iterator();
    }
}
