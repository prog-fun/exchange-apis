package org.progfun.examples;

import org.progfun.orderbook.DummyListener;
import java.io.IOException;
import org.progfun.InvalidFormatException;
import org.progfun.Market;
import org.progfun.bots.gemini.GeminiHandler;

/**
 * Gemini Exchange API crawler example
 */
public class GeminiExample {

    /**
     * Launch a proof-of-concept test
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        GeminiHandler handler = new GeminiHandler();
        try {
            Market market = new Market("BTC", "USD");
            market.addListener(new DummyListener());
            handler.setMarket(market);
            if (handler.connect()) {
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
