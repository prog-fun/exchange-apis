package org.progfun.bots.hitbtc;

import org.progfun.CurrencyPair;
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

    protected boolean subscribeToTicker(Subscription s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected boolean subscribeToTrades(Subscription s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected boolean subscribeToOrderbook(Subscription s) {
        String symbol = getSymbolForMarket(s.getMarket().getCurrencyPair());
        if (connector == null || symbol == null) {
            return false;
        }
        connector.send("{\n"
                + "  \"method\": \"subscribeOrderbook\",\n"
                + "  \"params\": {\n"
                + "    \"symbol\": \"" + symbol + "\"\n"
                + "  },\n"
                + "  \"id\": " + (channelId++) + "\n"
                + "}");
        // TODO - save the channelId somewhere, map to the currency pair
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
    public String getSymbolForMarket(CurrencyPair cp) {
        if (cp == null) {
            return null;
        }
        return cp.getBaseCurrency().toUpperCase()
                + cp.getQuoteCurrency().toUpperCase();
    }

}
