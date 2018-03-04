package org.progfun;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Use this class for storing prices and other decimal numbers without losing
 * precision
 */
public class Decimal implements Comparable<Decimal> {

    // Default rounding options
    private static final int DEFAULT_SCALE = 12;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
    private static final MathContext DEFAULT_PRECISION
            = new MathContext(DEFAULT_SCALE, DEFAULT_ROUNDING);

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
        this.number = bd.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
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
     * Return a new decimal whose value is original + d
     * (Syntactic sugar)
     *
     * @param d
     * @return
     */
    public Decimal add(double d) {
        return add(new Decimal(d));
    }

    /**
     * Return a new decimal whose value is original - d
     *
     * @param d
     * @return
     */
    public Decimal subtract(Decimal d) {
        return new Decimal(this.number.subtract(d.number));
    }

    /**
     * Return a new decimal whose value is original - d
     * (Syntactic sugar)
     *
     * @param d
     * @return
     */
    public Decimal subtract(double d) {
        return subtract(new Decimal(d));
    }

    /**
     * Return a new decimal whose value is original * d
     *
     * @param d
     * @return
     */
    public Decimal multiply(Decimal d) {
        return new Decimal(number.multiply(d.number));
    }

    /**
     * Return a new decimal whose value is original * d
     * (Syntactic sugar)
     *
     * @param d
     * @return
     */
    public Decimal multiply(double d) {
        return multiply(new Decimal(d));
    }

    /**
     * Return a new decimal whose value is original / d
     *
     * @param d
     * @return
     */
    public Decimal divide(Decimal d) {
        return new Decimal(number.divide(d.number));
    }

    /**
     * Return a new decimal whose value is original / d
     * (Syntactic sugar)
     *
     * @param d
     * @return
     */
    public Decimal divide(double d) {
        return divide(new Decimal(d));
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

    /**
     * Get string representation, with all trailing zeros cut-off
     * @return 
     */
    public String getNiceString() {
        if (isZero()) {
            return "0";
        }
        String s = number.toPlainString();
        if (s.indexOf('.') < 0) {
            // When not a decimal, don't strip off anything
            return s;
        }
        // Find first non-zero character
        int i = s.length() - 1;
        while (i > 0 && s.charAt(i) == '0') {
            --i;
        }
        if (s.charAt(i) == '.') {
            --i;
        }
        if (i > 0) {
            return s.substring(0, i + 1);
        } else {
            return "0";
        }
    }
    
    /**
     * Create an array of Decimals from an array of doubles
     * @param d
     * @return 
     */
    public static Decimal[] createArray(double[] d) {
        if (d == null) {
            return null;
        }
        Decimal[] res = new Decimal[d.length];
        for (int i = 0; i < d.length; ++i) {
            res[i] = new Decimal(d[i]);
        }
        return res;
    }
}
