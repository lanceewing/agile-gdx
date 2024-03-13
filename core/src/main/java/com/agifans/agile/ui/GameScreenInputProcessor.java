package com.agifans.agile.ui;

import com.agifans.agile.GameScreen;
import com.agifans.agile.UserInput;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

/**
 * InputProcessor for the touch/click parts of the game screen. In libgdx, mouse clicks
 * come through as touch events, so in that way we support both touch screens (e.g. Android,
 * or Windows tablets like the Microsoft Surface), and also mouse input.
 */
public class GameScreenInputProcessor extends InputAdapter {

    /**
     * The GameScreen that this InputProcessor is processing input for.
     */
    private GameScreen gameScreen;
    
    /**
     * The type of keyboard currently being used for input.
     */
    private KeyboardType keyboardType;
    
    /**
     * Whether the joystick is active or not.
     */
    private boolean joystickActive;
    
    /**
     * Invoked by AGILE whenever it would like to show a dialog, such as when it
     * needs the user to confirm an action, or to choose a file.
     */
    private DialogHandler dialogHandler;

    /**
     * The one and only ViewportManager used by AGILE.
     */
    private ViewportManager viewportManager;

    /**
     * We only track up to a maximum number of simultaneous touch events.
     */
    private static final int MAX_SIMULTANEOUS_TOUCH_EVENTS = 5;

    /**
     * Array of current touches indexed by touch pointer ID. This Map allows us to
     * keep track of active dragging. If a drag happens to start within a keyboard
     * key and then leaves it before being released, we need to automatically fire a
     * key up event for our virtual keyboard. Without handling this, drags can
     * completely confuse the keyboard state. And the joystick logic relies on
     * dragging, so this needs to work well.
     */
    private TouchInfo[] touches;

    /**
     * Represents the touch info for a particular pointer ID.
     */
    class TouchInfo {
        float startX;
        float startY;
        float lastX;
        float lastY;
        Integer lastKey;
    }

    /**
     * Keeps track of whether the mouse button is currently down or not, for the use of
     * the AGI Mouse hack. 0 = not down, whereas 1 and 2 are left and right buttons.
     */
    private int agiMouseButton;
    
    /**
     * The width of the screen/window before full screen mode was activated.
     */
    private int screenWidthBeforeFullScreen;
    
    /**
     * The height of the screen/width before ful screen mode was activated.
     */
    private int screenHeightBeforeFullScreen;
    
    /**
     * Constructor for GameScreenInputProcessor.
     * 
     * @param machineScreen
     * @param dialogHandler
     */
    public GameScreenInputProcessor(GameScreen gameScreen, DialogHandler dialogHandler) {
        this.gameScreen = gameScreen;
        this.dialogHandler = dialogHandler;
        this.keyboardType = KeyboardType.OFF;
        this.viewportManager = ViewportManager.getInstance();

        // Initialise the touch info for max num of pointers (multi touch). We create
        // these up
        // front and reuse them so as to avoid garbage collection.
        this.touches = new TouchInfo[MAX_SIMULTANEOUS_TOUCH_EVENTS];
        for (int i = 0; i < MAX_SIMULTANEOUS_TOUCH_EVENTS; i++) {
            touches[i] = new TouchInfo();
        }
    }
    
