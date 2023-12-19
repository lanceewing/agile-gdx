/*
 *  Picture.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.pic;

import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class Picture {
    protected Vector entries;

    /**
     * Creates new Picture
     */
    public Picture(Vector entries) {
        this.entries = entries;
    }

    public PictureContext draw() throws PictureException {
        PictureContext pictureContext = new PictureContext();

        draw(pictureContext);
        return pictureContext;
    }

    public void draw(PictureContext pictureContext) throws PictureException {
        Enumeration en = entries.elements();

        while (en.hasMoreElements()) {
            PictureEntry entry = (PictureEntry) en.nextElement();
            entry.draw(pictureContext);
        }
    }
}