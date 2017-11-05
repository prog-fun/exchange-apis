package org.progfun.bots.gemini;

import org.progfun.orderbook.DummyListener;
import java.io.IOException;
import org.progfun.InvalidFormatException;
import org.progfun.Market;
import org.progfun.connector.Parser;

/**
 * Gemini Exchange API reader
 */
public class GeminiHandler extends AbstractWebSocketHandler {

    // The URL is wss://api.gemini.com/v1/marketdata/{symbol}
    private static final String API_URL_BASE = "wss://api.gemini.com/v1/marketdata/";

    /**
     * Take a pair of currencies, convert it to a single symbols as understood
     * by Gemini exchange
     *
     * @param baseCurrency
     * @param quoteCurrency
     * @return
     */
    private String getSymbol() {
        if (market == null) {
            return null;
        }
        String baseCurrency = market.getBaseCurrency();
        String quoteCurrency = market.getQuoteCurrency();
        if (baseCurrency == null || quoteCurrency == null) {
            return null;
        }
        return baseCurrency.toLowerCase() + quoteCurrency.toLowerCase();
    }

    @Override
    protected String getUrl() {
        return API_URL_BASE + getSymbol();
    }

    @Override
    protected Parser createParser() {
        return new GeminiParser();
    }

    /**
     * Launch a proof-of-concept test
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        GeminiHandler client;
        try {
            client = new GeminiHandler();
            Market market = new Market("BTC", "USD");
            market.addListener(new DummyListener());
            client.setMarket(market);
            if (client.connect()) {
                System.in.read(); // Wait for <Enter>
                client.disconnect();
            }
        } catch (IOException ex) {
            System.out.println("Something wrong with input");
        } catch (InvalidFormatException ex) {
            System.out.println("Invlid currency pair: " + ex.getMessage());
        }
    }

}
