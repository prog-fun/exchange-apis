package org.progfun;

import org.junit.Test;
import static org.junit.Assert.*;

public class OrderBookTests {

    @Test
    public void testEmpty() {
        Orderbook b = new Orderbook();
        assertEquals(0, b.getBids().size());
        assertEquals(0, b.getAsks().size());
    }

    @Test
    public void testAdd() {
        Orderbook b = new Orderbook();
        final double BID_PRICE = 10.2;
        final double ASK_PRICE = 12.72;
        final double BID_AMOUNT = 7.24;
        final double ASK_AMOUNT = 42;
        b.addBid(BID_PRICE, BID_AMOUNT, 1);
        b.addAsk(ASK_PRICE, ASK_AMOUNT, 1);
        assertEquals(1, b.getBids().size());
        assertEquals(1, b.getAsks().size());
        Double[] bidPrices = b.getBids().getOrderedPrices(false);
        assertNotNull(bidPrices);
        double price = bidPrices[0];
        assertEquals(1, bidPrices.length);
        assertEquals(BID_PRICE, price, 0.001);
        Order b1 = b.getBids().getOrderForPrice(price);
        assertNotNull(b1);
        assertEquals(BID_AMOUNT, b1.getAmount(), 0.001);
        assertEquals(BID_PRICE, b1.getPrice(), 0.001);
    }
}
