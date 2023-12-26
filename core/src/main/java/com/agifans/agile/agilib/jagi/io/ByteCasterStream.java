/**
 * ByteCasterStream.java
 * Adventure Game Interpreter I/O Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.io;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interprets stream's data.
 *
 * @author Dr. Z
 * @version 0.00.00.02
 */
public class ByteCasterStream extends FilterInputStream {
    public ByteCasterStream(InputStream in) {
        super(in);
    }

    public short readUnsignedByte() throws IOException {
        int v = in.read();

        if (v < 0) {
            throw new EOFException();
        }

        return (short) v;
    }

    public int hiloReadUnsignedShort() throws IOException {
        byte[] b = new byte[2];

        IOUtils.fill(in, b, 0, 2);

        return ((b[0] & 0xFF) << 8) |
                (b[1] & 0xFF);
    }

    public long hiloReadUnsignedInt() throws IOException {
        byte[] b = new byte[4];

        IOUtils.fill(in, b, 0, 4);

        return ((long) (b[0] & 0xFF) << 24) |
                ((b[1] & 0xFF) << 16) |
                ((b[2] & 0xFF) << 8) |
                (b[3] & 0xFF);
    }

    public int lohiReadUnsignedShort() throws IOException {
        byte[] b = new byte[2];

        IOUtils.fill(in, b, 0, 2);

        return ((b[1] & 0xFF) << 8) |
                (b[0] & 0xFF);
    }

    public long lohiReadUnsignedInt() throws IOException {
        byte[] b = new byte[4];

        IOUtils.fill(in, b, 0, 4);

        return ((long) (b[3] & 0xFF) << 24) |
                ((b[2] & 0xFF) << 16) |
                ((b[1] & 0xFF) << 8) |
                (b[0] & 0xFF);
    }
}