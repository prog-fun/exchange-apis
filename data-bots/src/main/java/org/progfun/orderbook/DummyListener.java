package org.progfun.orderbook;

import org.progfun.Market;

/**
 * Prints all order book updates to system console
 */
public class DummyListener implements Listener {

    @Override
    public void bidAdded(Market market, Order bid) {
        System.out.println("ADD Bid: " + bid.getPrice() + " [" + bid.getAmount() + "]");
    }

    @Override
    public void askAdded(Market market, Order ask) {
        System.out.println("ADD Ask: " + ask.getPrice() + " [" + ask.getAmount() + "]");
    }

    @Override
    public void bidUpdated(Market market, Order bid) {
        System.out.println("  UPD Bid: " + bid.getPrice() + " [" + bid.getAmount() + "]");
    }

    @Override
    public void askUpdated(Market market, Order ask) {
        System.out.println("  UPD Ask: " + ask.getPrice() + " [" + ask.getAmount() + "]");
    }

    @Override
    public void bidRemoved(Market market, double price) {
        System.out.println("    REM Bid: " + price);
    }

    @Override
    public void askRemoved(Market market, double price) {
        System.out.println("    REM Ask: " + price);
    }

}
