package org.progfun.websocket;

/**
 * Response from parsing a single message. It contains desired action for the
 * Handler and reason for it.
 */
public class ParserResponse {

    private Action action;
    private String reason;

    public ParserResponse(Action action, String reason) {
        this.action = action;
        this.reason = reason;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ParserResponse{" + "action=" + action + ", reason=" + reason + '}';
    }
}
