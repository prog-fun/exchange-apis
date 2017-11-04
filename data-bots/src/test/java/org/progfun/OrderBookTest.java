package org.progfun;

import org.progfun.orderbook.Orderbook;
import org.progfun.orderbook.Order;
import org.junit.Test;
import static org.junit.Assert.*;
import org.progfun.orderbook.Listener;

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

    @Test
    public void testNotifications() {
        Orderbook ob = new Orderbook();
        DummyListener l = new DummyListener();
        ob.addListener(l);
        // Add one new ask
        ob.addAsk(5, 2, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 0);
        assertEquals(l.numUpdatedAsks, 0);
        assertEquals(l.numUpdatedBids, 0);
        // Add one new bid with the same price (not possible in real market)
        ob.addBid(5, 2, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 0);
        assertEquals(l.numUpdatedBids, 0);
        // Add one more ask with the same price
        ob.addAsk(5, 6, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 1);
        assertEquals(l.numUpdatedBids, 0);
        
        // Add one more bid with the same price
        ob.addBid(5, 17, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 1);
        assertEquals(l.numUpdatedBids, 1);
        
        // Remove ask
        ob.removeAsk(5);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 1);
        assertEquals(l.numUpdatedBids, 1);
        assertEquals(l.numDeletedAsks, 1);
        assertEquals(l.numDeletedBids, 0);

        // Remove bid
        ob.removeBid(5);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 1);
        assertEquals(l.numUpdatedBids, 1);
        assertEquals(l.numDeletedAsks, 1);
        assertEquals(l.numDeletedBids, 1);
    }
}

// Simply counting how many events of each type received
class DummyListener implements Listener {

    int numNewBids = 0;
    int numUpdatedBids = 0;
    int numDeletedBids = 0;
    int numNewAsks = 0;
    int numUpdatedAsks = 0;
    int numDeletedAsks = 0;

    @Override
    public void bidAdded(Order bid) {
        numNewBids++;
    }

    @Override
    public void askAdded(Order ask) {
        numNewAsks++;
    }

    @Override
    public void bidUpdated(Order bid) {
        numUpdatedBids++;
    }

    @Override
    public void askUpdated(Order ask) {
        numUpdatedAsks++;
    }

    @Override
    public void bidRemoved(double price) {
        numDeletedBids++;
    }

    @Override
    public void askRemoved(double price) {
        numDeletedAsks++;
    }

}
