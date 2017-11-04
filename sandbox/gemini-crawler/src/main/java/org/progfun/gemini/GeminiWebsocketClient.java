package org.progfun.gemini;

import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.progfun.Market;

/**
 * WebSocket client subscribing to Gemini stream
 */
public class GeminiWebsocketClient extends WebSocketClient {

    Market market;

    // The URL is wss://api.gemini.com/v1/marketdata/{symbol}
    private static final String API_URL_TEMPLATE = "wss://api.gemini.com/v1/marketdata/";

    private static void log(String msg) {
        Thread t = Thread.currentThread();
        System.out.println(msg + "[Thread #" + t.getId() + "]");
    }

    /**
     * Creates a crawler for a specific market symbol (BTCUSD, etc)
     *
     * @param baseCurrency BTC, etc
     * @param quoteCurrency USD, etc
     * @throws URISyntaxException
     */
    public GeminiWebsocketClient(String baseCurrency, String quoteCurrency)
            throws URISyntaxException, Exception {
        super(new URI(API_URL_TEMPLATE + getSymbol(baseCurrency, quoteCurrency)));
        market = new Market(baseCurrency, quoteCurrency);
    }

    public static void main(String[] args) throws Exception {
        log("Connecting...");
        GeminiWebsocketClient client;
        try {
            client = new GeminiWebsocketClient("BTC", "USD");
            client.start();
        } catch (URISyntaxException ex) {
            log("Something wrong with URL: " + ex.getMessage());
        }
    }

    public void start() {
        try {
            if (!connectBlocking()) {
                log("Could not connect to WebSocket server");
            }
            // The easiest (hacky) way to wait for the response: sleep for some time
            Thread.sleep(3000);
            close();

        } catch (InterruptedException ex) {
            log("Connection was interrupted");
        }
    }

    /* Some code taken from 
       https://github.com/TooTallNate/Java-WebSocket/blob/master/src/main/example/ExampleClient.java
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log("Opened connection");
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage(String message) {
        log("Received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        log("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }

    /**
     * Take a pair of currencies, convert it to a single symbols as understood
     * by Gemini exchange
     *
     * @param baseCurrency
     * @param quoteCurrency
     * @return
     */
    private static String getSymbol(String baseCurrency, String quoteCurrency) {
        if (baseCurrency == null || quoteCurrency == null) {
            return null;
        }
        return baseCurrency.toLowerCase() + quoteCurrency.toLowerCase();
    }

}
