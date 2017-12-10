package org.progfun.bots.hitbtc;

import org.json.JSONObject;
import org.progfun.wshandler.WebSocketHandler;
import org.progfun.connector.Parser;

/**
 * Gemini Exchange API reader
 */
public class HitBtcHandler extends WebSocketHandler {

    private static final String API_URL = "wss://api.hitbtc.com/api/2/ws";

    /**
     * Return a single symbols as understood by the exchange API
     *
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
        return baseCurrency.toUpperCase() + quoteCurrency.toUpperCase();
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
        String symbol = getSymbol();
        if (symbol == null) {
            return;
        }

        JSONObject obj = new JSONObject();
        obj.put("method", "subscribeOrderbook");
        JSONObject obj2 = new JSONObject();
        obj2.put("symbol", symbol);
        obj.put("params", obj2);
        obj.put("id", "123");
        String object = obj.toString();
        connector.send(object);
    }

    @Override
    public String getExchangeSymbol() {
        return "HITB";
    }

}
