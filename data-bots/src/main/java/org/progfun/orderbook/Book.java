package org.progfun.orderbook;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeMap;
import org.progfun.Decimal;

/**
 * One "book" holding either bids or asks. If iterator interface used, the
 * ordering is undefined. Use other methods to get orders sorted by price.
 */
public class Book implements Iterable<Order> {

    private final TreeMap<Decimal, Order> orders = new TreeMap<>();

    /**
     * Add a new bid/ask order. If an order with that price is already
     * registered, the amount and orderCount will be added to it. The amount can
     * be negative - meaning that the order has shrunk in size.
     *
     * Warning: if you try to add an order with zero amount, it will succeed!
     *
     * @param order the new order to add to the book
     * @param increment when this is true, the amount and order will be treated
     * as increment if an order for that price already exists. When false,
     * these values will be seen as "new values" and instead of increment will
     * be used as replacement for old values.
     * @return if this was a new order for the specific price, return null; if
     * an order with that price was already registered and is now updated,
     * return order with updated amount and count; If amount becomes negative,
     * the order is removed from the book and an order with this price and
     * amount=0, count=0 is returned If count becomes negative, count is reset
     * to null if order == null return null
     */
    public Order add(Order order, boolean increment) {
        if (order == null) {
            return null;
        }

        Order o = orders.get(order.getPrice());
        if (o == null) {
            // First order for this price
            orders.put(order.getPrice(), order);
            return null;
        } else {
            // Existing order
            if (increment) {
                // increment amount and count
                o.increase(order.getAmount(), order.getCount());
            } else {
                // replace the amount and count
                o.setAmount(order.getAmount());
                o.setCount(order.getCount());
            }
            // Check if amount became zero, then we remove the order
            if (!o.getAmount().isPositive()) {
                orders.remove(o.getPrice());
                o.setAmount(Decimal.ZERO);
                o.setCount(null);
            }
            return o;
        }
    }

    /**
     * Wrapper for add(), with increment=true
     * See documentation of add(increment)
     *
     * @param order
     * @return
     */
    public Order add(Order order) {
        return add(order, true);
    }

    /**
     * Remove an order with a specific price
     *
     * @param price
     */
    public void remove(Decimal price) {
        orders.remove(price);
    }

    /**
     * Clear all orders from the book
     */
    public void clear() {
        orders.clear();
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
    public Decimal[] getOrderedPrices(boolean ascending) {
        NavigableSet<Decimal> ns = ascending ? orders.navigableKeySet()
                : orders.descendingKeySet();
        Decimal[] prices = new Decimal[ns.size()];
        ns.toArray(prices);
        return prices;
    }

    /**
     * Get order for specific price or null if it does not exist
     *
     * @param price
     * @return
     */
    public Order getOrderForPrice(Decimal price) {
        return orders.get(price);
    }

    /**
     * Get order for specific price or null if it does not exist
     *
     * @param price
     * @return
     */
    public Order getOrderForPrice(String price) {
        return getOrderForPrice(new Decimal(price));
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
        NavigableSet<Decimal> ns = ascending ? orders.navigableKeySet()
                : orders.descendingKeySet();
        Decimal bestPrice = ns.first();
        double limit = (ascending ? 1 : -1) * limitPercent;
        Decimal threshold = getPriceThreshold(bestPrice, limit);

        for (Decimal price : ns) {
            if ((ascending && price.isGreaterThan(threshold))
                    || (!ascending && price.isSmallerThan(threshold))) {
                // Threshold reached
                break;
            }
            b.add(orders.get(price), false);
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
    public static Decimal getPriceThreshold(Decimal bestPrice, double limitPercent) {
        return bestPrice.multiply(new Decimal((100.0 + limitPercent) / 100.0));
    }

    @Override
    public Iterator<Order> iterator() {
        return orders.values().iterator();
    }

    /**
     * Implement our own equality function: two books are equal as long
     * as all their orders are equal
     *
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

        Decimal[] p1 = b1.getOrderedPrices(true);
        Decimal[] p2 = this.getOrderedPrices(true);
        // Prices may have "some fluctuation", compare with care
        for (int i = 0; i < p1.length; ++i) {
            if (!p1[i].equals(p2[i])) {
                return false;
            }
        }
        for (int i = 0; i < p1.length; ++i) {
            Decimal price = p1[i];
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
     *
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

    /**
     * Get the first order
     *
     * @param ascending when true - return order with the lowest price,
     * otherwise return order with the highest price
     * @return
     */
    public Order getFirstOrder(boolean ascending) {
        if (orders.isEmpty()) {
            return null;
        }
        NavigableSet<Decimal> ns = ascending ? orders.navigableKeySet()
                : orders.descendingKeySet();
        Decimal bestPrice = ns.first();
        return getOrderForPrice(bestPrice);
    }
}
