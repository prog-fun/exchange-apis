package main;

import java.io.IOException;
import org.progfun.connector.WebSocketConnector;

/**
 * Main class for the GDAX market, creates a websocket to connect to the API
 * and a parser to handle the responses
 */
public class GdaxWSClient {

    private static final String API_URL = "wss://ws-feed.gdax.com";
    private final WebSocketConnector webSocket = new WebSocketConnector();
    private final GdaxParser parser = new GdaxParser();

    /**
     * Main method for the GDAX crawler
     * @param args 
     */
    public static void main(String[] args) {
        System.out.println("Connecting...");
        GdaxWSClient client;

        client = new GdaxWSClient();
        client.start();
    }
    /**
     * Sets the listener for the WebSocketConnector and starts the connection
     * Sends the required JSON to subscribe to the correct channel
     * Stops the connection after Enter is pressed
     */
    public void start() {
        webSocket.setListener(parser);
        webSocket.start(API_URL);

        webSocket.send("{\"type\": \"subscribe\",\"product_ids\": [\"BTC-USD\"],\"channels\": [\"level2\"]}");

        try {
            System.in.read();
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }

        webSocket.stop();

        System.out.println("Connection has closed");

    }
}
