/**
 * SegmentedInputStream.java
 * Adventure Game Interpreter I/O Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Implentation of <CODE>InputStream</CODE> that gives access
 * to a part of a file.
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class SegmentedInputStream extends InputStream {
    /** File pointer to the opened volume file. */
    protected RandomAccessFile randomFile;

    /** Current offset in the volume file. */
    protected int offset;

    /** Length of the resource data remaining. */
    protected int length;

    /**
     * Offset of the last call to <code>mark</code>.
     *
     * @see #mark(int)
     */
    protected int marked;

    /**
     * Creates a new <code>SegmentedInputStream</code>.
     *
     * @param  pEntry      Pointer to a entry in the directory table.
     * @throws IOException Can throw IOException.
     */
    public SegmentedInputStream(RandomAccessFile file, int offset, int length) throws IOException {
        this.offset = offset;
        this.length = length;
        this.randomFile = file;
        this.marked = offset;

        randomFile.seek(offset);
    }

    public int available() {
        return length;
    }

    public void close() throws IOException {
        randomFile.close();
        randomFile = null;
    }

    public void mark(int readlimit) {
        marked = offset;
    }

    public void reset() throws IOException {
        int l = offset - marked;

        offset = marked;
        length -= l;

        randomFile.seek(offset);
    }

    public int diff() {
        return offset - marked;
    }

    public int getOffset() {
        return offset;
    }

    public RandomAccessFile getRandomAccessFile() {
        return randomFile;
    }

    /**
     * Tests if this input stream supports the <CODE>mark</CODE>
     * and <CODE>reset</CODE> methods.
     * <P>
     * In this implentation of <CODE>InputStream</CODE>, it always
     * returns <CODE>true</CODE>.
     *
     * @see    #mark(int)
     * @see    #reset()
     * @see    java.io.InputStream
     * @return Returns <CODE>true</CODE>
     */
    public boolean markSupported() {
        return true;
    }

    public int read() throws IOException {
        int r;

        if (length <= 0)
            return -1;

        r = randomFile.read();

        if (r >= 0) {
            offset++;
            length--;
        }

        return r;
    }

    public int read(byte[] b) throws IOException {
        int r, l = b.length;

        if (length <= 0) {
            return -1;
        }

        if (l > length) {
            l = length;
        }

        r = randomFile.read(b, 0, l);

        if (r > 0) {
            offset += r;
            length -= r;
        }

        return r;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int r;

        if (length <= 0) {
            return -1;
        }

        if (len > length) {
            len = length;
        }

        r = randomFile.read(b, off, len);

        if (r > 0) {
            offset += r;
            length -= r;
        }

        return r;
    }

    public long skip(long n) throws IOException {
        if (length <= 0) {
            return 0;
        }

        if (n > length) {
            n = length;
        }

        offset += n;
        length -= n;
        randomFile.seek(offset);
        return n;
    }
}