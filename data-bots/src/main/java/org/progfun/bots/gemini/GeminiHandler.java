package org.progfun.bots.gemini;

import org.progfun.CurrencyPair;
import org.progfun.Exchange;
import org.progfun.websocket.WebSocketHandler;
import org.progfun.websocket.Parser;

/**
 * Gemini Exchange API reader
 */
public class GeminiHandler extends WebSocketHandler {

    // The URL is wss://api.gemini.com/v1/marketdata/{symbol}
    private static final String API_URL_BASE = "wss://api.gemini.com/v1/marketdata/";

    private CurrencyPair mainMarket;
    
    /**
     * Return a single symbol as understood by the exchange API
     *
     * @return
     */
    /**
     * Return a single symbols as understood by the exchange API
     *
     * @param cp currency pair
     * @return
     */
    private String getSymbol(CurrencyPair cp) {
        if (cp == null) {
            return null;
        }
        return cp.getBaseCurrency().toLowerCase()
                + cp.getBaseCurrency().toLowerCase();
    }

    /**
     * This handler can operate only in a single market, and connection URL
     * depends on it
     *
     * @param cp currency pair specifying the main market
     */
    public void setMainMarket(CurrencyPair cp) {
        this.mainMarket = cp;
    }

    @Override
    protected String getUrl() {
        if (mainMarket == null) {
            return null;
        }
        return API_URL_BASE + getSymbol(mainMarket);
    }

    @Override
    protected Parser createParser() {
        return new GeminiParser();
    }

    /**
     * We don't have to send any init commands
     */
    @Override
    public void init() {
    }

    @Override
    protected Exchange createExchange() {
        Exchange e = new Exchange();
        e.setSymbol("GMNI");
        return e;
    }

    /**
     * This exchange supports only a single market for each WebSocket
     * @return 
     */
    @Override
    protected boolean supportsMultipleMarkets() {
        return false;
    }

}
