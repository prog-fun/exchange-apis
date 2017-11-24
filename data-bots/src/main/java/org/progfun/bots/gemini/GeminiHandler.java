package org.progfun.bots.gemini;

import org.progfun.connector.AbstractWebSocketHandler;
import org.progfun.connector.Parser;

/**
 * Gemini Exchange API reader
 */
public class GeminiHandler extends AbstractWebSocketHandler {

    // The URL is wss://api.gemini.com/v1/marketdata/{symbol}
    private static final String API_URL_BASE = "wss://api.gemini.com/v1/marketdata/";

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
        return baseCurrency.toLowerCase() + quoteCurrency.toLowerCase();
    }

    @Override
    protected String getUrl() {
        return API_URL_BASE + getSymbol();
    }

    @Override
    protected Parser createParser() {
        return new GeminiParser();
    }

    /**
     * We don't have to send any init commands
     */
    @Override
    public void sendInitCommands() {
    }

}
