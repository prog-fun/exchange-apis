package org.progfun;

import org.junit.Test;
import org.progfun.orderbook.Book;
import org.progfun.orderbook.Listener;
import org.progfun.orderbook.Order;

import static org.junit.Assert.*;

public class MarketTest {

    static final double DELTA = 0.00000000001;

    /**
     * Test if creating a market with empty base currency throws an exception
     */
    @Test(expected = Exception.class)
    public void testEmptyBaseCurrency() throws Exception {
        Market m = new Market("", "USD");
    }

    /**
     * Test if creating a market with null base currency throws an exception
     */
    @Test(expected = Exception.class)
    public void testNullBaseCurrency() throws Exception {
        Market m = new Market(null, "USD");
    }

    /**
     * Test if creating a market with empty quote currency throws an exception
     */
    @Test(expected = Exception.class)
    public void testEmptyQuoteCurrency() throws Exception {
        Market m = new Market("BTC", "");
    }

    /**
     * Test if creating a market with null quote currency throws an exception
     */
    @Test(expected = Exception.class)
    public void testNullQuoteCurrency() throws Exception {
        Market m = new Market("BTC", null);
    }

    /**
     * Test if creating a market makes the currencies uppercase
     */
    @Test
    public void uppercaseTest() throws Exception {
        Market m = new Market("btc", "usd");
        assertEquals("BTC", m.getBaseCurrency());
        assertEquals("USD", m.getQuoteCurrency());
    }

