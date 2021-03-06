package org.progfun.bots.gemini;

import java.util.LinkedList;
import java.util.List;
import org.progfun.Channel;
import org.progfun.Exchange;
import org.progfun.Logger;
import org.progfun.Market;
import org.progfun.Subscription;
import org.progfun.websocket.WebSocketHandler;
import org.progfun.websocket.Parser;

/**
 * Gemini Exchange API reader
 */
public class GeminiHandler extends WebSocketHandler {

    // The URL is wss://api.gemini.com/v1/marketdata/{symbol}
    private static final String API_URL_BASE = "wss://api.gemini.com/v1/marketdata/";

    private Market mainMarket;

    /**
     * This handler can operate only in a single market, and connection URL
     * depends on it
     *
     * @param market
     */
    public void setMainMarket(Market market) {
        this.mainMarket = market;
    }

    @Override
    protected String getUrl() {
        if (mainMarket == null) {
            return null;
        }
        return API_URL_BASE + mainMarket.getSymbol();
    }

    @Override
    public Parser createParser() {
        return new GeminiParser();
    }

    /**
     * Initialize the handler
     */
    @Override
    public void init() {
        // Check if we have necessary market. If not, create it
        Exchange e = getExchange();
        if (e != null && mainMarket != null) {
            if (e.addMarket(mainMarket)) {
                Logger.log("Gemini handler added market " + mainMarket
                        + " for the exchange");
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
    private boolean isMarketOk(Market market) {
        if (mainMarket == null) {
            Logger.log("Can not subscribe to channel when main market not set!");
            return false;
        }
        if (market == null) {
            return false;
        }
        if (!mainMarket.getCurrencyPair().equals(market.getCurrencyPair())) {
            Logger.log("Socket already connected to " + getUrl()
                    + ", can not subscribe to orderbook for another market"
                    + market.getCurrencyPair());
            return false;
        }
        return true;
    }

    @Override
    protected boolean subscribeToChannel(Subscription s) {
        // No additional messages must be sent, WebSocket is subscribed by default
        // TODO - somehow notify that subscription is active
        return isMarketOk(s.getMarket());
    }

    @Override
    public boolean supportsOrderbook() {
        return true;
    }

    @Override
    public boolean supportsTrades() {
        return false;
    }

    @Override
    public boolean supportsPriceCandles() {
        return false;
    }

    @Override
    public List<Channel> getCandleResolutions() {
        return new LinkedList<>();
    }
}
