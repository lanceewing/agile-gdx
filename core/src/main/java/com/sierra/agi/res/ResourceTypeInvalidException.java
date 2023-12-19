/**
 * ResourceTypeInvalidException.java
 * Adventure Game Interpreter Resource Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.res;

/**
 * Resource type passed by parameter is invalid.
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class ResourceTypeInvalidException extends ResourceException {
    /**
     * Creates new <code>ResourceTypeInvalidException</code> without detail message.
     */
    public ResourceTypeInvalidException() {
    }

    /**
     * Constructs an <code>ResourceTypeInvalidException</code> with the specified detail message.
     *
     * @param msg Detail message.
     */
    public ResourceTypeInvalidException(String msg) {
        super(msg);
    }
}