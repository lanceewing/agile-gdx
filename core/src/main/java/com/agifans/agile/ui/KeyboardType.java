package com.agifans.agile.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;

/**
 * Enum representing the different types of keyboard available within AGILE.
 * 
 * @author Lance Ewing
 */
public enum KeyboardType {

    LANDSCAPE(
            new Integer[][][] {{
              { null, null, Keys.NUM_1, Keys.NUM_1, Keys.NUM_1, Keys.NUM_1, Keys.NUM_2, Keys.NUM_2, Keys.NUM_2, Keys.NUM_2, Keys.NUM_3, Keys.NUM_3, Keys.NUM_3, Keys.NUM_3, Keys.NUM_4, Keys.NUM_4, Keys.NUM_4, Keys.NUM_4, Keys.NUM_5, Keys.NUM_5, Keys.NUM_5, Keys.NUM_5, Keys.NUM_6, Keys.NUM_6, Keys.NUM_6, Keys.NUM_6, Keys.NUM_7, Keys.NUM_7, Keys.NUM_7, Keys.NUM_7, Keys.NUM_8, Keys.NUM_8, Keys.NUM_8, Keys.NUM_8, Keys.NUM_9, Keys.NUM_9, Keys.NUM_9, Keys.NUM_9, Keys.NUM_0, Keys.NUM_0, Keys.NUM_0, Keys.NUM_0, Keys.MINUS, Keys.MINUS, Keys.MINUS, Keys.MINUS, Keys.EQUALS, Keys.EQUALS, Keys.EQUALS, Keys.EQUALS, Keys.BACKSLASH, Keys.BACKSLASH, Keys.BACKSLASH, Keys.BACKSLASH, null, null },
              { Keys.ESCAPE, Keys.ESCAPE, Keys.ESCAPE, Keys.ESCAPE, Keys.Q, Keys.Q, Keys.Q, Keys.Q, Keys.W, Keys.W, Keys.W, Keys.W, Keys.E, Keys.E, Keys.E, Keys.E, Keys.R, Keys.R, Keys.R, Keys.R, Keys.T, Keys.T, Keys.T, Keys.T, Keys.Y, Keys.Y, Keys.Y, Keys.Y, Keys.U, Keys.U, Keys.U, Keys.U, Keys.I, Keys.I, Keys.I, Keys.I, Keys.O, Keys.O, Keys.O, Keys.O, Keys.P, Keys.P, Keys.P, Keys.P, Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, Keys.DEL, Keys.DEL, Keys.DEL, Keys.DEL },
              { null, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.A, Keys.A, Keys.A, Keys.A, Keys.S, Keys.S, Keys.S, Keys.S, Keys.D, Keys.D, Keys.D, Keys.D, Keys.F, Keys.F, Keys.F, Keys.F, Keys.G, Keys.G, Keys.G, Keys.G, Keys.H, Keys.H, Keys.H, Keys.H, Keys.J, Keys.J, Keys.J, Keys.J, Keys.K, Keys.K, Keys.K, Keys.K, Keys.L, Keys.L, Keys.L, Keys.L, Keys.SEMICOLON, Keys.SEMICOLON, Keys.SEMICOLON, Keys.SEMICOLON, Keys.APOSTROPHE, Keys.APOSTROPHE, Keys.APOSTROPHE, Keys.APOSTROPHE, Keys.ENTER, Keys.ENTER, Keys.ENTER, Keys.ENTER, Keys.ENTER, Keys.ENTER, Keys.ENTER, null },
              { null, Keys.SHIFT_LEFT, Keys.SHIFT_LEFT, Keys.SHIFT_LEFT, Keys.SHIFT_LEFT, Keys.SHIFT_LEFT, Keys.SHIFT_LEFT, Keys.Z, Keys.Z, Keys.Z, Keys.Z, Keys.X, Keys.X, Keys.X, Keys.X, Keys.C, Keys.C, Keys.C, Keys.C, Keys.V, Keys.V, Keys.V, Keys.V, Keys.B, Keys.B, Keys.B, Keys.B, Keys.N, Keys.N, Keys.N, Keys.N, Keys.M, Keys.M, Keys.M, Keys.M, Keys.COMMA, Keys.COMMA, Keys.COMMA, Keys.COMMA, Keys.PERIOD, Keys.PERIOD, Keys.PERIOD, Keys.PERIOD, Keys.SLASH, Keys.SLASH, Keys.SLASH, Keys.SLASH, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, null, null, null },
              { null, null, null, Keys.LEFT, Keys.LEFT, Keys.LEFT, Keys.LEFT, Keys.DOWN, Keys.DOWN, Keys.DOWN, Keys.DOWN, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.UP, Keys.UP, Keys.UP, Keys.UP, Keys.RIGHT, Keys.RIGHT, Keys.RIGHT, Keys.RIGHT, Keys.ALT_RIGHT, Keys.ALT_RIGHT, Keys.ALT_RIGHT, Keys.ALT_RIGHT, null }
            }},
            new String[] {"png/keyboard_landscape.png"},
            0.4f,
            0,
            0,
            15,
            1890,
            0
          ),
      PORTRAIT(
            new Integer[][][] {{
              { null, null, Keys.NUM_1, Keys.NUM_1, Keys.NUM_1, Keys.NUM_1, Keys.NUM_2, Keys.NUM_2, Keys.NUM_2, Keys.NUM_2, Keys.NUM_3, Keys.NUM_3, Keys.NUM_3, Keys.NUM_3, Keys.NUM_4, Keys.NUM_4, Keys.NUM_4, Keys.NUM_4, Keys.NUM_5, Keys.NUM_5, Keys.NUM_5, Keys.NUM_5, Keys.NUM_6, Keys.NUM_6, Keys.NUM_6, Keys.NUM_6, Keys.NUM_7, Keys.NUM_7, Keys.NUM_7, Keys.NUM_7, Keys.NUM_8, Keys.NUM_8, Keys.NUM_8, Keys.NUM_8, Keys.NUM_9, Keys.NUM_9, Keys.NUM_9, Keys.NUM_9, Keys.NUM_0, Keys.NUM_0, Keys.NUM_0, Keys.NUM_0, Keys.MINUS, Keys.MINUS, Keys.MINUS, Keys.MINUS, Keys.EQUALS, Keys.EQUALS, Keys.EQUALS, Keys.EQUALS, Keys.BACKSLASH, Keys.BACKSLASH, Keys.BACKSLASH, Keys.BACKSLASH, null, null },
              { Keys.ESCAPE, Keys.ESCAPE, Keys.ESCAPE, Keys.ESCAPE, Keys.Q, Keys.Q, Keys.Q, Keys.Q, Keys.W, Keys.W, Keys.W, Keys.W, Keys.E, Keys.E, Keys.E, Keys.E, Keys.R, Keys.R, Keys.R, Keys.R, Keys.T, Keys.T, Keys.T, Keys.T, Keys.Y, Keys.Y, Keys.Y, Keys.Y, Keys.U, Keys.U, Keys.U, Keys.U, Keys.I, Keys.I, Keys.I, Keys.I, Keys.O, Keys.O, Keys.O, Keys.O, Keys.P, Keys.P, Keys.P, Keys.P, Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.LEFT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, Keys.RIGHT_BRACKET, Keys.DEL, Keys.DEL, Keys.DEL, Keys.DEL },
              { null, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.CONTROL_LEFT, Keys.A, Keys.A, Keys.A, Keys.A, Keys.S, Keys.S, Keys.S, Keys.S, Keys.D, Keys.D, Keys.D, Keys.D, Keys.F, Keys.F, Keys.F, Keys.F, Keys.G, Keys.G, Keys.G, Keys.G, Keys.H, Keys.H, Keys.H, Keys.H, Keys.J, Keys.J, Keys.J, Keys.J, Keys.K, Keys.K, Keys.K, Keys.K, Keys.L, Keys.L, Keys.L, Keys.L, Keys.SEMICOLON, Keys.SEMICOLON, Keys.SEMICOLON, Keys.SEMICOLON, Keys.APOSTROPHE, Keys.APOSTROPHE, Keys.APOSTROPHE, Keys.APOSTROPHE, Keys.ENTER, Keys.ENTER, Keys.ENTER, Keys.ENTER, Keys.ENTER, Keys.ENTER, Keys.ENTER, null },
              { null, Keys.SHIFT_LEFT, Keys.SHIFT_LEFT, Keys.SHIFT_LEFT, Keys.SHIFT_LEFT, Keys.SHIFT_LEFT, Keys.SHIFT_LEFT, Keys.Z, Keys.Z, Keys.Z, Keys.Z, Keys.X, Keys.X, Keys.X, Keys.X, Keys.C, Keys.C, Keys.C, Keys.C, Keys.V, Keys.V, Keys.V, Keys.V, Keys.B, Keys.B, Keys.B, Keys.B, Keys.N, Keys.N, Keys.N, Keys.N, Keys.M, Keys.M, Keys.M, Keys.M, Keys.COMMA, Keys.COMMA, Keys.COMMA, Keys.COMMA, Keys.PERIOD, Keys.PERIOD, Keys.PERIOD, Keys.PERIOD, Keys.SLASH, Keys.SLASH, Keys.SLASH, Keys.SLASH, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, Keys.SHIFT_RIGHT, null, null, null },
              { null, null, null, Keys.LEFT, Keys.LEFT, Keys.LEFT, Keys.LEFT, Keys.DOWN, Keys.DOWN, Keys.DOWN, Keys.DOWN, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.SPACE, Keys.UP, Keys.UP, Keys.UP, Keys.UP, Keys.RIGHT, Keys.RIGHT, Keys.RIGHT, Keys.RIGHT, Keys.ALT_RIGHT, Keys.ALT_RIGHT, Keys.ALT_RIGHT, Keys.ALT_RIGHT, null }
            }},
            new String[] {"png/keyboard_portrait.png"},
            0.6f,
            0,
            0,
            2,
            -1,
            -2
          ),
      JOYSTICK(
            new Integer[][][] {{
              { Keys.NUMPAD_7, Keys.NUMPAD_7, Keys.NUMPAD_7, Keys.NUMPAD_8, Keys.NUMPAD_8, Keys.NUMPAD_8, Keys.NUMPAD_9, Keys.NUMPAD_9, Keys.NUMPAD_9 },
              { Keys.NUMPAD_7, Keys.NUMPAD_7, Keys.NUMPAD_7, Keys.NUMPAD_8, Keys.NUMPAD_8, Keys.NUMPAD_8, Keys.NUMPAD_9, Keys.NUMPAD_9, Keys.NUMPAD_9 },
              { Keys.NUMPAD_7, Keys.NUMPAD_7, Keys.NUMPAD_7, Keys.NUMPAD_8, Keys.NUMPAD_8, Keys.NUMPAD_8, Keys.NUMPAD_9, Keys.NUMPAD_9, Keys.NUMPAD_9 },
              { Keys.NUMPAD_4, Keys.NUMPAD_4, Keys.NUMPAD_4, Keys.NUMPAD_7, Keys.NUMPAD_8, Keys.NUMPAD_9, Keys.NUMPAD_6, Keys.NUMPAD_6, Keys.NUMPAD_6 },
              { Keys.NUMPAD_4, Keys.NUMPAD_4, Keys.NUMPAD_4, Keys.NUMPAD_4, null,          Keys.NUMPAD_6, Keys.NUMPAD_6, Keys.NUMPAD_6, Keys.NUMPAD_6 },
              { Keys.NUMPAD_4, Keys.NUMPAD_4, Keys.NUMPAD_4, Keys.NUMPAD_1, Keys.NUMPAD_2, Keys.NUMPAD_3, Keys.NUMPAD_6, Keys.NUMPAD_6, Keys.NUMPAD_6 },
              { Keys.NUMPAD_1, Keys.NUMPAD_1, Keys.NUMPAD_1, Keys.NUMPAD_2, Keys.NUMPAD_2, Keys.NUMPAD_2, Keys.NUMPAD_3, Keys.NUMPAD_3, Keys.NUMPAD_3 },
              { Keys.NUMPAD_1, Keys.NUMPAD_1, Keys.NUMPAD_1, Keys.NUMPAD_2, Keys.NUMPAD_2, Keys.NUMPAD_2, Keys.NUMPAD_3, Keys.NUMPAD_3, Keys.NUMPAD_3 },
              { Keys.NUMPAD_1, Keys.NUMPAD_1, Keys.NUMPAD_1, Keys.NUMPAD_2, Keys.NUMPAD_2, Keys.NUMPAD_2, Keys.NUMPAD_3, Keys.NUMPAD_3, Keys.NUMPAD_3 }
            },
            {
              { Keys.NUMPAD_0, Keys.NUMPAD_0, Keys.NUMPAD_0 },
              { Keys.NUMPAD_0, Keys.NUMPAD_0, Keys.NUMPAD_0 },
              { Keys.NUMPAD_0, Keys.NUMPAD_0, Keys.NUMPAD_0 },
              { Keys.NUMPAD_0, Keys.NUMPAD_0, Keys.NUMPAD_0 },
              { Keys.NUMPAD_0, Keys.NUMPAD_0, Keys.NUMPAD_0 },
              { Keys.NUMPAD_0, Keys.NUMPAD_0, Keys.NUMPAD_0 }
            }},
            new String[] {"png/joystick_arrows.png", "png/joystick_fire.png"},
            1.0f,
            0,
            100,
            0,
            -1,
            0
          ),
    MOBILE_ON_SCREEN, OFF;

