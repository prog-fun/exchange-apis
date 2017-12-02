package org.progfun.bots.gdax;

import org.progfun.connector.AbstractWebSocketHandler;
import org.progfun.connector.Parser;

/**
 * Main class for the GDAX market, creates a websocket to connect to the API and
 * a parser to handle the responses
 */
public class GdaxHandler extends AbstractWebSocketHandler {

    private static final String API_URL = "wss://ws-feed.gdax.com";

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

    @Override
    public String getExchangeSymbol() {
        return "GDAX";
    }
}
