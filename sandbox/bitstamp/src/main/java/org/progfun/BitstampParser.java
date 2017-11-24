package org.progfun;

import org.json.*;
import org.progfun.connector.AbstractParser;

public class BitstampParser extends AbstractParser {

    public void onMessage(String jsonString) {
        try {

            JSONObject json = new JSONObject(jsonString);

            String timestamp = json.getString("timestamp");

            JSONArray bids = json.getJSONArray("bids");
            JSONArray asks = json.getJSONArray("asks");

            for (int i = 0; i < bids.length(); i++) {

                JSONArray bid = bids.getJSONArray(i);
                String price = bid.getString(0);
                String amount = bid.getString(1);

                if (amount.equals("0")) {
                    market.removeBid(Double.valueOf(price));
                    log("Bid removed. " + amount + " at " + price);
                } else {
                    market.addBid(Double.valueOf(price), Double.valueOf(amount), 1);
                    log("Bid added. " + amount + " at " + price);
                }
            }

            for (int i = 0; i < asks.length(); i++) {

                JSONArray ask = asks.getJSONArray(i);
                String price = ask.getString(0);
                String amount = ask.getString(1);

                if (amount.equals("0")) {
                    market.removeAsk(Double.valueOf(price));
                    log("Ask removed. " + amount + " at " + price);
                } else {
                    market.addAsk(Double.valueOf(price), Double.valueOf(amount), 1);
                    log("Ask added. " + amount + " at " + price);
                }
            }

        } catch (JSONException e) {
            log(e.getMessage());
        }
    }

    private static void log(String message) {
//        System.out.println("[Thread #" + Thread.currentThread().getId() + "] BitstampParser: " + message);
    }

}
