package org.progfun.gemini;

import java.io.IOException;
import org.progfun.InvalidFormatException;
import org.progfun.Market;
import org.progfun.connector.WebSocketConnector;

/**
 * Gemini Exchange API reader
 */
public class GeminiHandler {

    Market market;
    GeminiParser parser;
    WebSocketConnector connector;

    // The URL is wss://api.gemini.com/v1/marketdata/{symbol}
    private static final String API_URL_TEMPLATE = "wss://api.gemini.com/v1/marketdata/";

    /**
     * Set market to monitor
     *
     * @param market
     */
    public void setMarket(Market market) {
        this.market = market;
    }

    /**
     * Launch a proof-of-concept test
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        GeminiHandler client;
        try {
            client = new GeminiHandler();
            Market market = new Market("BTC", "USD");
            market.addListener(new DummyOrderbookListener());
            client.setMarket(market);
            if (client.connect()) {
                System.in.read(); // Wait for <Enter>
                client.disconnect();
            }
        } catch (IOException ex) {
            System.out.println("Something wrong with input");
        } catch (InvalidFormatException ex) {
            System.out.println("Invlid currency pair: " + ex.getMessage());
        }
    }

    /**
     * Connect and start listening for API messages
     *
     * @return true on successful start, false otherwise
     */
    public boolean connect() {
        if (market == null) {
            System.out.println("Can not start crawler without market, cancelling...");
            return false;
        }
        System.out.println("Connecting...");
        connector = new WebSocketConnector();

        // Bind together different components: market, parser and listener
        parser = new GeminiParser();
        parser.setMarket(market);
        connector.setListener(parser);

        String url = API_URL_TEMPLATE + getSymbol();
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
    private String getSymbol() {
        if (market == null) {
            return null;
        }
        String baseCurrency = market.getBaseCurrency();
        String quoteCurrency = market.getQuoteCurrency();
        if (baseCurrency == null || quoteCurrency == null) {
            return null;
        }
        return baseCurrency.toLowerCase() + quoteCurrency.toLowerCase();
    }

}
