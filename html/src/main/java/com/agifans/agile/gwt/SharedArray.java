package com.agifans.agile.gwt;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * GWT fixed size Array implementation that wraps a JavaScript SharedArrayBuffer so 
 * that two different JS contexts can share the data stored in the array. The 
 * SharedArrayBuffer is a mechanism that JS provides for sharing memory between either
 * two web workers, or a web worker and the UI thread. The data is visible to both
 * sides and does not require use of postMessage after the initial set up. For the GWT
 * platform implementation of AGILE, it is used for storing the state of the individual
 * keyboard keys. The UI thread sets the value and the web worker gets the value. This
 * is required due to the fact that the web worker isn't able to directly access 
 * keyboard input but must instead rely on the UI thread to provide it.
 */
public class SharedArray {

    /** 
     * Allocate the SharedArrayBuffer for the Array, based on the capacity required.
     * 
     * @param capacity The number of elements the array will be able to hold.
     * 
     * @return A SharedArrayBuffer of the right size.
     */
    public native static JavaScriptObject getStorageForCapacity(int capacity)/*-{
        // This class only supports Uint32Array, which has 4 bytes per element.
        var BYTES_PER_ELEMENT = 4;
        return new SharedArrayBuffer(capacity * BYTES_PER_ELEMENT);
    }-*/;
    
    /**
     * Constructor for SharedArray.
     * 
     * @param sab SharedArrayBuffer to use for SharedArray. Obtained by calling SharedArray.getStorageForCapacity.
     */
    public SharedArray(JavaScriptObject sab) {
        initialise(sab);
    }

    private native void initialise(JavaScriptObject sab)/*-{
        var BYTES_PER_ELEMENT = 4;
        this.buf = sab;
        this.storage = new Uint32Array(this.buf, 0, (sab.byteLength) / BYTES_PER_ELEMENT);
    }-*/;
    
    public native int get(int index)/*-{
        return Atomics.load(this.storage, index);
    }-*/;
    
    public native void set(int index, int value)/*-{
        Atomics.store(this.storage, index, value);
    }-*/;
}
