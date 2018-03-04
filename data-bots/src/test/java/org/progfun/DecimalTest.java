package org.progfun;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Decimal class
 */
public class DecimalTest {
    private static final double DELTA = 0.0000001;
    
    @Test
    public void testPositive() {
        Decimal d;
        d = new Decimal("0");
        assertFalse(d.isPositive());
        d = new Decimal("-1");
        assertFalse(d.isPositive());
        d = new Decimal("00000000.0000000");
        assertFalse(d.isPositive());
        d = new Decimal("-0.00000000001");
        assertFalse(d.isPositive());
        d = new Decimal("-999999999999999999");
        assertFalse(d.isPositive());

        d = new Decimal("1");
        assertTrue(d.isPositive());
        d = new Decimal("0.00000000001");
        assertTrue(d.isPositive());
        d = new Decimal("999999999999999999");
        assertTrue(d.isPositive());
    }
    
    @Test
    public void testZero() {
        Decimal z1 = Decimal.ZERO;
        Decimal z2 = Decimal.ZERO;
        assertEquals(z1, z2); // Test if ZERO is static
        assertEquals(0.0, z1.getDoubleVal(), DELTA);
    }
    
    @Test
    public void testCompare() {
        Decimal d1 = new Decimal("-5");
        Decimal d2 = new Decimal("-1");
        Decimal d3 = new Decimal("0");
        Decimal d4 = new Decimal("1");
        Decimal d5 = new Decimal("3");
        assertTrue(d1.isSmallerThan(d2));
        assertTrue(d2.isSmallerThan(d3));
        assertTrue(d3.isSmallerThan(d4));
        assertTrue(d4.isSmallerThan(d5));

        assertTrue(d5.isGreaterThan(d4));
        assertTrue(d4.isGreaterThan(d3));
        assertTrue(d3.isGreaterThan(d2));
        assertTrue(d2.isGreaterThan(d1));
    }
    
    @Test
    public void testMultiply() {
        Decimal d = new Decimal("5");
        d = d.multiply(new Decimal("7"));
        assertEquals(35.0, d.getDoubleVal(), DELTA);
        d = d.multiply(new Decimal("-0.2"));
        assertEquals(-7.0, d.getDoubleVal(), DELTA);
        d = d.multiply(new Decimal("0"));
        assertEquals(0.0, d.getDoubleVal(), DELTA);
        d = d.multiply(new Decimal("-0.2"));
        assertEquals(0.0, d.getDoubleVal(), DELTA);
    }
    
    public void testDivide() {
        Decimal d = new Decimal("5");
        assertEquals(Decimal.ONE, d.divide(d));
        assertEquals(d, d.divide(1));
        assertEquals(d.negate(), d.divide(-5));
        assertEquals(new Decimal(0.0005), d.divide(10000));
    }
    
    @Test
    public void testAdd() {
        Decimal d = new Decimal("5");
        d = d.add(1);
        assertEquals(6.0, d.getDoubleVal(), DELTA);
        d = d.add(-2);
        assertEquals(new Decimal(4.0), d);
        assertEquals(d, d.add(0));
        assertEquals(Decimal.ZERO, d.add(d.negate()));
    }
    
    @Test
    public void testSubtract() {
        Decimal d = new Decimal("5");
        d = d.subtract(1);
        assertEquals(4.0, d.getDoubleVal(), DELTA);
        d = d.subtract(-2);
        assertEquals(new Decimal(6.0), d);
        assertEquals(d, d.subtract(0));
        assertEquals(Decimal.ZERO, d.subtract(d));
    }

    @Test
    public void testNegate() {
        Decimal d = new Decimal("5");
        assertEquals(new Decimal("-5"), d.negate());
        assertEquals(Decimal.ZERO, Decimal.ZERO.negate());
        assertEquals(d, d.negate().negate());
    }
    
    @Test
    public void testCreateArray() {
        assertNull(Decimal.createArray(null));
        Decimal[] d = Decimal.createArray(new double[0]);
        assertNotNull(d);
        assertEquals(0, d.length);
        d = Decimal.createArray(new double[]{-2, 0, 0.12, 5555});
        Decimal[] exp = new Decimal[]{
            new Decimal(-2), 
            Decimal.ZERO,
            new Decimal("0.12"),
            new Decimal(5555)
        };
        assertEquals(exp.length, d.length);
        for (int i = 0; i < d.length; ++i) {
            assertEquals(exp[i], d[i]);
        }
    }
    
    @Test
    public void roundTiny() {
        Decimal d1 = new Decimal("0.0000000012");
        Decimal d2 = new Decimal("0.001");
        Decimal d3 = d1.multiply(d2);
        assertEquals(new Decimal("0.000000000001"), d3);
    }
    
    @Test
    public void testNiceString() {
        Decimal d;
        d = new Decimal("0");
        assertEquals("0", d.getNiceString());
        d = new Decimal("-0");
        assertEquals("0", d.getNiceString());
        d = new Decimal("-0.00000");
        assertEquals("0", d.getNiceString());
        d = new Decimal("0.00000000000");
        assertEquals("0", d.getNiceString());
        d = new Decimal("0.0000000001");
        assertEquals("0.0000000001", d.getNiceString());
        d = new Decimal("0.1000000000");
        assertEquals("0.1", d.getNiceString());
        d = new Decimal("0.007080000000");
        assertEquals("0.00708", d.getNiceString());
        d = new Decimal("-3.28");
        assertEquals("-3.28", d.getNiceString());
        d = new Decimal("-3.28000");
        assertEquals("-3.28", d.getNiceString());
        d = new Decimal("500");
        assertEquals("500", d.getNiceString());
    }
}
