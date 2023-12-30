package com.agifans.agile;

import java.nio.ByteBuffer;

import com.agifans.agile.ui.ViewportManager;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * AGILE is a multi-screen app. This Screen is the  screen on which the AGI games run.
 */
public class GameScreen implements Screen {

    private static final int AGI_SCREEN_WIDTH = 320;
    private static final int AGI_SCREEN_HEIGHT = 200;
    private static final int ADJUSTED_WIDTH = ((AGI_SCREEN_HEIGHT / 3) * 4);
    private static final int ADJUSTED_HEIGHT = AGI_SCREEN_HEIGHT;
    
    /**
     * SpriteBatch shared by all rendered components.
     */
    private SpriteBatch batch;
    
    // Platform specific implementations.
    private AgileRunner agileRunner;
    private WavePlayer wavePlayer;
    private SavedGameStore savedGameStore;;
    
    /**
     * The pixels array for the AGI screen. Any change made to this array will be copied 
     * to the Pixmap on every frame.
     */
    private int[] pixels;
    
    private Pixmap screenPixmap;
    private Viewport viewport;
    private Camera camera;
    private Texture[] screens;
    private int drawScreen = 1;
    private int updateScreen = 0;

    // UI components.
    private Texture joystickIcon;
    private Texture keyboardIcon;
    private Texture backIcon;

    private ViewportManager viewportManager;
    
    // Touchpad
    private Stage portraitStage;
    private Stage landscapeStage;
    private Touchpad portraitTouchpad;
    private Touchpad landscapeTouchpad;
    
    /**
     * Constructor for GameScreen.
     * @param savedGameStore 
     * @param wavePlayer 
     * @param agileRunner 
     */
    public GameScreen(AgileRunner agileRunner, WavePlayer wavePlayer, SavedGameStore savedGameStore) {
        this.agileRunner = agileRunner;
        this.wavePlayer = wavePlayer;
        this.savedGameStore = savedGameStore;
        
        batch = new SpriteBatch();
        
        // Uses an approach used successfully in my various libgdx emulators.
        pixels = new int[AGI_SCREEN_WIDTH * AGI_SCREEN_HEIGHT];
        screenPixmap = new Pixmap(AGI_SCREEN_WIDTH, AGI_SCREEN_HEIGHT, Pixmap.Format.RGBA8888);
        screenPixmap.setBlending(Pixmap.Blending.None);
        screens = new Texture[3];
        screens[0] = new Texture(screenPixmap, Pixmap.Format.RGBA8888, false);
        screens[0].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        screens[1] = new Texture(screenPixmap, Pixmap.Format.RGBA8888, false);
        screens[1].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        screens[2] = new Texture(screenPixmap, Pixmap.Format.RGBA8888, false);
        screens[2].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    
        camera = new OrthographicCamera();
        // TODO: Test if this is appropriate. Compare with AGILE C# and JOric.
        viewport = new ExtendViewport(ADJUSTED_WIDTH, ADJUSTED_HEIGHT, camera);
        
        keyboardIcon = new Texture("png/keyboard_icon.png");
        joystickIcon = new Texture("png/joystick_icon.png");
        backIcon = new Texture("png/back_arrow.png");
        
        // Create the portrait and landscape joystick touchpads.
        portraitTouchpad = createTouchpad(300);
        landscapeTouchpad = createTouchpad(200);
        
        viewportManager = ViewportManager.getInstance();
        
        //Create a Stage and add TouchPad
        portraitStage = new Stage(viewportManager.getPortraitViewport(), batch);
        portraitStage.addActor(portraitTouchpad);
        landscapeStage = new Stage(viewportManager.getLandscapeViewport(), batch);
        landscapeStage.addActor(landscapeTouchpad);
    }
    
    protected Touchpad createTouchpad(int size) {
        Skin touchpadSkin = new Skin();
        touchpadSkin.add("touchBackground", new Texture("png/touchBackground.png"));
        touchpadSkin.add("touchKnob", new Texture("png/touchKnob.png"));
        TouchpadStyle touchpadStyle = new TouchpadStyle();
        Drawable touchBackground = touchpadSkin.getDrawable("touchBackground");
        Drawable touchKnob = touchpadSkin.getDrawable("touchKnob");
        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;
        Touchpad touchpad = new Touchpad(10, touchpadStyle);
        touchpad.setBounds(15, 15, size, size);
        return touchpad;
    }
    
