/**
 * NoVolumeAvailableException.java
 * Adventure Game Interpreter Resource Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.res;

/**
 * The volume is not found.
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public final class VolumeNotFoundException extends ResourceException {
    /**
     * Creates new <code>VolumeNotFoundException</code> without detail message.
     */
    public VolumeNotFoundException() {
        super();
    }

    /**
     * Constructs an <code>VolumeNotFoundException</code> with the specified detail message.
     *
     * @param msg Detail message.
     */
    public VolumeNotFoundException(String msg) {
        super(msg);
    }
}