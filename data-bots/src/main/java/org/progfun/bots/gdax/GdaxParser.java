package org.progfun.bots.gdax;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.progfun.connector.AbstractParser;

/**
 * Handles the responses from the GDAX API and fills the orderbook with
 * information from the responses
 */
public class GdaxParser extends AbstractParser {

    /**
     * Handles the incoming messages from the API and puts it in the orderbook
     * See message documentation: https://docs.gdax.com/#websocket-feed 
     * @param message The incoming message
     */
    @Override
    public void onMessage(String message) {
        JSONObject JSONMessage = new JSONObject(message);
        Iterator it = JSONMessage.keys();
        JSONArray jsonArray = new JSONArray();
        while (it.hasNext()) {
            String key = (String) it.next();
            jsonArray.put(JSONMessage.get(key));
        }

        if (JSONMessage.getString("type").equals("snapshot")) {
            for (Object keys : jsonArray.getJSONArray(1)) {
                JSONArray bids = (JSONArray) keys;
                market.addBid(bids.getDouble(0), bids.getDouble(1), 0);
            }
            for (Object keys : jsonArray.getJSONArray(2)) {
                JSONArray asks = (JSONArray) keys;
                market.addAsk(asks.getDouble(0), asks.getDouble(1), 0);
            }

        } else if (JSONMessage.getString("type").equals("l2update")) {
            String type = jsonArray.getJSONArray(1).getJSONArray(0).getString(0);
            double price = jsonArray.getJSONArray(1).getJSONArray(0).getDouble(1);
            String countString = jsonArray.getJSONArray(1).getJSONArray(0).getString(2);

            if (countString.equals("0")) {
                if (type.equals("buy")) {
                    market.removeAsk(price);
                } else if (type.equals("sell")) {
                    market.removeBid(price);

                }
            } else {
                Double count = Double.parseDouble(countString);
                if (type.equals("buy")) {
                    market.addAsk(price, count, 0);
                } else if (type.equals("sell")) {
                    market.addBid(price, count, 0);
                }
            }
        }
    }

}
