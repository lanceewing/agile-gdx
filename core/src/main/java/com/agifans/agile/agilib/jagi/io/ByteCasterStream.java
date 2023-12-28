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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Interprets stream's data.
 *
 * @author Dr. Z
 * @version 0.00.00.02
 */
public class ByteCasterStream extends FilterInputStream {

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    
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
    
    public byte[] readAllBytes() throws IOException {
        return readNBytes(Integer.MAX_VALUE);
    }
    
    public byte[] readNBytes(int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("len < 0");
        }

        List<byte[]> bufs = null;
        byte[] result = null;
        int total = 0;
        int remaining = len;
        int n;
        do {
            byte[] buf = new byte[Math.min(remaining, DEFAULT_BUFFER_SIZE)];
            int nread = 0;

            // read to EOF which may read more or less than buffer size
            while ((n = read(buf, nread,
                    Math.min(buf.length - nread, remaining))) > 0) {
                nread += n;
                remaining -= n;
            }

            if (nread > 0) {
                if (nread < buf.length) {
                    buf = Arrays.copyOfRange(buf, 0, nread);
                }
                total += nread;
                if (result == null) {
                    result = buf;
                } else {
                    if (bufs == null) {
                        bufs = new ArrayList<>();
                        bufs.add(result);
                    }
                    bufs.add(buf);
                }
            }
            // if the last call to read returned -1 or the number of bytes
            // requested have been read then break
        } while (n >= 0 && remaining > 0);

        if (bufs == null) {
            if (result == null) {
                return new byte[0];
            }
            return result.length == total ?
                result : Arrays.copyOf(result, total);
        }

        result = new byte[total];
        int offset = 0;
        remaining = total;
        for (byte[] b : bufs) {
            int count = Math.min(b.length, remaining);
            System.arraycopy(b, 0, result, offset, count);
            offset += count;
            remaining -= count;
        }

        return result;
    }
}