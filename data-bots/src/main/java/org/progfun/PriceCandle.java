package org.progfun;

/**
 * Represents a Price "Candle": aggregated trades in a specific period. Contains
 * Open, Close, High, Low prices, volume and resolution
 */
public class PriceCandle {
    private long openTime;
    private Decimal openPrice;
    private Decimal closePrice;
    private Decimal lowPrice;
    private Decimal highPrice;
    private Decimal volume;
    // Resolution, in minutes
    private int resolution;

    public PriceCandle(long openTime, Decimal openPrice, Decimal closePrice,
            Decimal lowPrice, Decimal highPrice, Decimal volume, int resolution) {
        this.openTime = openTime;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.lowPrice = lowPrice;
        this.highPrice = highPrice;
        this.volume = volume;
        this.resolution = resolution;
    }

    /**
     * Get unix timestamp of period start
     * @return 
     */
    public long getOpenTime() {
        return openTime;
    }

    /**
     * Set unix timestamp of period start
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
     * @return period end timestamp or -1 on error
     */
    public long getCloseTime() {
        if (openTime <= 0 || resolution <= 0) {
            return -1;
        }
        return openTime + resolution * 60; // resolution is in minutes
    }
}
