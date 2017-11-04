package org.progfun.gemini;

import java.io.IOException;
import org.progfun.InvalidFormatException;
import org.progfun.Market;
import org.progfun.connector.WebSocketConnector;

/**
 * Gemini Exchange API reader
 */
public class GeminiHandler {

    private final String symbol;
    Market market;
    GeminiParser parser;
    WebSocketConnector connector;

    // The URL is wss://api.gemini.com/v1/marketdata/{symbol}
    private static final String API_URL_TEMPLATE = "wss://api.gemini.com/v1/marketdata/";

    /**
     * Creates a crawler for a specific market symbol (BTCUSD, etc)
     *
     * @param baseCurrency BTC, etc
     * @param quoteCurrency USD, etc
     * @throws org.progfun.InvalidFormatException if currencies incorrect
     */
    public void setMarket(|String baseCurrency, String quoteCurrency) throws InvalidFormatException {
        symbol = getSymbol(baseCurrency, quoteCurrency);
        market = new Market(baseCurrency, quoteCurrency);
    }

    public static void main(String[] args) {
        GeminiHandler client;
        try {
            client = new GeminiHandler("BTC", "USD");
            client.connect();
            System.in.read(); // Wait for <Enter>
            client.disconnect();
        } catch (IOException ex) {
            System.out.println("Something wrong with input");
        } catch (InvalidFormatException ex) {
            System.out.println("Invlid currency pair: " + ex.getMessage());
        }
    }

    public void setMarket(Market market) {
        this.market = market;
    }
    
    /**
     * Connect and start listening for API messages
     * @return true on successful start, false otherwise
     */
    public boolean connect() {
        System.out.println("Connecting...");
        connector = new WebSocketConnector();

        // Bind together different components: market, parser and listener
        parser = new GeminiParser();
        parser.setMarket(market);
        market.addListener(new DummyOrderbookListener());
        connector.setListener(parser);

        String url = API_URL_TEMPLATE + symbol;
        if (connector.start(url)) {
            return true;
        } else {
            System.out.println("Could not start WebSocket connector");
            return false;
        }
    }

    /**
     * Stop listening for data from API
     */
    public void disconnect() {
        if (connector.stop()) {
            System.out.println("Closed connection");
        } else {
            System.out.println("Failed to close connection");
        }
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
