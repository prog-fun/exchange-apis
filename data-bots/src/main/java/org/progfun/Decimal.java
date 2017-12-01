package org.progfun;

import java.math.BigDecimal;

/**
 * Use this class for storing prices and other decimal numbers without losing
 * precision
 */
public class Decimal {
    private BigDecimal number;
    /**
     * Initialize the number from a string
     * @param number 
     */
    public Decimal(String number) {
        this.number = new BigDecimal(number);
    }
}
