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

    // Info code meaning "please reconnect"
    private static final int RECONNECT_CODE = 20051;

    // TODO - refactor this to enum
    private final int GET_VERSION = 0;
    private final int SUBSCRIBE = 1;
    private final int SNAPSHOT = 2;
    private final int UPDATING = 3;

    private int state = 0;
    private static final int EXPECTED_VERSION = 2;

    @Override
    public Action parseMessage(String message) {
        //Logger.log("Received: " + message);
        return bitFinexWSClientStateMachine(message);
    }

    private Action bitFinexWSClientStateMachine(String message) {
        switch (state) {
            case GET_VERSION:
                return parseVersion(message);
            case SUBSCRIBE:
                return parseSubscribe(message);
            case SNAPSHOT:
                return parseSnapshot(message);
            case UPDATING:
                return parseUpdate(message);
            default:
                Logger.log("BitFinex state machine got into wrong state!");
                return Action.SHUTDOWN;
        }
    }

    private Action parseVersion(String message) {
        JSONObject msg = new JSONObject(message);
        // Response code
        if (msg.getString("event").equals("info")) {
            int v = msg.getInt("version");
            Logger.log("Received version info: " + v);
            if (v == EXPECTED_VERSION) {
                state++;
            } else {
                return shutDownAction("Wrong version, not supported!");
            }
        } else {
            return shutDownAction(
                    "Received unexpected API response while waiting "
                    + "for version info");
        }
        return null;
    }

    private Action parseSubscribe(String message) {
        JSONObject msg = new JSONObject(message);
        String event = msg.getString("event");
        if (event != null) {
            switch (event) {
                case "subscribed":
                    Logger.log("Successfully subscribed to "
                            + market.getBaseCurrency()
                            + "/" + market.getQuoteCurrency());
                    state++;
                    return null;
                case "error":
                    Logger.log("Error occurred: " + msg.getString("msg")
                            + ", code = " + msg.getInt("code"));
                    Logger.log("Current state was : " + state);
                    return shutDownAction("Received msg: " + message);
                case "info":
                    Logger.log("Info message received: " + msg.getString("msg")
                            + ", code = " + msg.getInt("code"));
                    // Info is not critical, we hope to recover by reconnecting
                    return Action.RECONNECT;
            }
        }

        Logger.log("Did not receive 'event' field!");
        Logger.log("Current state was : " + state);
        return shutDownAction("Received msg: " + message);
    }

    private Action parseUpdate(String message) {
        // Sometimes instead of data we may receive info message requiring us 
        // to reconnect
        if (isReconnectRequest(message)) {
            return Action.RECONNECT;
        }

        try {
            JSONArray data = new JSONArray(message);
            Object val = data.get(1);
            if (val instanceof String) {
                // This may be a heartbeat message
                String sv = (String) val;
                if ("hb".equals(sv)) {
                    heartbeatReceived();
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
            return null;
            // TODO - test if parsing works correctly
        } catch (JSONException e) {
            Logger.log("Current state was : " + state);
            Logger.log("Received msg: " + message);
            return shutDownAction("Error in BitFinex update parsing:"
                    + e.getMessage());
        }
    }

    private Action parseSnapshot(String message) {
        // Sometimes instead of data we may receive info message requiring us 
        // to reconnect
        if (isReconnectRequest(message)) {
            return Action.RECONNECT;
        }
        try {
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
        } catch (JSONException ex) {
            Logger.log("Current state was : " + state);
            Logger.log("Received msg: " + message);
            return shutDownAction("Error in BitFinex snapshot parsing:"
                    + ex.getMessage());
        }
        state++; // Snapshot parsed, move to Update parsing state
        return null;
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

    /**
     * Log an error message and return action asking to shut down
     *
     * @param errMsg
     * @return
     */
    private Action shutDownAction(String errMsg) {
        Logger.log(errMsg);
        return Action.SHUTDOWN;
    }

    /**
     * Return true if string contains an info message requiring to reconnect
     *
     * @param message
     * @return
     */
    private boolean isReconnectRequest(String message) {
        // We check if the message is something like this:
        // {"event":"info","code":20051,"msg":"Stopping. Please try to reconnect"}

        try {
            JSONObject o = new JSONObject(message);
            String event = o.getString("event");
            if (!"info".equals(event)) {
                return false;
            }
            return o.getInt("code") == RECONNECT_CODE;
        } catch (JSONException ex) {
            // Nope, not a valid info message
            return false;
        }
    }

}
