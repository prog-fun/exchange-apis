package org.progfun;

import java.util.Timer;
import java.util.TimerTask;
import org.progfun.websocket.WebSocketHandler;

/**
 * Creates periodic snapshots of the market, notifies listeners
 */
public class SnapshotGenerator {

    private Exchange exchange;
    private SnapshotListener listener;
    private Timer timer;
    private final boolean deleteTrades;
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
     */
    public SnapshotGenerator(WebSocketHandler handler, boolean deleteTrades) {
        this.deleteTrades = deleteTrades;
        this.handler = handler;
    }

    private TimerTask task;

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
        if (exchange == null || listener == null) {
            return;
        }
        listener.onSnapshot(exchange);
        if (deleteTrades) {
            // Delete all trades
            exchange.clearTrades();
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
