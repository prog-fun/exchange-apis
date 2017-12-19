package org.progfun;

import java.util.Date;

/**
 * Represents one historical trade
 */
public class Trade {

    private final Date time;
    private final Decimal price;
    private final Decimal amount;
    // Was buyer the market maker? (Bid came before ask)
    private final boolean sellSide;

    /**
     * Create a new Trade
     *
     * @param time timestamp of the trade
     * @param price
     * @param amount
     * @param sellSide when true, buyer was the market maker and seller was
     * market taker, i.e, bid came first as a "Limit order", and this was
     * actually a "Market Sell" order
     */
    public Trade(Date time, Decimal price, Decimal amount, boolean sellSide) {
        this.time = time;
        this.price = price;
        this.amount = amount;
        this.sellSide = sellSide;
    }

    public Date getTime() {
        return time;
    }

    public Decimal getPrice() {
        return price;
    }

    public Decimal getAmount() {
        return amount;
    }

    /**
     * Return true if this was a Sell-side order, i.e., Limit Buy order came
     * first as a Bid and someone made a Market Sell order.
     *
     * @return
     */
    public boolean isSellSide() {
        return sellSide;
    }

}
