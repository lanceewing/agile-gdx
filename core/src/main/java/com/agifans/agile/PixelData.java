package com.agifans.agile;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Pixmap;

/**
 * An Interface for plotting individual pixels. The desktop, mobile, and HTML 
 * platforms will implement this in their own way. The HTML platform in particular
 * is a bit different and needs to be handled in a platform specific way, which is 
 * the primary reason this interface exists. All platforms use the same colour
 * format though, i.e. RGBA8888, which makes things a little easier. For both
 * HTML5, where the Pixmap is a wrapper around an HTML5 canvas, and for Desktop
 * and Android, the colours are updated via a byte array where the RGBA components
 * are stored in the order R, G, B, A.
 */
public abstract class PixelData {
    
    /**
     * Map between the normal EGA palette and the currently set custom palette.
     */
    protected Map<Integer, Integer> egaToPaletteMap;
    
    /**
     * Map between the custom palette and the normal EGA palette.
     */
    protected Map<Integer, Integer> paletteToEgaMap;
    
    /**
     * Constructor for PixelData.
     */
    public PixelData() {
        egaToPaletteMap = new HashMap<>();
        paletteToEgaMap = new HashMap<>();
        
        // By default, the palette maps are simply between the standard EGA palette and itself.
        for (int colourNum=0; colourNum < 16; colourNum++) {
            egaToPaletteMap.put(EgaPalette.colours[colourNum], EgaPalette.colours[colourNum]);
            paletteToEgaMap.put(EgaPalette.colours[colourNum], EgaPalette.colours[colourNum]);
        }
    }
    
    /**
     * Clears the state of the PixelData back to its initial state.
     */
    public void clearState() {
        // By default, the palette maps are simply between the standard EGA palette and itself.
        for (int colourNum=0; colourNum < 16; colourNum++) {
            egaToPaletteMap.put(EgaPalette.colours[colourNum], EgaPalette.colours[colourNum]);
            paletteToEgaMap.put(EgaPalette.colours[colourNum], EgaPalette.colours[colourNum]);
        }
        
        clearPixels();
    }
    
    /**
     * Sets a new palette to use when plotting pixels. Intended for use with
     * the AGIPAL interpreter hack.
     * 
     * @param newPalette
     */
    public void setPalette(int[] newPalette) {
        // Whenever the palette changes, we need to convert the existing pixel array
        // from the previous palette to the new one.
        Map<Integer, Integer> paletteConversionMap = new HashMap<>();
        
        for (int colourNum=0; colourNum < 16; colourNum++) {
            // Update the old to new palette conversion Map first
            int oldPaletteColour = egaToPaletteMap.get(EgaPalette.colours[colourNum]);
            paletteConversionMap.put(oldPaletteColour, newPalette[colourNum]);
            
            // And then update the EGA to palette and palette to EGA Maps for new palette.
            egaToPaletteMap.put(EgaPalette.colours[colourNum], newPalette[colourNum]);
            paletteToEgaMap.put(newPalette[colourNum], EgaPalette.colours[colourNum]);
        }
        
        // Use the populated paletteConversionMap to update the pixels array to new palette.
        updatePixelsForNewPalette(paletteConversionMap);
    }
    
    /**
     * Applies the newly set palette to the pixels array, by using the provided Map, which
     * contains mappings from the old palette colours to the new palette equivalent colours.
     * 
     * @param paletteConversionMap 
     */
    protected abstract void updatePixelsForNewPalette(Map<Integer, Integer> paletteConversionMap);
    
    /**
     * Initialises the PixelData implementation with the given width and height.
     * 
     * @param width The width of the pixel data.
     * @param height The height of the pixel data.
     */
    public abstract void init(int width, int height);
    
    /**
     * Puts a single pixel into the pixel data using an AGI screen position, i.e.
     * where agiScreenIndex = (y * 320) + x.
     * 
     * @param agiScreenIndex AGI screen position (i.e. (y * 320) + x
     * @param rgba8888Colour
     */
    public abstract void putPixel(int agiScreenIndex, int rgba8888Colour);
    
    /**
     * Copies AGI screen pixels into the pixel data, using an AGI starting screen
     * index (i.e. (y * 320) + x) and AGI screen length (i.e. in pixels, not RGBA
     * components, i.e. not times 4).
     * 
     * @param rgba888Src
     * @param agiScreenIndex
     * @param agiScreenLength
     */
    public abstract void pixelCopy(int[] rgba888Src, int agiScreenIndex, int agiScreenLength);
    
    /**
     * Saves all pixels to a backup copy of the pixel data. 
     */
    public abstract void savePixels();
    
    /**
     * Restores all pixels from the backup copy of the pixel data.
     */
    public abstract void restorePixels();
    
    /**
     * Clears all pixels, i.e. sets to black.
     */
    public abstract void clearPixels();
    
    /**
     * Gets a single pixel from the pixel data, using an AGI screen position, i.e.
     * where agiScreenIndex = (y * 320) + x
     * 
     * @param agiScreenIndex
     * 
     * @return The RGBA8888 pixel value.
     */
    public abstract int getPixel(int agiScreenIndex);
    
    /**
     * Gets a single pixel from the backup pixel data, using an AGI screen position,
     * i.e. where agiScreenIndex = (y * 320) + x
     * 
     * @param agiScreenIndex
     * 
     * @return The RGBA8888 pixel value from the backup pixel array.
     */
    public abstract int getBackupPixel(int agiScreenIndex);
    
    /**
     * Updates Pixmap with the latest local changes. 
     * 
     * @param pixmap 
     */
    public abstract void updatePixmap(Pixmap pixmap);
    
}
