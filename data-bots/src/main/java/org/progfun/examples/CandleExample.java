package org.progfun.examples;

import java.io.IOException;
import org.progfun.Logger;
import org.progfun.SnapshotGenerator;
import org.progfun.Channel;
import org.progfun.Exchange;
import org.progfun.Market;
import org.progfun.Subscriptions;
import org.progfun.bots.bitfinex.BitFinexHandler;
import org.progfun.websocket.WebSocketHandler;

/**
 * Example for printing Price Candle snapshots
 */
public class CandleExample {

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
//        GeminiHandler handler = new GeminiHandler();
//        handler.setMainMarket(btcusd);
        handler.setLogging(true);
        handler.setVerbose(true);

        Subscriptions subs = new Subscriptions();
        Market m = new Market("BTC", "USD");
        subs.addInactive(m, Channel.PRICES_1MIN);        
        subs.addInactive(m, Channel.PRICES_1D);        
        Market m2 = new Market("YYW", "ETH");
        subs.addInactive(m2, Channel.PRICES_1H);
        handler.subscribe(subs);
        // Start handler in a separate thread
        Thread handlerThread = new Thread(handler);
        handlerThread.start();
        // Notify handler that it has to connect to the WebSocket
        handler.scheduleConnect(0);

        // Print a snapshot every second
        SnapshotGenerator sg = new SnapshotGenerator(handler, true, true);
        Exchange e = handler.getExchange();
        // Make sure we don't create copies of the same market
        e.addMarket(m);
        e.addMarket(m2);
        
        if (e == null) {
            Logger.log("Can't start Snapshot Generator without an exchange!");
            return;
        }
        sg.setExchange(e);
        ExchangeLogger ml = new ExchangeLogger(false, false, true);
        ml.setLimits(3, 4, 4); // Show only top X items
        sg.setListener(ml);
        sg.schedule(2000);

        try {
            Logger.log("Press Enter to quit");
            System.in.read(); // Wait for <Enter>

            handler.scheduleShutdown("Shutting down Handler and connection...");
        } catch (IOException ex) {
            Logger.log("Error while waiting for Enter key: " + ex.getMessage());
        }
    }
}
