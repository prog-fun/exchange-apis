package org.progfun.connector;

import java.net.SocketException;
import java.net.UnknownHostException;
import org.java_websocket.handshake.ServerHandshake;
import org.progfun.Logger;
import org.progfun.Market;

/**
 * An abstract base class that can be used for all WebSocket data gathering bots
 */
public abstract class WebSocketHandler implements Runnable {

    private enum State { 
        DISCONNECTED, 
        CONNECTING, 
        CONNECTED, 
        RUNNING, 
        DISCONNECTING,
        ERROR_MUST_RECONNECT // Error occurred, must reconnect
    };
    
    // How long to wait (milliseconds) before reconnection attempt
    private static final long RECONNECT_TIMEOUT = 5000;

    protected Market market;
    protected Parser parser;
    protected WebSocketConnector connector;
    private boolean logEnabled = false;

    // Current state of the Handler
    private State state = State.DISCONNECTED;

    // An action that is scheduled to be executed in the main Handler thread
    private Action scheduledAction = null;

    /**
     * Set market to monitor
     *
     * @param market
     */
    public void setMarket(Market market) {
        this.market = market;
    }

    /**
     * Wait a bit and try to connect. Connection must happen in the main
     * thread. This method schedules the connection command and wakes up the
     * main thread.
     *
     * @param timeout time to wait before connecting, in milliseconds. Set to
     * zero or negative value to connect immediately.
     */
    public void scheduleConnection(long timeout) {
        // If another action is already in progress, ignore this request
        if (state == State.CONNECTING) {
            Logger.log("Not scheduling CONNECT - connection already in progress");
            return;
        }

        if (timeout > 0) {
            Logger.log("Waiting before reconnect");
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ex) {
                Logger.log("Someone interrupted sleeping");
            }
        }

