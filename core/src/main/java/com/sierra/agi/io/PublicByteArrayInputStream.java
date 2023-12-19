package com.sierra.agi.io;

import java.io.ByteArrayInputStream;

/**
 * A sub-class of ByteArrayInputStream that makes public some of the internal state
 * of its super class, such as the count and pos.
 *
 * @author Lance Ewing
 */
public class PublicByteArrayInputStream extends ByteArrayInputStream {

    /**
     * Constructor for PublicByteArrayInputStream.
     *
     * @param buf The byte array from which the InputStream is to be created.
     */
    public PublicByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    /**
     * Constructor for PublicByteArrayInputStream.
     *
     * @param buf    The input buffer.
     * @param offset The offset in the buffer of the first byte to read.
     * @param length The maximum number of bytes to read from the buffer.
     */
    public PublicByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    /**
     * Gets the current position within the byte array.
     *
     * @return The current position within the byte array.
     */
    public int getPosition() {
        return this.pos;
    }

    /**
     * Gets the number of bytes in the byte array.
     *
     * @return The number of bytes in the byte array.
     */
    public int getCount() {
        return this.count;
    }
}
