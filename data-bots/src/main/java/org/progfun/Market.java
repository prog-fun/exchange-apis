package org.progfun;

import org.progfun.trade.Trade;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.progfun.orderbook.Book;
import org.progfun.orderbook.Order;
import org.progfun.orderbook.OrderbookListener;
import org.progfun.trade.TradeListener;

/**
 * Represents a market trading a pair of assets (Currencies). Example markets:
 * BTC/USD (trading Bitcoin for US Dollar), ETH/EUR, EUR/USD (Exchanging EUR and
 * USD currencies), etc.
 *
 * Market is always a place of exchange for two assets: base asset and quote
 * asset We simplify it by assuming that we always trade one "weird currency"
 * called base currency, and one "normal currency" called quote currency. I.e.,
 * we can buy Bitcoin for US Dollars, and then sell the Bitcoin for US Dollars.
 * Bitcoin would be the base currency that we are trading while US Dollar is
 * used as a quote currency.
 *
 * The class is NOT Thread safe!
 */
public class Market {

    private final CurrencyPair currencyPair;
    private String symbol;

    private final Book bids = new Book();
    private final Book asks = new Book();

    private final List<Trade> trades = new ArrayList<>();

    private final List<OrderbookListener> bookListeners = new ArrayList<>();
    private final List<TradeListener> tradeListeners = new ArrayList<>();

    private final Prices prices = new Prices();
    
    /**
     * Create a new market, convert the currencies to upper-case. Throws
     * exception if one of currencies is null or empty
     *
     * @param baseCurrency Base currency that is traded: Bitcoin (BTC), Litecoin
     * (LTC), etc
     * @param quoteCurrency Currency in which the trade is happening: USD, EUR,
     * NOK, etc
     * @throws InvalidFormatException when one of currencies missing
     */
    public Market(String baseCurrency, String quoteCurrency) throws InvalidFormatException {
        this(new CurrencyPair(baseCurrency, quoteCurrency));
    }

