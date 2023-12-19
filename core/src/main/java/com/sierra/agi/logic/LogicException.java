package com.sierra.agi.logic;

public class LogicException extends Exception {
    /**
     * Creates new <code>LogicException</code> without detail message.
     */
    public LogicException() {
    }

    /**
     * Constructs a <code>LogicException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public LogicException(String msg) {
        super(msg);
    }

}
