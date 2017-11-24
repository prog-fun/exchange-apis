package org.progfun.bots.bitfinex;

import org.progfun.connector.AbstractWebSocketHandler;
import org.progfun.connector.Parser;

/**
 * Example WebSocket client subscribing to BitFinex stream
 */
public class BitFinexHandler extends AbstractWebSocketHandler {

    private static final String API_URL = "wss://api.bitfinex.com/ws/2";

    @Override
    protected String getUrl() {
        return API_URL;
    }

    @Override
    protected Parser createParser() {
        return new BitFinexParser();
    }

    @Override
    public void sendInitCommands() {
        subscribeToOrderbook(getSymbol());
    }
    
    /**
     * Subscribe to orderbook update channel
     * @param symbol 
     */
    public void subscribeToOrderbook(String symbol) {
        connector.send("{\"event\":\"subscribe\", \"channel\":\"book\", "
                + "\"symbol\":\"t" + symbol 
                + "\", \"prec\":\"P0\", \"freq\":\"F0\", \"len\":\"100\"}");
    }
    
    /**
     * Return a single symbols as understood by the exchange API 
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
        return baseCurrency.toUpperCase() + quoteCurrency.toUpperCase();
    }
    
}
