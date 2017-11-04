package org.progfun.gemini;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.Market;
import org.progfun.connector.Parser;

/**
 * Parses JSON updates from Gemini API
 */
public class GeminiParser implements Parser {

    private Market market;

    public void setMarket(Market market) {
        this.market = market;
    }

    @Override
    public void onMessage(String message) {
        if (market == null) {
            System.out.println("Message received without orderbook, ignoring");
            return;
        }
        try {
            System.out.println("Received: " + message);
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

    @Override
    public void onError(Exception excptn) {
        System.out.println("Error: " + excptn);
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
                Double price = event.getDouble("price");
                boolean isBid = event.getString("side").equals("bid");
                rs = event.getString("remaining");
                Double amount = Double.parseDouble(rs);
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
                        market.addBid(price, amount, 0);
                    } else {
                        market.addAsk(price, amount, 0);
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
