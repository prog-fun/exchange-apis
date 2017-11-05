package org.progfun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * API Documentation: https://www.bitstamp.net/websocket/
 * Live Example: https://www.bitstamp.net/s/examples/live_diff_order_book.html
 */
public class BitstampBot {

    // URL for fetching current order book state.
    public static final String HTTP_API_ORDER_BOOK_URL = "https://www.bitstamp.net/api/order_book";

    // Bitstamp's API key.
    public static final String BITSTAMP_PUSHER_KEY = "de504dc5763aeef9ff52";

    // Bitstamp channels.
    // Each channel has a suffix of _{currency_pair}
    // If you omit the suffix, BTC/USD will be used.
    // Examples:
    //  - live_trades_btceur     (BTC/EUR)
    //  - diff_order_book_ethbtc (ETH/BTC)
    public static final String CHANNEL_LIVE_TRADES = "live_trades";
    public static final String CHANNEL_LIVE_ORDER_BOOK = "order_book";
    public static final String CHANNEL_LIVE_FULL_ORDER_BOOK = "diff_order_book";
    public static final String CHANNEL_LIVE_ORDERS = "live_orders";

    // Bitstamp events for channels.
    public static final String EVENT_LIVE_TRADES_TRADE = "trade";
    public static final String EVENT_LIVE_ORDER_BOOK_DATA = "data";
    public static final String EVENT_LIVE_FULL_ORDER_BOOK_DATA = "data";
    public static final String EVENT_LIVE_ORDERS_ORDER_CREATED = "order_created";
    public static final String EVENT_LIVE_ORDERS_ORDER_CHANGED = "order_changed";
    public static final String EVENT_LIVE_ORDERS_ORDER_DELETED = "order_deleted";

    private PusherClient client;

    public BitstampBot() {
        client = new PusherClient(BITSTAMP_PUSHER_KEY);
    }

    public void bindMarket(Market market) {

        // Setup parser.
        BitstampParser parser = new BitstampParser(market);

        // Get initial state of the order book.
        parser.onMessage(getFullOrderBook());

        // Subscribe for updates.
        String channelName = getChannelNameForMarket(CHANNEL_LIVE_FULL_ORDER_BOOK, market.getBaseCurrency(), market.getQuoteCurrency());
        client.subscribe(channelName);
        client.bind(channelName, EVENT_LIVE_FULL_ORDER_BOOK_DATA, new PusherListener(parser));

    }

    public void disconnect() {
        client.disconnect();
    }

    public static String getChannelNameForMarket(String channel, String baseCurrency, String quoteCurrency) {
        // As BTC/USD is default, there is no equivalent channel with a _btcusd suffix.
        if (baseCurrency.toLowerCase().equals("btc") && quoteCurrency.toLowerCase().equals("usd")) {
            return channel;
        }
        return channel + "_" + baseCurrency.toLowerCase() + baseCurrency.toLowerCase();
    }

    public String getFullOrderBook() {
        try {

            // Setup connection.
            URL url = new URL(HTTP_API_ORDER_BOOK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "";
            }

            // Build incoming bytes as string.
            InputStream stream = connection.getInputStream();
            ByteArrayOutputStream result = new ByteArrayOutputStream(connection.getContentLength());
            byte[] buffer = new byte [4096];
            while (true) { // TODO: Use a condition?
                int lastRead = stream.read(buffer);
                if (lastRead == -1) {
                    break;
                }
                result.write(buffer, 0, lastRead);
            }
            return result.toString();

        } catch (MalformedURLException e) {
            log("Malformed URL. " + e.getMessage());
        } catch (IOException e) {
            log(e.getMessage());
        }
        return "";
    }

    private static void log(String message) {
        System.out.println("[Thread #" + Thread.currentThread().getId() + "] BitstampBot: " + message);
    }

}
