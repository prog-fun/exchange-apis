package org.progfun;

/**
 * Orderbook with bids and asks
 */
public class Orderbook {
    private Book bids = new Book();
    private Book asks = new Book();

    /**
     * Add a new bid. If a bid with that price is already registered,
     * the amount and orderCount will be added to it.
     * @param price
     * @param amount how much of the base currency buyer wants to buy
     * @param orderCount how many orders have been aggregated in this bid.
     * Use zero if count is not known.
     */
    public void addBid(float price, float amount, int orderCount) {
        bids.add(price, amount, orderCount);
    }
    
    /**
     * Add a new ask. If an ask with that price is already registered,
     * the amount and orderCount will be added to it.
     * @param price
     * @param amount how much of the base currency seller wants to sell
     * @param orderCount how many orders have been aggregated in this ask
     * Use zero if count is not known.
     */
    public void addAsk(float price, float amount, int orderCount) {
        asks.add(price, amount, orderCount);
    }
    

    /**
     * Remove a bid - all the orders at specific price
     * @param price
     */
    public void removeBid(float price) {
        bids.remove(price);
    }
    
    /**
     * Remove an ask - all the orders at specific price
     * @param price
     */
    public void removeAsk(float price) {
        asks.remove(price);
    }
}
