package org.progfun;

import com.pusher.client.channel.SubscriptionEventListener;
import org.progfun.orderbook.Orderbook;

public class PusherListener implements SubscriptionEventListener {

    private BitstampParser parser;
    private Orderbook orderbook;

    public PusherListener(BitstampParser parser, Orderbook orderbook) {
        this.parser = parser;
        this.orderbook = orderbook;
    }

    @Override
    public void onEvent(String channel, String event, String data) {
        parser.parse(data, orderbook);
    }
}
