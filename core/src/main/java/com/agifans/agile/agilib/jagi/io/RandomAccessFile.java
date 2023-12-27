package com.agifans.agile.agilib.jagi.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * GWT doesn't support the RandomAccessFile, so this is a simple wrapper around
 * an InputStream that provides a random access style API by delegating to similar
 * methods that are provided by the InputStream. It should only be used by an 
 * InputStream that supports mark and reset and in practice is expected to most 
 * likely by a ByteArrayInputStream.
 */
public class RandomAccessFile {
    
    private InputStream inputStream;
    
    public RandomAccessFile(InputStream inputStream) {
        this.inputStream = inputStream;
        this.inputStream.mark(0);
    }

    public void seek(long pos) throws IOException {
        inputStream.reset();
        inputStream.skip(pos);
    }
    
    public int read() throws IOException {
        return inputStream.read();
    }
    
    public int read(byte b[], int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }
    
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }
    
    public void close() throws IOException {
        inputStream.close();
    }
}
