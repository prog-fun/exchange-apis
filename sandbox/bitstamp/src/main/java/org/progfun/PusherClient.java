package org.progfun;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import java.util.Map;
import java.util.HashMap;

public class PusherClient {

    private Map<String, Channel> channels;
    private Pusher pusher;

    public PusherClient(String key) {

        channels = new HashMap<>();

        PusherOptions options = new PusherOptions();
        pusher = new Pusher(key, options);
        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                log("State changed from " + change.getPreviousState() + " to " + change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                log("Error: " + message + " (" + code + ")");
            }
        }, ConnectionState.ALL);

    }

    public void subscribe(String channel) {
        if (channels.containsKey(channel)) {
            log("Already subscribed to channel: " + channel);
            return;
        }
        channels.put(channel, pusher.subscribe(channel));
    }

    public void bind(String channel, String event, PusherListener listener) {
        if (!channels.containsKey(channel)) {
            return;
        }
        channels.get(channel).bind(event, listener);
    }

    public void disconnect() {
        // Note: If connect() is called again, all channels will be resubscribed.
        pusher.disconnect();
    }

    private static void log(String message) {
        System.out.println("[Thread #" + Thread.currentThread().getId() + "] Pusher: " + message);
    }

}
