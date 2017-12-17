package org.progfun;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a list of subscriptions for Exchange API
 */
public class Subscriptions {

    private final List<Subscription> subs = new ArrayList<>();

    /**
     * Add a new subscription
     *
     * @param pair currency pair representing the market
     * @param channel type of subscription (orderbook, trades, ...)
     */
    public void add(CurrencyPair pair, Channel channel) {
        subs.add(new Subscription(pair, channel));
    }

    /**
     * Return first inactive subscription or null if all are activated (or in
     * progress)
     *
     * @return
     */
    public Subscription getNextInactive() {
        for (Subscription s : subs) {
            if (s != null && s.getState() == SubsState.INACTIVE) {
                return s;
            }
        }
        return null;
    }

}
