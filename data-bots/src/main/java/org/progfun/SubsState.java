package org.progfun;

/**
 * Subscription state
 */
public enum SubsState {
    INACTIVE, // Subscription not actuve
    INITIATED, // Request sent to Exchange, no answer received yet
    ACTIVE // Subscription established
}
