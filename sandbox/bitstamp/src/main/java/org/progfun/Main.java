package org.progfun;

import java.util.Scanner;
import org.progfun.orderbook.DummyListener;

public class Main {

    public static void main(String[] args) {

        try {

            Market market = new Market("btc", "usd");
//            market.addListener(new SQLOrderbookListener());
            market.addListener(new DummyListener());

            BitstampBot bot = new BitstampBot();
            bot.bindMarket(market);

            // Wait for close...
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();

            bot.disconnect();

        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }

    }

}
