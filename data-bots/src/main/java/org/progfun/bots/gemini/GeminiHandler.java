package org.progfun.bots.gemini;

import org.progfun.CurrencyPair;
import org.progfun.Exchange;
import org.progfun.Logger;
import org.progfun.Market;
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
     * Initialize the handler
     */
    @Override
    public void init() {
        // Check if we have necessary market. If not, create it
        Exchange e = getExchange();
        if (e != null) {
            Market m = e.getMarket(mainMarket);
            if (m == null) {
                Logger.log("Gemini handler creating market for the exchange");
                e.addMarket(new Market(mainMarket));
            }
        }
                
    }

    @Override
    protected Exchange createExchange() {
        Exchange e = new Exchange();
        e.setSymbol("GMNI");
        return e;
    }

    /**
     * This exchange supports only a single market for each WebSocket
     *
     * @return
     */
    @Override
    protected boolean supportsMultipleMarkets() {
        return false;
    }

    /**
     * This API supports only channels for one specific currency pair.
     * Check if this market (currency pair) is the one that we are connected to
     *
     * @param currencyPair
     * @return
     */
    private boolean isMarketOk(CurrencyPair currencyPair) {
        if (mainMarket == null) {
            Logger.log("Can not subscribe to channel when main market not set!");
            return false;
        }
        if (!mainMarket.equals(currencyPair)) {
            Logger.log("Socket already connected to " + getUrl()
                    + ", can not subscribe to orderbook for a currency pair: "
                    + currencyPair);
            return false;
        }
        return true;
    }

    @Override
    protected boolean subscribeToTicker(CurrencyPair currencyPair) {
        // No additional messages must be sent, WebSocket is subscribed by default
        // TODO - somehow notify that subscription is active
        return isMarketOk(currencyPair);
    }

    @Override
    protected boolean subscribeToTrades(CurrencyPair currencyPair) {
        // No additional messages must be sent, WebSocket is subscribed by default
        // TODO - somehow notify that subscription is active
        return isMarketOk(currencyPair);
    }

    @Override
    protected boolean subscribeToOrderbook(CurrencyPair currencyPair) {
        // No additional messages must be sent, WebSocket is subscribed by default
        // TODO - somehow notify that subscription is active
        return isMarketOk(currencyPair);
    }

}
