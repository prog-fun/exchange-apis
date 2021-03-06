package org.progfun;

import org.junit.Test;

import static org.junit.Assert.*;
import org.progfun.orderbook.Order;

public class OrderTest {

    static final double DELTA = 0.00000000001;

    /**
     * Test if .equals() works correctly
     */
    @Test
    public void testEquality() {
        final Decimal P1 = new Decimal(700);
        final Decimal A1 = new Decimal(15.2);
        final int C1 = 5;
        Order o1 = new Order(P1, A1, C1);
        assertFalse(o1.equals(null));
        Order o2 = new Order(P1, A1, C1);
        assertTrue(o1.equals(o2));
        
        final Decimal P2 = new Decimal(0.0000000007);
        final Decimal A2 = new Decimal(15.2);
        final int C2 = 4;
        o2 = new Order(P2, A2, C2);
        assertFalse(o1.equals(o2));
        assertFalse(o2.equals(o1));
        o1 = new Order(P2, A2, C2);
        assertTrue(o1.equals(o2));

        // Try null count
        o2 = new Order(P2, A2, null);
        assertFalse(o1.equals(o2));
        assertFalse(o2.equals(o1));
        
        o1 = new Order(P2, A2, 0);
        assertFalse(o1.equals(o2));
        assertFalse(o2.equals(o1));
        o1 = new Order(P2, A2, -1);
        assertFalse(o1.equals(o2));
        assertFalse(o2.equals(o1));
        
        // Try both with null count
        o1 = new Order(P2, A2, null);
        assertTrue(o2.equals(o1));
        
    }
}
