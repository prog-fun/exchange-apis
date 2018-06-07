package org.progfun.price;

import java.util.Objects;
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
            case PRICES_2H:
                return 120;
            case PRICES_3H:
                return 180;
            case PRICES_4H:
                return 240;
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

    /**
     * Convert resolution from integer representing number of minutes to Channel
     * @param resMin resolution in minutes
     * @return resolution as Channel, null on error
     */
    public static Channel minToResolution(int resMin) {
        switch (resMin) {
            case 1:
                return Channel.PRICES_1MIN;
            case 5:
                return Channel.PRICES_5MIN;
            case 15:
                return Channel.PRICES_15MIN;
            case 30:
                return Channel.PRICES_30MIN;
            case 60:
                return Channel.PRICES_1H;
            case 120:
                return Channel.PRICES_2H;
            case 180:
                return Channel.PRICES_3H;
            case 240:
                return Channel.PRICES_4H;
            case 360:
                return Channel.PRICES_6H;
            case 720:
                return Channel.PRICES_12H;
            case 1440:
                return Channel.PRICES_1D;
            case 10080:
                return Channel.PRICES_1W;
            default:
                return null;
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
     * @param resolution resolution in minutes
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
     * Get unix timestamp of period start, WITH milliseconds
     *
     * @return
     */
    public long getOpenTime() {
        return openTime;
    }

    /**
     * Set unix timestamp of period start, WITH milliseconds
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
     * Get Unix timestamp of period end, WITH milliseconds
     *
     * @return period end timestamp (timestamp where the next candle should start) or -1 on error
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PriceCandle other = (PriceCandle) obj;
        if (this.openTime != other.openTime) {
            return false;
        }
        if (this.resolution != other.resolution) {
            return false;
        }
        if (!Objects.equals(this.openPrice, other.openPrice)) {
            return false;
        }
        if (!Objects.equals(this.closePrice, other.closePrice)) {
            return false;
        }
        if (!Objects.equals(this.lowPrice, other.lowPrice)) {
            return false;
        }
        if (!Objects.equals(this.highPrice, other.highPrice)) {
            return false;
        }
        if (!Objects.equals(this.volume, other.volume)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (int) (this.openTime ^ (this.openTime >>> 32));
        hash = 47 * hash + Objects.hashCode(this.openPrice);
        hash = 47 * hash + Objects.hashCode(this.closePrice);
        hash = 47 * hash + Objects.hashCode(this.lowPrice);
        hash = 47 * hash + Objects.hashCode(this.highPrice);
        hash = 47 * hash + Objects.hashCode(this.volume);
        hash = 47 * hash + this.resolution;
        return hash;
    }

    
}
