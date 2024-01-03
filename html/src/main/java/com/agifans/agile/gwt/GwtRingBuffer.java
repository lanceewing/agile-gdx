package com.agifans.agile.gwt;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.Uint32Array;

/** 
 * GWT RingBuffer based onPaul Adenot's ringbuf.js JavaScript class. Paul works for
 * Mozilla on Firefox and wrote the RingBuffer JS class as a way to use the 
 * SharedArrayBuffer to send data between two JS thread (e.g. two workers, or 
 * a worker and the UI thread) without using the postMessage mechanism, which is
 * exactly what we need for the AGILE GWT platform's key press queue. We don't need
 * to push and pop multiple elements though, so that bit has been simplified.
 * 
 * Read here: https://blog.paul.cx/post/a-wait-free-spsc-ringbuffer-for-the-web/
 * 
 * Original JS code: https://github.com/padenot/ringbuf.js/blob/main/js/ringbuf.js
 */
public class GwtRingBuffer {

    /** 
     * Allocate the SharedArrayBuffer for a RingBuffer, based on the capacity required.
     * 
     * @param capacity The number of elements the ring buffer will be able to hold.
     * 
     * @return A SharedArrayBuffer of the right size.
     */
    public native static JavaScriptObject getStorageForCapacity(int capacity)/*-{
        // This class only supports Uint32Array, which has 4 bytes per element. The
        // extra 8 bytes are for the write and read pointers, so that they also are
        // shared by both ends.
        var BYTES_PER_ELEMENT = 4;
        var bytes = 8 + (capacity + 1) * BYTES_PER_ELEMENT;
        return new SharedArrayBuffer(bytes);
    }-*/;
    
    /**
     * Constructor for RingBuffer.
     * 
     * @param sab SharedArrayBuffer to use for RingBuffer. Obtained by calling RingBuffer.getStorageForCapacity.
     */
    public GwtRingBuffer(JavaScriptObject sab) {
        initialise(sab);
    }
    
    private native void initialise(JavaScriptObject sab)/*-{
        // Maximum usable size is 1<<32 - type.BYTES_PER_ELEMENT bytes in the ring
        // buffer for this version, easily changeable.
        // -4 for the write ptr (uint32_t offsets)
        // -4 for the read ptr (uint32_t offsets)
        // capacity counts the empty slot to distinguish between full and empty.
        this._type = type;
        this._capacity = (sab.byteLength - 8) / type.BYTES_PER_ELEMENT;
        this.buf = sab;
        this.write_ptr = new Uint32Array(this.buf, 0, 1);
        this.read_ptr = new Uint32Array(this.buf, 4, 1);
        this.storage = new Uint32Array(this.buf, 8, this._capacity);
    }-*/;

    /**
     * Push an int value to the ring buffer.
     * 
     * @param value The int to push on to the ring buffer.
     * 
     * @return 1 if added; otherwise 0.
     */
    public native int push(int value)/*-{
        var rd = Atomics.load(this.read_ptr, 0);
        var wr = Atomics.load(this.write_ptr, 0);

        if ((wr + 1) % this._storage_capacity() === rd) {
            // full
            return 0;
        }
        
        var elements = new Uint32Array(1);
        elements[0] = value;

        var len = elements.length;
        var to_write = Math.min(this._available_write(rd, wr), len);
        var first_part = Math.min(this._storage_capacity() - wr, to_write);
        var second_part = to_write - first_part;

        // Handles wrapping around in the buffer.
        this._copy(elements, offset, this.storage, wr, first_part);
        this._copy(elements, offset + first_part, this.storage, 0, second_part);

        // publish the enqueued data to the other side
        Atomics.store(
            this.write_ptr,
            0,
            (wr + to_write) % this._storage_capacity(),
        );

        return to_write;
    }-*/;
    
    /**
     * Read a single int from the ring buffer.
     * 
     * @return The int value read from the queue.
     */
    public native int pop()/*-{
        var rd = Atomics.load(this.read_ptr, 0);
        var wr = Atomics.load(this.write_ptr, 0);

        if (wr === rd) {
            return 0;
        }

        var elements = new Uint32Array(1);
        var len = elements.length;
        var to_read = Math.min(this._available_read(rd, wr), len);

        var first_part = Math.min(this._storage_capacity() - rd, to_read);
        var second_part = to_read - first_part;

        this._copy(this.storage, rd, elements, offset, first_part);
        this._copy(this.storage, 0, elements, offset + first_part, second_part);

        Atomics.store(this.read_ptr, 0, (rd + to_read) % this._storage_capacity());

        return elements[0];
    }-*/;
    
    /**
     * @return True if the ring buffer is empty false otherwise. This can be late
     * on the reader side: it can return true even if something has just been
     * pushed.
     */
    public native boolean empty()/*-{
        var rd = Atomics.load(this.read_ptr, 0);
        var wr = Atomics.load(this.write_ptr, 0);

        return wr === rd;
    }-*/;

    /**
     * @return True if the ring buffer is full, false otherwise. This can be late
     * on the write side: it can return true when something has just been popped.
     */
    public native boolean full()/*-{
        var rd = Atomics.load(this.read_ptr, 0);
        var wr = Atomics.load(this.write_ptr, 0);

        return (wr + 1) % this._storage_capacity() === rd;
    }-*/;

    /**
     * @return The usable capacity for the ring buffer: the number of elements
     * that can be stored.
     */
    public native boolean capacity()/*-{
        return this._capacity - 1;
    }-*/;

    /**
     * @return The number of elements available for reading. This can be late, and
     * report less elements that is actually in the queue, when something has just
     * been enqueued.
     */
    public native int availableRead()/*-{
        var rd = Atomics.load(this.read_ptr, 0);
        var wr = Atomics.load(this.write_ptr, 0);
        return this._available_read(rd, wr);
    }-*/;

    /**
     * @return The number of elements available for writing. This can be late, and
     * report less elements that is actually available for writing, when something
     * has just been dequeued.
     */
    public native int availableWrite()/*-{
        var rd = Atomics.load(this.read_ptr, 0);
        var wr = Atomics.load(this.write_ptr, 0);
        return this._available_write(rd, wr);
    }-*/;
    
    /**
     * @return Number of elements available for reading, given a read and write
     * pointer.
     */
    private native int _available_read(int rd, int wr)/*-{
        return (wr + this._storage_capacity() - rd) % this._storage_capacity();
    }-*/;

    /**
     * @return Number of elements available from writing, given a read and write
     * pointer.
     */
    private native int _available_write(int rd, int wr)/*-{
        return this.capacity() - this._available_read(rd, wr);
    }-*/;

    /**
     * @return The size of the storage for elements not accounting the space for
     * the index, counting the empty slot.
     * @private
     */
    private native int _storage_capacity()/*-{
        return this._capacity;
    }-*/;
    
    /**
     * Copy `size` elements from `input`, starting at offset `offset_input`, to
     * `output`, starting at offset `offset_output`.
     * 
     * @param input The array to copy from
     * @param offset_input The index at which to start the copy
     * @param output The array to copy to
     * @param offset_output The index at which to start copying the elements to
     * @param size The number of elements to copy
     */
    private native void _copy(Uint32Array input, int offset_input, Uint32Array output, int offset_output, int size)/*-{
        for (var i = 0; i < size; i++) {
            output[offset_output + i] = input[offset_input + i];
        }
    }-*/;
}
