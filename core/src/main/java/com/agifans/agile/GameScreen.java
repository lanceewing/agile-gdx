package com.agifans.agile;

import com.agifans.agile.config.AppConfigItem;
import com.agifans.agile.ui.DialogHandler;
import com.agifans.agile.ui.GameScreenInputProcessor;
import com.agifans.agile.ui.GameScreenInputProcessor.JoystickAlignment;
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
     * Whether or not the game was started by a user interaction.
     */
    private boolean startedByUser;
    
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
        screens[0].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest);
        screens[1] = new Texture(screenPixmap, Pixmap.Format.RGBA8888, false);
        screens[1].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest);
        screens[2] = new Texture(screenPixmap, Pixmap.Format.RGBA8888, false);
        screens[2].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest);
    
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
        
        if (agileRunner.isMobile()) {
            gameScreenInputProcessor.setJoystickAlignment(JoystickAlignment.RIGHT);
            
            if (!Gdx.graphics.isFullscreen() && startedByUser) {
                gameScreenInputProcessor.switchIntoFullScreen();
            }
        }
        
        agileRunner.start(appConfigItem);
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
            if (Gdx.graphics.isFullscreen()) {
                gameScreenInputProcessor.switchOutOfFullScreen();
            }
            agile.setScreen(agile.getHomeScreen());
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
        // Get the KeyboardType & JoystickAlignment currently being used by the GameScreenInputProcessor.
        KeyboardType keyboardType = gameScreenInputProcessor.getKeyboardType();
        JoystickAlignment joystickAlignment = gameScreenInputProcessor.getJoystickAlignment();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render the AGI screen.
        float cameraXOffset = 0;
        float cameraYOffset = 0;
        float sidePaddingWidth = viewportManager.getSidePaddingWidth();
        
        if (viewportManager.doesScreenFitWidth()) {
            // Override default screen centering logic to allow for narrower screens, so 
            // that the joystick can be rendered as a decent size.
            float agiWidthRatio = (viewportManager.getAgiScreenWidth() / ADJUSTED_WIDTH);
            if ((sidePaddingWidth > 64) && (sidePaddingWidth < 232)) {
                // 232 = 2 * min width on sides.
                // 64 = when icon on one side is perfectly centred.
                float unadjustedXOffset = Math.min(232 - sidePaddingWidth, sidePaddingWidth);
                cameraXOffset = (unadjustedXOffset / agiWidthRatio);
                if (joystickAlignment.equals(JoystickAlignment.LEFT)) {
                    cameraXOffset *= -1;
                }
            }
        } else {
            float agiScreenHeight = (viewportManager.getWidth() / 1.32f);
            float agiHeightRatio = (agiScreenHeight / ADJUSTED_HEIGHT);
            float topPadding = ((viewportManager.getHeight() - agiScreenHeight) / 2);
            cameraYOffset = (topPadding / agiHeightRatio);
        }
        gameScreenInputProcessor.setCameraXOffset(cameraXOffset);
        camera.position.set((ADJUSTED_WIDTH / 2) + cameraXOffset, (ADJUSTED_HEIGHT / 2) - cameraYOffset, 0.0f);
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
            if (keyboardType.getTexture() != null) {
                batch.setColor(c.r, c.g, c.b, keyboardType.getOpacity());
                batch.draw(
                        keyboardType.getTexture(), 
                        0, keyboardType.getRenderOffset(), 
                        keyboardType.getTexture().getWidth(), 
                        keyboardType.getHeight());
            }
        } 
        
        batch.setColor(c.r, c.g, c.b, 0.5f);
        if (viewportManager.isPortrait()) {
            // Portrait
            batch.draw(fullScreenIcon, 20, 20);
            batch.draw(joystickIcon, (viewportManager.getWidth() / 3) - 32, 20);
            batch.draw(keyboardIcon, (viewportManager.getWidth() - (viewportManager.getWidth() / 3)) - 64, 20);
            batch.draw(backIcon, viewportManager.getWidth() - 116, 20);
        } else {
            // Landscape
            if (cameraXOffset == 0) {
                // Middle.
                if ((viewportManager.getAgiScreenBase() > 0) || (sidePaddingWidth <= 64)) {
                    // The area between full landscape and full portrait.
                    float leftAdjustment = (viewportManager.getWidth() / 4) - 32;
                    batch.draw(fullScreenIcon, ((viewportManager.getWidth() / 2) - 48) - leftAdjustment, 16);
                    batch.draw(joystickIcon, ((viewportManager.getWidth() - (viewportManager.getWidth() / 3)) - 64) - leftAdjustment, 16);
                    batch.draw(keyboardIcon, ((viewportManager.getWidth() - (viewportManager.getWidth() / 6)) - 80) - leftAdjustment, 16);
                    batch.draw(backIcon, (viewportManager.getWidth() - 112) - leftAdjustment, 16);
                } else {
                    batch.draw(joystickIcon, 16, viewportManager.getHeight() - 112);
                    batch.draw(fullScreenIcon, viewportManager.getWidth() - 112, viewportManager.getHeight() - 112);
                    batch.draw(backIcon, viewportManager.getWidth() - 112, 16);
                    batch.draw(keyboardIcon, 16, 0);
                }
            } else if (cameraXOffset < 0) {
                // Left
                batch.draw(joystickIcon, 16, viewportManager.getHeight() - 324);
                batch.draw(fullScreenIcon, 16, viewportManager.getHeight() - 112);
                batch.draw(backIcon, 16, 16);
                batch.draw(keyboardIcon, 16, 228);
            } else if (cameraXOffset > 0) {
                // Right
                batch.draw(joystickIcon, viewportManager.getWidth() - 112, viewportManager.getHeight() - 324);
                batch.draw(fullScreenIcon, viewportManager.getWidth() - 112, viewportManager.getHeight() - 112);
                batch.draw(backIcon, viewportManager.getWidth() - 112, 16);
                batch.draw(keyboardIcon, viewportManager.getWidth() - 112, 228);
            }
        }
        
        batch.end();
        
        // The joystick touch pad is updated and rendered via the Stage.
        if (!joystickAlignment.equals(JoystickAlignment.OFF)) {
            float joyX = 0;
            float joyY = 0;
            if (viewportManager.isPortrait()) {
                // Top of keyboard is: 765 + 135 = 900.
                int joyWidth = 200;
                int agiScreenBase = viewportManager.getAgiScreenBase();
                int midBetweenKeybAndPic = ((agiScreenBase + 900) / 2);
                portraitTouchpad.setSize(joyWidth, joyWidth);
                portraitTouchpad.setY(midBetweenKeybAndPic - (joyWidth / 2));
                switch (joystickAlignment) {
                    case OFF:
                        break;
                    case RIGHT:
                        portraitTouchpad.setX(1080 - joyWidth - 20);
                        break;
                    case MIDDLE:
                        portraitTouchpad.setX(viewportManager.getWidth() - viewportManager.getWidth() / 2 - (joyWidth / 2));
                        break;
                    case LEFT:
                        portraitTouchpad.setX(20);
                        break;
                }
                portraitTouchpadStage.act(delta);
                portraitTouchpadStage.draw();
                joyX = portraitTouchpad.getKnobPercentX();
                joyY = portraitTouchpad.getKnobPercentY();
            } else {
                // Landscape
                if ((viewportManager.getAgiScreenBase() > 0) || (sidePaddingWidth <= 64)) {
                    int joyWidth = Math.max(Math.min(140 + viewportManager.getAgiScreenBase(), 216), 140);
                    landscapeTouchpad.setSize(joyWidth, joyWidth);
                    landscapeTouchpad.setY(16);
                    landscapeTouchpad.setX(viewportManager.getWidth() - joyWidth - 16);
                    landscapeTouchpad.getStyle().knob.setMinHeight(joyWidth * 0.6f);
                    landscapeTouchpad.getStyle().knob.setMinWidth(joyWidth * 0.6f);
                    landscapeTouchpadStage.act(delta);
                    landscapeTouchpadStage.draw();
                    joyX = landscapeTouchpad.getKnobPercentX();
                    joyY = landscapeTouchpad.getKnobPercentY();
                } else {
                    float joyWidth = Math.min(Math.max((sidePaddingWidth * 2) - 32, 96), 200);
                    landscapeTouchpad.setSize(joyWidth, joyWidth);
                    landscapeTouchpad.getStyle().knob.setMinHeight(joyWidth * 0.6f);
                    landscapeTouchpad.getStyle().knob.setMinWidth(joyWidth * 0.6f);
                    landscapeTouchpad.setY(viewportManager.getHeight() - (viewportManager.getHeight() / 2) - (joyWidth / 2));
                    switch (joystickAlignment) {
                        case OFF:
                            break;
                        case RIGHT:
                            landscapeTouchpad.setX(1920 - joyWidth - 16);
                            break;
                        case MIDDLE:
                            break;
                        case LEFT:
                            landscapeTouchpad.setX(16);
                            break;
                    }
                    landscapeTouchpadStage.act(delta);
                    landscapeTouchpadStage.draw();
                    joyX = landscapeTouchpad.getKnobPercentX();
                    joyY = landscapeTouchpad.getKnobPercentY();
                }
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
            if (direction != previousDirection) {
                userInput.keyDown(DIRECTION_TO_KEY_MAP[direction]);
            } else {
                userInput.setKey(DIRECTION_TO_KEY_MAP[direction], true);
            }
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
     * @param startedByUser true if the game is being started by a user interaction; otherwise false.
     */
    public void initGame(AppConfigItem appConfigItem, boolean startedByUser) {
        this.appConfigItem = appConfigItem;
        this.startedByUser = startedByUser;
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
