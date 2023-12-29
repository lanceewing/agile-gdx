package com.agifans.agile;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
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
        switch (Gdx.app.getType()) {
            case Android:
            case Desktop:
                BufferUtils.copy(pixels, 0, screenPixmap.getPixels(), 320 * 200);
                break;
            case WebGL:
                // The buffer is faked/emulated in the GWT/HTML backend, so we can't
                // use the getPixels method. The setPixels method has been implemented
                // to update the HTML5 canvas though, so we can use that instead.
                // TODO: Is there a faster way to do this?
                // NOTE: Must be a direct ByteBuffer.
                ByteBuffer pixelsBuffer = ByteBuffer.allocateDirect(pixels.length * 2);
                BufferUtils.copy(pixels, 0, pixelsBuffer, 320 * 200);
                screenPixmap.setPixels(pixelsBuffer);
                break;
            default:
                // No other platforms are supported.
        }
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
