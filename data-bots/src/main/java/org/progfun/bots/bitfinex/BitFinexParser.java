package org.progfun.bots.bitfinex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.Decimal;
import org.progfun.Logger;
import org.progfun.websocket.Action;
import org.progfun.websocket.Parser;

/**
 *
 * @author olavt
 */
public class BitFinexParser extends Parser {

    private final int GET_VERSION = 0;
    private final int SUBSCRIBE = 1;
    private final int SNAPSHOT = 2;
    private final int UPDATING = 3;

    private int state = 0;
    private static final int EXPECTED_VERSION = 2;

    @Override
    public Action parseMessage(String message) {
        //Logger.log("Received: " + message);
        bitFinexWSClientStateMachine(message);
        return null;
    }

    private void bitFinexWSClientStateMachine(String message) {
        JSONObject msg;
        switch (state) {
            case GET_VERSION:
                msg = new JSONObject(message);
                if (msg.getString("event").equals("info")) {
                    int v = msg.getInt("version");
                    Logger.log("Received version info: " + v);
                    if (v == EXPECTED_VERSION) {
                        state++;
                    } else {
                        Logger.log("Wrong version, not supported!");
                        // TODO - raise critical error
                    }
                }
                break;

            case SUBSCRIBE:
                msg = new JSONObject(message);
                if (msg.getString("event").equals("subscribed")) {
                    Logger.log("Successfully subscribed to " 
                            + market.getBaseCurrency() 
                            + "/" + market.getQuoteCurrency());
                    state++;
                }
                break;

            case SNAPSHOT:
                addSnapshot(message);
                state++;
                break;

            case UPDATING:
                parseAndUpdate(message);
                break;

        }
    }

    private void parseAndUpdate(String message) {
        try {
            JSONArray data = new JSONArray(message);
            Object val = data.get(1);
            if (val instanceof String) {
                // This may be a heartbeat message
                String sv = (String) val;
                if ("hb".equals(sv)) {
                    heartbeatReceived();
                    return;
                }
            } else if (val instanceof JSONArray) {
                JSONArray values = (JSONArray) val;
                Decimal price = new Decimal(values.getDouble(0));
                int count = values.getInt(1);
                Decimal amount = new Decimal(values.getDouble(2));
//            Logger.log("Price: " + price + ", Count: " + count + ", Amount: " + amount);
                if (count > 0) {
                    // BitFinex always reports the total updated amount, 
                    // not the difference. Therefore we must first remove the
                    // old order and then add it
                    if (amount.isPositive()) {
                        market.removeBid(price);
                        market.addBid(price, amount, count);
                    } else if (amount.isNegative()) {
                        market.removeAsk(price);
                        market.addAsk(price, amount.negate(), count);
                    }
                } else if (count == 0) {
                    if (amount.equals(Decimal.ONE)) {
                        market.removeBid(price);
                    } else if (amount.negate().equals(Decimal.ONE)) {
                        market.removeAsk(price);
                    }
                }
            }
            // TODO - test if parsing works correctly
        } catch (JSONException e) {
            Logger.log("Error in BitFinex update parsing:"
                    + e.getMessage());
            Logger.log("Received msg: " + message);
        }

//        Logger.log("Ask: " + market.getAsks().size());
//        Logger.log("Bid: " + market.getBids().size());
    }

    private void addSnapshot(String message) {
        JSONArray data = new JSONArray(message);
        JSONArray array = data.getJSONArray(1);
        for (Object json : array) {
            JSONArray values = (JSONArray) json;
            Decimal price = new Decimal(values.getDouble(0));
            int count = values.getInt(1);
            Decimal amount = new Decimal(values.getDouble(2));
            if (count > 0) {
                if (amount.isPositive()) {
                    market.addBid(price, amount, count);
                } else if (amount.isNegative()) {
                    market.addAsk(price, amount.negate(), count);
                }
            }
        }
//        Logger.log(market.getAsks().size());
//        Logger.log(market.getBids().size());
    }

    /**
     * Heart-beat message received from the server
     */
    private void heartbeatReceived() {
        // TODO - Reset alarm timer
        Logger.log("Heartbeat received");
    }

}
