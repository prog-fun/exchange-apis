package org.progfun;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.progfun.connector.WebSocketConnector;

/**
 * Example WebSocket client subscribing to BitFinex stream
 */
public class BitFinexWSClient implements Runnable {

    private static final String API_URL = "wss://api.bitfinex.com/ws/2";
    private Market market;

    private static void log(String msg) {
        Thread t = Thread.currentThread();
        System.out.println(msg + "[Thread #" + t.getId() + "]");
    }

    public void start() throws InvalidFormatException {
        log("Connecting...");
        WebSocketConnector connector = new WebSocketConnector();
        connector.setListener(new BitFinexGDAXParser(market));

        if (!connector.start(API_URL)) {
            System.out.println("Could not start WebSocket connector");
            return;
        }

        connector.send("{\"event\":\"subscribe\", \"channel\":\"book\", \"symbol\":\"tBTCUSD\", \"prec\":\"P0\", \"freq\":\"F0\", \"len\":\"100\"}");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            System.out.println("Oops, someone interrupted us");
        }
        if (connector.stop()) {
            System.out.println("Closed connection");
        } else {
            System.out.println("Failed to close connection");
        }
    }

    @Override
    public void run() {
        try {
            this.start();
        } catch (InvalidFormatException ex) {
            Logger.getLogger(BitFinexWSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setMarket(Market market) {
        this.market = market;
    }
}
