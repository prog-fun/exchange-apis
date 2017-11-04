package org.progfun;

/**
 * Represents one market order: Bid or Ask Bid: someone wants to buy the base
 * currency Ask: someone wants to sell the base currency
 *
 * Currency pair information is stored in the associated market.
 */
public class Order {

    // TODO - test if double is good enough for prices! Maybe need to store it
    // as an int with fixed precision? Or special Price class?
    private double price;
    private double amount;
    private Integer numOrders;

    /**
     * Create a Bid or Ask order
     *
     * @param price
     * @param amount volume of the order, in base currency units
     * @param numOrders number of real orders that have been aggregated in this
     * record (sometimes there are so many orders that we are interested to only
     * know the aggregated information). Value null, 0 or negative is
     * interpreted as "information not available"
     */
    public Order(double price, double amount, Integer numOrders) {
        this.price = price;
        this.amount = amount;
        this.numOrders = numOrders;
        if (numOrders != null && numOrders <= 0) {
            // Mark as "no order count info available"
            this.numOrders = null;
        }
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(float size) {
        this.amount = size;
    }

    /**
     * Return number of real orders that have been aggregated in this order or
     * null if this information is not available.
     *
     * @return
     */
    public Integer getNumberOfOrders() {
        return numOrders;
    }

    /**
     * Set the number of real orders that have been aggregated in this order.
     *
     * @param numOrders
     */
    public void setNumberOfOrders(Integer numOrders) {
        this.numOrders = numOrders;
        if (numOrders != null && numOrders <= 0) {
            // Mark as "no order count info available"
            this.numOrders = null;
        }
    }

    /**
     * Increase amount and count of orders for this specific price
     *
     * @param amount
     * @param orderCount
     */
    public void increase(double amount, int orderCount) {
        this.amount += amount;
    }

}
