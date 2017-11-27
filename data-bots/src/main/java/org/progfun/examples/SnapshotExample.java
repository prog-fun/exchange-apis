package org.progfun.examples;

import java.io.IOException;
import org.progfun.InvalidFormatException;
import org.progfun.Market;
import org.progfun.SnapshotGenerator;
import org.progfun.bots.BotRunner;
import org.progfun.bots.bitfinex.BitFinexHandler;
import org.progfun.bots.gdax.GdaxHandler;
import org.progfun.connector.AbstractWebSocketHandler;

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
        AbstractWebSocketHandler handler; 
//        handler = new GdaxHandler();
        handler = new BitFinexHandler();
        // Enable message logging to text file
        handler.setLogging(true);
        
        try {
            Market market = new Market("BTC", "USD");            
            BotRunner runner = new BotRunner(handler, market);
            runner.start();
            
            // Print a snapshot every second
            SnapshotGenerator sg = new SnapshotGenerator();
            sg.setMarket(market);
            MarketLogger ml = new MarketLogger();
            ml.setBidLimit(3); // Show only top 3 bids and asks
            sg.setListener(ml);
            sg.schedule(1000);
            
            System.out.println("Press Enter to quit");
            System.in.read(); // Wait for <Enter>
            runner.terminate();
            // Close log file
            handler.setLogging(false);
        } catch (InvalidFormatException ex) {
            System.out.println("Invlid currency pair: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Something wrong with input");
        }
    }
}
