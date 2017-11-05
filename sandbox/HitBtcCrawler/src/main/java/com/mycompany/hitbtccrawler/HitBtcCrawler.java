package com.mycompany.hitbtccrawler;

import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.InvalidFormatException;
import org.progfun.Market;

/**
 *
 * @author Simon
 */
public class HitBtcCrawler extends WebSocketClient {

    private Market market;
    private static final String API_URL = "wss://api.hitbtc.com/api/2/ws";

    private static void log(String msg) {
        Thread t = Thread.currentThread();
        System.out.println(msg + "[Thread #" + t.getId() + "]");
    }

    HitBtcCrawler() throws URISyntaxException {
        super(new URI(API_URL));
        try {
            this.market = new Market("BTC", "USD");
        } catch (InvalidFormatException ex) {
            System.out.println("Invalid currencies for market: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        log("Connecting...");
        HitBtcCrawler client;
        try {
            client = new HitBtcCrawler();
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
            log("Sending request, awaiting feedback...");

            //Creating orderbook data request
            JSONObject obj = new JSONObject();
            obj.put("method", "subscribeOrderbook");
            JSONObject obj2 = new JSONObject();
            obj2.put("symbol", "BTCUSD");
            obj.put("params", obj2);
            obj.put("id", "123");
            String object = obj.toString();
            send(object);

            // The easiest (hacky) way to wait for the response: sleep for some time
            try {
                System.in.read();
            } catch (Exception e) {
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
            System.out.println("bids: " + market.getBids().size());
            System.out.println("asks: " + market.getAsks().size());
        } catch (JSONException e) {
            System.out.println("test" + e);
        }

        log(message);
        //log("Received: " + arr.getJSONArray(0).getString()
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
