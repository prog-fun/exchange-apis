package org.progfun.bots.hitbtc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.Decimal;
import org.progfun.websocket.Parser;

/**
 *
 * @author Simon
 */
public class HitBtcParser extends Parser {

    @Override
    public void parseMessage(String message) {
        JSONObject msg = new JSONObject(message);
        try {

            JSONObject params = msg.getJSONObject("params");
            if (params.has("ask")) {
                JSONArray asks = params.getJSONArray("ask");
                for (Object obj : asks) {
                    JSONObject ask = (JSONObject) obj;
                    String askSizeString = ask.getString("size");
                    Decimal askSize = new Decimal(askSizeString);
                    Decimal askPrice = new Decimal(ask.getString("price"));
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
                    Decimal bidSize = new Decimal(bidSizeString);
                    Decimal bidPrice = new Decimal(bid.getString("price"));
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
