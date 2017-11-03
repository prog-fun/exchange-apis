package org.progfun;

import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * https://www.bitstamp.net/websocket/
 * https://www.bitstamp.net/s/examples/live_diff_order_book.html
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

    private Pusher pusher;

    public static String makeFullChannelName(String channel, String fromCurrency, String toCurrency) {
        return channel + "_" + fromCurrency.toLowerCase() + toCurrency.toLowerCase();
    }

    private static void log(String message) {
        System.out.println("[Thread #" + Thread.currentThread().getId() + "] Bitstamp/Pusher: " + message);
    }

    public static void main(String[] args) {
        BitstampBot bot = new BitstampBot();
        bot.start();
    }

    private Channel subscribe(String channel, String fromCurrency, String toCurrency) {

        // As BTC/USD is default, there is no equivalent channel with a _btcusd suffix.
        if (fromCurrency.toLowerCase().equals("btc") && toCurrency.toLowerCase().equals("usd")) {
            return pusher.subscribe(channel);
        }

        // Other currencies:
        return pusher.subscribe(makeFullChannelName(channel, fromCurrency, toCurrency));

    }

    public String getFullOrderBook() {

        try {

            URL url = new URL(HTTP_API_ORDER_BOOK_URL);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "";
            }

            InputStream stream = connection.getInputStream();
            StringBuilder builder = new StringBuilder(connection.getContentLength());

            byte[] buffer = new byte [4096];

            // TODO: Use a condition?
            while (true) {

                int lastRead = stream.read(buffer);

                if (lastRead == -1) {
                    break;
                }

                builder.append(new String(buffer));

            }

            return builder.toString();

        } catch (MalformedURLException e) {
            log("Malformed URL. " + e.getMessage());
        } catch (IOException e) {
            log(e.getMessage());
        }

        return "";

    }

    public void start() {

        PusherOptions options = new PusherOptions();

        pusher = new Pusher(BITSTAMP_PUSHER_KEY, options);

        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                log("State changed from " + change.getPreviousState() + " to " + change.getCurrentState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                log("Error: " + message + " (" + code + ")");
            }
        }, ConnectionState.ALL);

        String orderBookJSON = getFullOrderBook();
        log("[START]\n" + orderBookJSON + "\n[END]\n");

        Channel fullOrderBook = subscribe(CHANNEL_LIVE_FULL_ORDER_BOOK, "btc", "usd");
        fullOrderBook.bind(EVENT_LIVE_FULL_ORDER_BOOK_DATA, new SubscriptionEventListener() {
            @Override
            public void onEvent(String channel, String event, String data) {
                log(data + "\n");
            }
        });

        // Wait for close...
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        // Note: If connect() is called again, all channels will be resubscribed.
        pusher.disconnect();

    }

}
