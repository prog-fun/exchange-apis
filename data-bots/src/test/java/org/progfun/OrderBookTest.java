package org.progfun;

import org.progfun.orderbook.Orderbook;
import org.progfun.orderbook.Order;
import org.junit.Test;
import static org.junit.Assert.*;
import org.progfun.orderbook.Book;
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

    // Test if prices are ordered in correct sequence
    @Test
    public void testOrdering() {
        Orderbook ob = new Orderbook();
        Book bids = ob.getBids();
        Book asks = ob.getAsks();

        final double[] BID_PRICES = {7000.25, 7010.0, 6800.0, 6900};
        final double[] ORDERED_BID_PRICES = {7010.0, 7000.25, 6900, 6800.0};

        for (int i = 0; i < BID_PRICES.length; ++i) {
            ob.addBid(BID_PRICES[i], 1, 1);
        }

        // Check if all orders are added
        assertEquals(BID_PRICES.length, bids.size());

        // Check if the prices are ordered correctly
        Double[] orderedBids = bids.getOrderedPrices(false);
        assertEquals(BID_PRICES.length, orderedBids.length);

        // Create etalon ordering
        for (int i = 0; i < orderedBids.length; ++i) {
            assertEquals(orderedBids[i], ORDERED_BID_PRICES[i], 0.00000000001);
        }
    }

    // Check if two orders with the same price are fused together correctly
    @Test
    public void testMerging() {
        Orderbook ob = new Orderbook();
        ob.addAsk(7000, 2, 5);
        ob.addAsk(7100, 2, 5);
        ob.addAsk(60.00008, 2, 0);
        ob.addAsk(60.00008, 4, 8);
        ob.addAsk(60.00008, 6, 0);
        ob.addAsk(7000, 2, 5);
        ob.addAsk(7000, 0.000003, 0);
        Book asks = ob.getAsks();
        assertEquals(3, asks.size());
        Double[] prices = asks.getOrderedPrices(true);
        final double DELTA = 0.00000000001;
        
        assertEquals(60.00008, prices[0], DELTA);
        assertEquals(7000, prices[1], DELTA);
        assertEquals(7100, prices[2], DELTA);

        Order o1 = asks.getOrderForPrice(7000);
        assertEquals(4.000003, o1.getAmount(), DELTA);
        assertEquals(10, o1.getCount(), DELTA);
        Order o2 = asks.getOrderForPrice(7100);
        assertEquals(2, o2.getAmount(), DELTA);
        assertEquals(5, o2.getCount(), DELTA);
        Order o3 = asks.getOrderForPrice(60.00008);
        assertEquals(12, o3.getAmount(), DELTA);
        assertEquals(8, o3.getCount(), DELTA);
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
