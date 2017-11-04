package org.progfun;

import org.junit.Test;
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

}
