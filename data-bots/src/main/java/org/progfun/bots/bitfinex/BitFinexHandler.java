package org.progfun.bots.bitfinex;

import org.progfun.Channel;
import org.progfun.CurrencyPair;
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
     * Subscribe to orderbook update channel for a specific currency pair
     *
     * @param s subscription for the orderbook
     * @return true when request sent, false otherwise.
     */
    protected boolean subscribeToOrderbook(Subscription s) {
        String symbol = getSymbolForMarket(s.getMarket().getCurrencyPair());
        if (connector == null || symbol == null) {
            return false;
        }
        connector.send("{\"event\":\"subscribe\", \"channel\":\"book\", "
                + "\"symbol\":\"t" + symbol
                + "\", \"prec\":\"P1\", \"freq\":\"F0\", \"len\":\"100\"}");
        // Save the symbol for subscription, so that it can be identified 
        // when response is received
        String subsId = Parser.getInactiveSubsSymbol(symbol, Channel.ORDERBOOK);
        subscriptions.setInactiveId(subsId, s);
        return true;
    }

    protected boolean subscribeToTicker(Subscription s) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected boolean subscribeToTrades(Subscription s) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public String getSymbolForMarket(CurrencyPair cp) {
        if (cp == null) {
            return null;
        }
        return cp.getBaseCurrency().toUpperCase()
                + cp.getQuoteCurrency().toUpperCase();
    }

}