    // Constants for the two sides of a keyboard..
    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    /**
     * The vertical size of the keys in this KeyboardType.
     */
    private float vertKeySize;

    /**
     * The horizontal size of the keys in this KeyboardType.
     */
    private float horizKeySize;

    /**
     * The position of each key within this KeyboardType.
     */
    private Integer[][][] keyMap;

    /**
     * The Texture(s) holding the keyboard image(s) for this KeyboardType.
     */
    private Texture[] textures;

    /**
     * The path to the keyboard image file(s).
     */
    private String[] keyboardImages;

    /**
     * The opacity of this KeyboardType.
     */
    private float opacity;

    /**
     * Offset from the bottom of the screen that the keyboard is rendered at.
     */
    private int renderOffset;

    /**
     * The number of sides that this keyboard has.
     */
    private int numOfSides;

    /**
     * The Y value above which the keyboard will be closed.
     */
    private int closeHeight;

    /**
     * The X value at which the keyboard starts in the keyboard image.
     */
    private int xStart;

    /**
     * The Y value at which the keyboard starts in the keyboard image.
     */
    private int yStart;

    /**
     * Constructor for KeyboardType.
     * 
     * @param keyMap         The position of each key within this KeyboardType.
     * @param keyboardImages The path to the keyboard image file(s).
     * @param opacity        The opacity of this KeyboardType.
     * @param renderOffset   Offset from the bottom of the screen that the keyboard
     *                       is rendered at.
     * @param closeBuffer    Buffer over the keyboard above which a tap or click
     *                       will close the keyboard.
     * @param xStart         The X value at which the keyboard starts in the
     *                       keyboard image.
     * @param activeWidth    The width of the active part of the keyboard image, or
     *                       -1 to deduce from texture width and xStart.
     * @param yStart         The Y value at which the keyboard starts in the
     *                       keyboard image.
     */
    KeyboardType(Integer[][][] keyMap, String[] keyboardImages, float opacity, int renderOffset, int closeBuffer,
            int xStart, int activeWidth, int yStart) {
        this.keyMap = keyMap;
        this.keyboardImages = keyboardImages;
        this.numOfSides = keyboardImages.length;

        int maxTextureHeight = 0;
        this.textures = new Texture[keyboardImages.length];
        for (int i = 0; i < numOfSides; i++) {
            this.textures[i] = new Texture(keyboardImages[i]);
            if (this.textures[i].getHeight() > maxTextureHeight) {
                maxTextureHeight = this.textures[i].getHeight();
            }
        }

        this.xStart = xStart;
        this.yStart = yStart;
        activeWidth = (activeWidth == -1 ? this.textures[0].getWidth() - this.xStart : activeWidth);
        this.vertKeySize = ((float) (((float) this.textures[0].getHeight()) - (float) this.yStart)
                / (float) this.keyMap[0].length);
        this.horizKeySize = ((float) activeWidth / (float) this.keyMap[0][0].length);

        this.opacity = opacity;
        this.renderOffset = renderOffset;
        this.closeHeight = maxTextureHeight + renderOffset + closeBuffer;
    }

