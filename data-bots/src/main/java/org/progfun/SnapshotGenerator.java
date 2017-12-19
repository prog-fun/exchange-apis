package org.progfun;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Creates periodic snapshots of the market, notifies listeners
 */
public class SnapshotGenerator {

    private Exchange exchange;
    private SnapshotListener listener;

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
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
        if (exchange == null || listener == null) {
            return;
        }
        // Disable updates while listeners process the snapshot
        exchange.lockUpdates();
        listener.onSnapshot(exchange);        
        exchange.allowUpdates();
    }

}
