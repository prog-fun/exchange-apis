package org.progfun.bots.bitfinex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.connector.AbstractParser;

/**
 *
 * @author olavt
 */
public class BitFinexParser extends AbstractParser {
    
    private final int GET_VERSION = 0;
    private final int SUBSCRIBE = 1;
    private final int SNAPSHOT = 2;
    private final int UPDATING = 3;

    private int state = 0;
    
    @Override
    public void onMessage(String message) {
//        System.out.println("Received: " + message);
        bitFinexWSClientStateMachine(message);
    }

    @Override
    public void onError(Exception excptn) {
        System.out.println("Error: " + excptn.getMessage());
    }
    
    private void bitFinexWSClientStateMachine(String message) {

        switch (state) {
            case GET_VERSION:
                if (new JSONObject(message).getString("event").equals("info")) {
                    state++;
                }
                break;

            case SUBSCRIBE:
                if (new JSONObject(message).getString("event").equals("subscribed")) {
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
            JSONArray values = data.getJSONArray(1);
            double price = values.getDouble(0);
            int count = (int) values.getDouble(1);
            double amount = values.getDouble(2);
//            System.out.println("Price: " + price + ", Count: " + count + ", Amount: " + amount);
            if (count > 0) {
                // BitFinex always reports the total updated amount, 
                // not the difference. Therefore we must first remove the
                // old order and then add it
                if (amount > 0) {
                    market.removeBid(price);
                    market.addBid(price, amount, count);
                } else if (amount < 0) {
                    market.removeAsk(price);
                    market.addAsk(price, -amount, count);
                }
            } else if (count == 0) {
                if (amount == 1) {
                    market.removeBid(price);
                } else if (amount == -1) {
                    market.removeAsk(price);
                }
            }
            // TODO - test if parsing works correctly
        } catch (JSONException e) {
//            System.out.println("ops");
        }

//        System.out.println("Ask: " + market.getAsks().size());
//        System.out.println("Bid: " + market.getBids().size());
    }

    private void addSnapshot(String message) {
        JSONArray data = new JSONArray(message);
        JSONArray array = data.getJSONArray(1);
        for (Object json : array) {
            JSONArray values = (JSONArray) json;
            double price = values.getDouble(0);
            int count = (int) values.getDouble(1);
            double amount = values.getDouble(2);
            if (count > 0) {
                if (amount > 0) {
                    market.addBid(price, amount, count);
                } else if (amount < 0) {
                    market.addAsk(price, -amount, count);
                }
            }
        }
//        System.out.println(market.getAsks().size());
//        System.out.println(market.getBids().size());
    }
    
}
