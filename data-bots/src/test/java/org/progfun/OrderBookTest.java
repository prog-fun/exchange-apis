package org.progfun;

import org.progfun.orderbook.Orderbook;
import org.progfun.orderbook.Order;
import org.junit.Test;
import static org.junit.Assert.*;

public class OrderBookTest {

    @Test
    public void testEmpty() {
        Orderbook b = new Orderbook();
        assertEquals(0, b.getBids().size());
        assertEquals(0, b.getAsks().size());
    }

    // Test if simple adding works correctly
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
        // Check bid
        Double[] bidPrices = b.getBids().getOrderedPrices(false);
        assertNotNull(bidPrices);
        assertEquals(1, bidPrices.length);
        double bp = bidPrices[0];
        assertEquals(BID_PRICE, bp, 0.001);
        Order b1 = b.getBids().getOrderForPrice(bp);
        assertNotNull(b1);
        assertEquals(BID_AMOUNT, b1.getAmount(), 0.001);
        assertEquals(BID_PRICE, b1.getPrice(), 0.001);
        // Check ask
        Double[] askPrices = b.getAsks().getOrderedPrices(true);
        assertNotNull(askPrices);
        assertEquals(1, bidPrices.length);
        double ap = askPrices[0];
        assertEquals(ASK_PRICE, ap, 0.001);
        Order a1 = b.getAsks().getOrderForPrice(ap);
        assertNotNull(a1);
        assertEquals(ASK_AMOUNT, a1.getAmount(), 0.001);
        assertEquals(ASK_PRICE, a1.getPrice(), 0.001);
    }
}