    @Test
    public void testEmpty() {
        Market m = null;
        try {
            m = new Market("", "");
            fail();
        } catch (InvalidFormatException e) {

        }
        try {
            m = new Market("btc", "usd");
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(0, m.getBids().size());
        assertEquals(0, m.getAsks().size());
    }

    // Test if simple adding works correctly
    @Test
    public void testAdd() {
        Market m = null;
        try {
            m = new Market("btc", "usd");
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            fail();
        }
        final double BID_PRICE = 10.2;
        final double ASK_PRICE = 12.72;
        final double BID_AMOUNT = 7.24;
        final double ASK_AMOUNT = 42;
        m.addBid(BID_PRICE, BID_AMOUNT, 1);
        m.addAsk(ASK_PRICE, ASK_AMOUNT, 1);
        assertEquals(1, m.getBids().size());
        assertEquals(1, m.getAsks().size());
        // Check bid
        Double[] bidPrices = m.getBids().getOrderedPrices(false);
        assertNotNull(bidPrices);
        assertEquals(1, bidPrices.length);
        double bp = bidPrices[0];
        assertEquals(BID_PRICE, bp, 0.001);
        Order b1 = m.getBids().getOrderForPrice(bp);
        assertNotNull(b1);
        assertEquals(BID_AMOUNT, b1.getAmount(), 0.001);
        assertEquals(BID_PRICE, b1.getPrice(), 0.001);
        // Check ask
        Double[] askPrices = m.getAsks().getOrderedPrices(true);
        assertNotNull(askPrices);
        assertEquals(1, bidPrices.length);
        double ap = askPrices[0];
        assertEquals(ASK_PRICE, ap, 0.001);
        Order a1 = m.getAsks().getOrderForPrice(ap);
        assertNotNull(a1);
        assertEquals(ASK_AMOUNT, a1.getAmount(), 0.001);
        assertEquals(ASK_PRICE, a1.getPrice(), 0.001);
    }

    // Test if prices are ordered in correct sequence
    @Test
    public void testOrdering() {
        Market m = null;
        try {
            m = new Market("btc", "usd");
        } catch (InvalidFormatException e) {
            fail();
            e.printStackTrace();
        }
        Book bids = m.getBids();
        Book asks = m.getAsks();

        final double[] BID_PRICES = {7000.25, 7010.0, 6800.0, 6900};
        final double[] ORDERED_BID_PRICES = {7010.0, 7000.25, 6900, 6800.0};

        for (int i = 0; i < BID_PRICES.length; ++i) {
            m.addBid(BID_PRICES[i], 1, 1);
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
        Market m = null;
        try {
            m = new Market("btc", "usd");
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            fail();
        }
        m.addAsk(7000, 2, 5);
        m.addAsk(7100, 2, 5);
        m.addAsk(60.00008, 2, 0);
        m.addAsk(60.00008, 4, 8);
        m.addAsk(60.00008, 6, 0);
        m.addAsk(7000, 2, 5);
        m.addAsk(7000, 0.000003, 0);
        Book asks = m.getAsks();
        assertEquals(3, asks.size());
        Double[] prices = asks.getOrderedPrices(true);

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
        Market m = null;
        try {
            m = new Market("btc", "usd");
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(m);
        DummyListener l = new DummyListener();
        m.addListener(l);
        // Add one new ask
        m.addAsk(5, 2, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 0);
        assertEquals(l.numUpdatedAsks, 0);
        assertEquals(l.numUpdatedBids, 0);
        // Add one new bid with the same price (not possible in real market)
        m.addBid(5, 2, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 0);
        assertEquals(l.numUpdatedBids, 0);
        // Add one more ask with the same price
        m.addAsk(5, 6, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 1);
        assertEquals(l.numUpdatedBids, 0);

        // Add one more bid with the same price
        m.addBid(5, 17, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 1);
        assertEquals(l.numUpdatedBids, 1);

        // Remove ask
        m.removeAsk(5);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 1);
        assertEquals(l.numUpdatedBids, 1);
        assertEquals(l.numDeletedAsks, 1);
        assertEquals(l.numDeletedBids, 0);

        // Remove bid
        m.removeBid(5);
        assertEquals(1, l.numNewAsks, 1);
        assertEquals(1, l.numNewBids, 1);
        assertEquals(1, l.numUpdatedAsks, 1);
        assertEquals(1, l.numUpdatedBids, 1);
        assertEquals(1, l.numDeletedAsks, 1);
        assertEquals(1, l.numDeletedBids, 1);

        // Add ask, then add ask with same price but negative amount -
        // the ask should be reported as removed
        m.addAsk(700, 3, 5);
        assertEquals(2, l.numNewAsks);
        m.addAsk(700, -2, 5);
        assertEquals(2, l.numNewAsks);
        assertEquals(2, l.numUpdatedAsks);
        m.addAsk(700, -2, 5);
        assertEquals(2, l.numNewAsks);
        assertEquals(2, l.numUpdatedAsks);
        assertEquals(2, l.numDeletedAsks);
        assertEquals(0, m.getAsks().size());

        // Add ask, then add ask with same price but negative count -
        // the ask should be reported as updated and count should become 0
        m.addAsk(800, 3, 5);
        assertEquals(3, l.numNewAsks);
        m.addAsk(800, -2, -10);
        assertEquals(3, l.numNewAsks);
        assertEquals(3, l.numUpdatedAsks);
        assertEquals(1, m.getAsks().size());
        Order ask = m.getAsks().getOrderForPrice(800);
        assertNull(ask.getCount());
    }

    // Check if merge with negative amount works correctly
    @Test
    public void testNegativeMerge() throws InvalidFormatException {
        Market m = new Market("btc", "usd");
        Book asks = m.getAsks();
        m.addAsk(7000, 3, 1);
        Order a = asks.getOrderForPrice(7000);
        assertEquals(3, a.getAmount(), DELTA);
        m.addAsk(7000, -1, 1);
        assertEquals(2, a.getAmount(), DELTA);
        m.addAsk(7000, -1, 0);
        assertEquals(1, a.getAmount(), DELTA);
        m.addAsk(7000, -3, 1);
        // Now the ask should be deleted
        assertEquals(0, asks.size());
    }

    /**
     * Test if locking works properly
     *
     * @throws org.progfun.InvalidFormatException never actually throws it
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testLocking() throws InvalidFormatException, InterruptedException {
        Market m = new Market("BTC", "USD");
        m.lockUpdates();
        final int AMOUNT = 10;
        final int PRICE = 15;
        // Try to update the market in another thread
        Runnable r = new Runnable() {
            @Override
            public void run() {
                m.addBid(PRICE, AMOUNT, 1);
            }
        };
        Thread t;
        t = new Thread(r);
        t.start();

        // Wait a while, let the other thread ty to update the market
        Thread.sleep(200);

        assertEquals(0, m.getBids().size());
        m.allowUpdates();

        // Wait a while, let the other thread to update the market
        Thread.sleep(200);
        assertEquals(1, m.getBids().size());

        // Now update the bids without locking - it should update the same bid, 
        // just increase amount
        t = new Thread(r);
        t.start();
        Thread.sleep(200);
        assertEquals(1, m.getBids().size());
        Order o = m.getBids().getOrderForPrice(PRICE);
        assertNotNull(o);
        assertEquals(2 * AMOUNT, o.getAmount(), 0.001);
    }

    /**
     * Test if order limiting by price works
     *
     * @throws org.progfun.InvalidFormatException never throws it
     */
    @Test
    public void testLimitPercent() throws InvalidFormatException {
        Market m = new Market("BTC", "USD");
        Book bids;
        Book asks;
        final Double LIMIT = 10.0;
        final Double BEST_PRICE = 1000.0;

        // Test empty books first
        bids = m.getPriceLimitedBids(-LIMIT);
        asks = m.getPriceLimitedAsks(LIMIT);
        assertEquals(0, bids.size());
        assertEquals(0, asks.size());

        // Now add some values
        double P1 = BEST_PRICE - 10.0;
        double P2 = BEST_PRICE * (100.0 - LIMIT) / 100.0;
        m.addBid(P1, 1, 1);
        m.addBid(BEST_PRICE, 1, 1);
        m.addBid(P2, 1, 1);
        // Add some "out of range" orders
        m.addBid(BEST_PRICE * (100.0 - LIMIT - 1) / 100.0, 1, 1);
        m.addBid(BEST_PRICE * (100.0 - 2 * LIMIT) / 100.0, 1, 1);
        m.addBid(0.0, 1, 1);

        bids = m.getPriceLimitedBids(LIMIT);
        assertEquals(3, bids.size());
        Double[] prices;
        prices = bids.getOrderedPrices(false);
        assertEquals(BEST_PRICE, prices[0]);
        assertEquals(P1, (double) prices[1], 0.1);
        assertEquals(P2, (double) prices[2], 0.1);

        // Now check asks
        double P3 = BEST_PRICE + 10.0;
        double P4 = BEST_PRICE * (100.0 + LIMIT) / 100.0;
        m.addAsk(P3, 1, 1);
        m.addAsk(BEST_PRICE, 1, 1);
        m.addAsk(P4, 1, 1);
        // Add some "out of range" orders
        m.addAsk(BEST_PRICE * (100.0 + LIMIT + 1) / 100.0, 1, 1);
        m.addAsk(BEST_PRICE * (100.0 + 2 * LIMIT) / 100.0, 1, 1);
        m.addAsk(BEST_PRICE * 1000, 1, 1);
        
        asks = m.getPriceLimitedAsks(LIMIT);
        assertEquals(3, asks.size());
        prices = asks.getOrderedPrices(true);
        assertEquals(BEST_PRICE, prices[0]);
        assertEquals(P3, (double) prices[1], 0.1);
        assertEquals(P4, (double) prices[2], 0.1);
        
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
    public void bidAdded(Market market, Order bid) {
        numNewBids++;
    }

    @Override
    public void askAdded(Market market, Order ask) {
        numNewAsks++;
    }

    @Override
    public void bidUpdated(Market market, Order bid) {
        numUpdatedBids++;
    }

    @Override
    public void askUpdated(Market market, Order ask) {
        numUpdatedAsks++;
    }

    @Override
    public void bidRemoved(Market market, double price) {
        numDeletedBids++;
    }

    @Override
    public void askRemoved(Market market, double price) {
        numDeletedAsks++;
    }

}
