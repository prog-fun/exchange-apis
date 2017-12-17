package org.progfun;

/**
 * Interface for listening on Exchange snapshots
 */
public interface SnapshotListener {

    /**
     * This method is called whenever a new snapshot of the exchange is created
     * @param exchange
     */
    public void onSnapshot(Exchange exchange);

}
