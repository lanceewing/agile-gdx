/*
 *  PictureEntryRelLine.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.pic;

import java.awt.*;
import java.util.Enumeration;

/**
 * <P><B><CODE>0xF7</CODE></B>: Relative line</P>
 * <p>
 * Function: Draw short relative lines. By relative we mean that the data gives
 * displacements which are relative from the current location. The first
 * argument gives the standard starting coordinates. All the arguments which
 * follow these first two are of the following format:
 * </P><PRE>
 * +---+-----------+---+-----------+
 * | S |   Xdisp   | S |   Ydisp   |
 * +---+---+---+---+---+---+---+---+
 * | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
 * +---+---+---+---+---+---+---+---+</PRE>
 * <p>
 * This gives a displacement range of between -7 and +7 for both the X and the Y
 * direction.
 * </P><P>
 * Example: <CODE>F7 10 10 22 40 06 CC F?</CODE>
 * </P><PRE>
 * S
 * +              S = Start
 * X+++X         X = End of each line
 * +         + = pixels in each line
 * E   +         E = End
 * +  +
 * + +         Remember that CC = (x-4, y-4).
 * ++
 * X</PRE>
 */

public class PictureEntryRelLine extends PictureEntryMulti {
    public void draw(PictureContext pictureContext) {
        Enumeration en = points.elements();
        Point p;
        int x1, y1, x2, y2;

        p = (Point) en.nextElement();
        x1 = x2 = p.x;
        y1 = y2 = p.y;

        pictureContext.putPixel(x1, y1);

        while (en.hasMoreElements()) {
            p = (Point) en.nextElement();
            x2 += p.x;
            y2 += p.y;

            pictureContext.drawLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
        }
    }
}
