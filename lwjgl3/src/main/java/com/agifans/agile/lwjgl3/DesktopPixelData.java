package com.agifans.agile.lwjgl3;

import java.util.Arrays;
import java.util.Map;

import com.agifans.agile.PixelData;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.BufferUtils;

/**
 * Desktop implementation of the PixelData interface.
 */
public class DesktopPixelData extends PixelData {

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
        
        // All incoming RGBA8888 colours are from EGA palette, so convert to custom.
        int paletteColour = egaToPaletteMap.getOrDefault(rgba8888Colour, rgba8888Colour);
        
        try {
            // Adds RGBA8888 colour to byte array in expected R, G, B, A order.
            imageData[index + 0] = (byte)((paletteColour >> 24) & 0xFF);
            imageData[index + 1] = (byte)((paletteColour >> 16) & 0xFF);
            imageData[index + 2] = (byte)((paletteColour >>  8) & 0xFF);
            imageData[index + 3] = (byte)((paletteColour >>  0) & 0xFF);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Ignore. Some AGI fanmade games write things outside the screen, for
            // example, the "Sarien" demo. We ignore any such attempts.
        }
    }

    @Override
    public void pixelCopy(int[] src, int agiScreenIndex, int agiScreenLength) {
        int index = agiScreenIndex * 4;
        for (int srcPos = 0; srcPos < agiScreenLength; srcPos++) {
            // The src with be in the EGA palette. All incoming external colours are.
            int rgba8888Colour = src[srcPos];
            
            // Convert to palette colour.
            int paletteColour = egaToPaletteMap.getOrDefault(rgba8888Colour, rgba8888Colour);
            
            imageData[index++] = (byte)((paletteColour >> 24) & 0xFF);
            imageData[index++] = (byte)((paletteColour >> 16) & 0xFF);
            imageData[index++] = (byte)((paletteColour >>  8) & 0xFF);
            imageData[index++] = (byte)((paletteColour >>  0) & 0xFF);
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
        Arrays.fill(backupImageData, (byte)0);
    }
    
    @Override
    public int getPixel(int agiScreenIndex) {
        int index = agiScreenIndex * 4;
        int rgba8888Colour = 0;
        rgba8888Colour |= ((imageData[index + 0] << 24) & 0xFF000000);
        rgba8888Colour |= ((imageData[index + 1] << 16) & 0x00FF0000);
        rgba8888Colour |= ((imageData[index + 2] <<  8) & 0x0000FF00);
        rgba8888Colour |= ((imageData[index + 3] <<  0) & 0x000000FF);
        // We always return EGA palette to external callers.
        return paletteToEgaMap.getOrDefault(rgba8888Colour, rgba8888Colour);
    }

    @Override
    public int getBackupPixel(int agiScreenIndex) {
        int index = agiScreenIndex * 4;
        int rgba8888Colour = 0;
        rgba8888Colour |= ((backupImageData[index + 0] << 24) & 0xFF000000);
        rgba8888Colour |= ((backupImageData[index + 1] << 16) & 0x00FF0000);
        rgba8888Colour |= ((backupImageData[index + 2] <<  8) & 0x0000FF00);
        rgba8888Colour |= ((backupImageData[index + 3] <<  0) & 0x000000FF);
        // We always return EGA palette to external callers.
        return paletteToEgaMap.getOrDefault(rgba8888Colour, rgba8888Colour);
    }

    @Override
    public void updatePixmap(Pixmap pixmap) {
        BufferUtils.copy(imageData, 0, pixmap.getPixels(), imageData.length);
    }

    @Override
    protected void updatePixelsForNewPalette(Map<Integer, Integer> paletteConversionMap) {
        for (byte[] imageData : new byte[][] { this.imageData, this.backupImageData }) {
            for (int index=0; index < imageData.length; index += 4) {
                // Get the colour from the pixels array (which will be in the old palette)
                int oldPaletteRGBA8888Colour = 0;
                oldPaletteRGBA8888Colour |= ((imageData[index + 0] << 24) & 0xFF000000);
                oldPaletteRGBA8888Colour |= ((imageData[index + 1] << 16) & 0x00FF0000);
                oldPaletteRGBA8888Colour |= ((imageData[index + 2] <<  8) & 0x0000FF00);
                oldPaletteRGBA8888Colour |= ((imageData[index + 3] <<  0) & 0x000000FF);
                
                // Look up the new palette equivalent.
                int newPaletteRGBA8888Colour = paletteConversionMap.getOrDefault(oldPaletteRGBA8888Colour, oldPaletteRGBA8888Colour);
            
                // Update the pixel to be the equivalent colour from the new palette.
                if (newPaletteRGBA8888Colour != oldPaletteRGBA8888Colour) {
                    imageData[index + 0] = (byte)((newPaletteRGBA8888Colour >> 24) & 0xFF);
                    imageData[index + 1] = (byte)((newPaletteRGBA8888Colour >> 16) & 0xFF);
                    imageData[index + 2] = (byte)((newPaletteRGBA8888Colour >>  8) & 0xFF);
                    imageData[index + 3] = (byte)((newPaletteRGBA8888Colour >>  0) & 0xFF);
                }
            }
        }
    }
}
