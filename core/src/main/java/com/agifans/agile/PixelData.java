package com.agifans.agile;

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
public interface PixelData {

    /**
     * Initialises the PixelData implementation with the given width and height.
     * 
     * @param width The width of the pixel data.
     * @param height The height of the pixel data.
     */
    void init(int width, int height);
    
    /**
     * Puts a single pixel into the pixel data using an AGI screen position, i.e.
     * where agiScreenIndex = (y * 320) + x.
     * 
     * @param agiScreenIndex AGI screen position (i.e. (y * 320) + x
     * @param rgba8888Colour
     */
    void putPixel(int agiScreenIndex, int rgba8888Colour);
    
    /**
     * Copies AGI screen pixels into the pixel data, using an AGI starting screen
     * index (i.e. (y * 320) + x) and AGI screen length (i.e. in pixels, not RGBA
     * components, i.e. not times 4).
     * 
     * @param rgba888Src
     * @param agiScreenIndex
     * @param agiScreenLength
     */
    void pixelCopy(int[] rgba888Src, int agiScreenIndex, int agiScreenLength);
    
    /**
     * Saves all pixels to a backup copy of the pixel data. 
     */
    void savePixels();
    
    /**
     * Restores all pixels from the backup copy of the pixel data.
     */
    void restorePixels();
    
    /**
     * Gets a single pixel from the pixel data, using an AGI screen position, i.e.
     * where agiScreenIndex = (y * 320) + x
     * 
     * @param agiScreenIndex
     * 
     * @return The RGBA8888 pixel value.
     */
    int getPixel(int agiScreenIndex);
    
    /**
     * Gets a single pixel from the backup pixel data, using an AGI screen position,
     * i.e. where agiScreenIndex = (y * 320) + x
     * 
     * @param agiScreenIndex
     * 
     * @return The RGBA8888 pixel value from the backup pixel array.
     */
    int getBackupPixel(int agiScreenIndex);
    
    /**
     * Updates Pixmap with the latest local changes. 
     * 
     * @param pixmap 
     */
    void updatePixmap(Pixmap pixmap);
    
}
