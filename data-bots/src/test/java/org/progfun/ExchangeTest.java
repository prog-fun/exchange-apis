package org.progfun;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExchangeTest {
    
    
    /**
     * Test if adding markets works correctly
     */
    @Test
    public void testAddMarkets() throws Exception {
        Exchange e = new Exchange();
        Market[] markets;
        markets = e.getMarkets();
        assertNotNull(markets);
        assertEquals(0, markets.length);
        
        Market m1 = new Market("BTC", "USD");
        Market m2 = new Market("LTC", "EUR");
        
        assertTrue(e.addMarket(m1));
        markets = e.getMarkets();
        assertNotNull(markets);
        assertEquals(1, markets.length);
        
        assertTrue(e.addMarket(m2));
        markets = e.getMarkets();
        assertNotNull(markets);
        assertEquals(2, markets.length);

        // Adding the same markets should not change anything
        assertFalse(e.addMarket(m1));
        assertFalse(e.addMarket(m2));
        markets = e.getMarkets();
        assertNotNull(markets);
        assertEquals(2, markets.length);
        
    }
    
    /**
     * Test if getMarket() works correctly
     */
    @Test
    public void testGetMarket() {
        Exchange e = new Exchange();
        e.addMarket(new Market("BTC", "USD"));
        e.addMarket(new Market("eth", "USD"));
        e.addMarket(new Market("LTC", "eur"));
        e.addMarket(new Market("eth", "nok"));
        assertNotNull(e.getMarket("BTC", "USD"));
        assertNotNull(e.getMarket("ETH", "USD"));
        assertNotNull(e.getMarket("eth", "usd"));
        assertNotNull(e.getMarket("LTC", "EUR"));
        assertNotNull(e.getMarket("ETH", "NOK"));
        assertNull(e.getMarket("", "NOK"));
        assertNull(e.getMarket(null, "NOK"));
        assertNull(e.getMarket("BTC", ""));
        assertNull(e.getMarket("BTC", null));
        assertNull(e.getMarket(null, null));
    }
}
