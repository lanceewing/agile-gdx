/**
 * CryptedInputStream.java
 * Adventure Game Interpreter I/O Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Layer to support decryption of sierra's resources.
 * (simple XOR cryption)
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class CryptedInputStream extends FilterInputStream {
    /** Decryption key. */
    protected char[] key;

    /** Current offset. */
    protected int offset;

    /** Cryption begin offset. */
    protected int boffset;

    /**
     * Offset of the last <CODE>mark</CODE> method call.
     *
     * @see #mark(int)
     */
    protected int marked;

    /**
     * Creates a new decryption layer.
     *
     * @param key    Decryption key.
     * @param stream <CODE>InputStream</CODE> to decrypt.
     */
    public CryptedInputStream(InputStream in, String key) {
        super(in);
        this.key = key.toCharArray();
    }

    /**
     * Creates a new decryption layer.
     *
     * @param key    Decryption key.
     * @param stream <CODE>InputStream</CODE> to decrypt.
     */
    public CryptedInputStream(InputStream in, String key, int boffset) {
        super(in);
        this.boffset = boffset;
        this.key = key.toCharArray();
    }

    public void mark(int readlimit) {
        in.mark(readlimit);
        marked = offset;
    }

    public void reset() throws IOException {
        in.reset();
        offset = marked;
    }

    public long skip(long n) throws IOException {
        long r = in.skip(n);

        offset += r;
        return r;
    }

    public int read() throws IOException {
        int r = in.read();

        if (r < 0)
            return r;

        if (offset >= boffset) {
            r ^= key[(offset - boffset) % key.length];
        }

        offset++;
        return r;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int i, j, off2;
        int r = in.read(b, off, len);

        if (r < 0) {
            return r;
        }

        off2 = off + len;
        for (i = off; i < off2; i++) {
            if (offset >= boffset) {
                j = (b[i] & 0xFF);
                j ^= key[(offset - boffset) % key.length];
                b[i] = (byte) j;
            }

            offset++;
        }

        return r;
    }
}