    /**
     * Variant of the Constructor that doesn't support any key mapping, or visual
     * appearance
     */
    KeyboardType() {
    }

    /**
     * Gets the keycode that is mapped to the given X and Y world coordinates.
     * Returns null if there is no matching key at the given position.
     * 
     * @param x The X position within this KeyboardType's world coordinates.
     * @param y The Y position within this KeyboardType's world coordinates.
     * 
     * @return The keycode that is mapped to the given X and Y world coordinates, or
     *         null if there is not match.
     */
    public Integer getKeyCode(float x, float y) {
        if ((numOfSides == 1) || (x < getTexture(LEFT).getWidth())) {
            return getKeyCode(x, y, LEFT);
        } else {
            return getKeyCode(x, y, RIGHT);
        }
    }

    /**
     * Gets the keycode that is mapped to the given X and Y world coordinates.
     * Returns null if there is no matching key at the given position.
     * 
     * @param x    The X position within this KeyboardType's world coordinates.
     * @param y    The Y position within this KeyboardType's world coordinates.
     * @param side The side of the keyboard (left/right), for split keyboards.
     * 
     * @return The keycode that is mapped to the given X and Y world coordinates, or
     *         null if there is not match.
     */
    public Integer getKeyCode(float x, float y, int side) {
        Integer keyCode = null;
        int keyRow = 0;

        // If we're showing the mini version of the joystick, adjust the height here.
        if (equals(JOYSTICK) && !ViewportManager.getInstance().isPortrait() && (side == LEFT)) {
            // TODO: Make this a flag in the construction of a KeyboardType.
            keyRow = (int) ((textures[side].getHeight() - (y * 2) + renderOffset) / vertKeySize);

        } else {
            keyRow = (int) ((textures[side].getHeight() - (y - yStart) + renderOffset) / vertKeySize);
        }

        if (keyRow >= keyMap[side].length)
            keyRow = keyMap[side].length - 1;

        switch (this) {
        case LANDSCAPE:
        case PORTRAIT:
            if (x >= xStart) {
                keyCode = keyMap[side][keyRow][(int) ((x - xStart) / horizKeySize)];
            }
            break;

        case JOYSTICK:
            if (!ViewportManager.getInstance().isPortrait() && (side == LEFT)) {
                x = x * 2;
            }
            if (side == RIGHT) {
                x = x - (ViewportManager.getInstance().getWidth() - getTexture(RIGHT).getWidth());
            }
            int keyCol = (int) (x / vertKeySize);
            if (keyCol < keyMap[side][keyRow].length) {
                keyCode = keyMap[side][keyRow][keyCol];
            }
            break;

        default:
            break;
        }

        return keyCode;
    }

