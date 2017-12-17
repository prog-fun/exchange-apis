package org.progfun.bots.bitfinex;

import org.progfun.CurrencyPair;
import org.progfun.Exchange;
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
    protected Parser createParser() {
        return new BitFinexParser();
    }

    @Override
    public void init() {
//        subscribeToOrderbook(getSymbol());
    }

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
        return cp.getBaseCurrency().toUpperCase()
                + cp.getQuoteCurrency().toUpperCase();
    }

    @Override
    protected Exchange createExchange() {
        Exchange e = new Exchange();
        e.setSymbol("BITF");
        return e;
    }

    /**
     * Subscribe to orderbook update channel for a specific currency pair
     *
     * @param currencyPair currency pair, such as (BTC,USD)
     * @return true when request sent, false otherwise.
     */
    @Override
    protected boolean subscribeToOrderbook(CurrencyPair currencyPair) {
        String symbol = getSymbol(currencyPair);
        if (connector == null || symbol == null) {
            return false;
        }
        connector.send("{\"event\":\"subscribe\", \"channel\":\"book\", "
                + "\"symbol\":\"t" + symbol
                + "\", \"prec\":\"P1\", \"freq\":\"F0\", \"len\":\"100\"}");
        return true;
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

    @Override
    protected boolean subscribeToTicker(CurrencyPair currencyPair) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean subscribeToTrades(CurrencyPair currencyPair) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
