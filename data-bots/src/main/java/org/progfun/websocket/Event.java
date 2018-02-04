package org.progfun.websocket;

/**
 * An event for a single action.
 */
public class Event {

    // Action to be taken, event type
    private Action type;
    // Message (Comment)
    private String message;
    // Data associated with the event. Each event generator can have different data
    private Object data;

    /**
     * Create event
     *
     * @param type event type
     * @param data data associated with the event. Use type to find out what is
     * the class of data
     * @param message
     */
    public Event(Action type, Object data, String message) {
        this.type = type;
        this.data = data;
        this.message = message;
    }

    public Action getType() {
        return type;
    }

    public void setType(Action type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{" + "type=" + type + ", msg=" + message + '}';
    }
}