    public boolean copyPixels() {
        // TODO: WebGL and Desktop currently expect different endian byte ordering!!!
        
        switch (Gdx.app.getType()) {
            case Android:
            case Desktop:
                // For some reason, drawPixel responses the RRGGBBAA format, but using
                // this copy method expects the ints to be AABBGGRR instead.
                
                BufferUtils.copy(pixels, 0, screenPixmap.getPixels(), AGI_SCREEN_WIDTH * AGI_SCREEN_HEIGHT);
                break;
            case WebGL:
                // The buffer is faked/emulated in the GWT/HTML backend, so we can't
                // use the getPixels method. The setPixels method has been implemented
                // to update the HTML5 canvas though, so we can use that instead.
                // TODO: Is there a faster way to do this?
                // NOTE: Must be a direct ByteBuffer.
                ByteBuffer pixelsBuffer = ByteBuffer.allocateDirect(pixels.length * 4);
                BufferUtils.copy(pixels, 0, pixelsBuffer, AGI_SCREEN_WIDTH * AGI_SCREEN_HEIGHT);
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
    
    public int[] getPixels() {
        return pixels;
    }
    
    public Texture getDrawScreen() {
        return screens[drawScreen];
    }
    
    /**
     * Starts the AGI game contained in the given game folder.
     * 
     * @param gameFolder The folder from which we'll get all of the game data.
     */
    private void startGame(String gameFolder) {
        // Register the key event handlers for keyUp, keyDown, and keyTyped.
        UserInput userInput = new UserInput();
        Gdx.input.setInputProcessor(userInput);
        
        agileRunner.init(gameFolder, userInput, wavePlayer, savedGameStore, getPixels());
        
        // Start up the AgileRunner to run the interpreter in the background.
        agileRunner.start();
    }
    
    /**
     * Selects am AGI game folder to run.
     * 
     * @return The folder containing the AGI game's resources.
     */
    private String selectGame() {
        // TODO: Implement selection logic. This is a placeholder for now.
        // return "C:\\dev\\agi\\winagi\\kq4agi";
        // return "file:/C:/dev/agi/winagi/kq4agi/";
        // return "file://localhost/C:/dev/agi/winagi/kq4agi/";
        return "file:///C:/dev/agi/winagi/kq1/";
    }
    
    @Override
    public void dispose() {
        agileRunner.stop();
        screenPixmap.dispose();
        screens[0].dispose();
        screens[1].dispose();
        screens[2].dispose();
        batch.dispose();
    }

    @Override
    public void show() {
        startGame(selectGame());
    }

    // TODO: Remove once satisfied with performance.
    private long lastLogTime;
    private long avgRenderTime;
    private long avgDrawTime;
    private long renderCount;
    private long drawCount;
    
    @Override
    public void render(float delta) {
        // Copy pixels form interpreter into Pixmap.
        // copyPixels();

        // OLD LOGIC PRIOR TO JORIC CODE BEING ADDED
        // // Render.
        // Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        // Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // batch.begin();
        // batch.draw(getDrawScreen(), 0, 0, 960, 600);
        // batch.end();

        long renderStartTime = TimeUtils.nanoTime();
        long fps = Gdx.graphics.getFramesPerSecond();
        boolean draw = false;

        // TODO: Paused place holder.
        boolean paused = false;
        if (paused) {
            // When paused, we limit the draw frequency since there isn't anything to
            // change.
            draw = ((fps < 30) || ((renderCount % (fps / 30)) == 0));
        }
        else {
            copyPixels();
            draw = true;
        }

        if (draw) {
            drawCount++;
            draw(delta);
            long drawDuration = TimeUtils.nanoTime() - renderStartTime;
            if (renderCount == 0) {
                avgDrawTime = drawDuration;
            } else {
                avgDrawTime = ((avgDrawTime * renderCount) + drawDuration) / (renderCount + 1);
            }
        }

        long renderDuration = TimeUtils.nanoTime() - renderStartTime;
        if (renderCount == 0) {
            avgRenderTime = renderDuration;
        } else {
            avgRenderTime = ((avgRenderTime * renderCount) + renderDuration) / (renderCount + 1);
        }

        renderCount++;

        if ((lastLogTime == 0) || (renderStartTime - lastLogTime > 10000000000L)) {
            lastLogTime = renderStartTime;
            // Gdx.app.log("RenderTime", String.format(
            // "[%d] avgDrawTime: %d avgRenderTime: %d maxFrameDuration: %d delta: %f fps:
            // %d",
            // drawCount, avgDrawTime, avgRenderTime, maxFrameDuration, delta,
            // Gdx.graphics.getFramesPerSecond()));
        }

        // Trigger tick.
        agileRunner.tick();
    }

    private void draw(float delta) {
        // Get the KeyboardType currently being used by the MachineScreenProcessor.
        //KeyboardType keyboardType = machineInputProcessor.getKeyboardType();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render the AGI screen.
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.disableBlending();
        batch.begin();
        Color c = batch.getColor();
        batch.setColor(c.r, c.g, c.b, 1f);
        batch.draw(
                getDrawScreen(), 
                0, 0, ADJUSTED_WIDTH, ADJUSTED_HEIGHT,
                0, 0, AGI_SCREEN_WIDTH, AGI_SCREEN_HEIGHT, 
                false, false);
        batch.end();

        // Render the UI elements, e.g. the keyboard and joystick icons.
        viewportManager.getCurrentCamera().update();
        batch.setProjectionMatrix(viewportManager.getCurrentCamera().combined);
        batch.enableBlending();
        batch.begin();
        
        // TODO: Implement joystick being active.
        //if (keyboardType.equals(KeyboardType.JOYSTICK)) {
        //    if (viewportManager.isPortrait()) {
        //        // batch.draw(keyboardType.getTexture(KeyboardType.LEFT), 0, 0);
        //        batch.draw(keyboardType.getTexture(KeyboardType.RIGHT), viewportManager.getWidth() - 135, 0);
        //    } else {
        //        // batch.draw(keyboardType.getTexture(KeyboardType.LEFT), 0, 0, 201, 201);
        //        batch.draw(keyboardType.getTexture(KeyboardType.RIGHT), viewportManager.getWidth() - 135, 0);
        //    }
        //} else if (keyboardType.isRendered()) {
        //    batch.setColor(c.r, c.g, c.b, keyboardType.getOpacity());
        //    batch.draw(keyboardType.getTexture(), 0, keyboardType.getRenderOffset());
        //} else if (keyboardType.equals(KeyboardType.OFF)) {
            // The keyboard and joystick icons are rendered only when an input type isn't
            // showing.
            batch.setColor(c.r, c.g, c.b, 0.5f);
            if (viewportManager.isPortrait()) {
                batch.draw(joystickIcon, 0, 0);
                if (Gdx.app.getType().equals(ApplicationType.Android)) {
                    // Main AGI keyboard on the left.
                    batch.draw(keyboardIcon, viewportManager.getWidth() - 145, 0);
                    // Mobile keyboard for debug purpose. Wouldn't normally make this available.
                    batch.setColor(c.r, c.g, c.b, 0.15f);
                    batch.draw(keyboardIcon, viewportManager.getWidth() - viewportManager.getWidth() / 2 - 70, 0);

                } else {
                    // Desktop puts keyboard button in the middle.
                    batch.draw(keyboardIcon, viewportManager.getWidth() - viewportManager.getWidth() / 2 - 70, 0);
                    // and the back button on the right.
                    batch.draw(backIcon, viewportManager.getWidth() - 145, 0);
                }

            } else {
                batch.draw(joystickIcon, 0, viewportManager.getHeight() - 140);
                batch.draw(keyboardIcon, viewportManager.getWidth() - 150, viewportManager.getHeight() - 125);
                batch.draw(backIcon, viewportManager.getWidth() - 150, 0);
            }
        //}
        batch.end();
        
        //        if (keyboardType.equals(KeyboardType.JOYSTICK)) {
        //            float joyX = 0;
        //            float joyY = 0;
        //            if (viewportManager.isPortrait()) {
        //                portraitStage.act(delta);
        //                portraitStage.draw();
        //                joyX = portraitTouchpad.getKnobPercentX();
        //                joyY = portraitTouchpad.getKnobPercentY();
        //            } else {
        //                landscapeStage.act(delta);
        //                landscapeStage.draw();
        //                joyX = landscapeTouchpad.getKnobPercentX();
        //                joyY = landscapeTouchpad.getKnobPercentY();
        //            }
        //            machine.getJoystick().touchPad(joyX, joyY);
        //        }
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        
        // Align AGI screen's top edge to top of the viewport.
        Camera camera = viewport.getCamera();
        camera.position.x = ADJUSTED_WIDTH / 2;
        camera.position.y = ADJUSTED_HEIGHT - viewport.getWorldHeight() / 2;
        camera.update();
        
        // TODO: Add in when input processor is introduced.
        //machineInputProcessor.resize(width, height);
        
        viewportManager.update(width, height);
        
        // TODO: Add in when input processor is introduced.
        //if (viewportManager.isPortrait()) {
        //  Gdx.input.setInputProcessor(portraitInputProcessor);
        //} else {
        //  Gdx.input.setInputProcessor(landscapeInputProcessor);
        //}
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub
        
    }
}
