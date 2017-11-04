package org.progfun;

import org.progfun.orderbook.Listener;
import org.progfun.orderbook.Order;

import java.sql.*;

public class SQLOrderbookListener implements Listener {

    private Connection connection;

    public SQLOrderbookListener() {
        try {
            Class.forName("com.mysql.jdcb.Driver").newInstance();
            connection = DriverManager.getConnection("jdcb:mysql://localhost/bitbot?user=root&password=");
        } catch (SQLException e) {
            log("Error while connecting to SQL server: " + e.getMessage());
        } catch (Exception e) {
            log("Error loading MySQL driver: " + e.getMessage());
        }
    }

    private void insert(String table, Order order) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + table + " (price, amount, count) VALUES (?, ?, ?)");
            statement.setString(0, String.valueOf(order.getPrice()));
            statement.setString(1, String.valueOf(order.getAmount()));
            statement.setString(2, String.valueOf(order.getCount()));
            statement.executeUpdate();
        } catch (SQLException e) {
            log("Error creating statement: " + e.getMessage());
        }
    }

    private void update(String table, Order order) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + table + " SET ");
            statement.setString(0, String.valueOf(order.getPrice()));
            statement.setString(1, String.valueOf(order.getAmount()));
            statement.setString(2, String.valueOf(order.getCount()));
            statement.executeUpdate();
        } catch (SQLException e) {
            log("Error creating statement: " + e.getMessage());
        }
    }

    @Override
    public void bidAdded(Order bid) {
        insert("bid", bid);
    }

    @Override
    public void askAdded(Order ask) {
        insert("ask", ask);
    }

    @Override
    public void bidUpdated(Order bid) {

    }

    @Override
    public void askUpdated(Order ask) {

    }

    @Override
    public void bidRemoved(double price) {

    }

    @Override
    public void askRemoved(double price) {

    }

    private static void log(String message) {
        System.out.println("[Thread #" + Thread.currentThread().getId() + "] Database: " + message);
    }
}
