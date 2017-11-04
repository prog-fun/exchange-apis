package org.progfun;

import org.progfun.orderbook.Order;
import org.progfun.orderbook.Orderbook;

import org.json.*;

public class BitstampParser {

    public void parse(String jsonString, Orderbook orderbook) {
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
                    orderbook.removeBid(Double.valueOf(price));
                    log("Bid removed. " + amount + " at " + price);
                } else {
                    orderbook.addBid(Double.valueOf(price), Double.valueOf(amount), 1);
                    log("Bid added. " + amount + " at " + price);
                }
            }

            for (int i = 0; i < asks.length(); i++) {

                JSONArray ask = asks.getJSONArray(i);
                String price = ask.getString(0);
                String amount = ask.getString(1);

                if (amount.equals("0")) {
                    orderbook.removeAsk(Double.valueOf(price));
                    log("Ask removed. " + amount + " at " + price);
                } else {
                    orderbook.addAsk(Double.valueOf(price), Double.valueOf(amount), 1);
                    log("Ask added. " + amount + " at " + price);
                }
            }

        } catch (JSONException e) {
            log(e.getMessage());
        }
    }

    private static void log(String message) {
        System.out.println("[Thread #" + Thread.currentThread().getId() + "] BitstampParser: " + message);
    }

}
