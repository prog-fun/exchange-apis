package org.progfun.bots.bitfinex;

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.Channel;
import org.progfun.Decimal;
import org.progfun.Logger;
import org.progfun.Market;
import org.progfun.price.PriceCandle;
import org.progfun.Subscription;
import org.progfun.trade.Trade;
import org.progfun.websocket.Action;
import org.progfun.websocket.Parser;
import org.progfun.websocket.Event;

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
    public Event parseMessage(String message) {
        // We don't know whether we will receive a JSON Object or JSON array
        // Try one first, if it fails, try another. JSONArray is more likely,
        // we try it first

        try {
            JSONArray updateMsg = new JSONArray(message);
            // We got a valid JSON array. Now let's find out if it is a snapshot
            // or update
            int channelId = updateMsg.getInt(0);
            if (updateMsg.length() > 1) {
                Object val = updateMsg.get(1);
                if (val instanceof JSONArray) {
                    // Some data received
                    JSONArray data = (JSONArray) val;
                    // First item should be channel id
                    return parseDataMessage(channelId, data);
                } else if (val instanceof String) {
                    // This may be a heartbeat message
                    String sv = (String) val;
                    if ("hb".equals(sv)) {
                        return heartbeatReceived();
                    } else if ("te".equals(sv)) {
                        // Trade updates have a bit different format
                        Object val2 = updateMsg.get(2);
                        if (val2 instanceof JSONArray) {
                            JSONArray data = (JSONArray) val2;
                            return parseDataMessage(channelId, data);
                        }
                    } else if ("tu".equals(sv)) {
                        // "tu" is a trade update which historically included 
                        // some additional information. Now it is just a 
                        // duplicate of "te" message
                        return null;
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

    /**
     * Parse one API response which contains data
     *
     * @param channelId ID identifying the channel (Subscription)
     * @param data
     * @return
     */
    private Event parseDataMessage(int channelId, JSONArray data) {
        Subscription subscription = subscriptions.getActive("" + channelId);
        if (subscription == null) {
            return shutDownAction("Wrong channel ID for data update: "
                    + channelId);
        }
        Market market = subscription.getMarket();
        if (market == null) {
            return shutDownAction("Trying to parse snapshot without market!");
        }

        // Check if we got snapshot or update
        if (data.length() > 1) {
            Object firstItem = data.get(0);
            Channel channel = subscription.getChannel();
            if (firstItem instanceof JSONArray) {
                switch (channel) {
                    case ORDERBOOK:
                        return parseOrderSnapshot(market, data);
                    case TRADES:
                        return parseTradeSnapshot(market, data);
                    case PRICES_1MIN:
                    case PRICES_5MIN:
                    case PRICES_15MIN:
                    case PRICES_30MIN:
                    case PRICES_1H:
                    case PRICES_3H:
                    case PRICES_6H:
                    case PRICES_12H:
                    case PRICES_1D:
                    case PRICES_1W:
                        return parsePriceSnapshot(market, data, channel);
                    default:
                        return shutDownAction(
                                "Snapshot for unsupported channel: " + channel);
                }
            } else {
                switch (channel) {
                    case ORDERBOOK:
                        return parseOrderUpdate(market, data);
                    case TRADES:
                        return parseTrade(market, data);
                    case PRICES_1MIN:
                    case PRICES_5MIN:
                    case PRICES_15MIN:
                    case PRICES_30MIN:
                    case PRICES_1H:
                    case PRICES_3H:
                    case PRICES_6H:
                    case PRICES_12H:
                    case PRICES_1D:
                    case PRICES_1W:
                        return parsePrice(market, data, channel);
                    default:
                        return shutDownAction("Update for unsupported channel: "
                                + channel);
                }
            }
        } else {
            return shutDownAction("Wrong data message, not enough items: "
                    + data);
        }
    }

    private Event parseEvent(JSONObject event) {
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
    private Event parseInfo(JSONObject event) {
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
            return new Event(Action.RECONNECT, null,
                    "Reconnect requested by remote API");
        } else {
            return shutDownAction(
                    "Did not know how to react on info message, shutting down");
        }
    }

    private Event parseSubscriptionResponse(JSONObject msg) {
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
            case "trades":
                channel = Channel.TRADES;
                break;
            case "candles":
                // We will fix the resolution a bit later...
                channel = Channel.PRICES_1MIN;
                break;
            default:
                return shutDownAction("Invalid channel received: " + ch);
        }

        // Find the inactive subscription 
        String symbol;
        if (channel == Channel.PRICES_1MIN) {
            // Fixing the resolution and symbol
            symbol = null;

            // Candle channel does not include a symbol, rather a key
            String key = msg.getString("key");
            // The key is expected to be in the format trades:<resolution>:t<symbol>
            if (key != null && key.length() > 10
                    && key.substring(0, 6).equals("trade:")) {
                String resSym = key.substring(6); // <resolution>:t<symbol>
                int colonPos = resSym.indexOf(':');
                if (colonPos >= 2) {
                    String resolution = resSym.substring(0, colonPos);
                    // Now we set the correct channel
                    channel = stringResolutionToChannel(resolution);
                    if (channel != null) {
                        // We keep the "t", it will be stripped later
                        symbol = resSym.substring(colonPos + 1);
                    } else {
                        symbol = null;
                    }
                } else {
                    symbol = null;
                }
            } else {
            }
        } else {
            symbol = msg.getString("symbol");
        }
        // strip the first "t"
        if (symbol == null || symbol.length() < 1) {
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
        return new Event(Action.SUBSCRIBE, s, "Subscription successful");
    }

    private Event parseOrderSnapshot(Market market, JSONArray data) {
        if (data.length() < 1) {
            return shutDownAction("Wrong snapshot received: " + data);
        }

        // Clear previous orders, start fresh
        market.clearOrderBook();

        // Snapshot is an array of updates
        try {
            for (int i = 0; i < data.length(); ++i) {
                JSONArray values = data.getJSONArray(i);
                Event resp = parseOrderUpdate(market, values);
                if (resp != null) {
                    return resp;
                }
            }
        } catch (JSONException ex) {
            return shutDownAction("Error in BitFinex snapshot parsing:"
                    + ex.getMessage());
        }
        return null;
    }

    /**
     * Parse message that contains one update to the orderbook
     *
     * @param market
     * @param values
     * @return
     */
    private Event parseOrderUpdate(Market market, JSONArray values) {
        try {
            Decimal price = new Decimal(values.getDouble(0));
            int count = values.getInt(1);
            Decimal amount = new Decimal(values.getDouble(2));
            if (count > 0) {
                // BitFinex always reports the total updated amount, 
                // not the difference. Therefore we must set the final value, 
                // not increment
                if (amount.isPositive()) {
                    market.addBid(price, amount, count, false);
                } else if (amount.isNegative()) {
                    market.addAsk(price, amount.negate(), count, false);
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

    /**
     * Parse message that contains snapshot of trades
     *
     * @param market
     * @param data JSON array containing the items
     * @return
     */
    private Event parseTradeSnapshot(Market market, JSONArray data) {
        if (data.length() < 1) {
            return shutDownAction("Wrong snapshot received: " + data);
        }

        // Clear previous trades, start fresh
        market.clearTrades();

        // Snapshot is an array of updates
        try {
            for (int i = 0; i < data.length(); ++i) {
                JSONArray values = data.getJSONArray(i);
                Event resp = parseTrade(market, values);
                if (resp != null) {
                    return resp;
                }
            }
        } catch (JSONException ex) {
            return shutDownAction("Error in BitFinex snapshot parsing:"
                    + ex.getMessage());
        }
        return null;
    }

    /**
     * Parse message that contains one update - trade
     *
     * @param market
     * @param values
     * @return
     */
    private Event parseTrade(Market market, JSONArray values) {
        try {
            int tradeId = values.getInt(0);
            long timestampMs = values.getLong(1);
            Decimal amount = new Decimal(values.getDouble(2));
            boolean sellSide;
            if (amount.isNegative()) {
                // Negative amount means "this was a sell-side trade"
                amount = amount.negate();
                sellSide = true;
            } else {
                sellSide = false;
            }
            Decimal price = new Decimal(values.getDouble(3));
            Date time = new Date(timestampMs);
            Trade trade = new Trade(time, price, amount, sellSide);
            trade.setId(tradeId);
            market.addTrade(trade);

            return null;
        } catch (JSONException e) {
            Logger.log("Error while parsing JSON msg: " + values);
            return shutDownAction("Error in BitFinex update parsing:"
                    + e.getMessage());
        }
    }

    /**
     * Parse message that contains snapshot of price candles
     *
     * @param market
     * @param data JSON array containing the items
     * @param resolution a channel representing price candle resolution
     * @return
     */
    private Event parsePriceSnapshot(Market market, JSONArray data,
            Channel resolution) {
        if (data.length() < 1) {
            return shutDownAction("Wrong snapshot received: " + data);
        }
        Logger.log(data.length() + " prices in snapshot for resolution "
                + resolution);

        // Clear previous prices, start fresh
        market.clearPrices(resolution);

        // Snapshot is an array of updates
        try {
            for (int i = 0; i < data.length(); ++i) {
                JSONArray values = data.getJSONArray(i);
                Event resp = parsePrice(market, values, resolution);
                if (resp != null) {
                    return resp;
                }
            }
        } catch (JSONException ex) {
            return shutDownAction("Error in BitFinex snapshot parsing:"
                    + ex.getMessage());
        }
        return null;
    }

    /**
     * Parse message that contains one update - price candle
     *
     * @param market
     * @param values
     * @param resolution a channel representing price candle resolution
     * @return
     */
    private Event parsePrice(Market market, JSONArray values, Channel resolution) {
        try {
            long timestampMs = values.getLong(0);
            Decimal openPrice = new Decimal(values.getDouble(1));
            Decimal closePrice = new Decimal(values.getDouble(2));
            Decimal highPrice = new Decimal(values.getDouble(3));
            Decimal lowPrice = new Decimal(values.getDouble(4));
            Decimal amount = new Decimal(values.getDouble(5));
            // Convert resolution from Channel to minutes
            int resMin = PriceCandle.resolutionToMinutes(resolution);
            PriceCandle price = new PriceCandle(timestampMs, openPrice,
                    closePrice,
                    lowPrice, highPrice, amount, resMin);
            market.addPrice(resolution, price);

            return null;
        } catch (JSONException e) {
            Logger.log("Error while parsing JSON msg: " + values);
            return shutDownAction("Error in BitFinex update parsing:"
                    + e.getMessage());
        }
    }

    /**
     * Heart-beat message received from the server
     */
    private Event heartbeatReceived() {
        // TODO - Reset alarm timer
        // Logger.log("Heartbeat received");
        return null;
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

    /**
     * Converts a string channel ("1m", "30min", "1D", etc) to Channel.PRICES_xx
     *
     * @param resolution
     * @return the channel or null if string is incorrect
     */
    private Channel stringResolutionToChannel(String resolution) {
        switch (resolution) {
            case "1m":
                return Channel.PRICES_1MIN;
            case "5m":
                return Channel.PRICES_5MIN;
            case "15m":
                return Channel.PRICES_15MIN;
            case "30m":
                return Channel.PRICES_30MIN;
            case "1h":
                return Channel.PRICES_1H;
            case "3h":
                return Channel.PRICES_3H;
            case "6h":
                return Channel.PRICES_6H;
            case "12h":
                return Channel.PRICES_12H;
            case "1D":
                return Channel.PRICES_1D;
            case "7D":
                return Channel.PRICES_1W;
            default:
                return null;
        }
    }
}
