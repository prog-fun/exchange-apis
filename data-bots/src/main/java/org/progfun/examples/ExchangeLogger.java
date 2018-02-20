package org.progfun.examples;

import java.util.Collection;
import java.util.Iterator;
import org.progfun.Channel;
import org.progfun.Decimal;
import org.progfun.Exchange;
import org.progfun.Market;
import org.progfun.price.PriceCandle;
import org.progfun.SnapshotListener;
import org.progfun.orderbook.Book;
import org.progfun.orderbook.Order;
import org.progfun.price.Prices;
import org.progfun.trade.Trade;

/**
 * Prints out exchange information
 */
public class ExchangeLogger implements SnapshotListener {

    // How many items to include in snapshots
    private int bidLimit = Integer.MAX_VALUE;
    private int tradeLimit = Integer.MAX_VALUE;
    private int priceLimit = Integer.MAX_VALUE;
    
    // Which info to print in snapshots
    private boolean printOrders;
    private boolean printTrades;
    private boolean printPrices;

    /**
     * Initialize logger
     *
     * @param printOrders when true, bids/asks will be printed
     * @param printTrades when true, trades will be printed
     * @param printPrices when true, prices will be printed
     */
    public ExchangeLogger(boolean printOrders, boolean printTrades,
            boolean printPrices) {
        this.printOrders = printOrders;
        this.printTrades = printTrades;
        this.printPrices = printPrices;
    }

    /**
     * Specify how many bids,asks, trades and prices to show
     *
     * @param bidLimit number of bids/asks to show. Use 0 to show all
     * @param tradeLimit number of trades to show. Use 0 to show all 
     * @param priceLimit number of prices to show. Use 0 to show all 
     */
    public void setLimits(int bidLimit, int tradeLimit, int priceLimit) {
        if (bidLimit > 0) {
            this.bidLimit = bidLimit;
        } else {
            this.bidLimit = Integer.MAX_VALUE;
        }
        if (tradeLimit > 0) {
            this.tradeLimit = tradeLimit;
        } else {
            this.tradeLimit = Integer.MAX_VALUE;
        }
        if (priceLimit > 0) {
            this.priceLimit = priceLimit;
        } else {
            this.priceLimit = Integer.MAX_VALUE;
        }
    }

    /**
     * New market snapshot received, print it
     */
    @Override
    public void onSnapshot(Exchange exchange) {
        for (Market market : exchange.getMarkets()) {
            System.out.println("Market " + market.getCurrencyPair() + ":");
            if (printOrders) {
                printOrders(market.getBids(), false, "Bids");
                printOrders(market.getAsks(), true, "Asks");
            }
            if (printTrades) {
                printTrades(market.getTrades());
            }
            if (printPrices) {
                printPrices(market.getPrices(Channel.PRICES_1MIN), "1m");
                printPrices(market.getPrices(Channel.PRICES_5MIN), "5m");
                printPrices(market.getPrices(Channel.PRICES_15MIN), "15m");
                printPrices(market.getPrices(Channel.PRICES_30MIN), "30m");
                printPrices(market.getPrices(Channel.PRICES_1H), "1h");
                printPrices(market.getPrices(Channel.PRICES_3H), "3h");
                printPrices(market.getPrices(Channel.PRICES_6H), "6h");
                printPrices(market.getPrices(Channel.PRICES_12H), "12h");
                printPrices(market.getPrices(Channel.PRICES_1D), "1d");
                printPrices(market.getPrices(Channel.PRICES_1W), "1w");
            }
        }
    }

    /**
     * Print orders in a bid/ask book
     *
     * @param book
     * @param ascending if true, sort prices ascending
     * @param title Title to print
     */
    private void printOrders(Book book, boolean ascending, String title) {
        System.out.println("  " + title + ":");
        Decimal[] prices = book.getOrderedPrices(ascending);
        for (int i = 0; i < bidLimit && i < prices.length; ++i) {
            Order o = book.getOrderForPrice(prices[i]);
            System.out.println("    " + o.getPrice() + " [" + o.getAmount() + "]");
        }
    }

    /**
     * Print trades stored in the market
     */
    private void printTrades(Collection<Trade> trades) {
        System.out.println("  Trades: ");
        Iterator<Trade> it = trades.iterator();
        for (int i = 0; i < tradeLimit && it.hasNext(); ++i) {
            Trade t = it.next();
            System.out.println("    " + t.toString());
        }
    }

    /**
     * Print price candles
     * @param prices 
     */
    private void printPrices(Prices prices, String resolution) {
        if (prices == null || prices.size() == 0) {
            return;
        }
        System.out.println("  Prices " + resolution + ": ");
        Iterator<PriceCandle> it = prices.getAll().iterator();
        for (int i = 0; i < priceLimit && it.hasNext(); ++i) {
            PriceCandle p = it.next();
            System.out.println("    " + p.toString());
        }
    }

}
