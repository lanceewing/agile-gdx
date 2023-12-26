/**
 * NoVolumeAvailableException.java
 * Adventure Game Interpreter Resource Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.res;

/**
 * There is no volume available. Throwed when a ResourceProvider is created
 * with a folder that doesn't contain any resource directory.
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public final class NoVolumeAvailableException extends ResourceException {
    /**
     * Creates new <code>NoVolumeAvailableException</code> without detail message.
     */
    public NoVolumeAvailableException() {
        super();
    }

    /**
     * Constructs an <code>NoVolumeAvailableException</code> with the specified detail message.
     *
     * @param msg Detail message.
     */
    public NoVolumeAvailableException(String msg) {
        super(msg);
    }
}