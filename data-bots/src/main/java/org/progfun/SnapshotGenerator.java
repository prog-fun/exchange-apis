package org.progfun;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.progfun.websocket.WebSocketHandler;

/**
 * Creates periodic snapshots of the market, notifies listeners
 */
public class SnapshotGenerator {

    private Exchange exchange;
    private final List<SnapshotListener> listeners = new LinkedList<>();
    private Timer timer;
    private final boolean deleteTrades;
    private final boolean deletePrices;
    private final WebSocketHandler handler;

    // Notifier that will execute notification function on the Handler thread
    private final Runnable notifier = () -> {
        notifyListener();
    };

    /**
     * Create a new snapshot generator
     *
     * @param handler associated WebSocket handler
     * @param deleteTrades when true, trades will be cleared after each snapshot
     * @param deletePrices when true, prices will be cleared after each snapshot
     */
    public SnapshotGenerator(WebSocketHandler handler, boolean deleteTrades,
            boolean deletePrices) {
        this.deleteTrades = deleteTrades;
        this.handler = handler;
        this.deletePrices = deletePrices;
    }

    private TimerTask task;

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    /**
     * Add a listener for snapshot notifications
     * @param listener 
     */
    public void addListener(SnapshotListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a listener, it will not receive notifications anymore
     * @param listener 
     */
    public void removeListener(SnapshotListener listener) {
        listeners.remove(listener);
    }

    /**
     * Schedule snapshot generation at regular intervals
     *
     * @param interval interval in milliseconds
     */
    public void schedule(long interval) {
        stop(); // Stop previous timer if one running
        timer = new Timer(true);
        // We must create new task every time. Otherwise we get 
        // "IllegalStateException" the 2nd time we schedule the same task
        task = new TimerTask() {
            @Override
            public void run() {
                // We want to handle the notification on the main HandlerThread
                handler.scheduleExecution(notifier);
            }
        };
        timer.scheduleAtFixedRate(task, interval, interval);
        Logger.log("Snapshot generator started");
    }

    /**
     * Send snapshot of the market to the listeners
     */
    private void notifyListener() {
        if (exchange == null || listeners.isEmpty()) {
            return;
        }
        for (SnapshotListener listener : listeners) {
            listener.onSnapshot(exchange);
        }
        if (deleteTrades) {
            // Delete all trades
            exchange.clearTrades();
        }
        if (deletePrices) {
            exchange.clearPrices();
        }
    }

    /**
     * Stop the scheduled timer
     */
    public void stop() {
        if (timer != null) {
            Logger.log("Stopping snapshot generator...");
            timer.cancel();
            timer = null;
        }
    }

}
