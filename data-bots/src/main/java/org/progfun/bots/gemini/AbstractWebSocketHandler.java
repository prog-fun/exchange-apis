package org.progfun.bots.gemini;

import org.progfun.Market;
import org.progfun.connector.Parser;
import org.progfun.connector.WebSocketConnector;

/**
 * An abstract base class that can be used for all WebSocket data gathering bots 
 */
public abstract class AbstractWebSocketHandler {

    protected Market market;
    protected Parser parser;
    protected WebSocketConnector connector;

    /**
     * Set market to monitor
     *
     * @param market
     */
    public void setMarket(Market market) {
        this.market = market;
    }

    /**
     * Connect and start listening for API messages
     *
     * @return true on successful start, false otherwise
     */
    public boolean connect() {
        if (market == null) {
            System.out.println("Can not start bot without market, cancelling...");
            return false;
        }
        System.out.println("Connecting...");
        connector = new WebSocketConnector();

        // Bind together different components: market, parser and listener
        parser = createParser();
        if (parser == null) {
            System.out.println("Can not start bot without parser, cancelling...");
            return false;
        }
        parser.setMarket(market);
        connector.setListener(parser);

        String url = getUrl();
        if (url == null) {
            System.out.println("Can not start bot without URL, cancelling...");
            return false;
        }
        System.out.println("Starting bot for URL " + url);
        if (connector.start(url)) {
            return true;
        } else {
            System.out.println("Could not start WebSocket connector");
            return false;
        }
    }

    /**
     * Stop listening for data from API
     * @return true on success, false otherwise
     */
    public boolean disconnect() {
        if (connector == null) {
            System.out.println("Connection not started, can not close it");
            return false;
        }
        if (connector.stop()) {
            System.out.println("Closed connection");
            return true;
        } else {
            System.out.println("Failed to close connection");
            return false;
        }
    }

    /**
     * Return URL to WebSocket server
     * @return
     */
    protected abstract String getUrl();

    /**
     * Create a parser for API messages
     * @return 
     */
    protected abstract Parser createParser();

    /**
     * Send the initial commands (subscribe to channels, set options, etc).
     * This method must be run by the using part after the connect()
     */
    public abstract void sendInitCommands();
}
