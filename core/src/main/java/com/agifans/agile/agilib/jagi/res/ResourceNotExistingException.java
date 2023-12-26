/**
 * ResourceNotExistingException.java
 * Adventure Game Interpreter Resource Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.res;

/**
 * The resource doesn't exists.
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public final class ResourceNotExistingException extends ResourceException {
    /**
     * Creates new <code>ResourceNotExistingException</code> without detail message.
     */
    public ResourceNotExistingException() {
        super();
    }

    /**
     * Constructs an <code>ResourceNotExistingException</code> with the specified detail message.
     *
     * @param msg Detail message.
     */
    public ResourceNotExistingException(String msg) {
        super(msg);
    }
}
