package org.progfun.bots.gdax;

import java.io.IOException;
import org.progfun.InvalidFormatException;
import org.progfun.Market;
import org.progfun.bots.gemini.AbstractWebSocketHandler;
import org.progfun.connector.Parser;
import org.progfun.orderbook.DummyListener;

/**
 * Main class for the GDAX market, creates a websocket to connect to the API and
 * a parser to handle the responses
 */
public class GdaxHandler extends AbstractWebSocketHandler {

    private static final String API_URL = "wss://ws-feed.gdax.com";

    /**
     * Launch a proof-of-concept test
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        GdaxHandler handler;
        try {
            handler = new GdaxHandler();
            Market market = new Market("BTC", "USD");
            market.addListener(new DummyListener());
            handler.setMarket(market);
            if (handler.connect()) {
                handler.sendInitCommands();
                System.in.read(); // Wait for <Enter>
                handler.disconnect();
            }
        } catch (IOException ex) {
            System.out.println("Something wrong with input");
        } catch (InvalidFormatException ex) {
            System.out.println("Invlid currency pair: " + ex.getMessage());
        }
    }

    @Override
    protected String getUrl() {
        return API_URL;
    }

    @Override
    protected Parser createParser() {
        return new GdaxParser();
    }

    /**
     * Send commend to subscribe for specific market orderbook updates
     */
    @Override
    public void sendInitCommands() {
        String symbol = getSymbol();
        connector.send("{\"type\": \"subscribe\",\"product_ids\": [\""
                + symbol + "\"],\"channels\": [\"level2\"]}");
    }

    /**
     * Get Exchange-specific product symbol (BTC-USD, etc)
     *
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
        return baseCurrency.toUpperCase() + "-" + quoteCurrency.toUpperCase();
    }
}
