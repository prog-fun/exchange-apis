package org.progfun.bots.bitfinex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.Channel;
import org.progfun.Decimal;
import org.progfun.Logger;
import org.progfun.Market;
import org.progfun.Subscription;
import org.progfun.websocket.Action;
import org.progfun.websocket.Parser;

/**
 *
 * @author olavt
 */
public class BitFinexParser extends Parser {

    // Info code meaning "please reconnect"
    private static final int RECONNECT_CODE = 20051;
    // Info code meaning "Maintenance mode"
    private static final int MAINTENANCE_CODE = 20060;

    private static final int EXPECTED_VERSION = 2;

    @Override
    public Action parseMessage(String message) {
        // We don't know whether we will receive a JSON Object or JSON array
        // Try one first, if it fails, try another. JSONArray is more likely,
        // we try it first

        try {
            JSONArray updateMsg = new JSONArray(message);
            // We got a valid JSON array. Now let's find out if it is a snapshot
            // or update
            if (updateMsg.length() > 1) {
                Object val = updateMsg.get(1);
                if (val instanceof JSONArray) {
                    // Some data received, check whether it is snapshot or update
                    JSONArray data = (JSONArray) val;
                    if (data.length() > 0) {
                        Object firstItem = data.get(0);
                        if (firstItem instanceof JSONArray) {
                            return parseSnapshot(data);
                        } else {
                            return parseUpdate(data);
                        }
                    }
                } else if (val instanceof String) {
                    // This may be a heartbeat message
                    String sv = (String) val;
                    if ("hb".equals(sv)) {
                        return heartbeatReceived();
                    }
                }
            }
            // If we got here, something is wrong
            return shutDownAction("Could not understand API response: "
                    + message);
        } catch (JSONException ex) {
        }

        // Check if it is an event message
        try {
            JSONObject event = new JSONObject(message);
            return parseEvent(event);
        } catch (JSONException ex) {
            return shutDownAction("Could not understand API response: "
                    + message + ", excaption: " + ex.getMessage());
        }
    }

    private Action parseEvent(JSONObject event) {
        String eventType = event.getString("event");
        if (eventType != null) {
            switch (eventType) {
                case "subscribed":
                    return parseSubscriptionResponse(event);
                case "error":
                    Logger.log("Error occurred: " + event.getString("msg")
                            + ", code = " + event.getInt("code"));
                    return shutDownAction("Received msg: " + event);
                case "info":
                    return parseInfo(event);
            }
        }

        Logger.log("Did not receive 'event' field!");
        return shutDownAction("Received msg: " + event);
    }

    /**
     * Parse info event
     *
     * @param event
     * @return
     */
    private Action parseInfo(JSONObject event) {
        Logger.log("Info message received: " + event);
        if (event.has("version")) {
            int v = event.getInt("version");
            Logger.log("Received version info: " + v);
            if (v == EXPECTED_VERSION) {
                return null;
            } else {
                return shutDownAction("Wrong version, not supported!");
            }
        } else if (isReconnectRequest(event)) {
            return Action.RECONNECT;
        } else {
            return shutDownAction("Did not know how to react on info message, shutting down");
        }
    }

    private Action parseSubscriptionResponse(JSONObject msg) {
        if (subscriptions == null) {
            return shutDownAction("Error: received subscription response "
                    + "but subscriptions not set in BitFinexParser!");
        }

        // Find out the channel
        String ch = msg.getString("channel");
        Channel channel;
        switch (ch) {
            case "book":
                channel = Channel.ORDERBOOK;
                break;
            default:
                return shutDownAction("Invalid channel received: " + ch);
        }

        // Find the inactive subscription 
        String symbol = msg.getString("symbol");
        // strip the first "t"
        if (symbol.length() < 1) {
            return shutDownAction("Wrong symbol received, msg: " + msg);
        }
        symbol = symbol.substring(1);
            
        String subsId = getInactiveSubsSymbol(symbol, channel);
        Subscription s = subscriptions.getInactive(subsId);
        if (s != null) {
            // Activate the subscription, store the new ID
            int newId = msg.getInt("chanId");
            subscriptions.activate("" + newId, s);
        }

        // Tell the Handler that we are ready to process next subscription
        return Action.SUBSCRIBE;
    }

    private Action parseUpdate(JSONArray values) {
        // TODO - find the right market, based in session ID
        if (exchange == null) {
            Logger.log("Trying to parse update without exchange!");
            return Action.SHUTDOWN;
        }
        Market market = exchange.getFirstMarket();
        if (market == null) {
            Logger.log("Trying to parse update without market!");
            return Action.SHUTDOWN;
        }

        try {
            Decimal price = new Decimal(values.getDouble(0));
            int count = values.getInt(1);
            Decimal amount = new Decimal(values.getDouble(2));
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
            return null;
        } catch (JSONException e) {
            Logger.log("Error while parsing JSON msg: " + values);
            return shutDownAction("Error in BitFinex update parsing:"
                    + e.getMessage());
        }
    }

    private Action parseSnapshot(JSONArray data) {
        if (data.length() < 1) {
            return shutDownAction("Wrong snapshot received: " + data);
        }

        if (exchange == null) {
            Logger.log("Trying to parse snapshot without exchange!");
            return Action.SHUTDOWN;
        }
        // TODO - find the right market, based in session ID
        Market market = exchange.getFirstMarket();
        if (market == null) {
            Logger.log("Trying to parse snapshot without market!");
            return Action.SHUTDOWN;
        }

        try {
            for (int i = 0; i < data.length(); ++i) {
                JSONArray values = data.getJSONArray(i);
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
            return shutDownAction("Error in BitFinex snapshot parsing:"
                    + ex.getMessage());
        }
        return null;
    }

    /**
     * Heart-beat message received from the server
     */
    private Action heartbeatReceived() {
        // TODO - Reset alarm timer
        Logger.log("Heartbeat received");
        return null;
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
    private boolean isReconnectRequest(JSONObject event) {
        // We check if the message is something like this:
        // {"event":"info","code":20051,"msg":"Stopping. Please try to reconnect"}

        try {
            String eventType = event.getString("event");
            if (!"info".equals(eventType)) {
                return false;
            }
            int code = event.getInt("code");
            return code == RECONNECT_CODE || code == MAINTENANCE_CODE;
        } catch (JSONException ex) {
            // Nope, not a valid info message
            return false;
        }
    }

}
