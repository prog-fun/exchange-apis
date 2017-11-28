package org.progfun;

import java.math.BigDecimal;
import org.junit.Test;
import org.progfun.orderbook.Book;
import org.progfun.orderbook.Listener;
import org.progfun.orderbook.Order;

import static org.junit.Assert.*;

public class MarketTest {

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
        final BigDecimal BID_PRICE = new BigDecimal(10.2);
        final BigDecimal ASK_PRICE = new BigDecimal(12.72);
        final BigDecimal BID_AMOUNT = new BigDecimal(7.24);
        final BigDecimal ASK_AMOUNT = new BigDecimal(42);
        m.addBid(BID_PRICE, BID_AMOUNT, 1);
        m.addAsk(ASK_PRICE, ASK_AMOUNT, 1);
        assertEquals(1, m.getBids().size());
        assertEquals(1, m.getAsks().size());
        // Check bid
        BigDecimal[] bidPrices = m.getBids().getOrderedPrices(false);
        assertNotNull(bidPrices);
        assertEquals(1, bidPrices.length);
        BigDecimal bp = bidPrices[0];
        assertTrue(BID_PRICE.equals(bp));
        Order b1 = m.getBids().getOrderForPrice(bp);
        assertNotNull(b1);
        assertEquals(BID_AMOUNT, b1.getAmount());
        assertTrue(BID_PRICE.equals(b1.getPrice()));
        // Check ask
        BigDecimal[] askPrices = m.getAsks().getOrderedPrices(true);
        assertNotNull(askPrices);
        assertEquals(1, bidPrices.length);
        BigDecimal ap = askPrices[0];
        assertEquals(ASK_PRICE, ap);
        Order a1 = m.getAsks().getOrderForPrice(ap);
        assertNotNull(a1);
        assertEquals(ASK_AMOUNT, a1.getAmount());
        assertEquals(ASK_PRICE, a1.getPrice());
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
            m.addBid(new BigDecimal(BID_PRICES[i]), BigDecimal.ONE, 1);
        }

        // Check if all orders are added
        assertEquals(BID_PRICES.length, bids.size());

        // Check if the prices are ordered correctly
        BigDecimal[] orderedBids = bids.getOrderedPrices(false);
        assertEquals(BID_PRICES.length, orderedBids.length);

        // Create etalon ordering
        for (int i = 0; i < orderedBids.length; ++i) {
            assertEquals(orderedBids[i], ORDERED_BID_PRICES[i]);
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
        BigDecimal P1 = new BigDecimal(7000);
        BigDecimal P2 = new BigDecimal(7100);
        BigDecimal P3 = new BigDecimal(60.00008);
        BigDecimal A1 = new BigDecimal(2);
        BigDecimal A2 = new BigDecimal(4);
        BigDecimal A3 = new BigDecimal(6);
        BigDecimal A4 = new BigDecimal(0.000003);
        m.addAsk(P1, A1, 5);
        m.addAsk(P2, A1, 5);
        m.addAsk(P3, A1, 0);
        m.addAsk(P3, A2, 8);
        m.addAsk(P3, A3, 0);
        m.addAsk(P1, A1, 5);
        m.addAsk(P1, A4, 0);
        Book asks = m.getAsks();
        assertEquals(3, asks.size());
        BigDecimal[] prices = asks.getOrderedPrices(true);

        assertEquals(P3, prices[0]);
        assertEquals(P1, prices[1]);
        assertEquals(P2, prices[2]);

        Order o1 = asks.getOrderForPrice(P1);
        BigDecimal A1_1_4 = new BigDecimal(4.000003);
        assertEquals(A1_1_4, o1.getAmount());
        assertEquals(BigDecimal.TEN, o1.getCount());
        Order o2 = asks.getOrderForPrice(P2);
        assertEquals(A1, o2.getAmount());
        assertEquals(5, (int) o2.getCount());
        Order o3 = asks.getOrderForPrice(P3);
        BigDecimal A123 = new BigDecimal(12);
        assertEquals(A123, o3.getAmount());
        assertEquals(8, (int) o3.getCount());
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
        BigDecimal D5 = new BigDecimal(5);
        BigDecimal D2 = new BigDecimal(2);
        BigDecimal D3 = new BigDecimal(3);
        BigDecimal D6 = new BigDecimal(6);
        BigDecimal D17 = new BigDecimal(17);
        BigDecimal D700 = new BigDecimal(700);
        BigDecimal D800 = new BigDecimal(800);
        BigDecimal DM2 = new BigDecimal(-2);

        m.addAsk(D5, D2, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 0);
        assertEquals(l.numUpdatedAsks, 0);
        assertEquals(l.numUpdatedBids, 0);
        // Add one new bid with the same price (not possible in real market)
        m.addBid(D5, D2, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 0);
        assertEquals(l.numUpdatedBids, 0);
        // Add one more ask with the same price
        m.addAsk(D5, D6, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 1);
        assertEquals(l.numUpdatedBids, 0);

        // Add one more bid with the same price
        m.addBid(D5, D17, 0);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 1);
        assertEquals(l.numUpdatedBids, 1);

        // Remove ask
        m.removeAsk(D5);
        assertEquals(l.numNewAsks, 1);
        assertEquals(l.numNewBids, 1);
        assertEquals(l.numUpdatedAsks, 1);
        assertEquals(l.numUpdatedBids, 1);
        assertEquals(l.numDeletedAsks, 1);
        assertEquals(l.numDeletedBids, 0);

        // Remove bid
        m.removeBid(D5);
        assertEquals(1, l.numNewAsks, 1);
        assertEquals(1, l.numNewBids, 1);
        assertEquals(1, l.numUpdatedAsks, 1);
        assertEquals(1, l.numUpdatedBids, 1);
        assertEquals(1, l.numDeletedAsks, 1);
        assertEquals(1, l.numDeletedBids, 1);

        // Add ask, then add ask with same price but negative amount -
        // the ask should be reported as removed
        m.addAsk(D700, D3, 5);
        assertEquals(2, l.numNewAsks);
        m.addAsk(D700, DM2, 5);
        assertEquals(2, l.numNewAsks);
        assertEquals(2, l.numUpdatedAsks);
        m.addAsk(D700, DM2, 5);
        assertEquals(2, l.numNewAsks);
        assertEquals(2, l.numUpdatedAsks);
        assertEquals(2, l.numDeletedAsks);
        assertEquals(0, m.getAsks().size());

        // Add ask, then add ask with same price but negative count -
        // the ask should be reported as updated and count should become 0
        m.addAsk(D800, D3, 5);
        assertEquals(3, l.numNewAsks);
        m.addAsk(D800, DM2, -10);
        assertEquals(3, l.numNewAsks);
        assertEquals(3, l.numUpdatedAsks);
        assertEquals(1, m.getAsks().size());
        Order ask = m.getAsks().getOrderForPrice("800");
        assertNull(ask.getCount());
    }

    // Check if merge with negative amount works correctly
    @Test
    public void testNegativeMerge() throws InvalidFormatException {
        Market m = new Market("btc", "usd");
        Book asks = m.getAsks();
        m.addAsk("7000", "3", 1);
        Order a = asks.getOrderForPrice("7000");
        assertEquals(3, a.getAmount());
        m.addAsk("7000", "-1", 1);
        assertEquals(2, a.getAmount());
        m.addAsk("7000", "-1", 0);
        assertEquals(1, a.getAmount());
        m.addAsk("7000", "-3", 1);
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
        final BigDecimal AMOUNT = BigDecimal.TEN;
        final BigDecimal PRICE = new BigDecimal(15);
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
        assertEquals(AMOUNT.multiply(new BigDecimal(2)), o.getAmount());
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
        final double LIMIT = 10.0;
        final Double BEST_PRICE = 1000.0;
        BigDecimal BPD = new BigDecimal(BEST_PRICE);

        // Test empty books first
        bids = m.getPriceLimitedBids(-LIMIT);
        asks = m.getPriceLimitedAsks(LIMIT);
        assertEquals(0, bids.size());
        assertEquals(0, asks.size());

        // Now add some values
        double P1 = BEST_PRICE - 10.0;
        BigDecimal P1D = new BigDecimal(P1);
        double P2 = BEST_PRICE * (100.0 - LIMIT) / 100.0;
        BigDecimal P2D = new BigDecimal(P2);
        m.addBid(P1D, BigDecimal.ONE, 1);
        m.addBid(BPD, BigDecimal.ONE, 1);
        m.addBid(P2D, BigDecimal.ONE, 1);
        // Add some "out of range" orders
        double OUT_PRICE1 = BEST_PRICE * (100.0 - LIMIT - 1) / 100.0;
        double OUT_PRICE2 = BEST_PRICE * (100.0 - 2 * LIMIT) / 100.0;
        BigDecimal OP1D = new BigDecimal(OUT_PRICE1);
        BigDecimal OP2D = new BigDecimal(OUT_PRICE2);
        m.addBid(OP1D, BigDecimal.ONE, 1);
        m.addBid(OP2D, BigDecimal.ONE, 1);
        m.addBid(BigDecimal.ZERO, BigDecimal.ONE, 1);

        bids = m.getPriceLimitedBids(LIMIT);
        assertEquals(3, bids.size());
        BigDecimal[] prices;
        prices = bids.getOrderedPrices(false);
        assertEquals(BEST_PRICE, prices[0]);
        assertEquals(P1D, prices[1]);
        assertEquals(P2D, prices[2]);

        // Now check asks
        double P3 = BEST_PRICE + 10.0;
        double P4 = BEST_PRICE * (100.0 + LIMIT) / 100.0;
        BigDecimal P3D = new BigDecimal(P3);
        BigDecimal P4D = new BigDecimal(P4);
        m.addAsk(P3D, BigDecimal.ONE, 1);
        m.addAsk(BPD, BigDecimal.ONE, 1);
        m.addAsk(P4D, BigDecimal.ONE, 1);
        // Add some "out of range" orders
        double OUT_PRICE3 = BEST_PRICE * (100.0 + LIMIT + 1) / 100.0;
        double OUT_PRICE4 = BEST_PRICE * (100.0 + 2 * LIMIT) / 100.0;
        double OUT_PRICE5 = BEST_PRICE * 1000;
        m.addAsk(new BigDecimal(OUT_PRICE3), BigDecimal.ONE, 1);
        m.addAsk(new BigDecimal(OUT_PRICE4), BigDecimal.ONE, 1);
        m.addAsk(new BigDecimal(OUT_PRICE5), BigDecimal.ONE, 1);

        asks = m.getPriceLimitedAsks(LIMIT);
        assertEquals(3, asks.size());
        prices = asks.getOrderedPrices(true);
        assertEquals(BEST_PRICE, prices[0]);
        assertEquals(P3D, prices[1]);
        assertEquals(P4D, prices[2]);

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
    public void bidRemoved(Market market, BigDecimal price) {
        numDeletedBids++;
    }

    @Override
    public void askRemoved(Market market, BigDecimal price) {
        numDeletedAsks++;
    }

}
