/*
 *  PictureEntryAbsLine.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.pic;

import java.awt.*;
import java.util.Enumeration;

/**
 * <P><B><CODE>0xF6</CODE></B>: Absolute line</P>
 * <p>
 * Function: Draws lines between points. The first two arguments are the
 * starting coordinates. The remaining arguments are in groups of two which
 * give the coordinates of the next location to draw a line to. There can be
 * any number of arguments but there should always be an even number.
 * </P><P>
 * Example: <CODE>F6 30 50 34 51 38 53 F?</CODE>
 * </P><P>
 * This sequence draws a line from (48, 80) to (52, 81), and a line from
 * (52, 81) to (56, 83).
 * </P>
 */
public class PictureEntryAbsLine extends PictureEntryMulti {
    public void draw(PictureContext pictureContext) {
        Enumeration en = points.elements();

        Point p1 = (Point) en.nextElement();

        if (points.size() == 1) {
            pictureContext.drawLine(p1.x, p1.y, p1.x, p1.y);
        } else {
            while (en.hasMoreElements()) {
                Point p2 = (Point) en.nextElement();
                pictureContext.drawLine(p1.x, p1.y, p2.x, p2.y);
                p1 = p2;
            }
        }
    }
}
