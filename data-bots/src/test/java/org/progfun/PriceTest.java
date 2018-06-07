package org.progfun;

import org.junit.Test;

import static org.junit.Assert.*;
import org.progfun.price.PriceCandle;
import org.progfun.price.Prices;

public class PriceTest {

    /**
     * Test if close time calculation is correct
     */
    @Test
    public void testEmptyBaseCurrency() throws Exception {
        long t = (System.currentTimeMillis() / 1000) * 1000; // Round to seconds
        // Close time should be open time for the next candle        
        PriceCandle p = new PriceCandle(t, Decimal.ONE, Decimal.ZERO,
                Decimal.ONE, Decimal.ZERO, Decimal.ONE, 1);
        assertEquals(t, p.getOpenTime());
        assertEquals(t + 60000, p.getCloseTime());

        // 15 minute candle
        int resMin = PriceCandle.resolutionToMinutes(Channel.PRICES_15MIN);
        p = new PriceCandle(t, Decimal.ONE, Decimal.ZERO, Decimal.ONE,
                Decimal.ZERO, Decimal.ONE, resMin);
        assertEquals(t, p.getOpenTime());
        assertEquals(t + 15 * 60000, p.getCloseTime());

        // 1h candle
        resMin = PriceCandle.resolutionToMinutes(Channel.PRICES_1H);
        p = new PriceCandle(t, Decimal.ONE, Decimal.ZERO, Decimal.ONE,
                Decimal.ZERO, Decimal.ONE, resMin);
        assertEquals(t, p.getOpenTime());
        assertEquals(t + 60 * 60000, p.getCloseTime());

        // 4h candle
        resMin = PriceCandle.resolutionToMinutes(Channel.PRICES_4H);
        p = new PriceCandle(t, Decimal.ONE, Decimal.ZERO, Decimal.ONE,
                Decimal.ZERO, Decimal.ONE, resMin);
        assertEquals(t, p.getOpenTime());
        assertEquals(t + 4 * 60 * 60000, p.getCloseTime());

        // 1d candle
        resMin = PriceCandle.resolutionToMinutes(Channel.PRICES_1D);
        p = new PriceCandle(t, Decimal.ONE, Decimal.ZERO, Decimal.ONE,
                Decimal.ZERO, Decimal.ONE, resMin);
        assertEquals(t, p.getOpenTime());
        assertEquals(t + 24 * 60 * 60000, p.getCloseTime());

        // 1w candle
        resMin = PriceCandle.resolutionToMinutes(Channel.PRICES_1W);
        p = new PriceCandle(t, Decimal.ONE, Decimal.ZERO, Decimal.ONE,
                Decimal.ZERO, Decimal.ONE, resMin);
        assertEquals(t, p.getOpenTime());
        assertEquals(t + 7 * 24 * 60 * 60000, p.getCloseTime());
    }

    @Test
    public void testLatest() {
        Prices prices = new Prices();
        assertNull(prices.getLatest());
        PriceCandle p1 = new PriceCandle(10, Decimal.ONE, Decimal.ZERO,
                Decimal.ONE,
                Decimal.ZERO, Decimal.ONE, 1);
        PriceCandle p2 = new PriceCandle(20, Decimal.ONE, Decimal.ZERO,
                Decimal.ONE,
                Decimal.ZERO, Decimal.ONE, 1);
        PriceCandle p3 = new PriceCandle(30, Decimal.ONE, Decimal.ZERO,
                Decimal.ONE,
                Decimal.ZERO, Decimal.ONE, 1);
        PriceCandle p4 = new PriceCandle(40, Decimal.ONE, Decimal.ZERO,
                Decimal.ONE,
                Decimal.ZERO, Decimal.ONE, 1);
        prices.add(p1);
        prices.add(p4);
        prices.add(p2);
        prices.add(p3);
        assertEquals(p4, prices.getLatest());
    }
}