    /**
     * Tests if the given X/Y position is within the bounds of this KeyboardTypes
     * keyboard image.
     * 
     * @param x The X position to test.
     * @param y The Y position to test.
     * 
     * @return true if the given X/Y position is within the keyboard image;
     *         otherwise false.
     */
    public boolean isInKeyboard(float x, float y) {
        if (numOfSides == 1) {
            return isInKeyboard(x, y, LEFT);
        } else {
            return isInKeyboard(x, y, LEFT) || isInKeyboard(x, y, RIGHT);
        }
    }

    /**
     * Tests if the given X/Y position is within the bounds of this KeyboardTypes
     * keyboard image.
     * 
     * @param x    The X position to test.
     * @param y    The Y position to test.
     * @param side The side of the keyboard (left/right), for split keyboards.
     * 
     * @return true if the given X/Y position is within the keyboard image;
     *         otherwise false.
     */
    public boolean isInKeyboard(float x, float y, int side) {
        if (isRendered()) {
            int textureHeight = getTextures()[side].getHeight();
            int textureWidth = getTextures()[side].getWidth();

            if (this.equals(JOYSTICK) && !ViewportManager.getInstance().isPortrait() && (side == LEFT)) {
                textureHeight = textureHeight / 2;
                textureWidth = textureWidth / 2;
            }

            boolean isInYBounds = (y < (textureHeight + renderOffset) && (y > renderOffset));
            if (numOfSides == 1) {
                // In this case, we only need to test the Y position since the keyboard image
                // will span the whole width.
                return isInYBounds;
            } else {
                // Must be two sides...
                if (side == LEFT) {
                    // LEFT.
                    return (isInYBounds && (x < textureWidth));
                } else {
                    // RIGHT.
                    return (isInYBounds && (x > (ViewportManager.getInstance().getWidth() - textureWidth)));
                }
            }
        } else {
            // isInKeyboard only applies to rendered keyboards.
            return false;
        }
    }

