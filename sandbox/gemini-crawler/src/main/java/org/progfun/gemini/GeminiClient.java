package org.progfun.gemini;

import org.progfun.InvalidFormatException;
import org.progfun.Market;
import org.progfun.connector.WebSocketConnector;

/**
 * Gemini Exchange API reader
 */
public class GeminiClient {

    private final String symbol;
    Market market;
    GeminiParser parser;

    // The URL is wss://api.gemini.com/v1/marketdata/{symbol}
    private static final String API_URL_TEMPLATE = "wss://api.gemini.com/v1/marketdata/";

    /**
     * Creates a crawler for a specific market symbol (BTCUSD, etc)
     *
     * @param baseCurrency BTC, etc
     * @param quoteCurrency USD, etc
     * @throws org.progfun.InvalidFormatException if currencies incorrect
     */
    public GeminiClient(String baseCurrency, String quoteCurrency) throws InvalidFormatException {
        symbol = getSymbol(baseCurrency, quoteCurrency);
        market = new Market(baseCurrency, quoteCurrency);
    }

    public static void main(String[] args) {
        GeminiClient client;
        try {
            client = new GeminiClient("BTC", "USD");
            client.start();
        } catch (InvalidFormatException ex) {
            System.out.println("Invlid currency pair: " + ex.getMessage());
        }
    }

    public void start() {
        System.out.println("Connecting...");
        WebSocketConnector connector = new WebSocketConnector();

        // Bind together different components: market, parser and listener
        parser = new GeminiParser();
        parser.setOrderbook(market.getOrderbook());
        market.getOrderbook().addListener(new DummyOrderbookListener());
        connector.setListener(parser);

        String url = API_URL_TEMPLATE + symbol;
        if (!connector.start(url)) {
            System.out.println("Could not start WebSocket connector");
            return;
        }

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
