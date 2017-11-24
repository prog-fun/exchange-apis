package org.progfun.bots;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.progfun.Market;
import org.progfun.connector.AbstractWebSocketHandler;

/**
 * A class that is able to run a Bot
 */
public class BotRunner extends Thread {

    private final AbstractWebSocketHandler handler;
    private final Market market;

    private boolean isRunning; // A flag used to mark when the thread has to stop

    /**
     * Create a bot for specific exchange and market
     *
     * @param handler
     * @param market
     */
    public BotRunner(AbstractWebSocketHandler handler, Market market) {
        this.handler = handler;
        this.market = market;
        this.handler.setMarket(market);
    }

    /**
     * Launch the crawler bot
     */
    @Override
    public void run() {
        try {
            if (handler.connect()) {
                handler.sendInitCommands(); // Send the "subscribe" commands
                isRunning = true;
                // Wait until another thread will terminate this one
                synchronized (this) {
                    while (isRunning) {
                        wait();
                    }
                }
                handler.disconnect();
            }
        } catch (InterruptedException ex) {
            System.out.println("Runner Thread interrrupted");
        }
    }

    /**
     * Notify the thread that it has to stop
     */
    public synchronized void terminate() {
        isRunning = false;
        notifyAll();
    }
}
