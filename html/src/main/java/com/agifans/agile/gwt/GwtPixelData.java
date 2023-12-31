package com.agifans.agile.gwt;

import com.agifans.agile.PixelData;
import com.badlogic.gdx.graphics.Pixmap;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * GWT implementation of the PixelData interface.
 * 
 * Some important notes about the GWT implementation of Pixmap:
 * 
 * - It uses a canvas element, not a ByteBuffer, for the pixels.
 * - When the Pixmap is drawn to the Texture, it is directly from the canvas.
 * - Therefore, as long as the canvas is up to date, it will render to the Texture.
 */
public class GwtPixelData implements PixelData {

    /**
     * OffscreenCanvas is very new (only 9 months at the time of writing this), which
     * means that GWT doesn't yet have support for it. We therefore store it as a
     * generic JavaScriptObject instance.
     */
    private JavaScriptObject offscreenCanvas;

    /**
     * The 2d Context for the OffscreenCanvas.
     */
    private Context2d context;

    private ImageData imageData;
    
    private int[] backupPixelArray;
    
    @Override
    public void init(Pixmap pixmap) {
        // As its very new, we need to use a native method to create the OffscreenCanvas.
        createOffscreenCanvasAndContext(pixmap.getWidth(), pixmap.getHeight());
        
        // Create an empty ImageData for use with AGILE.
        this.imageData = this.context.createImageData(pixmap.getWidth(), pixmap.getHeight());
        
        // Create a backup array for when we need to restore an earlier state.
        this.backupPixelArray = new int[this.imageData.getData().getLength()];
    }

    private native void createOffscreenCanvasAndContext(int width, int height)/*-{
        // We can't use transferControlToOffscreen method, since libgdx has already
        // called getContext on the Pixmap canvas. So we instead create our own
        // OffscreenCanvas of the same size and will then use ImageBitmap to transfer
        // it back to the UI thread and main canvas.
        
        var offscreenCanvas = new OffscreenCanvas(width, height);
        this.@com.agifans.agile.gwt.GwtPixelData::offscreenCanvas = offscreenCanvas;
        this.@com.agifans.agile.gwt.GwtPixelData::context = offscreenCanvas.getContext('2d');
    }-*/;
    
    @Override
    public void putPixel(int agiScreenIndex, int rgba8888Colour) {
        int index = agiScreenIndex * 4;
        CanvasPixelArray pixelArray = imageData.getData();
        pixelArray.set(index, (rgba8888Colour >> 24) & 0xFF);
        pixelArray.set(index + 1, (rgba8888Colour >> 16) & 0xFF);
        pixelArray.set(index + 2, (rgba8888Colour >> 8) & 0xFF);
        pixelArray.set(index + 3, rgba8888Colour & 0xFF);
    }

    @Override
    public void pixelCopy(int[] rgba888Src, int agiScreenIndex, int agiScreenLength) {
        int index = agiScreenIndex * 4;
        CanvasPixelArray pixelArray = imageData.getData();
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
        CanvasPixelArray pixelArray = imageData.getData();
        int length = pixelArray.getLength();
        for (int pos=0; pos<length; pos++) {
            backupPixelArray[pos] = pixelArray.get(pos);
        }
    }

    @Override
    public void restorePixels() {
        CanvasPixelArray pixelArray = imageData.getData();
        int length = pixelArray.getLength();
        for (int pos=0; pos<length; pos++) {
            pixelArray.set(pos, backupPixelArray[pos]);
        }
    }

    @Override
    public int getPixel(int agiScreenIndex) {
        int index = agiScreenIndex * 4;
        int rgba8888Colour = 0;
        CanvasPixelArray pixelArray = imageData.getData();
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
        rgba8888Colour |= ((backupPixelArray[index + 0] << 24) & 0xFF000000);
        rgba8888Colour |= ((backupPixelArray[index + 1] << 16) & 0x00FF0000);
        rgba8888Colour |= ((backupPixelArray[index + 2] <<  8) & 0x0000FF00);
        rgba8888Colour |= ((backupPixelArray[index + 3] <<  0) & 0x000000FF);
        return rgba8888Colour;
    }

    @Override
    public void updatePixmap(Pixmap pixmap) {
        // Update the OffscreenCanvas with the latest changes.
        context.putImageData(imageData, 0, 0);
        
        copyImageBitmapToRealCanvas(pixmap.getContext());
        
        // TODO: Get ImageBitmap and send message to UI thread. ImageBitmap is transferrable.
    }
    
    private native void copyImageBitmapToRealCanvas(Context2d realCanvasContext)/*-{
        var offscreenCanvas = this.@com.agifans.agile.gwt.GwtPixelData::offscreenCanvas;
        var imageBitmap = offscreenCanvas.transferToImageBitmap();
        realCanvasContext.drawImage(imageBitmap, 0, 0);
        imageBitmap.close();
    }-*/;
}
