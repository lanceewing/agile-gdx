package com.agifans.agile.ui;

import com.agifans.agile.GameScreen;
import com.agifans.agile.UserInput;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Application.ApplicationType;
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
     * The current alignment of the joystick on screen, if active, otherwise
     * set to the OFF value.
     */
    private JoystickAlignment joystickAlignment = JoystickAlignment.OFF;
    
    /**
     * The current offset from centre of the camera in the X direction.
     */
    private float cameraXOffset;
    
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
        // these up front and reuse them so as to avoid garbage collection.
        this.touches = new TouchInfo[MAX_SIMULTANEOUS_TOUCH_EVENTS];
        for (int i = 0; i < MAX_SIMULTANEOUS_TOUCH_EVENTS; i++) {
            touches[i] = new TouchInfo();
        }
    }
    
    /**
     * Resets anything that needs resetting between multiple game executions.
     */
    public void reset() {
        this.keyboardType = KeyboardType.OFF;
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
        } else {
            // Update AGI mouse variables.
            updateAGIMouse(touchXY, button, true);
        }

        return true;
    }
    
    private UserInput getUserInput() {
        return gameScreen.getAgileRunner().getUserInput();
    }
    
    private void processVirtualKeyboardKeyDown(int keycode) {
        if ((keycode != Keys.SWITCH_CHARSET) && (keycode != Keys.CAPS_LOCK)) {
            if (((keycode >> 8) & 0xFF) == Keys.SHIFT_LEFT) {
                getUserInput().keyDown(Keys.SHIFT_LEFT);
            }
            
            getUserInput().keyDown(keycode & 0xFF);
            
            if (KeyboardTypeData.KEYTYPED_CHAR_MAP.containsKey(keycode)) {
                getUserInput().keyTyped(KeyboardTypeData.KEYTYPED_CHAR_MAP.get(keycode));
            }
        }
    }
    
    private void processVirtualKeyboardKeyUp(int keycode) {
        if (keycode == Keys.SWITCH_CHARSET) {
            processSwitchCharacterSet();
        } else if (keycode == Keys.CAPS_LOCK) {
            processCapsLockToggle();
        } else {
            getUserInput().keyUp(keycode & 0xFF);
            
            if (((keycode >> 8) & 0xFF) == Keys.SHIFT_LEFT) {
                getUserInput().keyUp(Keys.SHIFT_LEFT);
            }
        }
    }
    
    private void processSwitchCharacterSet() {
        switch (keyboardType) {
            case PORTRAIT_LOWER_CASE:
            case PORTRAIT_UPPER_CASE:
                keyboardType = KeyboardType.PORTRAIT_PUNC_NUMBERS;
                break;
                
            case LANDSCAPE_LOWER_CASE:
            case LANDSCAPE_UPPER_CASE:
                keyboardType = KeyboardType.LANDSCAPE_PUNC_NUMBERS;
                break;
                
            case PORTRAIT_PUNC_NUMBERS:
                keyboardType = KeyboardType.PORTRAIT_LOWER_CASE;
                break;
                
            case LANDSCAPE_PUNC_NUMBERS:
                keyboardType = KeyboardType.LANDSCAPE_LOWER_CASE;
                break;
                
            default:
                break;
        }
        
        viewportManager.update();
    }
    
    private void processCapsLockToggle() {
        switch (keyboardType) {
            case PORTRAIT_LOWER_CASE:
                keyboardType = KeyboardType.PORTRAIT_UPPER_CASE;
                break;
                
            case PORTRAIT_UPPER_CASE:
                keyboardType = KeyboardType.PORTRAIT_LOWER_CASE;
                break;
                
            case LANDSCAPE_LOWER_CASE:
                keyboardType = KeyboardType.LANDSCAPE_UPPER_CASE;
                break;
                
            case LANDSCAPE_UPPER_CASE:
                keyboardType = KeyboardType.LANDSCAPE_LOWER_CASE;
                break;
                
            default:
                break;
        }
        
        viewportManager.update();
    }

    /**
     * Updates the AGI Mouse state. AGI Mouse is a fan made hack to the original AGI
     * interpreter that overrides AGI command 171, so that when it is invoked, certain
     * AGI variables are assigned the mouse X/Y position and button status.
     * 
     * @param touchXY
     * @param button
     * @param buttonDown
     * 
     * @return true if AGI mouse state was updated; otherwise false.
     */
    private boolean updateAGIMouse(Vector2 touchXY, int button, boolean buttonDown) {
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
            if ((viewportManager.getAgiScreenBase() > 0) || (viewportManager.getSidePaddingWidth() <= 64)) {
                // Landscape/Portrait hybrid mode.
                agiX = Math.round((touchXY.x / viewportManager.getWidth()) * 160);
                float agiHeight = (1920.0f / 1.32f);
                float agiRatio = (agiHeight / 200);
                float agiStart = (viewportManager.getHeight() - agiHeight);
                agiY = 200 - Math.round((touchXY.y - agiStart) / agiRatio);
            } else {
                agiY = 200 - Math.round((touchXY.y / viewportManager.getHeight()) * 200);
                float agiWidth = (viewportManager.getHeight() * 1.32f);
                float agiRatio = (agiWidth / 160);
                float agiStart = (1920 / 2) - (agiWidth / 2);
                float adjustedCameraXOffset = (cameraXOffset * (agiWidth / 264));
                agiX = (int)(Math.round(((touchXY.x + adjustedCameraXOffset) - agiStart) / agiRatio));
            }
        }
        
        boolean agiMouseUpdated = false;
        
        if ((agiX >= 0) && (agiX <= 159) && (agiY >= 0) && (agiY < 199)) {
            // Only if it is within the AGI Screen do we set the mouse vars.
            if (buttonDown) {
                gameScreen.getAgileRunner().getVariableData().setMouseButton(button + 1);
                // TODO: Commenting this out for now, as it causes issues with menu and keyboard overlay. Needs rethinking.
                //getUserInput().keyDown(Keys.ENTER);
            } else {
                gameScreen.getAgileRunner().getVariableData().setMouseButton(0);
            }
            gameScreen.getAgileRunner().getVariableData().setMouseX(agiX);
            gameScreen.getAgileRunner().getVariableData().setMouseY(agiY);
            agiMouseUpdated = true;
        }
        
        agiMouseButton = (buttonDown? button + 1 : 0);
        
        return agiMouseUpdated;
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
        }
        else {
            // Update AGI mouse variables (click/touch released, so clear all to 0)
            updateAGIMouse(touchXY, button, false);
            
            // TODO: Need to handle the magic numbers in this block in a better way.
            boolean keyboardClicked = false;
            boolean joystickClicked = false;
            boolean backArrowClicked = false;
            boolean fullScreenClicked = false;
    
            if (viewportManager.isPortrait()) {
                // Portrait.
                if (touchXY.y < 135) {
                    if (touchXY.x < 126) {
                        fullScreenClicked = true;
                    } else if (touchXY.x > (viewportManager.getWidth() - 126)) {
                        backArrowClicked = true;
                    } else {
                        float thirdPos = (viewportManager.getWidth() / 3);
                        float twoThirdPos = (viewportManager.getWidth() - (viewportManager.getWidth() / 3));
                        if ((touchXY.x > (thirdPos - 42)) && (touchXY.x < (thirdPos + 84))) {
                            joystickClicked = true;
                        }
                        else if ((touchXY.x > (twoThirdPos - 84)) && (touchXY.x < (twoThirdPos + 42))) {
                            keyboardClicked = true;
                        }
                    }
                }
            } else {
                // Landscape.
                int screenTop = (int) viewportManager.getHeight();
                if (cameraXOffset == 0) {
                    if ((viewportManager.getAgiScreenBase() > 0) || (viewportManager.getSidePaddingWidth() <= 64)) {
                        if (touchXY.y < 104) {
                            float leftAdjustment = (viewportManager.getWidth() / 4) - 32;
                            if ((touchXY.x >= ((viewportManager.getWidth() / 2) - 48) - leftAdjustment) && 
                                (touchXY.x <= ((viewportManager.getWidth() / 2) + 48) - leftAdjustment)) {
                                fullScreenClicked = true;
                            }
                            else 
                            if ((touchXY.x >= ((viewportManager.getWidth() - (viewportManager.getWidth() / 3)) - 64) - leftAdjustment) && 
                                (touchXY.x <= ((viewportManager.getWidth() - (viewportManager.getWidth() / 3)) + 32) - leftAdjustment)) {
                                joystickClicked = true;
                            }
                            else
                            if ((touchXY.x >= ((viewportManager.getWidth() - (viewportManager.getWidth() / 6)) - 80) - leftAdjustment) && 
                                (touchXY.x <= ((viewportManager.getWidth() - (viewportManager.getWidth() / 6)) + 16) - leftAdjustment)) {
                                keyboardClicked = true;
                            }
                            else
                            if ((touchXY.x >= (viewportManager.getWidth() - 112) - leftAdjustment) && 
                                (touchXY.x <= (viewportManager.getWidth() - 16) - leftAdjustment)) {
                                backArrowClicked = true;
                            }
                        }
                    } else {
                        // Screen in middle.
                        if (touchXY.y > (screenTop - 104)) {
                            if (touchXY.x < 112) {
                                joystickClicked = true;
                            } else if (touchXY.x > (viewportManager.getWidth() - 112)) {
                                fullScreenClicked = true;
                            }
                        } else if (touchXY.y < 104) {
                            if (touchXY.x > (viewportManager.getWidth() - 112)) {
                                backArrowClicked = true;
                            } else if (touchXY.x < 112) {
                                keyboardClicked = true;
                            }
                        }
                    }
                }
                else {
                    // All buttons on same side
                    if (((touchXY.x < 128) && (cameraXOffset < 0)) || 
                        ((touchXY.x > (viewportManager.getWidth() - 128)) && (cameraXOffset > 0))) {
                        if (touchXY.y > (screenTop - 128)) {
                            fullScreenClicked = true;
                        }
                        else if ((touchXY.y < (screenTop - 212)) && (touchXY.y > (screenTop - 340))) {
                            joystickClicked = true;
                        }
                        else if ((touchXY.y > 212) && (touchXY.y < 340)) {
                            keyboardClicked = true;
                        }
                        else if ((touchXY.y > 0) && (touchXY.y < 128)) {
                            backArrowClicked = true;
                        }
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
               // Rotate the joystick screen alignment.
               joystickAlignment = joystickAlignment.rotateValue();
               if (!viewportManager.isPortrait()) {
                   if (joystickAlignment.equals(JoystickAlignment.MIDDLE)) {
                       joystickAlignment = joystickAlignment.rotateValue();
                   }
                   if ((viewportManager.getAgiScreenBase() > 0) || (viewportManager.getSidePaddingWidth() <= 64)) {
                       if (joystickAlignment.equals(JoystickAlignment.LEFT)) {
                           joystickAlignment = joystickAlignment.rotateValue();
                       }
                   }
               }
            }
            
            if (fullScreenClicked) {
                Boolean fullScreen = Gdx.graphics.isFullscreen();
                if (fullScreen == true) {
                    switchOutOfFullScreen();
                }
                else {
                    switchIntoFullScreen();
                }
            }
    
            if (backArrowClicked) {
                if (Gdx.app.getType().equals(ApplicationType.Desktop) && Gdx.graphics.isFullscreen()) {
                    // Dialog won't show for desktop unless we exit full screen,
                    switchOutOfFullScreen();
                }
                
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
     * Switches to full screen mode, storing the width and height beforehand so 
     * that it can be restored when switching back.
     */
    public void switchIntoFullScreen() {
        keyboardType = KeyboardType.OFF;
        Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
        screenWidthBeforeFullScreen = Gdx.graphics.getWidth();
        screenHeightBeforeFullScreen = Gdx.graphics.getHeight();
        Gdx.graphics.setFullscreenMode(currentMode);
    }
    
    /**
     * Switches out of full screen mode back to the windowed mode, restoring the
     * saved width and height.
     */
    public void switchOutOfFullScreen() {
        if (screenWidthBeforeFullScreen > (screenHeightBeforeFullScreen * 1.32f)) {
            keyboardType = KeyboardType.OFF;
        }
        Gdx.graphics.setWindowedMode(screenWidthBeforeFullScreen, screenHeightBeforeFullScreen);
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
     * Invokes by its GameScreen when the screen has resized.
     * 
     * @param width  The new screen width.
     * @param height The new screen height.
     */
    public void resize(int width, int height) {
        if (height > (width / 1.32f)) {
            // Change to portrait if it is not already a portrait keyboard.
            if (!keyboardType.isPortrait()) {
                keyboardType = KeyboardType.PORTRAIT_LOWER_CASE;
            }
            
            // For non-standard portrait sizes, where the keyboard would overlap the
            // screen, we turn the keyboard off on resize.
            if (((float)height/width) < 1.77) {
                keyboardType = KeyboardType.OFF;
            }
        } else if (keyboardType.isRendered()) {
            // If it wasn't previously landscape, then turn it off.
            if (!keyboardType.isLandscape()) {
                keyboardType = KeyboardType.OFF;
            }
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
     * Gets the current joystick screen alignment, i.e. where to place it on the 
     * screen (left aligned, middle aligned, right aligned, or turned off)
     * 
     * @return The current joystick screen alignment.
     */
    public JoystickAlignment getJoystickAlignment() {
        return joystickAlignment;
    }
    
    /**
     * Sets the current joystick screen alignment, i.e. where to place it on the 
     * screen (left aligned, middle aligned, right aligned, or turned off)
     * 
     * @param joystickAlignment
     */
    public void setJoystickAlignment(JoystickAlignment joystickAlignment) {
        this.joystickAlignment = joystickAlignment;
    }

    public static enum JoystickAlignment {
        OFF, RIGHT, MIDDLE, LEFT;
        
        JoystickAlignment rotateValue() {
            return values()[(ordinal() + 1) % 4];
        }
    }

    /**
     * Sets the current offset from centre of the camera in the X direction.
     * 
     * @param cameraXOffset The current offset from centre of the camera in the X direction.
     */
    public void setCameraXOffset(float cameraXOffset) {
        this.cameraXOffset = cameraXOffset;
    }
}
