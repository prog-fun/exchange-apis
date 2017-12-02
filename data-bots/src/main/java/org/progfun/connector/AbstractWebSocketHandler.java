package org.progfun.connector;

import org.progfun.Market;

/**
 * An abstract base class that can be used for all WebSocket data gathering bots
 */
public abstract class AbstractWebSocketHandler {

    protected Market market;
    protected Parser parser;
    protected WebSocketConnector connector;
    private boolean logEnabled = false;

    /**
     * Set market to monitor
     *
     * @param market
     */
    public void setMarket(Market market) {
        this.market = market;
    }

    /**
     * Enable or disable debug logging: log all the messages to a text file
     *
     * @param enabled when true, enable log, when false - disable. Logging is
     * disabled by default
     */
    public void setLogging(boolean enabled) {
        if (enabled != this.logEnabled) {
            if (enabled) {
                if (connector != null) {
                    // Connector already created, start logging immediately
                    startLogging();
                } else {
                    // Connector not ready yet, remember that we have to 
                    // start logging later
                    this.logEnabled = true;
                }
            } else {
                connector.stopLogging();
                this.logEnabled = false;
            }
        }
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

        if (logEnabled) {
            startLogging();
        }

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
     *
     * @return true on success, false otherwise
     */
    public boolean disconnect() {
        if (connector == null) {
            System.out.println("Connection not started, can not close it");
            return false;
        }
        if (connector.stop()) {
            System.out.println("Closed connection");
            if (logEnabled) {
                connector.stopLogging();
            }
            connector = null;
            return true;
        } else {
            System.out.println("Failed to close connection");
            return false;
        }
    }

    /**
     * Return URL to WebSocket server
     *
     * @return
     */
    protected abstract String getUrl();

    /**
     * Create a parser for API messages
     *
     * @return
     */
    protected abstract Parser createParser();

    /**
     * Send the initial commands (subscribe to channels, set options, etc). This
     * method must be run by the using part after the connect()
     */
    public abstract void sendInitCommands();

    /**
     * Each API handler should be able to return symbol of the corresponding
     * exchange: BITF, GDAX, etc.
     *
     * @return
     */
    public abstract String getExchangeSymbol();

    /**
     * Start logging. This method must be called only after the connector is
     * already created.
     */
    private void startLogging() {
        if (connector != null) {
            String logFileName = this.getClass().getSimpleName()
                    + "-msg-log.txt";
            this.logEnabled = connector.startLogging(logFileName);
        }
    }
}
