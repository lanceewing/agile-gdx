/*
 *  ViewException.java
 *  Adventure Game Interpreter View Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.view;

/**
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class ViewException extends Exception {
    /**
     * Creates new <code>ViewException</code> without detail message.
     */
    public ViewException() {
    }

    /**
     * Constructs an <code>ViewException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ViewException(String msg) {
        super(msg);
    }
}