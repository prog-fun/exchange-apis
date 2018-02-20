package org.progfun.bots.hitbtc;

import java.util.LinkedList;
import java.util.List;
import org.progfun.Channel;
import org.progfun.Exchange;
import org.progfun.Logger;
import org.progfun.Subscription;
import org.progfun.websocket.WebSocketHandler;
import org.progfun.websocket.Parser;

/**
 * Gemini Exchange API reader
 */
public class HitBtcHandler extends WebSocketHandler {

    private static final String API_URL = "wss://api.hitbtc.com/api/2/ws";
    // Used to separate different subscription channels
    private int channelId = 1;

    @Override
    protected String getUrl() {
        return API_URL;
    }

    @Override
    public Parser createParser() {
        return new HitBtcParser();
    }

    /**
     * We don't have to send any init commands
     */
    @Override
    public void init() {
    }

    @Override
    protected boolean supportsMultipleMarkets() {
        return true;
    }

    @Override
    protected Exchange createExchange() {
        Exchange e = new Exchange();
        e.setSymbol("HITB");
        return e;
    }

    protected boolean subscribeToTicker(Subscription s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected boolean subscribeToTrades(Subscription s) {
        String symbol = s.getMarket().getSymbol();
        if (connector == null || symbol == null) {
            return false;
        }
        // Save subscription so that we can find it later
        subscriptions.setInactiveId("" + channelId, s);
        // Send request to API
        connector.send("{"
                + "  \"method\": \"subscribeTrades\","
                + "  \"params\": {"
                + "    \"symbol\": \"" + symbol + "\""
                + "  },"
                + "  \"id\": " + channelId + ""
                + "}");
        channelId++;
        return true;
    }

    protected boolean subscribeToOrderbook(Subscription s) {
        String symbol = s.getMarket().getSymbol();
        if (connector == null || symbol == null) {
            return false;
        }
        // Save subscription so that we can find it later
        subscriptions.setInactiveId("" + channelId, s);
        // Send request to API
        connector.send("{"
                + "  \"method\": \"subscribeOrderbook\","
                + "  \"params\": {"
                + "    \"symbol\": \"" + symbol + "\""
                + "  },"
                + "  \"id\": " + channelId + ""
                + "}");
        channelId++;
        return true;
    }

    @Override
    protected boolean subscribeToChannel(Subscription s) {
        switch (s.getChannel()) {
            case ORDERBOOK:
                return subscribeToOrderbook(s);
            case TRADES:
                return subscribeToTrades(s);
            case TICKER:
                return subscribeToTicker(s);
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
        return true;
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