    /**
     * Get the market symbol, as used by exchange API (such as "BTC/USD")
     *
     * @return
     */
    public String getSymbol() {
        // Calculate it if symbol is empty
        if (symbol != null) {
            return symbol;
        } else {
            return currencyPair.getBaseCurrency() + currencyPair.getQuoteCurrency();
        }
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Creates a new market for given currency pair
     *
     * @param currencyPair
     * @throws InvalidFormatException when currency pair missing
     */
    public Market(CurrencyPair currencyPair) {
        if (currencyPair == null) {
            throw new InvalidFormatException(
                    "Currency pair can not be empty for a market!");
        }
        this.currencyPair = currencyPair;
    }

    public String getBaseCurrency() {
        return currencyPair.getBaseCurrency();
    }

    public String getQuoteCurrency() {
        return currencyPair.getQuoteCurrency();
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public Book getBids() {
        return bids;
    }

    public Book getAsks() {
        return asks;
    }

    /**
     * Return all currently cached real-time trades
     *
     * @return
     */
    public Collection<Trade> getTrades() {
        return trades;
    }
    
    /**
     * Return the list of price candles
     * @return 
     */
    public Prices getPrices() {
        return prices;
    }

    /**
     * Return a copy of bid orders, limited by price
     *
     * @param limit threshold for bid price, in percent. All the bids with price
     * less than bestBid - limit% will be ignored. Limit must be a positive
     * number, otherwise results are undefined.
     * @return
     */
    public Book getPriceLimitedBids(double limit) {
        return bids.getPriceLimitedOrders(limit, false);
    }

    /**
     * Return a copy of ask orders, limited by price
     *
     * @param limit threshold for ask price, in percent. All the asks with price
     * higher than bestAsk + limit% will be ignored. Limit must be a positive
     * number, otherwise results are undefined.
     * @return
     */
    public Book getPriceLimitedAsks(double limit) {
        return asks.getPriceLimitedOrders(limit, true);
    }

    /**
     * Add a new bid. If a bid with that price is already registered, the amount
     * and orderCount will be added to it.
     *
     * @param price
     * @param amount how much of the base currency buyer wants to buy
     * @param orderCount how many orders have been aggregated in this bid. Use
     * zero if count is not known.
     * @param increment when this is true, the amount and order will be treated
     * as increment if an order for that price already exists. When false,
     * these values will be seen as "new values" and instead of increment will
     * be used as replacement for old values.
     */
    public void addBid(Decimal price, Decimal amount, int orderCount,
            boolean increment) {
        Order bid = new Order(price, amount, orderCount);
        Order updatedBid = bids.add(bid, increment);
        // Notify listeners about changes
        for (OrderbookListener l : bookListeners) {
            if (updatedBid != null) {
                // Check if the update resulted in a delete
                Decimal a = updatedBid.getAmount();
                if (a.isPositive()) {
                    l.bidUpdated(this, updatedBid);
                } else {
                    // Order was actually removed
                    l.bidRemoved(this, price);
                }
            } else {
                l.bidAdded(this, bid);
            }
        }
    }

    /**
     * Wrapper for addBid with with increment=true
     *
     * @param price
     * @param amount
     * @param orderCount
     */
    public void addBid(Decimal price, Decimal amount, int orderCount) {
        addBid(price, amount, orderCount, true);
    }

    /**
     * Wrapper for addBid with Decimal parameters
     *
     * @param price
     * @param amount
     * @param orderCount
     * @param increment
     */
    public void addBid(String price, String amount, int orderCount,
            boolean increment) {
        addBid(new Decimal(price), new Decimal(amount), orderCount, increment);
    }

    /**
     * Wrapper for addBid with Decimal parameters, with increment=true
     *
     * @param price
     * @param amount
     * @param orderCount
     */
    public void addBid(String price, String amount, int orderCount) {
        addBid(new Decimal(price), new Decimal(amount), orderCount, true);
    }

    /**
     * Add a new ask. If an ask with that price is already registered, the
     * amount and orderCount will be added to it.
     *
     * @param price
     * @param amount how much of the base currency seller wants to sell
     * @param orderCount how many orders have been aggregated in this ask Use
     * zero if count is not known.
     * @param increment when this is true, the amount and order will be treated
     * as increment if an order for that price already exists. When false,
     * these values will be seen as "new values" and instead of increment will
     * be used as replacement for old values.
     */
    public void addAsk(Decimal price, Decimal amount, int orderCount,
            boolean increment) {
        Order ask = new Order(price, amount, orderCount);
        Order updatedAsk = asks.add(ask, increment);
        // Notify listeners about changes
        for (OrderbookListener l : bookListeners) {
            if (updatedAsk != null) {
                // Check if the update resulted in a delete
                if (updatedAsk.getAmount().isPositive()) {
                    l.askUpdated(this, updatedAsk);
                } else {
                    // Order was actually removed
                    l.askRemoved(this, price);
                }
            } else {
                l.askAdded(this, ask);
            }
        }
    }

    /**
     * Wrapper for addAsk with increment=true
     *
     * @param price
     * @param amount
     * @param orderCount
     */
    public void addAsk(Decimal price, Decimal amount, int orderCount) {
        addAsk(price, amount, orderCount, true);
    }

    /**
     * Wrapper for addAsk with Decimal parameters
     *
     * @param price
     * @param amount
     * @param orderCount
     * @param increment
     */
    public void addAsk(String price, String amount, int orderCount, boolean increment) {
        addAsk(new Decimal(price), new Decimal(amount), orderCount, increment);
    }

    /**
     * Wrapper for addAsk with Decimal parameters, increment=true
     *
     * @param price
     * @param amount
     * @param orderCount
     */
    public void addAsk(String price, String amount, int orderCount) {
        addAsk(new Decimal(price), new Decimal(amount), orderCount, true);
    }

    /**
     * Remove a bid - all the orders at specific price
     *
     * @param price
     */
    public void removeBid(Decimal price) {
        bids.remove(price);
        // Notify listeners about changes
        for (OrderbookListener l : bookListeners) {
            l.bidRemoved(this, price);
        }
    }

    /**
     * Remove an ask - all the orders at specific price
     *
     * @param price
     */
    public void removeAsk(Decimal price) {
        asks.remove(price);
        // Notify listeners about changes
        for (OrderbookListener l : bookListeners) {
            l.askRemoved(this, price);
        }
    }

    /**
     * Add a new listener for order book, if it is not already registered
     *
     * @param listener
     * @return true if listener was added
     */
    public boolean addBookListener(OrderbookListener listener) {
        if (listener != null && !bookListeners.contains(listener)) {
            bookListeners.add(listener);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a new listener for trades, if it is not already registered
     *
     * @param listener
     * @return true if listener was added
     */
    public boolean addTradeListener(TradeListener listener) {
        if (listener != null && !tradeListeners.contains(listener)) {
            tradeListeners.add(listener);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove listener. Return true if it was in the list.
     *
     * @param listener
     * @return
     */
    public boolean removeListener(OrderbookListener listener) {
        return bookListeners.remove(listener);
    }

    /**
     * Get bid with the best price, or null if bid orderbook is empty
     *
     * @return
     */
    public Order getBestBid() {
        return bids.getFirstOrder(false);
    }

    /**
     * Get ask with the best price, or null if ask orderbook is empty
     *
     * @return
     */
    public Order getBestAsk() {
        return asks.getFirstOrder(true);
    }

    /**
     * Returns true if order book seems to be consistent, false if there is an
     * obvious error (Such as bid price > ask price)
     *
     * @return
     */
    public boolean isOrderBookConsistent() {
        Order bb = getBestBid();
        Order ba = getBestAsk();
        if (ba != null && bb != null) {
            if (!bb.getPrice().isSmallerThan(ba.getPrice())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clear all bids and asks
     */
    public void clearOrderBook() {
        bids.clear();
        asks.clear();
    }

    /**
     * Register a new trade
     *
     * @param t
     */
    public void addTrade(Trade t) {
        trades.add(t);

        // Notify listeners about changes
        for (TradeListener l : tradeListeners) {
            l.tradeAdded(this, t);
        }
    }

    /**
     * Return number of currently registered trades
     *
     * @return
     */
    public int getTradeCount() {
        return trades.size();
    }

    /**
     * Add (or update) a price candle
     * @param price 
     */
    public void addPrice(PriceCandle price) {
        prices.add(price);
    }
    
    /**
     * Delete all trade information
     */
    public void clearTrades() {
        trades.clear();
    }

    /**
     * Delete all price candle information
     */
    public void clearPrices() {
        prices.clear();
    }
    
    /**
     * Delete all data
     */
    public void clearData() {
        // Update locking should happen inside the method calls
        clearTrades();
        clearOrderBook();
        clearPrices();
    }

    @Override
    public String toString() {
        return "Market " + currencyPair;
    }

}
