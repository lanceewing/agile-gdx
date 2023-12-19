package com.agifans.agile;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.BufferUtils;

public class GameScreen {

    /**
     * The pixels array for the AGI screen. Any change made to this array will be copied 
     * to the Pixmap on every frame.
     */
    private short[] pixels;
    
    private Pixmap screenPixmap;
    private Texture[] screens;
    private int drawScreen = 1;
    private int updateScreen = 0;

    /**
     * Constructor for GameScreen.
     */
    public GameScreen() {
        // Uses an approach used successfully in my various libgdx emulators.
        pixels = new short[320 * 200];
        screenPixmap = new Pixmap(320, 200, Pixmap.Format.RGB565);
        screens = new Texture[3];
        screens[0] = new Texture(screenPixmap, Pixmap.Format.RGB565, false);
        screens[0].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        screens[1] = new Texture(screenPixmap, Pixmap.Format.RGB565, false);
        screens[1].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        screens[2] = new Texture(screenPixmap, Pixmap.Format.RGB565, false);
        screens[2].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }
    
    public boolean render() {
        // TODO: After implementing web worker/separate background thread, need to response to message instead.
        BufferUtils.copy(pixels, 0, screenPixmap.getPixels(), 320 * 200);
        screens[updateScreen].draw(screenPixmap, 0, 0);
        updateScreen = (updateScreen + 1) % 3;
        drawScreen = (drawScreen + 1) % 3;
        return true;
    }
    
    public short[] getPixels() {
        return pixels;
    }
    
    public Texture getDrawScreen() {
        return screens[drawScreen];
    }
    
    public void dispose() {
        screenPixmap.dispose();
        screens[0].dispose();
        screens[1].dispose();
        screens[2].dispose();
    }
}
