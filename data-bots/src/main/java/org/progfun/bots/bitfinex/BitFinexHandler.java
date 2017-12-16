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
     * Subscribe to orderbook update channel for a specific currency pair
     * @param cp currency pair, such as (BTC,USD)
     */
    public void subscribeToOrderbook(CurrencyPair cp) {
        connector.send("{\"event\":\"subscribe\", \"channel\":\"book\", "
                + "\"symbol\":\"t" + getSymbol(cp)
                + "\", \"prec\":\"P1\", \"freq\":\"F0\", \"len\":\"100\"}");
    }

    /**
     * Return a single symbols as understood by the exchange API
     *
     * @param cp currency pair
     * @return
     */
    private String getSymbol(CurrencyPair cp) {
        return cp.getBaseCurrency().toUpperCase()
                + cp.getBaseCurrency().toUpperCase();
    }

    @Override
    protected Exchange createExchange() {
        Exchange e = new Exchange();
        e.setSymbol("BITF");
        return e;
    }
}
