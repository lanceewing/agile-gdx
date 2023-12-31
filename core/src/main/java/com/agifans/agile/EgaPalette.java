package com.agifans.agile;

/**
 * This class holds the 16 colours that make up the EGA palette.
 * 
 * @author Lance Ewing
 */
public class EgaPalette {

    // RGBA8888 format, so that we can more easily support HTML5.
    public final static int black = 0x000000FF;
    public final static int blue = 0x0000AAFF;
    public final static int green = 0x00AA00FF;
    public final static int cyan = 0x00AAAAFF;
    public final static int red = 0xAA0000FF;
    public final static int magenta = 0xAA00AAFF;
    public final static int brown = 0xAA5500FF;
    public final static int grey = 0xAAAAAAFF;
    public final static int darkgrey = 0x555555FF;
    public final static int lightblue = 0x5555FFFF;
    public final static int lightgreen = 0x55FF55FF;
    public final static int lightcyan = 0x55FFFFFF;
    public final static int pink = 0xFF5555FF;
    public final static int lightmagenta = 0xFF55FFFF;
    public final static int yellow = 0xFFFF55FF;
    public final static int white = 0xFFFFFFFF;

    /**
     * Holds the RGB565 values for the 16 EGA colours.
     */
    public final static int[] colours = { 
        black, 
        blue, 
        green, 
        cyan, 
        red, 
        magenta, 
        brown, 
        grey, 
        darkgrey, 
        lightblue, 
        lightgreen, 
        lightcyan, 
        pink, 
        lightmagenta, 
        yellow, 
        white
    };
}
