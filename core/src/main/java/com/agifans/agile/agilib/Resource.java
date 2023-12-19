package com.agifans.agile.agilib;

public abstract class Resource {

    /**
     * True if this resource has been "loaded" by the interpreter.
     */
    public boolean isLoaded;

    public int index;

    /**
     * Handles both the encrypt and decrypt operations. They're both the same, as the XOR is reversed
     * if you do it a second time.
     * 
     * @param rawData 
     * @param start
     * @param end 
     */
    protected void crypt(byte[] rawData, int start, int end) {
        int avisDurganPos = 0;

        for (int i = start; i < end; i++) {
            rawData[i] ^= (byte)"Avis Durgan".charAt(avisDurganPos++ % 11);
        }
    }
    
}
