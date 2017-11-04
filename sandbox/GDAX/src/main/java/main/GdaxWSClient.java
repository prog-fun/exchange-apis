package main;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.orderbook.Orderbook;

/**
 * Example WebSocket client subscribing to BitFinex stream
 */
public class GdaxWSClient extends WebSocketClient {

    private static final String API_URL = "wss://ws-feed.gdax.com";
    private Orderbook orderbook = new Orderbook();

    private static void log(String msg) {
        Thread t = Thread.currentThread();
        System.out.println(msg + "[Thread #" + t.getId() + "]");
    }

    GdaxWSClient() throws URISyntaxException {
        super(new URI(API_URL));
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
        try {
            if (!connectBlocking()) {
                log("Could not connect to WebSocket server");
            }
            //send("{\"type\": \"subscribe\",\"product_ids\": [\"BTC-USD\"],\"channels\": [\"level2\",\"heartbeat\",{\"name\": \"ticker\", \"product_id\": [\"BTC-USD\"]}]}");
            send("{\"type\": \"subscribe\",\"product_ids\": [\"BTC-USD\"],\"channels\": [\"level2\"]}");
            // The easiest (hacky) way to wait for the response: sleep for some time
            //Thread.sleep(10000);
            try {
                System.in.read();
            } catch (IOException e) {
            }

            close();

        } catch (InterruptedException ex) {
            log("Connection was interrupted");
        }
    }

    /* Some code taken from 
       https://github.com/TooTallNate/Java-WebSocket/blob/master/src/main/example/ExampleClient.java
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log("Opened connection");
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage(String message) {
        JSONObject JSONMessage = new JSONObject(message);
        Iterator it = JSONMessage.keys();
        JSONArray jsonArray = new JSONArray();
        while (it.hasNext()) {
            String key = (String) it.next();
            jsonArray.put(JSONMessage.get(key));
        }
        try {
            log("Received: " + jsonArray.getJSONArray(1).getJSONArray(0));
            String type = jsonArray.getJSONArray(1).getJSONArray(0).getString(0);
            float price = Float.parseFloat(jsonArray.getJSONArray(1).getJSONArray(0).getString(1));
            float count = Float.parseFloat(jsonArray.getJSONArray(1).getJSONArray(0).getString(2));
            if (count == 0f) {
                if (type.equals("buy")) {
                    orderbook.removeAsk(price);
                } else if (type.equals("sell")) {
                    orderbook.removeBid(price);
                }
            } else {
                if (type.equals("buy")) {
                    orderbook.addAsk(price, count, 0);
                } else if (type.equals("sell")) {
                    orderbook.addBid(price, count, 0);
                }
            }
        } catch (JSONException e) {
            System.out.println("JSON not recognized");
        }

        //log("Received: " + jsonArray);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        log("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }

}
