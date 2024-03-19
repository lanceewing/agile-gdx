package com.agifans.agile;

import com.agifans.agile.config.AppConfigItem;
import com.agifans.agile.ui.DialogHandler;
import com.agifans.agile.ui.GameScreenInputProcessor;
import com.agifans.agile.ui.KeyboardType;
import com.agifans.agile.ui.ViewportManager;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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
    
    private Agile agile;
    
    private GameScreenInputProcessor gameScreenInputProcessor;
    
    /**
     * This is an InputMultiplexor, which includes input processors for the Stage, 
     * the GameScreen components (other than AGI interpreter), and the AGI interpreter's
     * UserInput class.
     */
    private InputMultiplexer portraitInputProcessor;
    private InputMultiplexer landscapeInputProcessor;
    
    /**
     * SpriteBatch shared by all rendered components.
     */
    private SpriteBatch batch;
    
    // Platform specific AgileRunner implementation.
    private AgileRunner agileRunner;
    
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
    private Texture fullScreenIcon;

    private ViewportManager viewportManager;
    
    // Touchpad
    private Stage portraitTouchpadStage;
    private Stage landscapeTouchpadStage;
    private Touchpad portraitTouchpad;
    private Touchpad landscapeTouchpad;
    private int previousDirection;
    
    /**
     * Details about the AGI game that was selected to be run.
     */
    private AppConfigItem appConfigItem;
    
    /**
     * Constructor for GameScreen.
     * 
     * @param agileRunner 
     */
    public GameScreen(Agile agile, AgileRunner agileRunner, DialogHandler dialogHandler) {
        this.agile = agile;
        this.agileRunner = agileRunner;
        
        batch = new SpriteBatch();
        
        // Uses an approach used successfully in my various libgdx emulators.
        screenPixmap = new Pixmap(AGI_SCREEN_WIDTH, AGI_SCREEN_HEIGHT, Pixmap.Format.RGBA8888);
        screenPixmap.setBlending(Pixmap.Blending.None);
        agileRunner.init(this, screenPixmap);
        screens = new Texture[3];
        screens[0] = new Texture(screenPixmap, Pixmap.Format.RGBA8888, false);
        screens[0].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        screens[1] = new Texture(screenPixmap, Pixmap.Format.RGBA8888, false);
        screens[1].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        screens[2] = new Texture(screenPixmap, Pixmap.Format.RGBA8888, false);
        screens[2].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(ADJUSTED_WIDTH, ADJUSTED_HEIGHT, camera);
        
        keyboardIcon = new Texture("png/keyboard_icon.png");
        joystickIcon = new Texture("png/joystick_icon.png");
        backIcon = new Texture("png/back_arrow.png");
        fullScreenIcon = new Texture("png/full_screen.png");
        
        // Create the portrait and landscape joystick touchpads.
        portraitTouchpad = createTouchpad();
        landscapeTouchpad = createTouchpad();
        
        viewportManager = ViewportManager.getInstance();
        
        // Create a Stage and add TouchPad
        portraitTouchpadStage = new Stage(viewportManager.getPortraitViewport(), batch);
        portraitTouchpadStage.addActor(portraitTouchpad);
        landscapeTouchpadStage = new Stage(viewportManager.getLandscapeViewport(), batch);
        landscapeTouchpadStage.addActor(landscapeTouchpad);
        
        // Create and register an input processor for keys, etc.
        gameScreenInputProcessor = new GameScreenInputProcessor(this, dialogHandler);
        portraitInputProcessor = new InputMultiplexer();
        portraitInputProcessor.addProcessor(agileRunner.userInput);
        portraitInputProcessor.addProcessor(portraitTouchpadStage);
        portraitInputProcessor.addProcessor(gameScreenInputProcessor);
        landscapeInputProcessor = new InputMultiplexer();
        landscapeInputProcessor.addProcessor(agileRunner.userInput);
        landscapeInputProcessor.addProcessor(landscapeTouchpadStage);
        landscapeInputProcessor.addProcessor(gameScreenInputProcessor);
    }
    
    protected Touchpad createTouchpad() {
        Skin touchpadSkin = new Skin();
        touchpadSkin.add("touchBackground", new Texture("png/joystick_background.png"));
        touchpadSkin.add("touchKnob", new Texture("png/joystick_knob.png"));
        TouchpadStyle touchpadStyle = new TouchpadStyle();
        Drawable touchBackground = touchpadSkin.getDrawable("touchBackground");
        Drawable touchKnob = touchpadSkin.getDrawable("touchKnob");
        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;
        return new Touchpad(10, touchpadStyle);
    }
    
    public boolean copyPixels() {
        agileRunner.updatePixmap(screenPixmap);
        screens[updateScreen].draw(screenPixmap, 0, 0);
        updateScreen = (updateScreen + 1) % 3;
        drawScreen = (drawScreen + 1) % 3;
        return true;
    }
    
    public Texture getDrawScreen() {
        return screens[drawScreen];
    }
    
    @Override
    public void dispose() {
        agileRunner.stop();
        screenPixmap.dispose();
        screens[0].dispose();
        screens[1].dispose();
        screens[2].dispose();
        joystickIcon.dispose();
        keyboardIcon.dispose();
        backIcon.dispose();
        fullScreenIcon.dispose();
        batch.dispose();
    }

    @Override
    public void show() {
        if (viewportManager.isPortrait()) {
            Gdx.input.setInputProcessor(portraitInputProcessor);
        } else {
            Gdx.input.setInputProcessor(landscapeInputProcessor);
        }
        
        agileRunner.start(appConfigItem.getFilePath());
    }

    // TODO: Remove once satisfied with performance.
    private long lastLogTime;
    private long avgRenderTime;
    private long avgDrawTime;
    private long renderCount;
    private long drawCount;
    
    @Override
    public void render(float delta) {
        long renderStartTime = TimeUtils.nanoTime();
        long fps = Gdx.graphics.getFramesPerSecond();
        boolean draw = false;

        if (agileRunner.hasStopped()) {
            // If game has ended then go back to home screen. It has to be the UI thread
            // that calls the setScreen method. The AgileRunner itself can't do this.
            agileRunner.reset();
            // This makes sure we update the Pixmap one last time before leaving, as that
            // will mean that the AGI game screen starts out black for the next game.
            copyPixels();
            agile.setScreen(agile.getHomeScreen());;
            return;
        }
        
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
        }

        // Trigger tick.
        agileRunner.tick();
    }

    private void draw(float delta) {
        // Get the KeyboardType currently being used by the GameScreenInputProcessor.
        KeyboardType keyboardType = gameScreenInputProcessor.getKeyboardType();

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

        // Now render the UI elements, e.g. the keyboard, full screen, and joystick icons.
        viewportManager.getCurrentCamera().update();
        batch.setProjectionMatrix(viewportManager.getCurrentCamera().combined);
        batch.enableBlending();
        batch.begin();
        
        // The keyboard is always render in portrait mode, as there is space for it,
        // but in landscape mode, it needs to be enabled via the keyboard icon.
        if (keyboardType.isRendered() || viewportManager.isPortrait()) {
            batch.setColor(c.r, c.g, c.b, keyboardType.getOpacity());
            batch.draw(keyboardType.getTexture(), 0, keyboardType.getRenderOffset());
        } 
        
        batch.setColor(c.r, c.g, c.b, 0.5f);
        if (viewportManager.isPortrait()) {
            // Portrait
            batch.draw(joystickIcon, 20, 20);
            batch.draw(fullScreenIcon, viewportManager.getWidth() - viewportManager.getWidth() / 2 - 48, 20);
            batch.draw(backIcon, viewportManager.getWidth() - 116, 20);
        } else {
            // Landscape
            batch.draw(joystickIcon, 16, viewportManager.getHeight() - 112);
            batch.draw(fullScreenIcon, viewportManager.getWidth() - 112, viewportManager.getHeight() - 112);
            batch.draw(backIcon, viewportManager.getWidth() - 112, 16);
            batch.draw(keyboardIcon, 16, 0);
        }
        
        batch.end();
        
        // The joystick touch pad is updated and rendered via the Stage.
        if (gameScreenInputProcessor.isJoystickActive()) {
            float joyX = 0;
            float joyY = 0;
            if (viewportManager.isPortrait()) {
                // Top of keyboard is: 765 + 135 = 900.
                int joyWidth = 200;
                int agiScreenBase = (int)(viewportManager.getHeight() - (viewportManager.getWidth() / 1.32));
                int midBetweenKeybAndPic = ((agiScreenBase + 900) / 2);
                portraitTouchpad.setSize(joyWidth, joyWidth);
                portraitTouchpad.setY(midBetweenKeybAndPic - (joyWidth / 2));
                portraitTouchpad.setX(1080 - joyWidth - 20);
                portraitTouchpadStage.act(delta);
                portraitTouchpadStage.draw();
                joyX = portraitTouchpad.getKnobPercentX();
                joyY = portraitTouchpad.getKnobPercentY();
            } else {
                // Landscape
                int joyWidth = 96;
                landscapeTouchpad.setSize(joyWidth, joyWidth);
                landscapeTouchpad.setY(viewportManager.getHeight() - (viewportManager.getHeight() / 2) - (joyWidth / 2));
                landscapeTouchpad.setX(1920 - joyWidth - 16);
                landscapeTouchpadStage.act(delta);
                landscapeTouchpadStage.draw();
                joyX = landscapeTouchpad.getKnobPercentX();
                joyY = landscapeTouchpad.getKnobPercentY();
            }
            processJoystickInput(joyX, joyY);
        }
    }
    
    private static final int[] DIRECTION_TO_KEY_MAP = new int[] {
        0, 
        Keys.UP, 
        Keys.PAGE_UP, 
        Keys.RIGHT, 
        Keys.PAGE_DOWN, 
        Keys.DOWN, 
        Keys.END, 
        Keys.LEFT, 
        Keys.HOME
    };
    
    /**
     * Processes joystick input, converting the touchpad position into an AGI
     * direction and then setting the corresponding direction key.
     * 
     * @param joyX
     * @param joyY
     */
    private void processJoystickInput(float joyX, float joyY) {
        double heading = Math.atan2(-joyY, joyX);
        double distance = Math.sqrt((joyX * joyX) + (joyY * joyY));
        
        int direction = 0;
        
        if (distance > 0.3) {
            // Convert heading to an AGI direction.
            if (heading == 0) {
                // Right
                direction = 3;
            }
            else if (heading > 0) {
                // Down
                if (heading < 0.3926991) {
                    // Right
                    direction = 3;
                }
                else if (heading < 1.178097) {
                    // Down/Right
                    direction = 4;
                }
                else if (heading < 1.9634954) {
                    // Down
                    direction = 5;
                }
                else if (heading < 2.7488936) {
                    // Down/Left
                    direction = 6;
                }
                else {
                    // Left
                    direction = 7;
                }
            }
            else {
                // Up
                if (heading > -0.3926991) {
                    // Right
                    direction = 3;
                }
                else if (heading > -1.178097) {
                    // Up/Right
                    direction = 2;
                }
                else if (heading > -1.9634954) {
                    // Up
                    direction = 1;
                }
                else if (heading > -2.7488936) {
                    // Up/Left
                    direction = 8;
                }
                else {
                    // Left
                    direction = 7;
                }
            }
        }
        
        UserInput userInput = getAgileRunner().getUserInput();
        
        if (previousDirection != 0) {
            userInput.setKey(DIRECTION_TO_KEY_MAP[previousDirection], false);
        }
        
        if ((previousDirection != 0) && (direction == 0)) {
            getAgileRunner().getVariableData().setVar(Defines.EGODIR, direction);
        }
        else if (direction != 0) {
            userInput.setKey(DIRECTION_TO_KEY_MAP[direction], true);
        }
        
        previousDirection = direction;
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        
        // Align AGI screen's top edge to top of the viewport.
        Camera camera = viewport.getCamera();
        camera.position.x = ADJUSTED_WIDTH / 2;
        camera.position.y = ADJUSTED_HEIGHT - viewport.getWorldHeight() / 2;
        camera.update();
        
        gameScreenInputProcessor.resize(width, height);
        
        viewportManager.update(width, height);
        
        if (viewportManager.isPortrait()) {
            Gdx.input.setInputProcessor(portraitInputProcessor);
        } else {
            Gdx.input.setInputProcessor(landscapeInputProcessor);
        }
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

    /**
     * Initialises the GameScreen with the given AppConfigItem. This will represent an AGI game 
     * that was selected on the HomeScreen.
     * 
     * @param appConfigItem The configuration for the app that was selected on the HomeScreen.
     */
    public void initGame(AppConfigItem appConfigItem) {
        this.appConfigItem = appConfigItem;
    }
    
    /**
     * Saves a screenshot of the machine's current screen contents.
     */
    public void saveScreenshot() {
        agileRunner.saveScreenshot(agile, appConfigItem, screenPixmap);
    }
    
    /**
     * Gets the AgileRunner implementation instance that is running the AGI game.
     * 
     * @return The AgileRunner implementation instance that is running the AGI game.
     */
    public AgileRunner getAgileRunner() {
        return agileRunner;
    }
}
