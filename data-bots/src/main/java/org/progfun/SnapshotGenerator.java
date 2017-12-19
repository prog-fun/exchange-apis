package org.progfun;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Creates periodic snapshots of the market, notifies listeners
 */
public class SnapshotGenerator {

    private Exchange exchange;
    private SnapshotListener listener;
    private Timer timer;
    private final TimerTask task = new TimerTask() {
        @Override
        public void run() {
            notifyListener();
        }
    };

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
        stop(); // Stop previous timer if one running
        timer = new Timer(true);
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

    /**
     * Stop the scheduled timer
     */
    public void stop() {
        if (timer != null) {
            Logger.log("Stopping scheduled timer task...");
            timer.cancel();
            timer = null;
        }
    }

}
