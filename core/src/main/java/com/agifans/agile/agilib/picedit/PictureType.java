package com.agifans.agile.agilib.picedit;

/**
 * Enum representing the different sierra picture types supported.
 * 
 * @author Lance Ewing
 */
public enum PictureType {

    AGI(160, 168, 2), 
    SCI0(320, 190, 1);

    /**
     * The width of the picture in pixels.
     */
    private int width;

    /**
     * The height of the picture in pixels.
     */
    private int height;

    /**
     * The number of the game pixels in this picture type.
     */
    private int numberOfPixels;

    /**
     * The number of EGA pixels in this picture type.
     */
    private int numberOfEGAPixels;

    /**
     * The number of EGA pixels per AGI/SCI pixel.
     */
    private int egaPixelsPerGamePixel;

    /**
     * Constructor for PictureType.
     * 
     * @param width The width of the picture in pixels.
     * @param height The height of the picture in pixels.
     * @param egaPixelsPerGamePixel The number of EGA pixels per AGI/SCI pixel.
     */
    PictureType(int width, int height, int egaPixelsPerGamePixel) {
        this.width = width;
        this.height = height;
        this.egaPixelsPerGamePixel = egaPixelsPerGamePixel;
        this.numberOfPixels = (this.width * this.height);
        this.numberOfEGAPixels = (this.numberOfPixels * this.egaPixelsPerGamePixel);
    }

    /**
     * Gets the width of the picture in pixels.
     * 
     * @return the width of the picture in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the picture in pixels.
     * 
     * @return the height of the picture in pixels.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the number of pixels in this type of picture.
     * 
     * @return the number of pixels in this type of picture.
     */
    public int getNumberOfPixels() {
        return numberOfPixels;
    }

    /**
     * Gets the number of EGA pixels in this type of picture.
     * 
     * @return the number of EGA pixels in this type of picture.
     */
    public int getNumberOfEGAPixels() {
        return numberOfEGAPixels;
    }

    /**
     * Gets the number of EGA pixels per game pixel in this type of picture.
     * 
     * @return the number of EGA pixels per game pixel in this type of picture.
     */
    public int getEgaPixelsPerGamePixel() {
        return egaPixelsPerGamePixel;
    }
}
