package main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import org.progfun.connector.WebSocketConnector;
import org.progfun.orderbook.Orderbook;

/**
 * Example WebSocket client subscribing to BitFinex stream
 */
public class GdaxWSClient {

    private static final String API_URL = "wss://ws-feed.gdax.com";
    private Orderbook orderbook = new Orderbook();
    private WebSocketConnector WebSocket = new WebSocketConnector();

    private static void log(String msg) {
        Thread t = Thread.currentThread();
        System.out.println(msg + "[Thread #" + t.getId() + "]");
    }

    GdaxWSClient() throws URISyntaxException {
        WebSocket.start(API_URL);

    }

    public static void main(String[] args) {
        log("Connecting...");
        GdaxWSClient client;
        try {
            client = new GdaxWSClient();
            client.start();
        } catch (URISyntaxException ex) {
            log("Something wrong with URL: " + ex.getMessage());
        }
    }

    public void start() {

        WebSocket.send("{\"type\": \"subscribe\",\"product_ids\": [\"BTC-USD\"],\"channels\": [\"level2\"]}");
        try {
            System.in.read();
        } catch (IOException e) {
        }

        WebSocket.stop();

        log("Connection was interrupted");

    }
    /**
     * Gets called when a message is received, performes all required logic on
     * the message and sends it to the orderbook
     */
    public void onMessage() {
        String message = WebSocket.
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

        //log("Received: " + jsonArray);
    }
}