    /**
     * Called when the screen was touched or a mouse button was pressed. The button
     * parameter will be {@link Buttons#LEFT} on iOS.
     * 
     * @param screenX The x coordinate, origin is in the upper left corner
     * @param screenY The y coordinate, origin is in the upper left corner
     * @param pointer the pointer for the event.
     * @param button  the button
     * 
     * @return whether the input was processed
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Convert the screen coordinates to world coordinates.
        Vector2 touchXY = viewportManager.unproject(screenX, screenY);

        // Update AGI mouse variables.
        updateAGIMouse(touchXY, button, true);
                
        // Update the touch info for this pointer.
        TouchInfo touchInfo = null;
        if (pointer < MAX_SIMULTANEOUS_TOUCH_EVENTS) {
            touchInfo = touches[pointer];
            touchInfo.startX = touchInfo.lastX = touchXY.x;
            touchInfo.startY = touchInfo.lastY = touchXY.y;
            touchInfo.lastKey = null;
        }

        // If the tap is within the keyboard...
        if (keyboardType.isInKeyboard(touchXY.x, touchXY.y)) {
            Integer keycode = keyboardType.getKeyCode(touchXY.x, touchXY.y);
            if (keycode != null) {
                processVirtualKeyboardKeyDown(keycode);
            }
            if (touchInfo != null) {
                touchInfo.lastKey = keycode;
            }
        }

        return true;
    }
    
    private UserInput getUserInput() {
        return gameScreen.getAgileRunner().getUserInput();
    }
    
    private void processVirtualKeyboardKeyDown(int keycode) {
        if (((keycode >> 8) & 0xFF) == Keys.SHIFT_LEFT) {
            getUserInput().keyDown(Keys.SHIFT_LEFT);
        }
        
        getUserInput().keyDown(keycode & 0xFF);
        
        if (KeyboardTypeData.KEYTYPED_CHAR_MAP.containsKey(keycode)) {
            getUserInput().keyTyped(KeyboardTypeData.KEYTYPED_CHAR_MAP.get(keycode));
        }
    }
    
    private void processVirtualKeyboardKeyUp(int keycode) {
        getUserInput().keyUp(keycode & 0xFF);
        
        if (((keycode >> 8) & 0xFF) == Keys.SHIFT_LEFT) {
            getUserInput().keyUp(Keys.SHIFT_LEFT);
        }
    }

    /**
     * Updates the AGI Mouse state. AGI Mouse is a fan made hack to the original AGI
     * interpreter that overrides AGI command 171, so that when it is invoked, certain
     * AGI variables are assigned the mouse X/Y position and button status.
     * 
     * @param touchXY
     * @param button
     * @param buttonDown
     */
    private void updateAGIMouse(Vector2 touchXY, int button, boolean buttonDown) {
        int agiX = 0;
        int agiY = 0;
        if (viewportManager.isPortrait()) {
            // Portrait
            agiX = Math.round((touchXY.x / viewportManager.getWidth()) * 160);
            float agiHeight = (1080.0f / 1.32f);
            float agiRatio = (agiHeight / 200);
            float agiStart = (viewportManager.getHeight() - agiHeight);
            agiY = 200 - Math.round((touchXY.y - agiStart) / agiRatio);
        }
        else {
            // Landscape
            agiY = 200 - Math.round((touchXY.y / viewportManager.getHeight()) * 200);
            float agiWidth = (viewportManager.getHeight() * 1.32f);
            float agiRatio = (agiWidth / 160); 
            float agiStart = (1920 / 2) - (agiWidth / 2);
            agiX = Math.round((touchXY.x - agiStart) / agiRatio);
        }
        if ((agiX >= 0) && (agiX <= 159) && (agiY >= 0) && (agiY < 199)) {
            // Only if it is within the AGI Screen do we set the mouse vars.
            if (buttonDown) {
                gameScreen.getAgileRunner().getVariableData().setMouseButton(button + 1);
            } else {
                gameScreen.getAgileRunner().getVariableData().setMouseButton(0);
            }
            gameScreen.getAgileRunner().getVariableData().setMouseX(agiX);
            gameScreen.getAgileRunner().getVariableData().setMouseY(agiY);
        }
        
        agiMouseButton = (buttonDown? button + 1 : 0);
    }
    
