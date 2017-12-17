package org.progfun.bots.gdax;

import org.progfun.CurrencyPair;
import org.progfun.Exchange;
import org.progfun.websocket.WebSocketHandler;
import org.progfun.websocket.Parser;

/**
 * Main class for the GDAX market, creates a websocket to connect to the API and
 * a parser to handle the responses
 */
public class GdaxHandler extends WebSocketHandler {

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
     * Initialize the handler
     */
    @Override
    public void init() {
    }


    /**
     * Get Exchange-specific product symbol (BTC-USD, etc)
     *
     * @param cp currency pair
     * @return
     */
    private String getSymbol(CurrencyPair cp) {
        if (cp == null) {
            return null;
        }
        return cp.getBaseCurrency().toUpperCase()
                +  "-" + cp.getBaseCurrency().toUpperCase();
    }
    /**
     *
     * @return
     */

//    @Override
//    public String getExchangeSymbol() {
//        return "GDAX";
//    }

    @Override
    protected Exchange createExchange() {
        Exchange e = new Exchange();
        e.setSymbol("GDAX");
        return e;
    }

    @Override
    protected boolean supportsMultipleMarkets() {
        return true;
    }

    @Override
    protected boolean subscribeToTicker(CurrencyPair currencyPair) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean subscribeToTrades(CurrencyPair currencyPair) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean subscribeToOrderbook(CurrencyPair currencyPair) {
        String symbol = getSymbol(currencyPair);
        if (connector == null || symbol == null) {
            return false;
        }
        connector.send("{\"type\": \"subscribe\",\"product_ids\": [\""
                + symbol + "\"],\"channels\": [\"level2\"]}");
        return true;
    }
}
