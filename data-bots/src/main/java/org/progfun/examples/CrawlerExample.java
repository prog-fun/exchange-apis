package org.progfun.examples;

import java.io.IOException;
import org.progfun.InvalidFormatException;
import org.progfun.Market;
import org.progfun.bots.bitfinex.BitFinexHandler;
import org.progfun.bots.gemini.GeminiHandler;
import org.progfun.bots.gdax.GdaxHandler;
import org.progfun.connector.AbstractWebSocketHandler;
import org.progfun.orderbook.DummyListener;

/**
 * Example for different Exchange API crawlers
 */
public class CrawlerExample {

    /**
     * Launch a proof-of-concept test
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Create your crawler here - this example should work the same
        // with all the crawlers: Gemini, GDAX, etc
        AbstractWebSocketHandler handler;
        // handler = new GeminiHandler();
        // handler = new GdaxHandler();
        handler = new BitFinexHandler();
        
        try {
            Market market = new Market("BTC", "USD");
            market.addListener(new DummyListener());
            handler.setMarket(market);
            if (handler.connect()) {
                handler.sendInitCommands(); // Send the "subscribe" commands
                System.out.println("Press Enter to quit");
                System.in.read(); // Wait for <Enter>
                handler.disconnect();
            }
        } catch (IOException ex) {
            System.out.println("Something wrong with input");
        } catch (InvalidFormatException ex) {
            System.out.println("Invlid currency pair: " + ex.getMessage());
        }
    }
}
