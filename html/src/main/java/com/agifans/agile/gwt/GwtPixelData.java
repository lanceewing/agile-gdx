package com.agifans.agile.gwt;

import com.agifans.agile.PixelData;
import com.badlogic.gdx.graphics.Pixmap;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8ClampedArray;

/**
 * GWT implementation of the PixelData interface.
 * 
 * Some important notes about the GWT implementation of Pixmap:
 * 
 * - It uses a canvas element, not a ByteBuffer, for the pixels.
 * - When the Pixmap is drawn to the Texture, it is directly from the canvas.
 * - Therefore, as long as the canvas is up to date, it will render to the Texture.
 * - And so the updatePixel method simply copies the pixelArray to the canvas image data.
 * 
 * Since the GWT version of AGILE is using a web worker for the background thread,
 * the only way to truly match what the original AGI interpreter does with regards
 * to the pixels is to use a SharedArrayBuffer, so that the UI thread always has 
 * access to the latest pixel data regardless of whether the web worker is busy 
 * waiting for the user to do something, such as press a key. An earlier version of
 * the class used an ImageBitmap instead, and since this is a Transferable object,
 * it was sending this back to the UI thread at the end of processing in "Tick"
 * message. Unfortunately this did not work in isolation and would have required
 * other "mid-Tick" messages to be sent back to the UI thread, which would have
 * been quite complicated to implement in a way that would have been truly the same
 * as the original AGI interpreter. It would also have been a lot more overhead.
 * Luckily the SharedArrayBuffer works well as an alternative and it can exactly
 * match what the Desktop platform is doing, which in turn matches AGI. We don't really
 * have to worry about synchronising the SharedArrayBuffer access with Atomics either,
 * as one side is always ready and will not modify.
 */
public class GwtPixelData implements PixelData {

    private Uint8ClampedArray pixelArray;
    
    private Uint8ClampedArray backupPixelArray;

    /**
     * Constructor for GwtPixelData (used by UI thread)
     */
    public GwtPixelData() {
    }
    
    /**
     * Constructor for GwtPixelData (used by web worker).
     * 
     * @param sharedArrayBuffer The same SharedArrayBuffer used by the UI thread.
     */
    public GwtPixelData(JavaScriptObject sharedArrayBuffer) {
        pixelArray = createPixelArray(sharedArrayBuffer);
    }
    
    private native Uint8ClampedArray createPixelArray(JavaScriptObject sharedArrayBuffer)/*-{
        return new Uint8ClampedArray(sharedArrayBuffer);
    }-*/;

    private native Uint8ClampedArray createPixelArray(int width, int height)/*-{
        var sharedArrayBuffer = new SharedArrayBuffer(width * height * 4);
        return new Uint8ClampedArray(sharedArrayBuffer);
    }-*/;
    
    public native JavaScriptObject getSharedArrayBuffer()/*-{
        var pixelArray = this.@com.agifans.agile.gwt.GwtPixelData::pixelArray;
        return pixelArray.buffer;
    }-*/;
    
    @Override
    public void init(int width, int height) {
        // The actual pixel array is created using a SharedArrayBuffer, so we need
        // to use a native method to do this.
        pixelArray = createPixelArray(width, height);
        backupPixelArray = TypedArrays.createUint8ClampedArray(width * height * 4);
    }

    @Override
    public void putPixel(int agiScreenIndex, int rgba8888Colour) {
        int index = agiScreenIndex * 4;
        
        // Adds RGBA8888 colour to byte array in expected R, G, B, A order.
        pixelArray.set(index, (rgba8888Colour >> 24) & 0xFF);
        pixelArray.set(index + 1, (rgba8888Colour >> 16) & 0xFF);
        pixelArray.set(index + 2, (rgba8888Colour >> 8) & 0xFF);
        pixelArray.set(index + 3, rgba8888Colour & 0xFF);
    }

    @Override
    public void pixelCopy(int[] rgba888Src, int agiScreenIndex, int agiScreenLength) {
        int index = agiScreenIndex * 4;
        for (int srcPos = 0; srcPos < agiScreenLength; srcPos++) {
            int rgba8888Colour = rgba888Src[srcPos];
            pixelArray.set(index++, (rgba8888Colour >> 24) & 0xFF);
            pixelArray.set(index++, (rgba8888Colour >> 16) & 0xFF);
            pixelArray.set(index++, (rgba8888Colour >>  8) & 0xFF);
            pixelArray.set(index++, (rgba8888Colour >>  0) & 0xFF);
        }
    }

    @Override
    public void savePixels() {
        backupPixelArray.set(pixelArray);
    }

    @Override
    public void restorePixels() {
        pixelArray.set(backupPixelArray);
    }

    @Override
    public int getPixel(int agiScreenIndex) {
        int index = agiScreenIndex * 4;
        int rgba8888Colour = 0;
        rgba8888Colour |= ((pixelArray.get(index + 0) << 24) & 0xFF000000);
        rgba8888Colour |= ((pixelArray.get(index + 1) << 16) & 0x00FF0000);
        rgba8888Colour |= ((pixelArray.get(index + 2) <<  8) & 0x0000FF00);
        rgba8888Colour |= ((pixelArray.get(index + 3) <<  0) & 0x000000FF);
        return rgba8888Colour;
    }

    @Override
    public int getBackupPixel(int agiScreenIndex) {
        int index = agiScreenIndex * 4;
        int rgba8888Colour = 0;
        rgba8888Colour |= ((backupPixelArray.get(index + 0) << 24) & 0xFF000000);
        rgba8888Colour |= ((backupPixelArray.get(index + 1) << 16) & 0x00FF0000);
        rgba8888Colour |= ((backupPixelArray.get(index + 2) <<  8) & 0x0000FF00);
        rgba8888Colour |= ((backupPixelArray.get(index + 3) <<  0) & 0x000000FF);
        return rgba8888Colour;
    }

    @Override
    public void updatePixmap(Pixmap pixmap) {
        setImageData(pixelArray, pixmap.getWidth(), pixmap.getHeight(), pixmap.getContext());
    }
    
    private native static void setImageData (ArrayBufferView pixels, int width, int height, Context2d ctx)/*-{
        var imgData = ctx.createImageData(width, height);
        var data = imgData.data;
    
        for (var i = 0, len = width * height * 4; i < len; i++) {
            data[i] = pixels[i] & 0xff;
        }
        ctx.putImageData(imgData, 0, 0);
    }-*/;
}