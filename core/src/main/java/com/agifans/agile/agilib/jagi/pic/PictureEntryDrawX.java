/*
 *  PictureEntryDrawX.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.pic;

import java.util.Enumeration;

import com.agifans.agile.agilib.jagi.awt.Point;

/**
 * <P><B><CODE>0xF5</CODE></B>: Draw an X corner</P>
 *
 * <P>Function: The first two arguments for this action are the coordinates of
 * the starting position on the screen in the order x and then y. The remaining
 * arguments are in the order x1, y1, x2, y2, ...
 * </P><P>
 * Note that the x component is the first to be changed and also note that this
 * action does not necessarily end on either component, it just ends when the
 * next byte of 0xF0 or above is encountered. A line is drawn after each byte
 * is processed.
 * </P><P>
 * Example: <CODE>F5 16 16 18 12 16 F?</CODE>
 * </P><PRE>
 * (0x16, 0x12)   (0x18, 0x12)
 * EXX
 * X            S = Start
 * X            E = End
 * X            X = normal piXel
 * SXX
 * (0x16, 0x16)   (0x18, 0x16)</PRE>
 */
public class PictureEntryDrawX extends PictureEntryMulti {
    public void draw(PictureContext pictureContext) {
        Enumeration en = points.elements();
        int x1, y1, x2, y2;
        boolean b = true;
        Point p;

        p = (Point) en.nextElement();
        x1 = x2 = p.x;
        y1 = y2 = p.y;

        while (en.hasMoreElements()) {
            if (b) {
                x2 = ((Integer) en.nextElement()).intValue();
            } else {
                y2 = ((Integer) en.nextElement()).intValue();
            }

            pictureContext.drawLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
            b = !b;
        }
    }
}
