package org.progfun;

import com.pusher.client.channel.SubscriptionEventListener;

public class PusherListener implements SubscriptionEventListener {

    private BitstampParser parser;

    public PusherListener(BitstampParser parser) {
        this.parser = parser;
    }

    @Override
    public void onEvent(String channel, String event, String data) {
        parser.onMessage(data);
    }
}
