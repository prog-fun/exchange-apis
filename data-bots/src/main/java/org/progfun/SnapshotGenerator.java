package org.progfun;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Creates periodic snapshots of the market, notifies listeners
 */
public class SnapshotGenerator {

    private Market market;
    private SnapshotListener listener;

    public void setMarket(Market market) {
        this.market = market;
    }
    
    public void setListener(SnapshotListener listener) {
        this.listener = listener;
    }

    /**
     * Schedule snapshot generation at regular intervals
     *
     * @param interval interval in milliseconds
     */
    public void schedule(long interval) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                notifyListener();
            }
        };
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(task, interval, interval);

    }

    /**
     * Send snapshot of the market to the listeners
     */
    private void notifyListener() {
        if (market == null || listener == null) {
            return;
        }
        // Disable updates while listeners process the snapshot
        market.lockUpdates();
        listener.onSnapshot(market);        
        market.allowUpdates();
    }

}
