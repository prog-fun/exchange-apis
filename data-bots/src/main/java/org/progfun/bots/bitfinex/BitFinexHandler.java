package org.progfun.bots.bitfinex;

import org.progfun.Channel;
import org.progfun.Exchange;
import org.progfun.Logger;
import org.progfun.Subscription;
import org.progfun.websocket.Parser;
import org.progfun.websocket.WebSocketHandler;

/**
 * Example WebSocket client subscribing to BitFinex stream
 */
public class BitFinexHandler extends WebSocketHandler {

    private static final String API_URL = "wss://api.bitfinex.com/ws/2";

    @Override
    protected String getUrl() {
        return API_URL;
    }

    @Override
    public Parser createParser() {
        return new BitFinexParser();
    }

    @Override
    public void init() {
        // Nothing to do initially
    }

    @Override
    protected Exchange createExchange() {
        Exchange e = new Exchange();
        e.setSymbol("BITF");
        return e;
    }

    /**
     * Supports multiple markets on a single websocket
     *
     * @return
     */
    @Override
    protected boolean supportsMultipleMarkets() {
        return true;
    }

    /**
     * Subscribe to an update channel (orderbook, trades, ...)
     *
     * @param s
     * @return
     */
    @Override
    protected boolean subscribeToChannel(Subscription s) {
        if (s == null) {
            return false;
        }

        String symbol = s.getMarket().getSymbol();
        if (connector == null || symbol == null) {
            return false;
        }

        Channel channel = s.getChannel();
        String channelName;
        String optionalParams = "";
        switch (channel) {
            case ORDERBOOK:
                channelName = "book";
                optionalParams = ", \"prec\":\"P1\", \"freq\":\"F0\", \"len\":\"100\"";
                break;
            case TRADES:
                channelName = "trades";
                break;
            case PRICES_1MIN:
                // TODO - could be handled by a HashMap<Channel, symbol>
                channelName = "candles";
                optionalParams = ", \"key\":\"trade:1m:t" + symbol + "\"";
                break;
            case PRICES_5MIN:
                channelName = "candles";
                optionalParams = ", \"key\":\"trade:5m:t" + symbol + "\"";
                break;
             case PRICES_15MIN:
                channelName = "candles";
                optionalParams = ", \"key\":\"trade:15m:t" + symbol + "\"";
                break;
            case PRICES_30MIN:
                channelName = "candles";
                optionalParams = ", \"key\":\"trade:30m:t" + symbol + "\"";
                break;
            case PRICES_1H:
                channelName = "candles";
                optionalParams = ", \"key\":\"trade:1h:t" + symbol + "\"";
                break;
            case PRICES_3H:
                channelName = "candles";
                optionalParams = ", \"key\":\"trade:3h:t" + symbol + "\"";
                break;
            case PRICES_6H:
                channelName = "candles";
                optionalParams = ", \"key\":\"trade:6h:t" + symbol + "\"";
                break;
            case PRICES_12H:
                channelName = "candles";
                optionalParams = ", \"key\":\"trade:12h:t" + symbol + "\"";
                break;
            case PRICES_1D:
                channelName = "candles";
                optionalParams = ", \"key\":\"trade:1d:t" + symbol + "\"";
                break;
            case PRICES_1W:
                channelName = "candles";
                optionalParams = ", \"key\":\"trade:1w:t" + symbol + "\"";
                break;
           default:
                Logger.log("Channel "
                        + s.getChannel() + " not supported!");
                return false;
        }

        
        String msg = "{\"event\":\"subscribe\", \"channel\":\""
                + channelName + "\", "
                + "\"symbol\":\"t" + symbol + "\"" + optionalParams + "}";
        connector.send(msg);
        // Save the symbol for subscription, so that it can be identified 
        // when response is received
        String subsId = Parser.getInactiveSubsSymbol(symbol, channel);
        subscriptions.setInactiveId(subsId, s);
        return true;

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
        return true;
    }

}
