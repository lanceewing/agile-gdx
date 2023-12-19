package com.agifans.agile;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the 16 colours that make up the EGA palette.
 * 
 * @author Lance Ewing
 */
public class EgaPalette {

    // The Color constants for the 16 EGA colours (and also the transparent colour we use).
    public final static Color BLACK = new Color(0x000000);
    public final static Color BLUE = new Color(0x0000AA);
    public final static Color GREEN = new Color(0x00AA00);
    public final static Color CYAN = new Color(0x00AAAA);
    public final static Color RED = new Color(0xAA0000);
    public final static Color MAGENTA = new Color(0xAA00AA);
    public final static Color BROWN = new Color(0xAA5500);
    public final static Color GREY = new Color(0xAAAAAA);
    public final static Color DARKGREY = new Color(0x555555);
    public final static Color LIGHTBLUE = new Color(0x5555FF);
    public final static Color LIGHTGREEN = new Color(0x55FF55);
    public final static Color LIGHTCYAN = new Color(0x55FFFF);
    public final static Color PINK = new Color(0xFF5555);
    public final static Color LIGHTMAGENTA = new Color(0xFF55FF);
    public final static Color YELLOW = new Color(0xFFFF55);
    public final static Color WHITE = new Color(0xFFFFFF);

    // JAGI RGB values
    // 0x005454FC
    
    // RGB values for use in colors array.
    public final static int black = BLACK.getRGB();
    public final static int blue = BLUE.getRGB();
    public final static int green = GREEN.getRGB();
    public final static int cyan = CYAN.getRGB();
    public final static int red = RED.getRGB();
    public final static int magenta = MAGENTA.getRGB();
    public final static int brown = BROWN.getRGB();
    public final static int grey = GREY.getRGB();
    public final static int darkgrey = DARKGREY.getRGB();
    public final static int lightblue = LIGHTBLUE.getRGB();
    public final static int lightgreen = LIGHTGREEN.getRGB();
    public final static int lightcyan = LIGHTCYAN.getRGB();
    public final static int pink = PINK.getRGB();
    public final static int lightmagenta = LIGHTMAGENTA.getRGB();
    public final static int yellow = YELLOW.getRGB();
    public final static int white = WHITE.getRGB();
    
    private static short toRGB565(int argb8888) {
        com.badlogic.gdx.graphics.Color color = new com.badlogic.gdx.graphics.Color();
        com.badlogic.gdx.graphics.Color.argb8888ToColor(color, argb8888);
        return (short)com.badlogic.gdx.graphics.Color.rgb565(color);
    }
    
    /**
     * Holds a mapping from RGB8888 value to libgdx RGB565 value.
     */
    public final static Map<Integer, Short> RGB888_TO_RGB565_MAP = new HashMap<>();
    static {
        RGB888_TO_RGB565_MAP.put(black & 0xFFFFFF, toRGB565(black));
        RGB888_TO_RGB565_MAP.put(blue & 0xFFFFFF, toRGB565(blue));
        RGB888_TO_RGB565_MAP.put(green & 0xFFFFFF, toRGB565(green));
        RGB888_TO_RGB565_MAP.put(cyan & 0xFFFFFF, toRGB565(cyan));
        RGB888_TO_RGB565_MAP.put(red & 0xFFFFFF, toRGB565(red));
        RGB888_TO_RGB565_MAP.put(magenta & 0xFFFFFF, toRGB565(magenta));
        RGB888_TO_RGB565_MAP.put(brown & 0xFFFFFF, toRGB565(brown));
        RGB888_TO_RGB565_MAP.put(grey & 0xFFFFFF, toRGB565(grey));
        RGB888_TO_RGB565_MAP.put(darkgrey & 0xFFFFFF, toRGB565(darkgrey));
        RGB888_TO_RGB565_MAP.put(lightblue & 0xFFFFFF, toRGB565(lightblue));
        RGB888_TO_RGB565_MAP.put(lightgreen & 0xFFFFFF, toRGB565(lightgreen));
        RGB888_TO_RGB565_MAP.put(lightcyan & 0xFFFFFF, toRGB565(lightcyan));
        RGB888_TO_RGB565_MAP.put(pink & 0xFFFFFF, toRGB565(pink));
        RGB888_TO_RGB565_MAP.put(lightmagenta & 0xFFFFFF, toRGB565(lightmagenta));
        RGB888_TO_RGB565_MAP.put(yellow & 0xFFFFFF, toRGB565(yellow));
        RGB888_TO_RGB565_MAP.put(white & 0xFFFFFF, toRGB565(white));
    }
    
    /**
     * Holds the RGB values for the 16 EGA colours.
     */
    // TODO: Remove when satisfied that RGB565 is working.
    //public final static int[] colours = { black, blue, green, cyan, red, magenta, brown, grey, darkgrey, lightblue, lightgreen, lightcyan, pink, lightmagenta, yellow, white };

    public final static short[] colours = { 
        RGB888_TO_RGB565_MAP.get(black & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(blue & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(green & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(cyan & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(red & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(magenta & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(brown & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(grey & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(darkgrey & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(lightblue & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(lightgreen & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(lightcyan & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(pink & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(lightmagenta & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(yellow & 0xFFFFFF), 
        RGB888_TO_RGB565_MAP.get(white & 0xFFFFFF)
    };
}
