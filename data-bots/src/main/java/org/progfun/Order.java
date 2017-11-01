package org.progfun;

/**
 * Represents one market order: Bid or Ask Bid: someone wants to buy the base
 * currency Ask: someone wants to sell the base currency
 *
 * Currency pair information is stored in the associated market.
 */
public class Order {

    private float price;
    private float size;
    private Integer numOrders;
    public boolean bid;

    /**
     * Create a Bid or Ask order
     *
     * @param price
     * @param size volume of the order, in base currency units
     * @param numOrders number of real orders that have been aggregated in this
     * record (sometimes there are so many orders that we are interested to only
     * know the aggregated information). Value null, 0 or negative is
     * interpreted as "information not available"
     * @param bid when true, this is a Bid order, when false: Ask order
     */
    public Order(float price, float size, Integer numOrders, boolean bid) {
        this.price = price;
        this.size = size;
        this.numOrders = numOrders;
        if (numOrders != null && numOrders <= 0) {
            // Mark as "no order count info available"
            this.numOrders = null;
        }
        this.bid = bid;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
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
     * @param numOrders 
     */
    public void setNumberOfOrders(Integer numOrders) {
        this.numOrders = numOrders;
        if (numOrders != null && numOrders <= 0) {
            // Mark as "no order count info available"
            this.numOrders = null;
        }
    }

}
