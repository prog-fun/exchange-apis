package org.progfun.gemini;

import org.progfun.orderbook.Listener;
import org.progfun.orderbook.Order;

/**
 * Prints out all updates to order book
 */
public class DummyOrderbookListener implements Listener {

    @Override
    public void bidAdded(Order bid) {
        System.out.println("ADD Bid: " + bid.getPrice() + " [" + bid.getAmount() + "]");
    }

    @Override
    public void askAdded(Order ask) {
        System.out.println("ADD Ask: " + ask.getPrice() + " [" + ask.getAmount() + "]");
    }

    @Override
    public void bidUpdated(Order bid) {
        System.out.println("UPD Bid: " + bid.getPrice() + " [" + bid.getAmount() + "]");
    }

    @Override
    public void askUpdated(Order ask) {
        System.out.println("UPD ask: " + ask.getPrice() + " [" + ask.getAmount() + "]");
    }

    @Override
    public void bidRemoved(double price) {
        System.out.println("REM bid: " + price);
    }

    @Override
    public void askRemoved(double price) {
        System.out.println("REM ask: " + price);
    }

}
