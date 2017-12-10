package org.progfun.websocket;

import org.java_websocket.handshake.ServerHandshake;
import org.progfun.Logger;
import org.progfun.Market;

/**
 * An abstract base class that can be used for all WebSocket data gathering bots
 */
public abstract class WebSocketHandler implements Runnable {

    // How long to wait (milliseconds) before reconnection attempt
    private static final long RECONNECT_TIMEOUT = 10000;

    protected Market market;
    protected Parser parser;
    protected WebSocketConnector connector;
    private boolean logEnabled = false;

    // Current state of the Handler
    private State currentState = State.DISCONNECTED;

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
    public void scheduleConnect(long timeout) {
        // If another action is already in progress, ignore this request
        if (!isValidTransition(State.CONNECT_SCHEDULED)) {
            return;
        }

        if (timeout > 0) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ex) {
                Logger.log("Someone interrupted sleeping");
            }
        }

        setState(State.CONNECT_SCHEDULED);
        scheduleAction(Action.CONNECT);
    }

    /**
     * Disconnect first, wait a bit and then connect again. 
     * Connection must happen in the main
     * thread. This method schedules the disconnect command and wakes up the
     * main thread.
     */
    public void scheduleReconnect() {
        // If another action is already in progress, ignore this request
        if (!isValidTransition(State.RECONNECT_SCHEDULED)) {
            return;
        }

        setState(State.RECONNECT_SCHEDULED);
        scheduleAction(Action.RECONNECT);
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

            // State changes must be checked within the methods, not here
            // Process the action
            Logger.log("Executing action " + a);
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
                    break;
                case RECONNECT:
                    disconnectNow();
                    break;
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
        
        Logger.log("Handler thread finished work, exiting...");
    }

    /**
     * Terminate the Handler immediately, do not close WebSocket connection
     */
    public synchronized void scheduleKill() {
        scheduleAction(Action.TERMINATE);
    }

    /**
     * Close WebSocket connection and shut down the Handler thread
     */
    public synchronized void scheduleShutdown() {
        setState(State.SHUTDOWN_SCHEDULED);
        scheduleAction(Action.SHUTDOWN);
    }

    /**
     * Close WebSocket connection, but keep the Handler thread alive, allow to
     * restart it afterwards.
     */
    public synchronized void scheduleDisconnect() {
        setState(State.DISCONNECT_SCHEDULED);
        scheduleAction(Action.DISCONNECT);
    }

    private synchronized void setState(State s) {
        this.currentState = s;
        Logger.log("---> State: " + s);
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

        if (currentState != State.CONNECT_SCHEDULED) {
            Logger.log("Trying to run connectNow() from incorrect state: " + currentState);
            return true;
        }

        Logger.log("Handler starts connection...");

        String url = getUrl();
        if (url == null) {
            Logger.log("Can not start bot without URL, cancelling...");
            setState(State.DISCONNECTED);
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
        if (!isValidTransition(State.RUNNING)) {
            Logger.log("Trying startNow() from invalid state, aborting");
            return false;
        }

        // Bind together different components: market, parser and listener
        if (market == null) {
            Logger.log("Can not start bot without market, cancelling...");
            setState(State.CONNECTED);
            return false;
        }

        parser = createParser();
        if (parser == null) {
            Logger.log("Can not start bot without parser, cancelling...");
            setState(State.CONNECTED);
            return false;
        }
        parser.setMarket(market);

        // Get rid of some old bids and asks. Start fresh
        market.clearOrderBook();
        
        Logger.log("Starting Handler process...");

        if (logEnabled) {
            startLogging();
        }
        
        // Call Handler-specific initialization
        init();

        setState(State.RUNNING);

        return true;
    }

    /**
     * Initiate disconnect from remote API. It is an asynchronous operation and
     * will finish somewhere in the background. This function is not blocking!
     *
     * @return true on success, false otherwise
     */
    private boolean disconnectNow() {
        if (connector == null) {
            Logger.log("Connection not started, can not disconnect");
            return false;
        }

        switch (currentState) {
            case SHUTDOWN_SCHEDULED:
                setState(State.SHUTTING_DOWN);
                break;
            case DISCONNECT_SCHEDULED:
                setState(State.DISCONNECTING);
                break;
            case RECONNECT_SCHEDULED:
                setState(State.REC_DISCONNECTING);
                break;
            default:
                Logger.log("Trying to disconnect from a wrong state: "
                        + currentState + ", ignoring the request");
                return false;
        }

        if (connector.close()) {
            Logger.log("Initiated connection close");
            if (logEnabled) {
                connector.stopLogging();
            }
            connector = null;
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

        if (!isValidTransition(State.CONNECTED)) {
            Logger.log("onSocketOpened() got called from a wrong state: "
                    + currentState + ", ignoring it");
            return;
        }

        // Technically we get to state CONNECTED, then we immediately transition 
        // to START_SCHEDULED. If startNow() fails, we may get back to 
        // CONNECTED state
        setState(State.CONNECTED);
        setState(State.START_SCHEDULED);
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
        Logger.log("Handler.onSocketClosed " + (remote ? "by remote end" : "")
                + ", reason: " + reason);

        if (code == 1006) {
            Logger.log("Code 1006 received. Probably, Internet connection error");
        }

        if (mustReconnectOnClose()) {
            setState(State.WAIT_CONNECT);
            scheduleConnect(RECONNECT_TIMEOUT);
        } else {
            Logger.log("No need to reconnect, because state == " + currentState);
            switch (currentState) {
                case SHUTTING_DOWN:
                    setState(State.TERMINATED);
                    break;
                case DISCONNECTING:
                    setState(State.DISCONNECTED);
                    break;
            }
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
        // Wait a while and reconnect
        if (mustReconnectOnClose()) {
            setState(State.WAIT_CONNECT);
            scheduleConnect(RECONNECT_TIMEOUT);
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

    /**
     * Check if it is allowed to transition to the target state from the current
     * state. This function does not change the current state.
     *
     * @param targetState
     * @return true if transition is allowed, false if it isn't.
     *
     */
    private boolean isValidTransition(State targetState) {
        if (targetState == null) {
            return false;
        }
        boolean valid;
        switch (targetState) {
            case CONNECT_SCHEDULED:
                valid = currentState == State.DISCONNECTED
                        || currentState == State.WAIT_CONNECT;
                break;
            case RECONNECT_SCHEDULED:
                valid = currentState == State.RUNNING;
                break;
            case RUNNING:
                valid = currentState == State.START_SCHEDULED;
                break;
            case CONNECTED:
                valid = currentState == State.CONNECTING;
                break;
                // TODO - add all the other states
            default:
                valid = false;
        }
        if (!valid) {
            Logger.log("Trying incorrect state change: from " + currentState
                    + " to " + currentState);
        }
        return valid;
    }

    /**
     * Return true if we must reconnect in case connection is closed in the
     * current state
     *
     * @return
     */
    private boolean mustReconnectOnClose() {
        switch (currentState) {
            case CONNECTING:
            case CONNECTED:
            case START_SCHEDULED:
            case RUNNING:
            case REC_DISCONNECTING:
                return true;
            default:
                return false;
        }
    }
}
