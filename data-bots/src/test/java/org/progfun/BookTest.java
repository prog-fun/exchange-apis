package org.progfun;

import org.junit.Test;
import org.progfun.orderbook.Book;

import static org.junit.Assert.*;
import org.progfun.orderbook.Order;

public class BookTest {

    static final double DELTA = 0.00000000001;

    /**
     * Test if .equals() works correctly
     */
    @Test
    public void testEquality() {
        Book b1 = new Book();
        Book b2 = new Book();
        // Empty books should be equal
        assertFalse(b1.equals(null));
        assertTrue(b1.equals(b2));
        final Decimal P1 = new Decimal(700);
        final Decimal A1 = new Decimal(15.2);
        final int C1 = 5;
        b1.add(new Order(P1, A1, C1));
        // Empty book not equal with one-order book 
        assertFalse(b1.equals(b2));
        assertFalse(b2.equals(b1));

        // Books with same order equal
        b2.add(new Order(P1, A1, C1));
        assertTrue(b1.equals(b2));
        
        // Books with two different orders
        final Decimal P2 = new Decimal(7000);
        final Decimal A2 = new Decimal(15.3);
        final int C2 = 50;
        final Decimal P3 = P2.add(new Decimal(50));
        final Decimal A3 = A2.add(new Decimal(8));
        final int C3 = C2 + 10;
        b1.add(new Order(P2, A2, C2));
        b2.add(new Order(P3, A3, C3));
        assertFalse(b1.equals(b2));
        assertFalse(b2.equals(b1));

        // Books with two equal orders
        b1 = new Book();
        b2 = new Book();
        b1.add(new Order(P1, A1, C1));
        b1.add(new Order(P2, A2, C2));
        b2.add(new Order(P1, A1, C1));
        b2.add(new Order(P2, A2, C2));
        assertTrue(b1.equals(b2));
        
        // Even orders added in a different sequence should be equal
        b1 = new Book();
        b2 = new Book();
        b1.add(new Order(P2, A2, C2));
        b1.add(new Order(P1, A1, C1));
        b2.add(new Order(P1, A1, C1));
        b2.add(new Order(P2, A2, C2));
        assertTrue(b1.equals(b2));
    }
}
