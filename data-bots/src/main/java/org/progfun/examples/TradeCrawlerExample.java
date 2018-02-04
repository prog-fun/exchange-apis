package org.progfun.examples;

import java.io.IOException;
import org.progfun.Channel;
import org.progfun.InvalidFormatException;
import org.progfun.Logger;
import org.progfun.Market;
import org.progfun.Subscriptions;
import org.progfun.bots.bitfinex.BitFinexHandler;
import org.progfun.trade.Trade;
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
        handler.setDebugReconnect(20);

        try {
            Subscriptions subs = new Subscriptions();
            Market m = new Market("XRP", "USD");
            subs.addInactive(m, Channel.TRADES);
            handler.subscribe(subs);
            handler.setVerbose(false);
            m.addTradeListener((Market market, Trade trade) -> {
                Logger.log("Added trade: " + trade.toString());
            });
            
            Thread handlerThread = new Thread(handler);
            handlerThread.start();
            // Notify handler that it has to connect to the WebSocket
            handler.scheduleConnect(0);

            System.out.println("Press Enter to quit");
            System.in.read(); // Wait for <Enter>

            handler.scheduleShutdown("Shutting down Handler and connection...");
        } catch (InvalidFormatException ex) {
            System.out.println("Invlid currency pair: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Something wrong with input");
        }
    }
}
