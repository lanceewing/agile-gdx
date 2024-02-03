package com.agifans.agile.gwt;

import com.agifans.agile.PixelData;
import com.badlogic.gdx.graphics.Pixmap;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Int32Array;
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
public class GwtPixelData extends PixelData {

    private Uint8ClampedArray pixelArray;
    
    private Uint8ClampedArray backupPixelArray;
    
    private Int32Array egaPaletteImageData;
    
    private Int32Array backupEgaPaletteImageData;

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
        backupPixelArray = TypedArrays.createUint8ClampedArray(pixelArray.byteLength());
        egaPaletteImageData = TypedArrays.createInt32Array(pixelArray.byteLength() / 4);
        backupEgaPaletteImageData = TypedArrays.createInt32Array(egaPaletteImageData.length());
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
    }

    @Override
    public void putPixel(int agiScreenIndex, int rgba8888Colour) {
        // Store original colour in EGA palette version of screen.
        egaPaletteImageData.set(agiScreenIndex, rgba8888Colour);
        
        int index = agiScreenIndex * 4;
        
        // All incoming RGBA8888 colours are from EGA palette, so convert to custom palette.
        int paletteColour = egaToPaletteMap.getOrDefault(rgba8888Colour, rgba8888Colour);
        
        // Adds RGBA8888 colour to byte array in expected R, G, B, A order.
        pixelArray.set(index, (paletteColour >> 24) & 0xFF);
        pixelArray.set(index + 1, (paletteColour >> 16) & 0xFF);
        pixelArray.set(index + 2, (paletteColour >> 8) & 0xFF);
        pixelArray.set(index + 3, paletteColour & 0xFF);
    }

    @Override
    public void pixelCopy(int[] rgba888Src, int agiScreenIndex, int agiScreenLength) {
        int index = agiScreenIndex * 4;
        for (int srcPos = 0; srcPos < agiScreenLength; srcPos++) {
            // The src with be in the EGA palette. All incoming external colours are.
            int rgba8888Colour = rgba888Src[srcPos];
            
            // Store original colour in EGA palette version of screen.
            egaPaletteImageData.set(agiScreenIndex++, rgba8888Colour);
            
            // Convert to palette colour.
            int paletteColour = egaToPaletteMap.getOrDefault(rgba8888Colour, rgba8888Colour);
            
            pixelArray.set(index++, (paletteColour >> 24) & 0xFF);
            pixelArray.set(index++, (paletteColour >> 16) & 0xFF);
            pixelArray.set(index++, (paletteColour >>  8) & 0xFF);
            pixelArray.set(index++, (paletteColour >>  0) & 0xFF);
        }
    }

    @Override
    public void savePixels() {
        backupPixelArray.set(pixelArray);
        backupEgaPaletteImageData.set(egaPaletteImageData);
    }

    @Override
    public void restorePixels() {
        pixelArray.set(backupPixelArray);
        egaPaletteImageData.set(backupEgaPaletteImageData);
    }
    
    @Override
    public void clearPixels() {
        for (int index = 0; index < pixelArray.length(); index++) {
            pixelArray.set(index, 0);
        }
    }

    @Override
    public int getPixel(int agiScreenIndex) {
        return egaPaletteImageData.get(agiScreenIndex);
    }

    @Override
    public int getBackupPixel(int agiScreenIndex) {
        return backupEgaPaletteImageData.get(agiScreenIndex);
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

    @Override
    protected void updatePixelsForNewPalette() {
        for (Uint8ClampedArray pixelArray : new Uint8ClampedArray[] { this.pixelArray, this.backupPixelArray }) {
            for (int index=0, agiScreenIndex=0; index < pixelArray.byteLength(); index += 4) {
                int egaRGBA8888Colour = egaPaletteImageData.get(agiScreenIndex++);
                
                // Look up the new palette equivalent.
                int newPaletteRGBA8888Colour = egaToPaletteMap.getOrDefault(egaRGBA8888Colour, egaRGBA8888Colour);
            
                // Update the pixel to be the equivalent colour from the new palette.
                pixelArray.set(index, (newPaletteRGBA8888Colour >> 24) & 0xFF);
                pixelArray.set(index + 1, (newPaletteRGBA8888Colour >> 16) & 0xFF);
                pixelArray.set(index + 2, (newPaletteRGBA8888Colour >> 8) & 0xFF);
                pixelArray.set(index + 3, newPaletteRGBA8888Colour & 0xFF);
            }
        }
    }
}