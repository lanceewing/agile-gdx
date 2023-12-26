/*
 *  PictureException.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.pic;

/**
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class PictureException extends Exception {
    /**
     * Creates new <code>PictureException</code> without detail message.
     */
    public PictureException() {
    }

    /**
     * Constructs an <code>PictureException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public PictureException(String msg) {
        super(msg);
    }
}