package main;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.progfun.InvalidFormatException;
import org.progfun.Market;
import org.progfun.connector.Parser;

/**
 * Handles the responses from the GDAX API and fills the orderbook with information
 * from the responses
 */
public class GdaxParser implements Parser {

    private Market market;

    public GdaxParser() {
        try {
            market = new Market("BTC", "USD");
        } catch (InvalidFormatException ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * Handles the incoming messages from the API and puts it in the orderbook
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
            System.out.println(jsonArray);
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
        System.out.println(market.getAsks().size());
        System.out.println(market.getBids().size());
    }
    /**
     * Prints out errors from the API
     * @param excptn The incoming exception
     */
    @Override
    public void onError(Exception excptn) {
        System.out.println(excptn.toString());
    }

}
