/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.hitbtccrawler;

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
 *
 * @author Simon
 */
public class HitBtcCrawler extends WebSocketClient {

    private Orderbook orderBook = new Orderbook();
    private static final String API_URL = "wss://api.hitbtc.com/api/2/ws";

    private static void log(String msg) {
        Thread t = Thread.currentThread();
        System.out.println(msg + "[Thread #" + t.getId() + "]");
    }

    HitBtcCrawler() throws URISyntaxException {
        super(new URI(API_URL));
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
        System.out.println(msg);
        /*  Iterator it = msg.keys();
        JSONArray arr = new JSONArray(); 
        while (it.hasNext()) { 
            String key = (String) it.next(); 
            arr.put(msg.get(key));
        } */
        try {
            if (msg.getString("method").equals("snapshotOrderbook")) {
                /*if (msg.getJSONObject("params").getString("ask")) {
                
                }*/
                JSONObject params = msg.getJSONObject("params");
                JSONArray asks = params.getJSONArray("ask");
                for (Object obj : asks) {
                    JSONObject ask = (JSONObject) obj;
                    double price = ask.getDouble("price");
                    double size = ask.getDouble("size");
                    orderBook.addAsk(price, size, 0);
                    
                }
                JSONArray bids = params.getJSONArray("bid");
                for (Object obj : bids) {
                    JSONObject bid = (JSONObject) obj;
                    double price = bid.getDouble("price");
                    double size = bid.getDouble("size");
                    orderBook.addAsk(price, size, 0);
                    
                }
            } else {
                JSONObject params = msg.getJSONObject("params");
                if (params.has("ask")) {
                    JSONObject ask = params.getJSONObject("ask");
                    // TODO - loop
                    double askSize = ask.getJSONObject(0).getDouble("size");
                    double askPrice = ask.getJSONObject(0).getDouble("price");
                }
                
                double bidSize = msg.getJSONObject("params").getJSONArray("bid").getJSONObject(0).getDouble("size");
                double bidPrice = msg.getJSONObject("params").getJSONArray("bid").getJSONObject(0).getDouble("price");
                orderBook.addBid(bidPrice, bidSize, 0);
                orderBook.addAsk(askPrice, askSize, 0);
            }
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
