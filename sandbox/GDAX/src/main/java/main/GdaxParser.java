/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.progfun.connector.Parser;
import org.progfun.orderbook.Orderbook;

/**
 *
 * @author randr
 */
public class GdaxParser implements Parser {

    private Orderbook orderbook = new Orderbook();

    @Override
    public void onMessage(String message) {
        System.out.println(message);
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
                orderbook.addBid(bids.getDouble(0), bids.getDouble(1), 0);
            }
            for (Object keys : jsonArray.getJSONArray(2)) {
                JSONArray asks = (JSONArray) keys;
                orderbook.addAsk(asks.getDouble(0), asks.getDouble(1), 0);
            }

        } else if (JSONMessage.getString("type").equals("l2update")) {
            String type = jsonArray.getJSONArray(1).getJSONArray(0).getString(0);
            double price = jsonArray.getJSONArray(1).getJSONArray(0).getDouble(1);
            String countString = jsonArray.getJSONArray(1).getJSONArray(0).getString(2);

            if (countString.equals("0")) {
                if (type.equals("buy")) {
                    orderbook.removeAsk(price);
                } else if (type.equals("sell")) {
                    orderbook.removeBid(price);

                }
            } else {
                Double count = Double.parseDouble(countString);
                if (type.equals("buy")) {
                    orderbook.addAsk(price, count, 0);
                } else if (type.equals("sell")) {
                    orderbook.addBid(price, count, 0);
                }
            }
        }
        System.out.println("Number of bids: " + orderbook.getBids().size());
        System.out.println("Number of asks: " + orderbook.getAsks().size());
    }

    @Override
    public void onError(Exception excptn) {
        System.out.println(excptn.toString());
    }

}
