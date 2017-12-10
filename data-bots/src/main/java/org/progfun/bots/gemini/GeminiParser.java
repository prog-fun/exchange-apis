package org.progfun.bots.gemini;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.Decimal;
import org.progfun.connector.Parser;

/**
 * Parses JSON updates from Gemini API
 */
public class GeminiParser extends Parser {

    @Override
    public void parseMessage(String message) {
//        System.out.println("Received: " + message);
        if (market == null) {
            System.out.println("Message received without orderbook, ignoring");
            return;
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
                        parseUpdateEvent(event);
                    } else {
                        System.out.println("Event not an object: " + eo);
                    }
                }
            }
        } catch (JSONException ex) {
            System.out.println("Error parsing JSON: " + ex.getMessage());
        }
    }

    /**
     * Parse one update event
     *
     * @param event
     */
    private void parseUpdateEvent(JSONObject event) {
        String rs = null;
        try {
            String type = event.getString("type");
            if (type.equals("change")) {
                Decimal price = new Decimal(event.getString("price"));
                boolean isBid = event.getString("side").equals("bid");
                rs = event.getString("remaining");
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
            }
        } catch (NumberFormatException ex) {
            System.out.println("Remaining amount not a number: " + rs);
        } catch (JSONException ex) {
            System.out.println("Error parsing JSON event: " + ex.getMessage());
        }
    }
}
