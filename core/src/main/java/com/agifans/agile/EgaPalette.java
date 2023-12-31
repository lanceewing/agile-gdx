package com.agifans.agile;

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the 16 colours that make up the EGA palette.
 * 
 * @author Lance Ewing
 */
public class EgaPalette {

    // RGB values for use in colors array.
    //    public final static int black = 0x00000000;
    //    public final static int blue = 0x000000AA;
    //    public final static int green = 0x0000AA00;
    //    public final static int cyan = 0x0000AAAA;
    //    public final static int red = 0x00AA0000;
    //    public final static int magenta = 0x00AA00AA;
    //    public final static int brown = 0x00AA5500;
    //    public final static int grey = 0x00AAAAAA;
    //    public final static int darkgrey = 0x00555555;
    //    public final static int lightblue = 0x005555FF;
    //    public final static int lightgreen = 0x0055FF55;
    //    public final static int lightcyan = 0x0055FFFF;
    //    public final static int pink = 0x00FF5555;
    //    public final static int lightmagenta = 0x00FF55FF;
    //    public final static int yellow = 0x00FFFF55;
    //    public final static int white = 0x00FFFFFF;
    
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
    
    //    // RGBA8888 format, litte-endian, since BufferUtils seems to require it like that.
    //    public final static int black = 0xFF000000;
    //    public final static int blue = 0xFFAA0000;
    //    public final static int green = 0xFF00AA00;
    //    public final static int cyan = 0xFFAAAA00;
    //    public final static int red = 0xFF0000AA;
    //    public final static int magenta = 0xFFAA00AA;
    //    public final static int brown = 0xFF0055AA;
    //    public final static int grey = 0xFFAAAAAA;
    //    public final static int darkgrey = 0xFF555555;
    //    public final static int lightblue = 0xFFFF5555;
    //    public final static int lightgreen = 0xFF55FF55;
    //    public final static int lightcyan = 0xFFFFFF55;
    //    public final static int pink = 0xFF5555FF;
    //    public final static int lightmagenta = 0xFFFF55FF;
    //    public final static int yellow = 0xFF55FFFF;
    //    public final static int white = 0xFFFFFFFF;
    
    private static short toRGB565(int argb8888) {
        com.badlogic.gdx.graphics.Color color = new com.badlogic.gdx.graphics.Color();
        com.badlogic.gdx.graphics.Color.argb8888ToColor(color, argb8888);
        return (short)com.badlogic.gdx.graphics.Color.rgb565(color);
    }
    
    /**
     * Holds a mapping from RGB8888 value to libgdx RGB565 value.
     */
    // TODO: Remove conversion map and change colours array to use RGR565 directly.
    //    public final static Map<Integer, Short> RGB888_TO_RGB565_MAP = new HashMap<>();
    //    static {
    //        RGB888_TO_RGB565_MAP.put(black & 0xFFFFFF, toRGB565(black));
    //        RGB888_TO_RGB565_MAP.put(blue & 0xFFFFFF, toRGB565(blue));
    //        RGB888_TO_RGB565_MAP.put(green & 0xFFFFFF, toRGB565(green));
    //        RGB888_TO_RGB565_MAP.put(cyan & 0xFFFFFF, toRGB565(cyan));
    //        RGB888_TO_RGB565_MAP.put(red & 0xFFFFFF, toRGB565(red));
    //        RGB888_TO_RGB565_MAP.put(magenta & 0xFFFFFF, toRGB565(magenta));
    //        RGB888_TO_RGB565_MAP.put(brown & 0xFFFFFF, toRGB565(brown));
    //        RGB888_TO_RGB565_MAP.put(grey & 0xFFFFFF, toRGB565(grey));
    //        RGB888_TO_RGB565_MAP.put(darkgrey & 0xFFFFFF, toRGB565(darkgrey));
    //        RGB888_TO_RGB565_MAP.put(lightblue & 0xFFFFFF, toRGB565(lightblue));
    //        RGB888_TO_RGB565_MAP.put(lightgreen & 0xFFFFFF, toRGB565(lightgreen));
    //        RGB888_TO_RGB565_MAP.put(lightcyan & 0xFFFFFF, toRGB565(lightcyan));
    //        RGB888_TO_RGB565_MAP.put(pink & 0xFFFFFF, toRGB565(pink));
    //        RGB888_TO_RGB565_MAP.put(lightmagenta & 0xFFFFFF, toRGB565(lightmagenta));
    //        RGB888_TO_RGB565_MAP.put(yellow & 0xFFFFFF, toRGB565(yellow));
    //        RGB888_TO_RGB565_MAP.put(white & 0xFFFFFF, toRGB565(white));
    //    }
    
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
    
    //    public final static short[] colours = { 
    //        RGB888_TO_RGB565_MAP.get(black & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(blue & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(green & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(cyan & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(red & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(magenta & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(brown & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(grey & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(darkgrey & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(lightblue & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(lightgreen & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(lightcyan & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(pink & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(lightmagenta & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(yellow & 0xFFFFFF), 
    //        RGB888_TO_RGB565_MAP.get(white & 0xFFFFFF)
    //    };
}
