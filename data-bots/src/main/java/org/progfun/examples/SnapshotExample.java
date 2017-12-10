package org.progfun.examples;

import java.io.IOException;
import org.progfun.InvalidFormatException;
import org.progfun.Logger;
import org.progfun.Market;
import org.progfun.SnapshotGenerator;
import org.progfun.bots.bitfinex.BitFinexHandler;
import org.progfun.bots.gdax.GdaxHandler;
import org.progfun.bots.gemini.GeminiHandler;
import org.progfun.wshandler.WebSocketHandler;

/**
 * Example for printing Orderbook snapshots for different Exchange API crawlers
 */
public class SnapshotExample {

    /**
     * Launch a proof-of-concept test
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Create your crawler here - this example should work the same
        // with all the crawlers: Gemini, GDAX, etc
        WebSocketHandler handler;
//        handler = new GdaxHandler();
        handler = new BitFinexHandler();
//        handler = new GeminiHandler();

        try {
            Market market = new Market("BTC", "USD");
            handler.setMarket(market);
            // Start handler in a separate thread
            Thread handlerThread = new Thread(handler);
            handlerThread.start();
            // Notify handler that it has to connect to the WebSocket
            handler.scheduleConnect(0);

            // Print a snapshot every second
            SnapshotGenerator sg = new SnapshotGenerator();
            sg.setMarket(market);
            MarketLogger ml = new MarketLogger();
            ml.setBidLimit(3); // Show only top 3 bids and asks
            sg.setListener(ml);
            sg.schedule(2000);

            Logger.log("Press Enter to quit");
            System.in.read(); // Wait for <Enter>
            
            Logger.log("Shutting down Handler and connection...");
            handler.scheduleShutdown();
        } catch (InvalidFormatException ex) {
            Logger.log("Invalid currency pair: " + ex.getMessage());
        } catch (IOException ex) {
            Logger.log("Error while waiting for Enter key: " + ex.getMessage());
        }
    }
}
