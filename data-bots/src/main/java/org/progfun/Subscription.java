package org.progfun;

/**
 * Subscription for updates to a particular channel, for particular currency
 * pair (market)
 */
public class Subscription {

    private final Market market;
    private final Channel channel;
    private SubsState state;
    private String id;

    /**
     * Get unique ID for this subscription
     * @return 
     */
    public String getId() {
        return id;
    }

    /**
     * Set a unique ID for this subscription
     * @param id 
     */
    public void setId(String id) {
        this.id = id;
    }

    public Subscription(Market market, Channel channel) {
        this(market, channel, SubsState.INACTIVE);
    }

    public Subscription(Market market, Channel channel, SubsState state) {
        this.market = market;
        this.channel = channel;
        this.state = state;
    }

    public Market getMarket() {
        return market;
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
