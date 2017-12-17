package org.progfun.bots.hitbtc;

import org.json.JSONObject;
import org.progfun.CurrencyPair;
import org.progfun.Exchange;
import org.progfun.websocket.WebSocketHandler;
import org.progfun.websocket.Parser;

/**
 * Gemini Exchange API reader
 */
public class HitBtcHandler extends WebSocketHandler {

    private static final String API_URL = "wss://api.hitbtc.com/api/2/ws";

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
                + cp.getBaseCurrency().toUpperCase();
    }

    @Override
    protected String getUrl() {
        return API_URL;
    }

    @Override
    protected Parser createParser() {
        return new HitBtcParser();
    }

    /**
     * We don't have to send any init commands
     */
    @Override
    public void init() {
        //Creating orderbook data request
//        String symbol = getSymbol();
//        if (symbol == null) {
//            return;
//        }
//
//        JSONObject obj = new JSONObject();
//        obj.put("method", "subscribeOrderbook");
//        JSONObject obj2 = new JSONObject();
//        obj2.put("symbol", symbol);
//        obj.put("params", obj2);
//        obj.put("id", "123");
//        String object = obj.toString();
//        connector.send(object);
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

}
