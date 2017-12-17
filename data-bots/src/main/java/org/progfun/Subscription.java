package org.progfun;

/**
 * Subscription for updates to a particular channel, for particular currency
 * pair
 */
public class Subscription {

    private final CurrencyPair currencyPair;
    private final Channel channel;
    private SubsState state;

    public Subscription(CurrencyPair currencyPair, Channel channel) {
        this(currencyPair, channel, SubsState.INACTIVE);
    }

    public Subscription(CurrencyPair currencyPair, Channel channel, SubsState state) {
        this.currencyPair = currencyPair;
        this.channel = channel;
        this.state = state;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public Channel getChannel() {
        return channel;
    }

    public SubsState getState() {
        return state;
    }

    public void setState(SubsState state) {
        this.state = state;
    }
}
