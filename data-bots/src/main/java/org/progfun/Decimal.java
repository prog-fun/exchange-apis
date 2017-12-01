package org.progfun;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Use this class for storing prices and other decimal numbers without losing
 * precision
 */
public class Decimal implements Comparable<Decimal> {

    // Default rounding options
    private static final int DEFAULT_SCALE = 10;
    private static final MathContext DEFAULT_PRECISION
            = new MathContext(DEFAULT_SCALE);

    public static final Decimal ZERO = new Decimal(0);
    public static final Decimal ONE = new Decimal("1");
    public static final Decimal TEN = new Decimal("10");
    private final BigDecimal number;

    /**
     * Initialize the number from a string
     *
     * @param number
     */
    public Decimal(String number) {
        this.number = new BigDecimal(number, DEFAULT_PRECISION);
    }

    public Decimal(double d) {
        this.number = new BigDecimal(d, DEFAULT_PRECISION);
    }

    public Decimal(BigDecimal bd) {
        this.number = bd.setScale(DEFAULT_SCALE);
    }

    /**
     * Return a new decimal whose value is original + d
     *
     * @param d
     * @return
     */
    public Decimal add(Decimal d) {
        return new Decimal(this.number.add(d.number));
    }

    /**
     * Return a new decimal whole value is original * d
     *
     * @param d
     * @return
     */
    public Decimal multiply(Decimal d) {
        return new Decimal(number.multiply(d.number));
    }

    /**
     * Return a new decimal whose value is original * -1
     *
     * @return
     */
    public Decimal negate() {
        return new Decimal(number.negate());
    }

    /**
     * Returns true if the number is positive (greater than zero)
     *
     * @return
     */
    public boolean isPositive() {
        return number.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Return true if value is negative
     *
     * @return
     */
    public boolean isNegative() {
        return number.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Return true if value is equal to zero
     *
     * @return
     */
    public boolean isZero() {
        return number.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Get double value
     *
     * @return
     */
    public double getDoubleVal() {
        return number.doubleValue();
    }

    /**
     * Return true if value is greater than the threshold
     *
     * @param threshold
     * @return
     */
    public boolean isGreaterThan(Decimal threshold) {
        return this.compareTo(threshold) > 0;
    }

    /**
     * Return true if value is smaller than the threshold
     *
     * @param threshold
     * @return
     */
    public boolean isSmallerThan(Decimal threshold) {
        return this.compareTo(threshold) < 0;
    }

    @Override
    public int compareTo(Decimal d) {
        if (d == null) {
            return 1;
        }
        return this.number.compareTo(d.number);
    }

    /**
     * Compare only the numeric value, ignore the scale
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Decimal)) {
            return false;
        }
        Decimal d = (Decimal) o;
        return d.number.compareTo(this.number) == 0;
    }

    @Override
    public String toString() {
        return number.toString();
    }
}
