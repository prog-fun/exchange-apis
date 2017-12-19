package org.progfun.examples;

import java.io.IOException;
import org.progfun.Logger;
import org.progfun.SnapshotGenerator;
import org.progfun.Channel;
import org.progfun.Exchange;
import org.progfun.Market;
import org.progfun.Subscriptions;
import org.progfun.bots.bitfinex.BitFinexHandler;
import org.progfun.bots.gdax.GdaxHandler;
import org.progfun.bots.gemini.GeminiHandler;
import org.progfun.websocket.WebSocketHandler;

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
//        GeminiHandler handler = new GeminiHandler();
//        handler.setMainMarket(btcusd);
        handler.setLogging(true);
//        handler.setVerbose(true);

        Subscriptions subs = new Subscriptions();
        subs.add(new Market("BTC", "USD"), Channel.ORDERBOOK);
        subs.add(new Market("ETH", "USD"), Channel.ORDERBOOK);
        subs.add(new Market("LTC", "USD"), Channel.ORDERBOOK);
//        subs.add(new Market("LTC", "BTC"), Channel.ORDERBOOK);
//        subs.add(new Market("ETH", "BTC"), Channel.ORDERBOOK);
//        subs.add(new Market("DAT", "USD"), Channel.ORDERBOOK);
//        subs.add(new Market("QTM", "USD"), Channel.ORDERBOOK);
//        subs.add(new Market("QSH", "USD"), Channel.ORDERBOOK);
//        subs.add(new Market("YYW", "USD"), Channel.ORDERBOOK);
        handler.subscribe(subs);
        // Start handler in a separate thread
        Thread handlerThread = new Thread(handler);
        handlerThread.start();
        // Notify handler that it has to connect to the WebSocket
        handler.scheduleConnect(0);

        // Print a snapshot every second
        SnapshotGenerator sg = new SnapshotGenerator();
        Exchange e = handler.getExchange();
        if (e == null) {
            Logger.log("Can't start Snapshot Generator without an exchange!");
            return;
        }
        sg.setExchange(e);
        ExchangeLogger ml = new ExchangeLogger();
        ml.setBidLimit(3); // Show only top 3 bids and asks
        sg.setListener(ml);
        sg.schedule(2000);

        try {
            Logger.log("Press Enter to quit");
            System.in.read(); // Wait for <Enter>

            Logger.log("Shutting down Handler and connection...");
            handler.scheduleShutdown();
        } catch (IOException ex) {
            Logger.log("Error while waiting for Enter key: " + ex.getMessage());
        }
    }
}
