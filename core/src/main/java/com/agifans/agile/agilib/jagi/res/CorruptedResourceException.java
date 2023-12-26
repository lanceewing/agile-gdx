/**
 * CorruptedResourceException.java
 * Adventure Game Interpreter Resource Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.res;

/**
 * The resource is currupted.
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public final class CorruptedResourceException extends ResourceException {
    /**
     * Creates new <code>CorruptedResourceException</code> without detail message.
     */
    public CorruptedResourceException() {
        super();
    }

    /**
     * Constructs an <code>CorruptedResourceException</code> with the specified detail message.
     *
     * @param msg Detail message.
     */
    public CorruptedResourceException(String msg) {
        super(msg);
    }
}