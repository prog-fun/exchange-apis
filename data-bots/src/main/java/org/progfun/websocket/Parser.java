package org.progfun.websocket;

import org.progfun.Channel;
import org.progfun.Exchange;
import org.progfun.Subscriptions;

/**
 * Abstract API command parser
 */
public abstract class Parser {

    protected Exchange exchange;
    protected Subscriptions subscriptions;

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    /**
     * Set subscriptions that will be used for this parser. No activation is
     * done at this moment!
     *
     * @param subscriptions
     */
    public void setSubscriptions(Subscriptions subscriptions) {
        this.subscriptions = subscriptions;
    }

    /**
     * Generate a temporary ID for inactive subscription. Used to identify
     * subscription from exchange response message
     *
     * @param symbol
     * @param channel
     * @return
     */
    public static String getInactiveSubsSymbol(String symbol, Channel channel) {
        return symbol + "-" + channel.toString();
    }

    /**
     * This method is called by a connector when a message arrives. It may be
     * called on another thread.
     *
     * @param message
     * @return Action that the Handler should perform as a response to the
     * received message. This interface allows parser to notify that a reconnect
     * is needed, etc. When no action is needed, return null.
     */
    public abstract Action parseMessage(String message);
}
