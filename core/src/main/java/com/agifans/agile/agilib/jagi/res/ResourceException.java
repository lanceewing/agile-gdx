/**
 * ResourceException.java
 * Adventure Game Interpreter Resource Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.res;

/**
 * Base class for Resource Exceptions.
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class ResourceException extends Exception {
    /**
     * Creates new <code>ResourceException</code> without detail message.
     */
    public ResourceException() {
        super();
    }

    /**
     * Constructs an <code>ResourceException</code> with the specified detail message.
     *
     * @param msg Detail message.
     */
    public ResourceException(String msg) {
        super(msg);
    }
}