package org.progfun;

/**
 * Interface for listening on Market snapshots
 */
public interface SnapshotListener {

    /**
     * This method is called whenever a new snapshot of the market is created
     * @param market 
     */
    public void onSnapshot(Market market);

}
