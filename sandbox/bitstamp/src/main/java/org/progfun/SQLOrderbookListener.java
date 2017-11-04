package org.progfun;

import org.progfun.orderbook.Listener;
import org.progfun.orderbook.Order;

import java.sql.*;

public class SQLOrderbookListener implements Listener {

    private Connection connection;

    public SQLOrderbookListener() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://localhost/bitbot?user=root&password=");
        } catch (SQLException e) {
            log("Error while connecting to SQL server: " + e.getMessage());
        } catch (InstantiationException e) {
            log("Error loading MySQL driver: InstantiationException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            log("Error loading MySQL driver: IllegalAccessException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log("Error loading MySQL driver: ClassNotFoundException: " + e.getMessage());
        }
    }

    private void insert(String table, Order order, Market market) {
        try {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + table + " VALUES (0, ?, ?, ?, (" +
                         "SELECT id FROM market WHERE base_currency = ? AND quote_currency = ? LIMIT 1))");
            statement.setString(1, String.valueOf(order.getPrice()));
            statement.setString(2, String.valueOf(order.getAmount()));
            statement.setString(3, String.valueOf(order.getCount()));
            statement.setString(4, market.getBaseCurrency());
            statement.setString(5, market.getQuoteCurrency());
            statement.executeUpdate();
        } catch (SQLException e) {
            log("Error creating statement: " + e.getMessage());
        }
    }

    private void update(String table, Order order) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + table + " SET ");
            statement.setString(1, String.valueOf(order.getPrice()));
            statement.setString(2, String.valueOf(order.getAmount()));
            statement.setString(3, String.valueOf(order.getCount()));
            statement.executeUpdate();
        } catch (SQLException e) {
            log("Error creating statement: " + e.getMessage());
        }
    }

    @Override
    public void bidAdded(Market market, Order bid) {
        insert("bid", bid, market);
    }

    @Override
    public void askAdded(Market market, Order ask) {
        log("ASK ADDED!!");
        insert("ask", ask, market);
    }

    @Override
    public void bidUpdated(Market market, Order bid) {

    }

    @Override
    public void askUpdated(Market market, Order ask) {

    }

    @Override
    public void bidRemoved(Market market, double price) {

    }

    @Override
    public void askRemoved(Market market, double price) {

    }

    private static void log(String message) {
        System.out.println("[Thread #" + Thread.currentThread().getId() + "] Database: " + message);
    }
}
