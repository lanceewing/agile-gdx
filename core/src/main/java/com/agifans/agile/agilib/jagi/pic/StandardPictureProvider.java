/*
 *  StandardPictureProvider.java
 *  Adventure Game Interpreter Picture Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.pic;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class StandardPictureProvider implements PictureProvider {
    protected static final short CMD_START = (short) 0xF0;

    protected static final short CMD_CHANGEPICCOLOR = (short) 0xF0;
    protected static final short CMD_DISABLEPICDRAW = (short) 0xF1;
    protected static final short CMD_CHANGEPRICOLOR = (short) 0xF2;
    protected static final short CMD_DISABLEPRIDRAW = (short) 0xF3;
    protected static final short CMD_DRAWYCORNER = (short) 0xF4;
    protected static final short CMD_DRAWXCORNER = (short) 0xF5;
    protected static final short CMD_DRAWABSLINE = (short) 0xF6;
    protected static final short CMD_DRAWRELLINE = (short) 0xF7;
    protected static final short CMD_FILL = (short) 0xF8;
    protected static final short CMD_CHANGEPEN = (short) 0xF9;
    protected static final short CMD_PLOT = (short) 0xFA;
    protected static final short CMD_EOP = (short) 0xFF;

    public Picture loadPicture(InputStream in) throws IOException, PictureException {
        int command, c, x, y;
        Vector entries = new Vector();
        int lastPen = 0;
        PictureEntryMulti entry;

        try {
            command = in.read();

            while (true) {
                if (command < 0) {
                    break;
                }

                switch (command) {
                    case CMD_CHANGEPICCOLOR:
                        entries.add(new PictureEntryChangePicColor((byte) in.read()));
                        command = in.read();
                        break;

                    case CMD_CHANGEPRICOLOR:
                        entries.add(new PictureEntryChangePriColor((byte) in.read()));
                        command = in.read();
                        break;

                    case CMD_DISABLEPICDRAW:
                        entries.add(new PictureEntryChangePicColor(null));
                        command = in.read();
                        break;

                    case CMD_DISABLEPRIDRAW:
                        entries.add(new PictureEntryChangePriColor(null));
                        command = in.read();
                        break;

                    case CMD_DRAWXCORNER:
                    case CMD_DRAWYCORNER:
                        if (command == CMD_DRAWXCORNER) {
                            entry = new PictureEntryDrawX();
                        } else {
                            entry = new PictureEntryDrawY();
                        }

                        entry.add(in.read(), in.read());

                        while (true) {
                            command = in.read();

                            if ((command >= CMD_START) || (command < 0)) {
                                break;
                            }

                            entry.add(command);
                        }

                        entries.add(entry);
                        break;

                    case CMD_DRAWABSLINE:
                        entry = new PictureEntryAbsLine();
                        entry.add(in.read(), in.read());

                        while (true) {
                            command = in.read();

                            if ((command >= CMD_START) || (command < 0)) {
                                break;
                            }

                            entry.add(command, in.read());
                        }

                        entries.add(entry);
                        break;

                    case CMD_DRAWRELLINE:
                        entry = new PictureEntryRelLine();
                        entry.add(in.read(), in.read());

                        while (true) {
                            command = in.read();

                            if ((command >= CMD_START) || (command < 0)) {
                                break;
                            }

                            x = (command & 0x70) >> 4;
                            y = (command & 0x07);

                            if ((command & 0x80) == 0x80) {
                                x = -x;
                            }

                            if ((command & 0x08) == 0x08) {
                                y = -y;
                            }

                            entry.add(x, y);
                        }

                        entries.add(entry);
                        break;

                    case CMD_FILL:
                        entry = new PictureEntryFill();

                        while (true) {
                            command = in.read();

                            if ((command >= CMD_START) || (command < 0)) {
                                break;
                            }

                            c = in.read();
                            entry.add(command, c);
                        }

                        entries.add(entry);
                        break;

                    case CMD_CHANGEPEN:
                        lastPen = in.read();
                        entries.add(new PictureEntryChangePen((byte) lastPen));
                        command = in.read();
                        break;

                    case CMD_PLOT:
                        entry = new PictureEntryPlot();

                        while (true) {
                            command = in.read();

                            if ((command < 0) || (command >= CMD_START)) {
                                break;
                            }

                            if ((lastPen & 0x20) == 0x20) {
                                command = (command >> 1) & 0x7f;
                                x = in.read();
                                y = in.read();
                                entry.add(new int[]{command, x, y});
                            } else {
                                x = command;
                                y = in.read();
                                entry.add(x, y);
                            }
                        }

                        entries.add(entry);
                        break;

                    case CMD_EOP:
                        command = -1;
                        break;

                    default:
                        throw new CorruptedPictureException();
                }
            }
        } catch (EOFException eex) {
        }

        in.close();
        return new Picture(entries);
    }

    /*
    protected boolean next() throws PictureException
    {
        if (in == null)
        {
            return false;
        }
        
        try
        {
            if (nextCommand < 0)
            {
                nextCommand = in.read();

                if (nextCommand < 0)
                {
                    if (provider != null)
                    {
                        in.close();
                        in = null;
                    }
                    
                    endReached = true;
                    return false;
                }
            }

            switch (nextCommand)
            {
            case CMD_CHANGEPICCOLOR:
                picContext.picColor = (byte)in.read();
                nextCommand = -1;
                break;

            case CMD_CHANGEPRICOLOR:
                picContext.priColor = (byte)in.read();
                nextCommand = -1;
                break;

            case CMD_DISABLEPICDRAW:
                picContext.picColor = (byte)-1;
                nextCommand = -1;
                break;

            case CMD_DISABLEPRIDRAW:
                picContext.priColor = (byte)-1;
                nextCommand = -1;
                break;

            case CMD_DRAWXCORNER:
                drawXCorner();
                break;

            case CMD_DRAWYCORNER:
                drawYCorner();
                break;

            case CMD_DRAWABSLINE:
                drawAbsoluteLine();
                break;

            case CMD_DRAWRELLINE:
                drawRelativeLine();
                break;

            case CMD_FILL:
                drawFill();
                break;

            case CMD_CHANGEPEN:
                picContext.penStyle = (byte)in.read();
                nextCommand = -1;
                break;

            case CMD_PLOT:
                drawPlot();
                break;

            case CMD_EOP:
                in.close();
                in = null;
                break;

            default:
                throw new CorruptedPictureException();
            }
        }
        catch (IOException ioex)
        {
            in = null;
        }
        
        return true;
    }*/

    /*
    protected void drawPlot() throws IOException
    {
        int c, x, y;
        
        while (true)
        {
            c = in.read();
            
            if ((c < 0) || (c >= CMD_START))
            {
                nextCommand = c;
                break;
            }
            
            if ((picContext.penStyle & 0x20) == 0x20)
            {
                c = (c >> 1) & 0x7f;
                x = in.read();
                y = in.read();
                drawPlot(c, x, y);
            }
            else
            {
                x = c;
                y = in.read();
                drawPlot(x, y);
            }
        }
    }
    
    protected void drawPlot(int patternNumber, int x, int y)
    {
	int     circlePos = 0;
        int     bitPos    = splatterStart[patternNumber];
	int     x1, y1, penSize, penSizeTrue;
        boolean circle;

        circle      = !((picContext.penStyle & 0x10) == 0x10);
        penSize     = (picContext.penStyle & 0x07);
        penSizeTrue = penSize;

	if (x < penSize)
        {
            x = penSize - 1;
        }
        
	if (y < penSize)
        {
            y = penSize;
        }

	for (y1 = y - penSize; y1 <= y + penSize; y1++)
        {
            for (x1 = x - (penSize + 1) / 2; x1 <= x + penSize / 2; x1++)
            {
                if (circle)
                {
                    if (!(((circles[penSizeTrue][circlePos >> 0x3] >> (0x7 - (circlePos & 0x7))) & 0x1) == 0x1))
                    {
                        circlePos++;
                        continue;
                    }
                    
                    circlePos++;
                }
                
                if (((splatterMap[bitPos >> 3] >> (7 - (bitPos & 7))) & 1) == 1)
                {
                    picContext.putPixel(x1, y1);
                }

                bitPos++;

                if (bitPos == 0xff)
                {
                    bitPos = 0;
                }
            }
	}
    }
    
    protected void drawPlot(int x, int y)
    {
	int     circlePos = 0;
	int     x1, y1, penSize, penSizeTrue;
        boolean circle;

        circle      = !((picContext.penStyle & 0x10) == 0x10);
        penSize     = (picContext.penStyle & 0x07);
        penSizeTrue = penSize;

	if (x < penSize)
        {
            x = penSize - 1;
        }
        
	if (y < penSize)
        {
            y = penSize;
        }

	for (y1 = y - penSize; y1 <= y + penSize; y1++)
        {
            for (x1 = x - (penSize + 1) / 2; x1 <= x + penSize / 2; x1++)
            {
                if (circle)
                {
                    if (!(((circles[penSizeTrue][circlePos >> 0x3] >> (0x7 - (circlePos & 0x7))) & 0x1) == 0x1))
                    {
                        circlePos++;
                        continue;
                    }
                    
                    circlePos++;
                }

                picContext.putPixel(x1, y1);
            }
	}
    }*/
}
