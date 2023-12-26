/**
 * LZWInputStream.java
 * Adventure Game Interpreter I/O Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * LZW Decompressor. This class is a Input Stream Layer
 * to uncompress on-the-fly resource stream using the LZW
 * algorithm used in AGI v3.
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class LZWInputStream extends InputStream {
    protected final static int MAX_BITS = 12;
    protected final static int TABLE_SIZE = 18041;
    protected final static int START_BITS = 9;
    protected boolean endOfStream = false;
    protected int bits;
    protected int maxValues;
    protected int maxCodes;
    protected InputStream in;
    protected byte[] appendChars = new byte[TABLE_SIZE];
    protected byte[] decodeStack = new byte[8192];
    protected int decodeStackSize = -1;
    protected int[] prefixCode = new int[TABLE_SIZE];

    protected int bitCount = 0;
    protected long bitBuffer = 0;
    protected int unext;
    protected int unew;
    protected int uold;
    protected int ubits;
    protected int uc;
    /** Creates new LZWException */
    public LZWInputStream(InputStream in) throws IOException {
        this.in = in;
        ubits = setBits(START_BITS);
        unext = 257;
        uold = inputCode();
        uc = uold;
        unew = inputCode();
    }

    protected int setBits(int value) {
        if (value == MAX_BITS) {
            return 1;
        }

        bits = value;
        maxValues = (1 << bits) - 1;
        maxCodes = maxValues - 1;
        return 0;
    }

    protected int inputCode() throws IOException {
        long b;
        int r;

        long q = bitBuffer;
        int s = bitCount;

        while (s <= 24) {
            b = in.read();

            if (b < 0) {
                if (s == 0) {
                    throw new EOFException();
                }

                break;
            }

            b <<= s;
            q |= b;
            s += 8;
        }

        r = (int) (q & 0x7fff);
        r %= (1 << bits);

        bitBuffer = (q >> bits);
        bitCount = (s - bits);

        return r;
    }

    protected int decodeString(int offset, int code) throws IOException {
        int i;

        for (i = 0; code > 255; ) {
            decodeStack[offset] = appendChars[code];
            offset++;
            code = prefixCode[code];

            if (i++ >= 4000) {
                throw new IOException("LZW: Error in Code Expansion");
            }
        }

        decodeStack[offset] = (byte) code;
        return offset;
    }

    protected void unpack() throws IOException {
        if (endOfStream) {
            return;
        }

        if (decodeStackSize > 0) {
            return;
        }

        if (unew == 0x101) {
            endOfStream = true;
            return;
        }

        if (unew == 0x100) {
            unext = 258;
            ubits = setBits(START_BITS);
            uold = inputCode();
            uc = uold;

            decodeStack[0] = (byte) uc;
            decodeStackSize = 0;

            unew = inputCode();
        } else {
            if (unew >= unext) {
                decodeStack[0] = (byte) uc;
                decodeStackSize = decodeString(1, uold);
            } else {
                decodeStackSize = decodeString(0, unew);
            }

            uc = decodeStack[decodeStackSize];

            if (unext > maxCodes) {
                ubits = setBits(bits + 1);
            }

            prefixCode[unext] = uold;
            appendChars[unext] = (byte) uc;

            unext++;
            uold = unew;

            unew = inputCode();
        }
    }

    public int read() throws IOException {
        int c;

        while (decodeStackSize < 0) {
            try {
                unpack();
            } catch (EOFException eex) {
                endOfStream = true;
            }

            if (endOfStream) {
                close();
                return -1;
            }
        }

        c = decodeStack[decodeStackSize];
        decodeStackSize--;

        return c;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int c = 0;

        while (!endOfStream) {
            if (decodeStackSize >= 0) {
                while ((decodeStackSize >= 0) && (len > 0)) {
                    b[off] = decodeStack[decodeStackSize];
                    decodeStackSize--;
                    off++;
                    len--;
                    c++;
                }

                if (len == 0) {
                    break;
                }
            }

            try {
                unpack();
            } catch (EOFException eex) {
                endOfStream = true;
            }
        }

        if (endOfStream) {
            close();
        }

        if (c == 0)
            c = -1;

        return c;
    }

    public void close() throws IOException {
        endOfStream = true;

        if (in != null) {
            in.close();
        }

        /** Garbage Collector Optimization */
        in = null;
        appendChars = null;
        decodeStack = null;
        prefixCode = null;
    }
}