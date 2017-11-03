package org.progfun;

import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Example WebSocket client subscribing to BitFinex stream
 */
public class BitFinexWSClient extends WebSocketClient {

    private static final String API_URL = "wss://api.bitfinex.com/ws/2";
    
    private int CONNECTING = 0;
    private int GET_VERSION = 1;
    private int SUBSCRIBE = 2;
    private int SNAPSHOT = 3;
    private int UPDATING = 4;
    
    private int state = 0;

    private static void log(String msg) {
        Thread t = Thread.currentThread();
        System.out.println(msg + "[Thread #" + t.getId() + "]");
    }

    BitFinexWSClient() throws URISyntaxException {
        super(new URI(API_URL));
    }

    public static void main(String[] args) {
        log("Connecting...");
        BitFinexWSClient client;
        try {
            client = new BitFinexWSClient();
            client.start();
            
        } catch (URISyntaxException ex) {
            log("Something wrong with URL: " + ex.getMessage());
        }
    }

    public void start() {
        state++;
        try {
            if (!connectBlocking()) {
                log("Could not connect to WebSocket server");
            }
            send("{\"event\":\"subscribe\", \"channel\":\"book\", \"symbol\":\"tBTCUSD\", \"prec\":\"P0\", \"freq\":\"F0\", \"len\":\"25\"}");
            // The easiest (hacky) way to wait for the response: sleep for some time
            Thread.sleep(10000);
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
        log("Received: " + message);
        if(state == SNAPSHOT) {
            state++;
        } else if (state != UPDATING) {
            state++;
        }else {
            parseAndLog(message);
        }
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
    
    public void parseAndLog(String message) {
        try {
        JSONArray data = new JSONArray(message);
        JSONArray values = data.getJSONArray(1);
        double string = values.getDouble(0);
        System.out.println("" + string);
        } catch (Exception e) {
            System.out.println("ops");
        }
        
    }
}
