package com.agifans.agile.lwjgl3;

import java.util.Arrays;

import com.agifans.agile.PixelData;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.BufferUtils;

/**
 * Desktop implementation of the PixelData interface.
 */
public class DesktopPixelData implements PixelData {

    // This byte array is in exactly the format that we can copy into the Pixmap's buffer.
    private byte[] imageData;
    
    private byte[] backupImageData;
    
    @Override
    public void init(int width, int height) {
        imageData = new byte[width * height * 4];
        backupImageData = new byte[width * height * 4];
    }

    @Override
    public void putPixel(int agiScreenIndex, int rgba8888Colour) {
        int index = agiScreenIndex * 4;
        
        try {
            // Adds RGBA8888 colour to byte array in expected R, G, B, A order.
            imageData[index + 0] = (byte)((rgba8888Colour >> 24) & 0xFF);
            imageData[index + 1] = (byte)((rgba8888Colour >> 16) & 0xFF);
            imageData[index + 2] = (byte)((rgba8888Colour >>  8) & 0xFF);
            imageData[index + 3] = (byte)((rgba8888Colour >>  0) & 0xFF);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Ignore. Some AGI fanmade games write things outside the screen, for
            // example, the "Sarien" demo. We ignore any such attempts.
        }
    }

    @Override
    public void pixelCopy(int[] src, int agiScreenIndex, int agiScreenLength) {
        int index = agiScreenIndex * 4;
        for (int srcPos = 0; srcPos < agiScreenLength; srcPos++) {
            int rgba8888Colour = src[srcPos];
            imageData[index++] = (byte)((rgba8888Colour >> 24) & 0xFF);
            imageData[index++] = (byte)((rgba8888Colour >> 16) & 0xFF);
            imageData[index++] = (byte)((rgba8888Colour >>  8) & 0xFF);
            imageData[index++] = (byte)((rgba8888Colour >>  0) & 0xFF);
        }
    }
    
    @Override
    public void savePixels() {
        System.arraycopy(imageData, 0, backupImageData, 0, imageData.length);
    }

    @Override
    public void restorePixels() {
        System.arraycopy(backupImageData, 0, imageData, 0, backupImageData.length);
    }

    @Override
    public void clearPixels() {
        Arrays.fill(imageData, (byte)0);
    }
    
    @Override
    public int getPixel(int agiScreenIndex) {
        int index = agiScreenIndex * 4;
        int rgba8888Colour = 0;
        rgba8888Colour |= ((imageData[index + 0] << 24) & 0xFF000000);
        rgba8888Colour |= ((imageData[index + 1] << 16) & 0x00FF0000);
        rgba8888Colour |= ((imageData[index + 2] <<  8) & 0x0000FF00);
        rgba8888Colour |= ((imageData[index + 3] <<  0) & 0x000000FF);
        return rgba8888Colour;
    }

    @Override
    public int getBackupPixel(int agiScreenIndex) {
        int index = agiScreenIndex * 4;
        int rgba8888Colour = 0;
        rgba8888Colour |= ((backupImageData[index + 0] << 24) & 0xFF000000);
        rgba8888Colour |= ((backupImageData[index + 1] << 16) & 0x00FF0000);
        rgba8888Colour |= ((backupImageData[index + 2] <<  8) & 0x0000FF00);
        rgba8888Colour |= ((backupImageData[index + 3] <<  0) & 0x000000FF);
        return rgba8888Colour;
    }

    @Override
    public void updatePixmap(Pixmap pixmap) {
        BufferUtils.copy(imageData, 0, pixmap.getPixels(), imageData.length);
    }
}
