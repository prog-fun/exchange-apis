package org.progfun.bots.hitbtc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.Channel;
import org.progfun.Decimal;
import org.progfun.Market;
import org.progfun.Subscription;
import org.progfun.websocket.Action;
import org.progfun.websocket.Parser;
import org.progfun.websocket.ParserResponse;

/**
 * Parser for HitBTC exchange API messages
 * TODO - this class needs polishing and tests
 */
public class HitBtcParser extends Parser {

    @Override
    public ParserResponse parseMessage(String message) {
        JSONObject msg = new JSONObject(message);
        if (msg.has("result")) {
            return parseSubscriptionResult(msg);
        }
        if (msg.has("error")) {
            int id = msg.getInt("id");
            return parseErrorMsg(msg, id);
        }
        try {
            String responseType = msg.getString("method");
            JSONObject params = msg.getJSONObject("params");
            switch (responseType) {
                case "snapshotOrderbook":
                    return parseOderbookUpdate(params);
                case "updateOrderbook":
                    return parseOderbookUpdate(params);
                case "snapshotTrades":
                    return parseTradeSnapshot(params);
                case "updateTrades":
                    return parseTradeUpdate(params);
                default:
                    return shutDownAction("Unknwon response: " + message);
            }
        } catch (JSONException e) {
            System.out.println("test" + e);
        }
        return null;
    }

    private ParserResponse parseOderbookUpdate(JSONObject params) {
        // Find market
        Subscription subscription = null;
        if (params.has("symbol")) {
            String symbol = params.getString("symbol");
            String subsId = getActiveSubscriptionId(symbol, Channel.ORDERBOOK);
            subscription = subscriptions.getActive(subsId);
        }
        if (subscription == null) {
            return shutDownAction("OB snapshot for unknown subscription!");
        }
        // TODO - check sequence number, reconnect when gap detected
        Market market = subscription.getMarket();

        if (params.has("ask")) {
            JSONArray asks = params.getJSONArray("ask");
            for (Object obj : asks) {
                JSONObject ask = (JSONObject) obj;
                Decimal askSize = new Decimal(ask.getString("size"));
                Decimal askPrice = new Decimal(ask.getString("price"));
                if (askSize.isZero()) {
                    market.removeAsk(askPrice);
                } else {
                    // HitBTC always reports the final value, not difference,
                    // therefore we should not increment
                    market.addAsk(askPrice, askSize, 0, false);
                }
            }

        }
        if (params.has("bid")) {
            JSONArray bids = params.getJSONArray("bid");
            for (Object obj : bids) {
                JSONObject bid = (JSONObject) obj;
                Decimal bidSize = new Decimal(bid.getString("size"));
                Decimal bidPrice = new Decimal(bid.getString("price"));
                if (bidSize.isZero()) {
                    market.removeBid(bidPrice);
                } else {
                    // HitBTC always reports the final value, not difference,
                    // therefore we should not increment
                    market.addBid(bidPrice, bidSize, 0, false);
                }
            }
        }
        return null;
    }

    private ParserResponse parseTradeSnapshot(JSONObject params) {
        return shutDownAction("Trade snapshot parsing not implemented!");
    }

    private ParserResponse parseTradeUpdate(JSONObject params) {
        return shutDownAction("Trade update parsing not implemented!");
    }

    private ParserResponse parseSubscriptionResult(JSONObject msg) {
        if (subscriptions == null) {
            return shutDownAction("Error: received subscription response "
                    + "but subscriptions not set in HitBTCParser!");
        }
        try {
            boolean result = msg.getBoolean("result");
            if (!result) {
                return shutDownAction("Got result which was not true!");
            } else {
                // Last action was ok, find subscription and activate it
                int id = msg.getInt("id");
                Subscription s = subscriptions.getInactive("" + id);

                if (s != null) {
                    // Activate the subscription, store the new ID
                    Market m = s.getMarket();
                    // BTCUSD, etc
                    String symbol = m.getSymbol();
                    String subsId = getActiveSubscriptionId(symbol, s.getChannel());
                    subscriptions.activate(subsId, s);
                } else {
                    return shutDownAction("Got result for unknown subscription: "
                            + msg);
                }

                // Tell the Handler that we are ready to process next subscription
                return new ParserResponse(Action.SUBSCRIBE, "Subscription successful");
            }
        } catch (JSONException ex) {
            return shutDownAction("Error in HitBTC result parsing:"
                    + ex.getMessage());
        }
    }

    private ParserResponse parseErrorMsg(JSONObject msg, int id) {
        try {
            JSONObject error = msg.getJSONObject("error");
            int code = error.getInt("code");
            String errMsg = error.getString("message");
            String description = "";
            if (error.has("description")) {
                description = error.getString("description");
            }
            String errLog = "Error for req #" + id + ", code " + code
                    + ": " + errMsg + "; " + description;
            if (notCriticalError(code)) {
                // We can recover from the error, retry connection
                return new ParserResponse(Action.RECONNECT, errLog);
            } else {
                return shutDownAction(errLog);
            }
        } catch (JSONException ex) {
            return shutDownAction("Error in HitBTC error parsing:"
                    + ex.getMessage());
        }
    }

    /**
     * Return ID for an active subscription
     *
     * @param symbol "BTCUSD" etc
     * @param channel orderbook, etc
     * @return
     */
    private static String getActiveSubscriptionId(String symbol, Channel channel) {
        return symbol + "-" + channel.toString();
    }

    /**
     * Return true if error with this code is not critical and can be solved by
     * restarting WebSocket
     *
     * @param code
     * @return
     */
    private static boolean notCriticalError(int code) {
        // See https://api.hitbtc.com/?shell#errors
        switch (code) {
            case 503:
            case 504:
                return true;
            default:
                return false;
        }
    }
}
