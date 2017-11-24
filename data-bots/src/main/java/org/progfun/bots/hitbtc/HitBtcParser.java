package org.progfun.bots.hitbtc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.connector.AbstractParser;

/**
 *
 * @author Simon
 */
public class HitBtcParser extends AbstractParser {

    @Override
    public void onMessage(String message) {
        JSONObject msg = new JSONObject(message);
        try {

            JSONObject params = msg.getJSONObject("params");
            if (params.has("ask")) {
                JSONArray asks = params.getJSONArray("ask");
                for (Object obj : asks) {
                    JSONObject ask = (JSONObject) obj;
                    String askSizeString = ask.getString("size");
                    double askSize = Double.parseDouble(askSizeString);
                    double askPrice = ask.getDouble("price");
                    if (askSizeString.equals("0.00")) {
                        market.removeAsk(askPrice);
                    } else {
                        market.addAsk(askPrice, askSize, 0);
                    }
                }

            }
            if (params.has("bid")) {
                JSONArray bids = params.getJSONArray("ask");
                for (Object obj : bids) {
                    JSONObject bid = (JSONObject) obj;
                    String bidSizeString = bid.getString("size");
                    double bidSize = Double.parseDouble(bidSizeString);
                    double bidPrice = bid.getDouble("price");
                    if (bidSizeString.equals("0.00")) {
                        market.removeBid(bidPrice);
                    } else {
                        market.addBid(bidPrice, bidSize, 0);
                    }
                }

            }
//            System.out.println("bids: " + market.getBids().size());
//            System.out.println("asks: " + market.getAsks().size());
        } catch (JSONException e) {
            System.out.println("test" + e);
        }
    }
}
