package org.progfun.examples;

import org.progfun.Decimal;
import org.progfun.Exchange;
import org.progfun.Market;
import org.progfun.SnapshotListener;
import org.progfun.orderbook.Book;
import org.progfun.orderbook.Order;

/**
 * Prints out exchange information
 */
public class ExchangeLogger implements SnapshotListener {
    private int bidLimit = Integer.MAX_VALUE;
    
    /**
     * Specify how many bids and asks to show from the orderbook
     * @param limit number of bids/asks to show. Use 0 to show all books
     */
    public void setBidLimit(int limit) {
        if (limit > 0) {
            this.bidLimit = limit;
        } else {
            this.bidLimit = Integer.MAX_VALUE;
        }
    }
    
    /**
     * New market snapshot received, print it
     */
    @Override
    public void onSnapshot(Exchange exchange) {
        for (Market market : exchange.getMarkets()) {
            System.out.println("Market " + market.getCurrencyPair() + ":");
            printPrices(market.getBids(), false, "Bids");
            printPrices(market.getAsks(), true, "Asks");
        }
    }

    /**
     * Print prices of a bid/ask book
     * @param book
     * @param ascending if true, sort prices ascending
     * @param title Title to print 
     */
    private void printPrices(Book book, boolean ascending, String title) {
        System.out.println(title + ":");
        Decimal[] prices = book.getOrderedPrices(ascending);
        for (int i = 0; i < bidLimit && i < prices.length; ++ i) {
            Order o = book.getOrderForPrice(prices[i]);
            System.out.println(o.getPrice() + " [" + o.getAmount()+ "]");
        }
    }

}