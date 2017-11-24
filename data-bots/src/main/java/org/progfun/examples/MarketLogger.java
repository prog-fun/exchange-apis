package org.progfun.examples;

import org.progfun.Market;
import org.progfun.SnapshotListener;
import org.progfun.orderbook.Book;
import org.progfun.orderbook.Order;

/**
 * Prints out market information
 */
public class MarketLogger implements SnapshotListener {
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
    public void onSnapshot(Market market) {
        printPrices(market.getBids(), false, "Bids");
        printPrices(market.getAsks(), true, "Asks");
    }

    /**
     * Print prices of a bid/ask book
     * @param book
     * @param sortAscendingly if true, sort prices ascendingly
     * @param title Title to print 
     */
    private void printPrices(Book book, boolean sortAscendingly, String title) {
        System.out.println(title + ":");
        Double[] prices = book.getOrderedPrices(sortAscendingly);
        for (int i = 0; i < bidLimit && i < prices.length; ++ i) {
            Order o = book.getOrderForPrice(prices[i]);
            System.out.println(o.getPrice() + " [" + o.getAmount()+ "]");
        }
    }

}
