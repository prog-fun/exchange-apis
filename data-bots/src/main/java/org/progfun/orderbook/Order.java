package org.progfun.orderbook;

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
    private Integer count;

    /**
     * Create a Bid or Ask order
     *
     * @param price
     * @param amount volume of the order, in base currency units
     * @param count number of real orders that have been aggregated in this
     * record (sometimes there are so many orders that we are interested to only
     * know the aggregated information). Value null or zero is interpreted as
     * "information not available". Negative count is allowed (negative values
     * can be used as a "relative order change")
     */
    public Order(double price, double amount, Integer count) {
        this.price = price;
        this.amount = amount;
        this.count = count;
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
    public Integer getCount() {
        return count;
    }

    /**
     * Set the number of real orders that have been aggregated in this order.
     *
     * @param count
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Increase amount and count of orders for this specific price
     *
     * @param amount
     * @param count
     */
    public void increase(double amount, Integer count) {
        this.amount += amount;
        if (this.count != null) {
            if (count != null) {
                this.count += count;
                // If count becomes negative, make it null
                if (this.count < 0) {
                    this.count = null;
                }
            }
        } else {
            this.count = count;
        }
    }

    /**
     * Implement content-wise comparison of Orders
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Order)) {
            // This should cover null as well
            return false;
        }
        
        Order o1 = (Order) obj;
        
        if (this.count != null) {
            if (!this.count.equals(o1.count)) {
                return false;
            }
        } else if (o1.count != null) {
            return false;
        }
        
        return this.price == o1.price && this.amount == o1.amount;
    }
}
