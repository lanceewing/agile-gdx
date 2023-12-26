/**
 * ResourceDirectory.java
 * Adventure Game Interpreter Resource Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.res.dir;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.agifans.agile.agilib.jagi.io.IOUtils;

/**
 * <P><B>Directories</B><BR>
 * Each directory file is of the same format. They contain a finite number
 * of three byte entries, no more than 256. The size will vary depending on the
 * number of files of the type that the directory file is pointing to. Dividing
 * the filesize by three gives the maximum file number of that type of data
 * file. Each entry is of the following format:</P>
 * <PRE>
 * Byte 1          Byte 2          Byte 3 
 * 7 6 5 4 3 2 1 0 7 6 5 4 3 2 1 0 7 6 5 4 3 2 1 0 
 * V V V V P P P P P P P P P P P P P P P P P P P P 
 *
 * V = VOL number.
 * P = Position (offset into VOL file)</PRE>
 * <P>
 * The entry number itself gives the number of the data file that it is
 * pointing to. For example, if the following three byte entry is entry
 * number 45 in the SOUND directory file, <CODE>12 3D FE</CODE>
 * then SOUND.45 is located at position 0x23DFE in the VOL.1 file. The first
 * entry number is entry 0.
 * </P><P>
 * If the three bytes contain the value 0xFFFFFF, then the resource does not
 * exist.</P>
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class ResourceDirectory {
    /** Maximum number of entries in a directory. */
    protected static final int MAX_ENTRIES = 256;

    /** Directory Entries */
    protected int[] entries = new int[MAX_ENTRIES];

    /** Directory CRC */
    protected int crc;

    /** Directory */
    protected int count;

    /** Creates a new Resource Directory */
    public ResourceDirectory(InputStream in) throws IOException {
        byte[] b = new byte[3];
        int i = 0, j = 0, e;
        int c = 0, n = 0;

        try {
            while (true) {
                IOUtils.fill(in, b, 0, 3);

                for (j = 0; j < 3; j++) {
                    c += (b[j] & 0xff);
                }

                if (b[0] != -1) {
                    entries[i] = ((b[0] & 0xf0) >> 4) << 24 | ((b[0] & 0x0f) << 16) | ((b[1] & 0xff) << 8) | (b[2] & 0xff);
                    n++;
                } else {
                    entries[i] = -1;
                }

                i++;
            }
        } catch (EOFException ex) {
        }

        for (; i < MAX_ENTRIES; i++) {
            entries[i] = -1;
        }

        crc = c;
        count = n;
    }

    public int getCRC() {
        return crc;
    }

    public int getVolume(int resourceNumber) {
        if (entries[resourceNumber] == -1) {
            return -1;
        }

        return (entries[resourceNumber] & 0xff000000) >> 24;
    }

    public int getOffset(int resourceNumber) {
        if (entries[resourceNumber] == -1) {
            return -1;
        }

        return (entries[resourceNumber] & 0x00ffffff);
    }

    public int getCount() {
        return count;
    }

    public short[] getNumbers() {
        short[] numbers = new short[count];
        short i, j;

        for (i = 0, j = 0; (i < 256) && (j < count); i++) {
            if (entries[i] != -1) {
                numbers[j++] = i;
            }
        }

        return numbers;
    }
}