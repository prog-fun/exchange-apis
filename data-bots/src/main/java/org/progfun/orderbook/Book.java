package org.progfun.orderbook;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeMap;

/**
 * One "book" holding either bids or asks. If iterator interface used, the
 * ordering is undefined. Use other methods to get orders sorted by price.
 */
public class Book implements Iterable<Order> {

    private final TreeMap<BigDecimal, Order> orders = new TreeMap<>();

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
            if (o.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                orders.remove(o.getPrice());
                o.setAmount(BigDecimal.ZERO);
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
    public void remove(BigDecimal price) {
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
    public BigDecimal[] getOrderedPrices(boolean ascending) {
        NavigableSet<BigDecimal> ns = ascending ? orders.navigableKeySet()
                : orders.descendingKeySet();
        BigDecimal[] prices = new BigDecimal[ns.size()];
        ns.toArray(prices);
        return prices;
    }

    /**
     * Get order for specific price or null if it does not exist
     *
     * @param price
     * @return
     */
    public Order getOrderForPrice(BigDecimal price) {
        return orders.get(price);
    }

    /**
     * Get order for specific price or null if it does not exist
     *
     * @param price
     * @return
     */
    public Order getOrderForPrice(String price) {
        return getOrderForPrice(new BigDecimal(price));
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
        NavigableSet<BigDecimal> ns = ascending ? orders.navigableKeySet()
                : orders.descendingKeySet();
        BigDecimal bestPrice = ns.first();
        double limit = (ascending ? 1 : -1) * limitPercent;
        BigDecimal threshold = getPriceThreshold(bestPrice, limit);

        for (BigDecimal price : ns) {
            if ((ascending && price.compareTo(threshold) > 0)
                    || (!ascending && price.compareTo(threshold) < 0)) {
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
    public static BigDecimal getPriceThreshold(BigDecimal bestPrice, double limitPercent) {
        return bestPrice.multiply(new BigDecimal((100.0 + limitPercent) / 100.0));
    }

    @Override
    public Iterator<Order> iterator() {
        return orders.values().iterator();
    }

    /**
     * Implement our own equality function: two books are equal as long
     * as all their orders are equal
     * @param obj
     * @return true if books equal, false if some orders differ, or comparing
     * to a non-Book object
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Book)) {
            // This should cover null as well
            return false;
        }
        Book b1 = (Book) obj;
        if (b1.size() != this.size()) {
            return false;
        }
        
        if (this.size() == 0) {
            return true;
        }
        
        BigDecimal[] p1 = b1.getOrderedPrices(true);
        BigDecimal[] p2 = this.getOrderedPrices(true);
        // Prices may have "some fluctuation", compare with care
        for (int i = 0; i < p1.length; ++i) {
            if (!p1[i].equals(p2[i])) {
                return false;
            }
        }
        for (int i = 0; i < p1.length; ++i) {
            BigDecimal price = p1[i];
            Order o1 = b1.getOrderForPrice(price);
            Order o2 = this.getOrderForPrice(price);
            if (!o1.equals(o2)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Print meaningful book representation
     * @return 
     */
    @Override
    public String toString() {
        String res = "";
        // Limit to first few orders
        int i = 0;
        Iterator<Order> it = iterator();
        while (it.hasNext() && i++ < 5) {
            Order o = it.next();
            res += o.toString() + "; ";
        }
        return res;
    }
}