        Logger.log("Scheduling CONNECT");
        scheduleAction(Action.CONNECT);
    }

    /**
     * Run the handler. By default, it does not do anything, it waits for
     * next command to be scheduled. We run it in a separate thread to make
     * sure that all the WebSocket related commands (connect/disconnect)
     * always happen in a single thread
     */
    @Override
    public void run() {
        Action a;
        boolean mustRun = true;

        // Set name for this thread, used for debugging
        Thread.currentThread().setName("Handler Thread");

        while (mustRun) {

            synchronized (this) {
                Logger.log("Handler waiting for scheduled action...");
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Logger.log("Stopping Handler - got interrupted");
                    mustRun = false;
                }
            }

            a = getScheduledAction();

            // Allow next action to be queued
            clearScheduledAction();

            // Process the action
            Logger.log("[[ Executing " + a);
            switch (a) {
                case CONNECT:
                    connectNow();
                    break;
                case START:
                    startNow();
                    break;
                case DISCONNECT:
                    // Disconnect but wait for other commands
                    disconnectNow();
                case SHUTDOWN:
                    // Disconnect and exit this loop
                    disconnectNow();
                    mustRun = false;
                    break;
                case TERMINATE:
                    // Exit immediately
                    mustRun = false;
                    break;
            }

        }
    }

    /**
     * Terminate the Handler immediately, do not close WebSocket connection
     */
    public synchronized void scheduleKill() {
        scheduleAction(Action.TERMINATE);
    }

    /**
     * Close WebSocket connection and shut down the Handler
     */
    public synchronized void scheduleShutdown() {
        scheduleAction(Action.SHUTDOWN);
    }

    private synchronized void setState(State s) {
        this.state = s;
        Logger.log("State: " + s);
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
     * Schedule an action to be performed in the main Handler thread. This
     * thread is expected to be sleeping, therefore we wake it up
     *
     * @param action
     */
    private synchronized void scheduleAction(Action action) {
        Logger.log(">> Scheduling action " + action);
        scheduledAction = action;
        notifyAll();
    }

    /**
     * Connect to remote API. The connection process is asynchronous: we will
     * receive a notification onConnect() when connection is established. But
     * that will happen in another thread, and then we will schedule the next
     * step: start connection.
     *
     * @return true when connection was started. It does not mean
     * that the connection will be successful!
     */
    private boolean connectNow() {
        Logger.log("connectNow()");

        if (state == State.CONNECTING) {
            Logger.log("Already in CONNECTING state, abort second connectNow()");
            return true;
        }
        
        if (market == null) {
            Logger.log("Can not start bot without market, cancelling...");
            return false;
        }
        Logger.log("Handler starts connection...");

        String url = getUrl();
        if (url == null) {
            Logger.log("Can not start bot without URL, cancelling...");
            return false;
        }

        // Create connector only once
        if (connector == null) {
            connector = new WebSocketConnector();

            // Create an anonymous listener to keep the onOpen, etc methods 
            // for this class private
            connector.setListener(new ApiListener() {
                @Override
                public void onOpen(ServerHandshake sh) {
                    onSocketOpened(sh);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    onSocketClosed(code, reason, remote);
                }

                @Override
                public void onMessage(String message) {
                    onSocketMsg(message);
                }

                @Override
                public void onError(Exception ex) {
                    onSocketErr(ex);
                }
            });
        }

        setState(State.CONNECTING);

        Logger.log("Starting bot for URL " + url);
        if (connector.connect(url)) {
            // Connection successful, start the process
            return true;
        } else {
            Logger.log("Could not start WebSocket connector");
            return false;
        }
    }

    /**
     * Initialize the handler. This must be called on the main thread, after
     * connection is established.
     *
     * @return true when process was started, false otherwise.
     */
    private boolean startNow() {
        Logger.log("Starting Handler process...");
        
        if (state == State.RUNNING) {
            Logger.log("Handler already running, abort second startNow()");
            return true;
        }
        
        if (logEnabled) {
            startLogging();
        }

        // Bind together different components: market, parser and listener
        parser = createParser();
        if (parser == null) {
            Logger.log("Can not start bot without parser, cancelling...");
            return false;
        }
        parser.setMarket(market);

        // Call Handler-specific initialization
        init();

        setState(State.RUNNING);
        
        return true;
    }

    /**
     * Disconnect from remote API
     *
     * @return true on success, false otherwise
     */
    private boolean disconnectNow() {
        Logger.log("disconnectNow()");
        
        if (state == State.DISCONNECTED || state == State.DISCONNECTING) {
            Logger.log("Already disconnected, aborting second try");
            return true;
        }
        

        if (connector == null) {
            Logger.log("Connection not started, can not close it");
            return false;
        }
        
        if (connector.close()) {
            Logger.log("Initiated connection close");
            if (logEnabled) {
                connector.stopLogging();
            }
            connector = null;
            setState(State.DISCONNECTING);
            return true;
        } else {
            Logger.log("Failed to close connection");
            return false;
        }
    }

    /**
     * This method is called in a sub-thread when WebSocket connection has been
     * established
     *
     * @param sh HTTP status received from the remote end
     */
    private void onSocketOpened(ServerHandshake sh) {
        // When connection is established, we can start the main process. 
        // But that has happen on the main thread, therefore we must schedule it.
        setState(State.CONNECTED);
        scheduleAction(Action.START);
    }

    /**
     * This method is called in a sub-thread when WebSocket has been closed
     *
     * @param code
     * @param reason
     * @param remote
     */
    private void onSocketClosed(int code, String reason, boolean remote) {
        // TODO - check reasons - maybe some reasons are terminal and we 
        // should not retry to reconnect?
        Logger.log("Handler.onSocketClosed " + (remote ? "by remote end" : "")
                + ", reason: " + reason);

        if (code == 1006) {
            Logger.log("Code 1006 received. Probably, Internet connection error");
        }
        

        // If disconnect request was initiated by ourselves, do not try to connect again
        if (state != State.DISCONNECTING) {
            setState(State.DISCONNECTED);
            scheduleConnection(RECONNECT_TIMEOUT);
        } else {
            Logger.log("It was our own will to close the connection, do not retry to open it");
            setState(State.DISCONNECTED);
        }
    }

    /**
     * Message received from the socket
     *
     * @param message
     */
    private void onSocketMsg(String message) {
//        Logger.log("Received WS msg: " + message);
        if (parser != null) {
            parser.parseMessage(message);
        }
    }

    /**
     * Error received from the WebSocket
     *
     * @param ex
     */
    private void onSocketErr(Exception ex) {
        Logger.log("Handler.onSocketErr: " + ex.getMessage());
        if (ex instanceof UnknownHostException) {
            // We could not connect to the host, retry in a while
            setState(State.ERROR_MUST_RECONNECT);
            scheduleConnection(RECONNECT_TIMEOUT);
        } else if (ex instanceof SocketException) {
            setState(State.ERROR_MUST_RECONNECT);
            Logger.log("Network connection error");
            scheduleConnection(RECONNECT_TIMEOUT);
        }
    }

    /**
     * Get an action that is scheduled to be executed in the main Handler thread
     *
     * @return scheduled action or null if nothing is scheduled
     */
    private synchronized Action getScheduledAction() {
        return scheduledAction;
    }

//    /**
//     * Return true is there is a scheduled action which is not yet processed in
//     * the main Handler thread
//     *
//     * @return
//     */
//    private synchronized boolean actionInProgress() {
//        return getScheduledAction() != null;
//    }
//
    /**
     * Mark current scheduled action as done
     */
    private synchronized void clearScheduledAction() {
        scheduledAction = null;
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
     * Initialize the handler: send the initial commands (subscribe to channels,
     * set options, etc). This method will be executed in the main Handler
     * thread when connection is established
     */
    protected abstract void init();

    /**
     * Each API handler should be able to return symbol of the corresponding
     * exchange: BITF, GDAX, etc.
     *
     * @return
     */
    public abstract String getExchangeSymbol();

    /**
     * Start writing log to file.
     */
    private void startLogging() {
        if (connector != null) {
            // Connector already created, start logging immediately
            String logFileName = this.getClass().getSimpleName()
                    + "-msg-log.txt";
            this.logEnabled = connector.startLogging(logFileName);
        }
    }
}

