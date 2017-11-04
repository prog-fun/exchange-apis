package org.progfun;

import org.progfun.connector.Parser;
import org.progfun.connector.WebSocketConnector;

/**
 * Example WebSocket client subscribing to BitFinex stream
 */
public class BitFinexWSClient implements Parser {

    private static final String API_URL = "wss://api.bitfinex.com/ws/2";

    public static void main(String[] args) {
        BitFinexWSClient client = new BitFinexWSClient();
        client.start();
    }

    public void start() {
        System.out.println("Connecting...");
        WebSocketConnector connector = new WebSocketConnector();
        connector.setListener(this);
        if (!connector.start(API_URL)) {
            System.out.println("Could not start WebSocket connector");
            return;
        }
        
        connector.send("{\"event\":\"ping\"}");

        try {
            Thread.sleep(5000);
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
    public void onMessage(String message) {
        System.out.println("Received: " + message);
    }

    @Override
    public void onError(Exception excptn) {
        System.out.println("Error: " + excptn.getMessage());
    }

}