    /**
     * Called when a finger was lifted or a mouse button was released. The button
     * parameter will be {@link Buttons#LEFT} on iOS.
     * 
     * @param pointer the pointer for the event.
     * @param button  the button
     * 
     * @return whether the input was processed
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Convert the screen coordinates to world coordinates.
        Vector2 touchXY = viewportManager.unproject(screenX, screenY);

        // Update AGI mouse variables (click/touch released, so clear all to 0)
        updateAGIMouse(touchXY, button, false);
        
        // Update the touch info for this pointer.
        TouchInfo touchInfo = null;
        if (pointer < MAX_SIMULTANEOUS_TOUCH_EVENTS) {
            touchInfo = touches[pointer];
            touchInfo.lastX = touchXY.x;
            touchInfo.lastY = touchXY.y;
            touchInfo.lastKey = null;
        }

        if (keyboardType.isInKeyboard(touchXY.x, touchXY.y)) {
            Integer keycode = keyboardType.getKeyCode(touchXY.x, touchXY.y);
            if (keycode != null) {
                processVirtualKeyboardKeyUp(keycode);
            }
        } else if (keyboardType.equals(KeyboardType.MOBILE_ON_SCREEN)) {
            // If the onscreen keyboard is being shown then if we receive a tap event, it
            // won't be
            // on the virtual keyboard but must therefore be outside it. So we hide the
            // keyboard.
            Gdx.input.setOnscreenKeyboardVisible(false);
            keyboardType = KeyboardType.OFF;

        } else if (!keyboardType.equals(KeyboardType.OFF)) {
            // If rendered keyboard is being shown, and the tap isn't within the keyboard,
            // but is
            // instead above the close height, then we close it.
            if (touchXY.y > keyboardType.getCloseHeight()) {
                keyboardType = KeyboardType.OFF;
            }

        } else {
            // TODO: Need to handle the magic numbers in this block in a better way.
            boolean keyboardClicked = false;
            boolean joystickClicked = false;
            boolean backArrowClicked = false;
            boolean fullScreenClicked = false;

            if (viewportManager.isPortrait()) {
                // Portrait.
                if (touchXY.y < 130) {
                    if (touchXY.x < 140) {
                        joystickClicked = true;

                    } else if (touchXY.x > (viewportManager.getWidth() - 145)) {
                        // If not Android, then right area is Back button.
                        if (Gdx.app.getType().equals(ApplicationType.Android)) {
                            keyboardClicked = true;
                        } else {
                            backArrowClicked = true;
                        }
                    } else {
                        // Mobile soft keyboard is only available in portrait mode (debug only)
                        int midWidth = (int) (viewportManager.getWidth() - viewportManager.getWidth() / 2);
                        if ((touchXY.x > (midWidth - 70)) && (touchXY.y < (midWidth + 70))) {
                            if (Gdx.app.getType().equals(ApplicationType.Android)) {
                                Gdx.input.setOnscreenKeyboardVisible(true);
                                keyboardType = KeyboardType.MOBILE_ON_SCREEN;
                            } else {
                                keyboardClicked = true;
                            }
                        }
                    }
                }
            } else {
                // Landscape.
                int screenTop = (int) viewportManager.getHeight();
                if (touchXY.y > (screenTop - 140)) {
                    if (touchXY.x < 140) {
                        joystickClicked = true;
                    } else if (touchXY.x > (viewportManager.getWidth() - 150)) {
                        fullScreenClicked = true;
                    }
                } else if (touchXY.y < 140) {
                    if (touchXY.x > (viewportManager.getWidth() - 150)) {
                        backArrowClicked = true;
                    } else if (touchXY.x < 140) {
                        keyboardClicked = true;
                    }
                }
            }

            if (keyboardClicked) {
                if (keyboardType.equals(KeyboardType.OFF)) {
                    keyboardType = (viewportManager.isPortrait() ? KeyboardType.PORTRAIT_LOWER_CASE : KeyboardType.LANDSCAPE_LOWER_CASE);
                    viewportManager.update();
                } else {
                    keyboardType = KeyboardType.OFF;
                }
            }

            if (joystickClicked) {
                joystickActive = !joystickActive;
            }
            
            if (fullScreenClicked) {
                Boolean fullScreen = Gdx.graphics.isFullscreen();
                if (fullScreen == true) {
                    Gdx.graphics.setWindowedMode(screenWidthBeforeFullScreen, screenHeightBeforeFullScreen);
                }
                else {
                    Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
                    screenWidthBeforeFullScreen = Gdx.graphics.getWidth();
                    screenHeightBeforeFullScreen = Gdx.graphics.getHeight();
                    Gdx.graphics.setFullscreenMode(currentMode);
                }
            }

            if (backArrowClicked) {
                dialogHandler.confirm("Are you sure you want to quit the game?", 
                        new ConfirmResponseHandler() {
                    @Override
                    public void yes() {
                        gameScreen.getAgileRunner().stop();
                    }
                    
                    @Override
                    public void no() {
                        // Nothing to do.
                    }
                });
            }
        }

        return true;
    }

    /**
     * Called whenever the mouse moves.
     * 
     * @param screenX 
     * @param screenY 
     */
    public boolean mouseMoved (int screenX, int screenY) {
        // Convert the screen coordinates to world coordinates.
        Vector2 touchXY = viewportManager.unproject(screenX, screenY);

        // Update AGI mouse variables.
        updateAGIMouse(touchXY, agiMouseButton - 1, (agiMouseButton > 0));
        
        return true;
    }
    
    /**
     * Called when a finger or the mouse was dragged.
     * 
     * @param pointer the pointer for the event.
     * 
     * @return whether the input was processed
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Convert the screen coordinates to world coordinates.
        Vector2 touchXY = viewportManager.unproject(screenX, screenY);
        
        // Update AGI mouse variables.
        updateAGIMouse(touchXY, agiMouseButton - 1, true);
        
        // Update the touch info for this pointer.
        TouchInfo touchInfo = null;
        if (pointer < MAX_SIMULTANEOUS_TOUCH_EVENTS) {
            touchInfo = touches[pointer];

            Integer lastKey = touchInfo.lastKey;
            Integer newKey = null;

            if (keyboardType.isInKeyboard(touchXY.x, touchXY.y)) {
                newKey = keyboardType.getKeyCode(touchXY.x, touchXY.y);
            }

            // If the drag has resulted in the position moving in to or out of a key, then
            // we simulate the corresponding key events.
            if ((lastKey != null) && ((newKey == null) || (newKey != lastKey))) {
                processVirtualKeyboardKeyUp(lastKey);
            }
            if ((newKey != null) && ((lastKey == null) || (lastKey != newKey))) {
                processVirtualKeyboardKeyDown(newKey);
            }

            // Finally we update the new last position and last key for this pointer.
            touchInfo.lastX = touchXY.x;
            touchInfo.lastY = touchXY.y;
            touchInfo.lastKey = newKey;
        }

        return true;
    }

    /**
     * Invokes by its MachineScreen when the screen has resized.
     * 
     * @param width  The new screen width.
     * @param height The new screen height.
     */
    public void resize(int width, int height) {
        if (keyboardType.isRendered()) {
            // Switch keyboard layout based on the orientation.
            keyboardType = (height > width ? KeyboardType.PORTRAIT_LOWER_CASE : KeyboardType.LANDSCAPE_LOWER_CASE);
        }
    }

    /**
     * Gets the current KeyboardType that is being used for input.
     * 
     * @return The current KeyboardType this is being used for input.
     */
    public KeyboardType getKeyboardType() {
        return keyboardType;
    }
    
    /**
     * Returns whether the joystick is active or not.
     * 
     * @return whether the joystick is active or not.
     */
    public boolean isJoystickActive() {
        return joystickActive;
    }
}
