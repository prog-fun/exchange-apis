package org.progfun;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Holds a list of subscriptions for Exchange API
 */
public class Subscriptions {

    // Active subscriptions
    private final Map<String, Subscription> activeSubs = new HashMap<>();
    // Inactive subscriptions
    private final List<Subscription> inactiveSubs = new LinkedList<>();

    /**
     * Add a new subscription, inactive by default
     *
     * @param market
     * @param channel type of subscription (orderbook, trades, ...)
     * @return the newly created subscription
     */
    public Subscription addInactive(Market market, Channel channel) {
        Subscription s = new Subscription(market, channel);
        addInactive(s);
        return s;
    }
    
    /**
     * Add an inactive subscription
     * @param s
     * @return true if subscription added, false otherwise
     */
    public boolean addInactive(Subscription s) {
        if (s == null) {
            return false;
        }
        s.setState(SubsState.INACTIVE);
        if (!inactiveSubs.contains(s)) {
            inactiveSubs.add(s);
            return true;
        }
        return false;
    }

    /**
     * Return first inactive subscription or null if all are activated (or in
     * progress)
     *
     * @return
     */
    public Subscription getNextInactive() {
        if (!inactiveSubs.isEmpty()) {
            return inactiveSubs.get(0);
        } else {
            return null;
        }
    }

    /**
     * Register that a subscription is active, and assign a unique ID to it.
     * Later the ID can be used to find the subscription again.
     *
     * @param subsId any kind of unique identifier for the subscription. Some
     * Exchanges issue a unique number, some use currency pair.
     * @param subscription
     */
    public void activate(String subsId, Subscription subscription) {
        Logger.log("Activating " + subscription.getChannel()
                + " subscription for "
                + subscription.getMarket().getCurrencyPair());
        // Move the ssubscription from inactive list to active list
        inactiveSubs.remove(subscription);
        activeSubs.put(subsId, subscription);
        subscription.setId(subsId);
    }

    /**
     * Set temporary ID for an inactive subscriptions
     *
     * @param subsId
     * @param s
     * @return true when ID successfully set, false otherwise
     */
    public boolean setInactiveId(String subsId, Subscription s) {
        Logger.log("Storing inactive subscription with ID " + subsId);
        if (s != null && inactiveSubs.contains(s)) {
            s.setId(subsId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Find an inactive subscription by ID
     *
     * @param subsId
     * @return the subscription or null if none found
     */
    public Subscription getInactive(String subsId) {
        if (subsId == null) {
            return null;
        }
        for (Subscription s : inactiveSubs) {
            if (subsId.equals(s.getId())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Return active subscription identified by a particular ID. Warning: search
     * is performed only among ACTIVE subscriptions!
     *
     * @param subsId subscription ID
     * @return subscription or null if no active subscription with give ID found
     */
    public Subscription getActive(String subsId) {
        return activeSubs.get(subsId);
    }

    /**
     * Mark all subscriptions as inactive
     */
    public void inactivateAll() {
        for (Subscription s: activeSubs.values()) {
            s.setId(null);
            s.setState(SubsState.INACTIVE);
            addInactive(s);
        }
        activeSubs.clear();
    }

}
