package org.progfun;

import org.progfun.orderbook.Orderbook;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Orderbook orderbook = new Orderbook();
        orderbook.addListener(new SQLOrderbookListener());

        BitstampBot bot = new BitstampBot();
        bot.bindMarket(orderbook,"btc", "usd");

        // Wait for close...
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        bot.disconnect();

    }

}
