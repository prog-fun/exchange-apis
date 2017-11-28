package org.progfun.orderbook;

import java.math.BigDecimal;

/**
 * Represents one market order: Bid or Ask Bid: someone wants to buy the base
 * currency Ask: someone wants to sell the base currency
 *
 * Currency pair information is stored in the associated market.
 */
public class Order {

    private BigDecimal price;
    private BigDecimal amount;
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
    public Order(BigDecimal price, BigDecimal amount, Integer count) {
        this.price = price;
        this.amount = amount;
        this.count = count;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
    public void increase(BigDecimal amount, Integer count) {
        this.amount.add(amount);
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
        
        return this.price.equals(o1.price)
                && this.amount.equals(o1.amount);
    }
    
    /**
     * Meaningful representation of order as a string
     * @return 
     */
    @Override
    public String toString() {
        return "[" + price + ", " + amount + ", " + count + "]";
    }
}
