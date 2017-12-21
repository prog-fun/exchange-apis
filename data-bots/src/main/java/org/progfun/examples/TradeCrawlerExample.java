package org.progfun.examples;

import java.io.IOException;
import org.progfun.Channel;
import org.progfun.InvalidFormatException;
import org.progfun.Logger;
import org.progfun.Market;
import org.progfun.Subscriptions;
import org.progfun.bots.bitfinex.BitFinexHandler;
import org.progfun.websocket.WebSocketHandler;

/**
 * Example for different Exchange API crawlers
 */
public class TradeCrawlerExample {

    /**
     * Launch a proof-of-concept test
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Create your crawler here - this example should work the same
        // with all the crawlers: Gemini, GDAX, etc
        WebSocketHandler handler;
        handler = new BitFinexHandler();

        try {
            Subscriptions subs = new Subscriptions();
            subs.add(new Market("LTC", "USD"), Channel.TRADES);
            handler.subscribe(subs);
            handler.setVerbose(true);
            
            Thread handlerThread = new Thread(handler);
            handlerThread.start();
            // Notify handler that it has to connect to the WebSocket
            handler.scheduleConnect(0);

            System.out.println("Press Enter to quit");
            System.in.read(); // Wait for <Enter>

            Logger.log("Shutting down Handler and connection...");
            handler.scheduleShutdown();
        } catch (InvalidFormatException ex) {
            System.out.println("Invlid currency pair: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Something wrong with input");
        }
    }
}
