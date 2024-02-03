package com.agifans.agile.lwjgl3;

import java.util.Arrays;

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
    
    private int[] egaPaletteImageData;
    
    private int[] backupEgaPaletteImageData;
    
    @Override
    public void init(int width, int height) {
        imageData = new byte[width * height * 4];
        backupImageData = new byte[width * height * 4];
        egaPaletteImageData = new int[width * height];
        backupEgaPaletteImageData = new int[width * height];
    }

    @Override
    public void putPixel(int agiScreenIndex, int rgba8888Colour) {
        try {
            // Store original colour in EGA palette version of screen.
            egaPaletteImageData[agiScreenIndex] = rgba8888Colour;
            
            // All incoming RGBA8888 colours are from EGA palette, so convert to custom palette.
            int paletteColour = egaToPaletteMap.getOrDefault(rgba8888Colour, rgba8888Colour);
            
            int index = agiScreenIndex * 4;
            
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
            
            // Store original colour in EGA palette version of screen.
            egaPaletteImageData[agiScreenIndex++] = rgba8888Colour;
            
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
        System.arraycopy(egaPaletteImageData, 0, backupEgaPaletteImageData, 0, egaPaletteImageData.length);
    }

    @Override
    public void restorePixels() {
        System.arraycopy(backupImageData, 0, imageData, 0, backupImageData.length);
        System.arraycopy(backupEgaPaletteImageData, 0, egaPaletteImageData, 0, backupEgaPaletteImageData.length);
    }

    @Override
    public void clearPixels() {
        Arrays.fill(imageData, (byte)0);
        Arrays.fill(backupImageData, (byte)0);
        Arrays.fill(egaPaletteImageData, (byte)0);
        Arrays.fill(backupEgaPaletteImageData, (byte)0);
    }
    
    @Override
    public int getPixel(int agiScreenIndex) {
        return egaPaletteImageData[agiScreenIndex];
    }

    @Override
    public int getBackupPixel(int agiScreenIndex) {
        return backupEgaPaletteImageData[agiScreenIndex];
    }

    @Override
    public void updatePixmap(Pixmap pixmap) {
        BufferUtils.copy(imageData, 0, pixmap.getPixels(), imageData.length);
    }

    @Override
    protected void updatePixelsForNewPalette() {
        for (byte[] imageData : new byte[][] { this.imageData, this.backupImageData }) {
            for (int index=0, agiScreenIndex=0; index < imageData.length; index += 4) {
                int egaRGBA8888Colour = egaPaletteImageData[agiScreenIndex++];
                
                // Look up the new palette equivalent.
                int newPaletteRGBA8888Colour = egaToPaletteMap.getOrDefault(egaRGBA8888Colour, egaRGBA8888Colour);
            
                // Update the pixel to be the equivalent colour from the new palette.
                imageData[index + 0] = (byte)((newPaletteRGBA8888Colour >> 24) & 0xFF);
                imageData[index + 1] = (byte)((newPaletteRGBA8888Colour >> 16) & 0xFF);
                imageData[index + 2] = (byte)((newPaletteRGBA8888Colour >>  8) & 0xFF);
                imageData[index + 3] = (byte)((newPaletteRGBA8888Colour >>  0) & 0xFF);
            }
        }
    }
}
