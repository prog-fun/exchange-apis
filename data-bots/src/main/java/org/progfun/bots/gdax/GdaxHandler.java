package org.progfun.bots.gdax;

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
    public Parser createParser() {
        return new GdaxParser();
    }

    /**
     * Initialize the handler
     */
    @Override
    public void init() {
    }

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

    protected boolean subscribeToTicker(Market market) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected boolean subscribeToTrades(Market market) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected boolean subscribeToOrderbook(Market market) {
        if (connector == null || market == null) {
            return false;
        }
        // Note: GDAX markets should have dash in symbol, such as BTC-USD!
        String symbol = market.getSymbol();
        if (symbol == null) {
            return false;
        }
        connector.send("{\"type\": \"subscribe\",\"product_ids\": [\""
                + symbol + "\"],\"channels\": [\"level2\"]}");
        // TODO - save subscription ID
        return true;
    }

    @Override
    protected boolean subscribeToChannel(Subscription s) {
        switch (s.getChannel()) {
            case ORDERBOOK:
                return subscribeToOrderbook(s.getMarket());
            case TRADES:
                return subscribeToTrades(s.getMarket());
            case TICKER:
                return subscribeToTicker(s.getMarket());
            default:
                Logger.log("Channel "
                        + s.getChannel() + " not supported!");
                return false;
        }
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
