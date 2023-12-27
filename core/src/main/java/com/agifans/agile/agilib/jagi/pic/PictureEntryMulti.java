/*
 *  PictureEntryMulti.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.pic;

import java.util.Vector;

import com.agifans.agile.agilib.jagi.awt.Point;

public abstract class PictureEntryMulti extends PictureEntry {
    protected Vector points = new Vector();

    public void add(int x, int y) {
        points.add(new Point(x, y));
    }

    public void add(int c) {
        points.add(Integer.valueOf(c));
    }

    public void add(int[] c) {
        points.add(c);
    }
}
