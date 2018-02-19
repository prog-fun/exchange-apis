package org.progfun.price;

import org.progfun.Channel;
import org.progfun.Decimal;

/**
 * Represents a Price "Candle": aggregated trades in a specific period. Contains
 * Open, Close, High, Low prices, volume and resolution
 */
public class PriceCandle {

    /**
     * Convert resolution from Channel to integer representing number of minutes
     *
     * @param resolution
     * @return number of minutes, or -1 if resolution is incorrect
     */
    public static int resolutionToMinutes(Channel resolution) {
        switch (resolution) {
            case PRICES_1MIN:
                return 1;
            case PRICES_5MIN:
                return 5;
            case PRICES_15MIN:
                return 15;
            case PRICES_30MIN:
                return 30;
            case PRICES_1H:
                return 60;
            case PRICES_3H:
                return 180;
            case PRICES_6H:
                return 360;
            case PRICES_12H:
                return 720;
            case PRICES_1D:
                return 1440;
            case PRICES_1W:
                return 10080;
            default:
                return -1;
        }
    }

    private long openTime;
    private Decimal openPrice;
    private Decimal closePrice;
    private Decimal lowPrice;
    private Decimal highPrice;
    private Decimal volume;
    // Resolution, in minutes
    private int resolution;

    /**
     * Create price candle
     *
     * @param msTime open timestamp, in milliseconds! (not seconds)
     * @param openPrice
     * @param closePrice
     * @param lowPrice
     * @param highPrice
     * @param volume
     * @param resolution
     */
    public PriceCandle(long msTime, Decimal openPrice, Decimal closePrice,
            Decimal lowPrice, Decimal highPrice, Decimal volume, int resolution) {
        this.openTime = msTime;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.lowPrice = lowPrice;
        this.highPrice = highPrice;
        this.volume = volume;
        this.resolution = resolution;
    }

    /**
     * Get unix timestamp of period start
     *
     * @return
     */
    public long getOpenTime() {
        return openTime;
    }

    /**
     * Set unix timestamp of period start
     *
     * @param openTime
     */
    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }

    public Decimal getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(Decimal openPrice) {
        this.openPrice = openPrice;
    }

    public Decimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(Decimal closePrice) {
        this.closePrice = closePrice;
    }

    public Decimal getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(Decimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public Decimal getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(Decimal highPrice) {
        this.highPrice = highPrice;
    }

    public Decimal getVolume() {
        return volume;
    }

    public void setVolume(Decimal volume) {
        this.volume = volume;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    /**
     * Get unix timestamp of period end
     *
     * @return period end timestamp or -1 on error
     */
    public long getCloseTime() {
        if (openTime <= 0 || resolution <= 0) {
            return -1;
        }
        return openTime + resolution * 60000; // resolution is in minutes
    }

    @Override
    public String toString() {
        return "Price {" + "openTime=" + openTime + ", prices: [open="
                + openPrice + ", close=" + closePrice + ", low=" + lowPrice
                + ", high=" + highPrice + "], volume=" + volume + '}';
    }

}
