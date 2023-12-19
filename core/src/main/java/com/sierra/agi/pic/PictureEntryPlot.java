/*
 *  PictureEntryPlot.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.pic;

import java.awt.*;
import java.util.Enumeration;

public class PictureEntryPlot extends PictureEntryMulti {
    /**
     * Circle Bitmaps
     */
    protected static final short[][] circles = new short[][]
            {
                    {0x80},
                    {0xfc},
                    {0x5f, 0xf4},
                    {0x66, 0xff, 0xf6, 0x60},
                    {0x23, 0xbf, 0xff, 0xff, 0xee, 0x20},
                    {0x31, 0xe7, 0x9e, 0xff, 0xff, 0xde, 0x79, 0xe3, 0x00},
                    {0x38, 0xf9, 0xf3, 0xef, 0xff, 0xff, 0xff, 0xfe, 0xf9, 0xf3, 0xe3, 0x80},
                    {0x18, 0x3c, 0x7e, 0x7e, 0x7e, 0xff, 0xff, 0xff, 0xff, 0xff, 0x7e, 0x7e, 0x7e, 0x3c, 0x18}
            };

    /**
     * Splatter Brush Bitmaps
     */
    protected static final short[] splatterMap = new short[]
            {
                    0x20, 0x94, 0x02, 0x24, 0x90, 0x82, 0xa4, 0xa2,
                    0x82, 0x09, 0x0a, 0x22, 0x12, 0x10, 0x42, 0x14,
                    0x91, 0x4a, 0x91, 0x11, 0x08, 0x12, 0x25, 0x10,
                    0x22, 0xa8, 0x14, 0x24, 0x00, 0x50, 0x24, 0x04
            };

    /**
     * Starting Bit Position
     */
    protected static final short[] splatterStart = new short[]
            {
                    0x00, 0x18, 0x30, 0xc4, 0xdc, 0x65, 0xeb, 0x48,
                    0x60, 0xbd, 0x89, 0x05, 0x0a, 0xf4, 0x7d, 0x7d,
                    0x85, 0xb0, 0x8e, 0x95, 0x1f, 0x22, 0x0d, 0xdf,
                    0x2a, 0x78, 0xd5, 0x73, 0x1c, 0xb4, 0x40, 0xa1,
                    0xb9, 0x3c, 0xca, 0x58, 0x92, 0x34, 0xcc, 0xce,
                    0xd7, 0x42, 0x90, 0x0f, 0x8b, 0x7f, 0x32, 0xed,
                    0x5c, 0x9d, 0xc8, 0x99, 0xad, 0x4e, 0x56, 0xa6,
                    0xf7, 0x68, 0xb7, 0x25, 0x82, 0x37, 0x3a, 0x51,
                    0x69, 0x26, 0x38, 0x52, 0x9e, 0x9a, 0x4f, 0xa7,
                    0x43, 0x10, 0x80, 0xee, 0x3d, 0x59, 0x35, 0xcf,
                    0x79, 0x74, 0xb5, 0xa2, 0xb1, 0x96, 0x23, 0xe0,
                    0xbe, 0x05, 0xf5, 0x6e, 0x19, 0xc5, 0x66, 0x49,
                    0xf0, 0xd1, 0x54, 0xa9, 0x70, 0x4b, 0xa4, 0xe2,
                    0xe6, 0xe5, 0xab, 0xe4, 0xd2, 0xaa, 0x4c, 0xe3,
                    0x06, 0x6f, 0xc6, 0x4a, 0xa4, 0x75, 0x97, 0xe1
            };

    public void draw(PictureContext pictureContext) {
        if ((pictureContext.penStyle & 0x20) == 0x20) {
            drawPlot(pictureContext);
        } else {
            drawPoints(pictureContext);
        }
    }

    public void drawPlot(PictureContext pictureContext) {
        Enumeration en = points.elements();
        int circlePos = 0;
        int bitPos;
        int x, y, x1, y1, penSize, penSizeTrue;
        boolean circle;
        int[] p;

        circle = !((pictureContext.penStyle & 0x10) == 0x10);
        penSize = (pictureContext.penStyle & 0x07);
        penSizeTrue = penSize;

        while (en.hasMoreElements()) {
            p = (int[]) en.nextElement();
            circlePos = 0;
            bitPos = splatterStart[p[0]];
            x = p[1];
            y = p[2];

            if (x < penSize) {
                x = penSize - 1;
            }

            if (y < penSize) {
                y = penSize;
            }

            for (y1 = y - penSize; y1 <= y + penSize; y1++) {
                for (x1 = x - (penSize + 1) / 2; x1 <= x + penSize / 2; x1++) {
                    if (circle) {
                        if (!(((circles[penSizeTrue][circlePos >> 0x3] >> (0x7 - (circlePos & 0x7))) & 0x1) == 0x1)) {
                            circlePos++;
                            continue;
                        }

                        circlePos++;
                    }

                    if (((splatterMap[bitPos >> 3] >> (7 - (bitPos & 7))) & 1) == 1) {
                        pictureContext.putPixel(x1, y1);
                    }

                    bitPos++;

                    if (bitPos == 0xff) {
                        bitPos = 0;
                    }
                }
            }
        }
    }

    public void drawPoints(PictureContext pictureContext) {
        Enumeration en = points.elements();
        int circlePos;
        int x, y, x1, y1, penSize, penSizeTrue;
        boolean circle;
        Point p;

        circle = !((pictureContext.penStyle & 0x10) == 0x10);
        penSize = (pictureContext.penStyle & 0x07);
        penSizeTrue = penSize;

        while (en.hasMoreElements()) {
            p = (Point) en.nextElement();
            x = p.x;
            y = p.y;
            circlePos = 0;

            if (x < penSize) {
                x = penSize - 1;
            }

            if (y < penSize) {
                y = penSize;
            }

            for (y1 = y - penSize; y1 <= y + penSize; y1++) {
                for (x1 = x - (penSize + 1) / 2; x1 <= x + penSize / 2; x1++) {
                    if (circle) {
                        if (!(((circles[penSizeTrue][circlePos >> 0x3] >> (0x7 - (circlePos & 0x7))) & 0x1) == 0x1)) {
                            circlePos++;
                            continue;
                        }

                        circlePos++;
                    }

                    pictureContext.putPixel(x1, y1);
                }
            }
        }
    }
}
