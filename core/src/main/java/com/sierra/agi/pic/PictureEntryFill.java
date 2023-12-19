/*
 *  PictureEntryFill.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.pic;

import java.awt.*;
import java.util.EmptyStackException;
import java.util.Enumeration;

/**
 * <P><B><CODE>0xF8</CODE></B>: Fill</P>
 * <p>
 * Function: Flood fill from the locations given. Arguments are given in groups
 * of two bytes which give the coordinates of the location to start the fill
 * at. If picture drawing is enabled then it flood fills from that location on
 * the picture screen to all pixels locations that it can reach which are white
 * in colour. The boundary is given by any pixels which are not white.
 * </P><P>
 * If priority drawing is enabled, and picture drawing is not enabled, then it
 * flood fills from that location on the priority screen to all pixels that it
 * can reach which are red in colour. The boundary in this case is given by any
 * pixels which are not red.
 * </P><P>
 * If both picture drawing and priority drawing are enabled, then a flood fill
 * naturally enough takes place on both screens. In this case there is a
 * difference in the way the fill takes place in the priority screen. The
 * difference is that it not only looks for its own boundary, but also stops if
 * it reaches a boundary that exists in the picture screen but does not
 * necessarily exist in the priority screen.
 * </P>
 */
public class PictureEntryFill extends PictureEntryMulti {
    public void draw(PictureContext pictureContext) {
        Point current;
        Enumeration en = points.elements();
        PointStack stack = new PointStack(200, 200);
        int width = pictureContext.width - 1;
        int height = pictureContext.height - 1;

        while (en.hasMoreElements()) {
            current = (Point) en.nextElement();

            stack.push(current.x, current.y);

            try {
                while (true) {
                    stack.pop(current);

                    if (pictureContext.isFillCorrect(current.x, current.y)) {
                        pictureContext.putPixel(current.x, current.y);

                        if (current.x > 0 && pictureContext.isFillCorrect(current.x - 1, current.y)) {
                            stack.push(current.x - 1, current.y);
                        }

                        if (current.x < width && pictureContext.isFillCorrect(current.x + 1, current.y)) {
                            stack.push(current.x + 1, current.y);
                        }

                        if (current.y < height && pictureContext.isFillCorrect(current.x, current.y + 1)) {
                            stack.push(current.x, current.y + 1);
                        }

                        if (current.y > 0 && pictureContext.isFillCorrect(current.x, current.y - 1)) {
                            stack.push(current.x, current.y - 1);
                        }
                    }
                }
            } catch (EmptyStackException esex) {
            }
        }
    }

    public static class PointStack {
        protected int increment;
        protected int elementCount;
        protected short[] x;
        protected short[] y;

        /**
         * Creates new Point Stack
         */
        public PointStack() {
            increment = 15;
        }

        public PointStack(int initialSize, int increment) {
            this.increment = increment;
            ensureCapacity(initialSize);
        }

        public void pop(Point pt) {
            if (elementCount == 0) {
                throw new EmptyStackException();
            }

            elementCount--;
            pt.x = x[elementCount];
            pt.y = y[elementCount];
        }

        public void push(int x, int y) {
            ensureCapacity(elementCount + 1);

            this.x[elementCount] = (short) x;
            this.y[elementCount] = (short) y;
            elementCount++;
        }

        public void clear() {
            elementCount = 0;
        }

        public void ensureCapacity(int minCapacity) {
            if (x == null) {
                x = new short[minCapacity + increment];
                y = new short[minCapacity + increment];
            } else if (x.length < minCapacity) {
                short[] nx = new short[minCapacity + increment];
                short[] ny = new short[minCapacity + increment];
                int i, l = elementCount;

                for (i = 0; i < l; i++) {
                    nx[i] = x[i];
                }

                for (i = 0; i < l; i++) {
                    ny[i] = y[i];
                }

                x = nx;
                y = ny;
            }
        }
    }
}
