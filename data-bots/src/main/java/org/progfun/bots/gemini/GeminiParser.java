package org.progfun.bots.gemini;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.Decimal;
import org.progfun.Market;
import org.progfun.websocket.Parser;
import org.progfun.websocket.ParserResponse;

/**
 * Parses JSON updates from Gemini API
 */
public class GeminiParser extends Parser {
    private Market market;
    
    @Override
    public ParserResponse parseMessage(String message) {
        if (exchange == null) {
            return shutDownAction("Gemini msg received without exchange, ignoring");
        }
        if (market == null) {
            // initialize market on first use
            market = exchange.getFirstMarket();
        }
        if (market == null) {
            return shutDownAction("Gemini msg received without market, ignoring");
        }
                
        try {
            // Check events only inside messages with type=update
            JSONObject mainMsg = new JSONObject(message);
            String type = mainMsg.getString("type");
            if (type.equals("update")) {
                JSONArray events = mainMsg.getJSONArray("events");
                for (Object eo : events) {
                    if (eo instanceof JSONObject) {
                        JSONObject event = (JSONObject) eo;
                        ParserResponse resp = parseUpdateEvent(event);
                        if (resp != null) {
                            // If some action was needed as a result of parsing
                            // the event message, return it and skip parsing the rest
                            return resp;
                        }                                
                    } else {
                        System.out.println("Event not an object: " + eo);
                    }
                }
            }
        } catch (JSONException ex) {
            System.out.println("Error parsing JSON: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Parse one update event
     *
     * @param event
     */
    private ParserResponse parseUpdateEvent(JSONObject event) {
        try {
            String type = event.getString("type");
            if (type.equals("change")) {
                return parseOrderBookChange(event);
            } else if (type.equals("trade")) {
                return parseTradeEvent(event);
            }
        } catch (JSONException ex) {
            System.out.println("Error parsing JSON event: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Parse changes in 
     * @param event 
     */
    private ParserResponse parseOrderBookChange(JSONObject event) {
        Decimal price = new Decimal(event.getString("price"));
        boolean isBid = event.getString("side").equals("bid");
        String rs = event.getString("remaining");
        Decimal delta = new Decimal(event.getString("delta"));
        if (rs.equals("0")) {
            // Order removed, zero remaining
            if (isBid) {
                market.removeBid(price);
            } else {
                market.removeAsk(price);
            }
        } else {
            // Order added or updated
            if (isBid) {
                market.addBid(price, delta, 0);
            } else {
                market.addAsk(price, delta, 0);
            }
        }
        return null;
    }

    private ParserResponse parseTradeEvent(JSONObject event) {
        return shutDownAction("Gemini Trade parsing not supported yet");
    }
}