    /**
     * @return The array of Textures holding the keyboard images for this
     *         KeyboardType.
     */
    public Texture[] getTextures() {
        if ((textures == null) && (keyboardImages != null)) {
            this.textures = new Texture[keyboardImages.length];
            for (int i = 0; i < keyboardImages.length; i++) {
                this.textures[i] = new Texture(keyboardImages[i]);
            }
        }
        return this.textures;
    }

    /**
     * @return The Texture holding the keyboard image for the given side of this
     *         keyboard.
     */
    public Texture getTexture(int side) {
        return textures[side];
    }

    /**
     * @return The Texture holding the keyboard image for this KeyboardType.
     */
    public Texture getTexture() {
        return textures[0];
    }

    /**
     * @return The opacity of this KeyboardType.
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * @return true if this KeyboardType is rendered by the AGILE render code;
     *         otherwise false.
     */
    public boolean isRendered() {
        return (textures != null);
    }

    /**
     * @return Offset from the bottom of the screen that the keyboard is rendered
     *         at.
     */
    public int getRenderOffset() {
        return renderOffset;
    }

    /**
     * @return The height above which the keyboard will close.
     */
    public int getCloseHeight() {
        return closeHeight;
    }

    /**
     * Disposes of the libGDX Textures for all KeyboardTypes.
     */
    public static void dispose() {
        for (KeyboardType keyboardType : KeyboardType.values()) {
            if (keyboardType.textures != null) {
                for (int i = 0; i < keyboardType.textures.length; i++) {
                    keyboardType.textures[i].dispose();
                    keyboardType.textures[i] = null;
                }
                keyboardType.textures = null;
            }
        }
    }

    /**
     * Re-creates the libGDX Textures for all KeyboardTypes.
     */
    public static void init() {
        for (KeyboardType keyboardType : KeyboardType.values()) {
            keyboardType.getTextures();
        }
    }
}
