package com.agifans.agile;

import com.agifans.agile.AnimatedObject.CycleType;
import com.agifans.agile.AnimatedObject.MotionType;
import com.agifans.agile.ScriptBuffer.ScriptBufferEvent;
import com.agifans.agile.agilib.Logic;
import com.agifans.agile.agilib.Logic.Action;
import com.agifans.agile.agilib.Logic.Condition;
import com.agifans.agile.agilib.Logic.GotoAction;
import com.agifans.agile.agilib.Logic.IfAction;
import com.agifans.agile.agilib.Picture;
import com.agifans.agile.agilib.Sound;
import com.agifans.agile.agilib.View;

/**
 * Performs the execution of an AGI Logic script.
 */
public class Commands {

    /**
     * The GameState class holds all of the data and state for the Game currently 
     * being run by the interpreter.
     */
    private GameState state;

    /**
     * Holds the data and state for the user input, i.e. keyboard and mouse input.
     */
    private UserInput userInput;

    /**
     * The pixels array for the AGI screen on which the background Picture and 
     * AnimatedObjects will be drawn to.
     */
    private short[] pixels;

    /**
     * Provides methods for drawing text on to the AGI screen.
     */
    private TextGraphics textGraphics;

    /**
     * Responsible for parsing the user input line to match known words
     */
    private Parser parser;

    /**
     * Responsible for displaying the inventory screen.
     */
    private Inventory inventory;

    /**
     * Responsible for displaying the menu system.
     */
    private Menu menu;

    /**
     * Responsible for saving and restoring saved game files.
     */
    private SavedGames savedGames;

    /**
     * Responsible for playing Sound resources.
     */
    private SoundPlayer soundPlayer;

    /**
     * Constructor for Commands.
     *
     * @param pixels 
     * @param state 
     * @param userInput 
     * @param textGraphics 
     * @param parser 
     * @param soundPlayer 
     * @param menu 
     */
    public Commands(short[] pixels, GameState state, UserInput userInput, TextGraphics textGraphics, Parser parser, SoundPlayer soundPlayer, Menu menu) {
        this.pixels = pixels;
        this.state = state;
        this.userInput = userInput;
        this.textGraphics = textGraphics;
        this.parser = parser;
        this.menu = menu;
        this.inventory = new Inventory(state, userInput, textGraphics, pixels);
        this.savedGames = new SavedGames(state, userInput, textGraphics, pixels);
        this.soundPlayer = soundPlayer;
    }

    /**
     * Draws the AGI Picture identified by the given picture number.
     *
     * @param pictureNum The number of the picture to draw.
     */
    private void drawPicture(int pictureNum) {
        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.DRAW_PIC, pictureNum);
        state.restoreBackgrounds();

        // We create a clone of the Picture so that is drawing state isn't persisted
        // back to the master list of pictures in the GameState.
        Picture picture = state.pictures[pictureNum].clone();
        picture.drawPicture();

        state.currentPicture = picture;

        updatePixelArrays();

        state.drawObjects();

        state.pictureVisible = false;
    }

    /**
     * Updates the Visual, Priority and Control pixel arrays with the bitmaps from the
     * current Picture.
     */
    private void updatePixelArrays() {
        Picture picture = state.currentPicture;

        int[] visualPixels = picture.getVisualPixels();

        // Copy the pixels to our VisualPixels array, doubling each one as we go.
        for (int i = 0, ii = 0; i < (160 * 168); i++, ii += 2) {
            // NOTE: Visual pixel array in JAGI is in RGB888 format
            short rgb565Color = EgaPalette.RGB888_TO_RGB565_MAP.get(visualPixels[i]);
            state.visualPixels[ii + 0] = rgb565Color;
            state.visualPixels[ii + 1] = rgb565Color;
        }
        
        splitPriorityPixels();
    }

    /**
     * Overlays an AGI Picture identified by the given picture number over the current picture.
     *
     * @param pictureNum 
     */
    private void overlayPicture(int pictureNum) {
        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.OVERLAY_PIC, pictureNum);
        state.restoreBackgrounds();

        // Draw the overlay picture on top of the current picture.
        Picture overlayPicture = state.pictures[pictureNum];
        state.currentPicture.overlayPicture(overlayPicture);
        
        updatePixelArrays();

        state.drawObjects();

        showVisualPixels();

        state.pictureVisible = false;
    }

    /**
     * For the current picture, sets the relevant pixels in the PriorityPixels and 
     * ControlPixels arrays in  the GameState. It determines the priority information for 
     * pixels that are overdrawn by control lines by the same method used in Sierra's 
     * interpreter. To quote the original AGI specs: "Control pixels still have a visual 
     * priority from 4 to 15. To accomplish this, AGI scans directly down the control 
     * priority until it finds some 'non-control' priority".
     */
    private void splitPriorityPixels() {
        Picture picture = state.currentPicture;
        int[] priorityPixels = picture.getPriorityPixels();
        
        for (int x = 0; x < 160; x++) {
            for (int y = 0; y < 168; y++) {
                // Shift left 7 + shift level 5 is a trick to avoid multiplying by 160.
                int index = (y << 7) + (y << 5) + x;
                int data = priorityPixels[index];

                if (data == 3) {
                    state.priorityPixels[index] = 3;
                    state.controlPixels[index] = data;
                }
                else if (data < 3) {
                    state.controlPixels[index] = data;

                    int dy = y + 1;
                    boolean priFound = false;

                    while (!priFound && (dy < 168)) {
                        data = priorityPixels[(dy << 7) + (dy << 5) + x];

                        if (data > 2) {
                            priFound = true;
                            state.priorityPixels[index] = data;
                        }
                        else {
                            dy++;
                        }
                    }
                }
                else {
                    state.controlPixels[index] = 4;
                    state.priorityPixels[index] = data;
                }
            }
        }
    }

    /**
     * Shows the current priority pixels and control pixels to screen.
     */
    public void showPriorityScreen() {
        short[] backPixels = new short[pixels.length];
        
        System.arraycopy(pixels, 0, backPixels, 0, pixels.length);
        
        for (int i = 0, ii = (8 * state.pictureRow) * 320; i < (160 * 168); i++, ii += 2) {
            int priColorIndex = state.priorityPixels[i];
            int ctrlColorIndex = state.controlPixels[i];
            short rgb565Color = EgaPalette.colours[ctrlColorIndex <= 3 ? ctrlColorIndex : priColorIndex];
            pixels[ii + 0] = rgb565Color;
            pixels[ii + 1] = rgb565Color;
        }

        userInput.waitForKey(true);

        System.arraycopy(backPixels, 0, pixels, 0, pixels.length);
    }

    /**
     * Blits the current VisualPixels array to the screen pixels array.
     */
    private void showVisualPixels() {
        // Perform the copy to the pixels array of the VisualPixels. This is where the PictureRow comes in to effect.
        System.arraycopy(state.visualPixels, 0, this.pixels, (8 * state.pictureRow) * 320, state.visualPixels.length);
    }

    /**
     * Implements the show.pic command. Blits the current VisualPixels array to the screen pixels 
     * array. If there is an open window, it will be closed by default.
     */
    private void showPicture() {
        showPicture(true);
    }
    
    /**
     * Implements the show.pic command. Blits the current VisualPixels array to the screen pixels 
     * array. If there is an open window, the closeWindow parameter determines when to close the
     * window.
     *
     * @param closeWindow Skips the closing of open windows if set to false.
     */
    private void showPicture(boolean closeWindow) {
        if (closeWindow) {
            // It is possible to leave the window up from the previous room, so we force a close.
            state.flags[Defines.LEAVE_WIN] = false;
            textGraphics.closeWindow(false);
        }

        // Perform the copy to the pixels array of the VisualPixels
        showVisualPixels();

        // Remember that the picture is now being displayed to the user.
        state.pictureVisible = true;
    }

    /**
     * Executes the shake.screen command. Implementation is based on the scummvm code.
     *
     * @param repeatCount The number of times to do the shake routine.
     */
    private void shakeScreen(int repeatCount) {
        int shakeCount = (repeatCount * 8);
        short backgroundRGB565 = EgaPalette.colours[0];
        short[] backPixels = new short[pixels.length];

        System.arraycopy(pixels, 0, backPixels, 0, pixels.length);
        
        for (int shakeNumber = 0; shakeNumber < shakeCount; shakeNumber++) {
            if ((shakeNumber & 1) == 1) {
                System.arraycopy(backPixels, 0, pixels, 0, pixels.length);
            }
            else {
                for (int y = 0, screenPos = 0; y < 200; y++) {
                    for (int x = 0; x < 320; x++, screenPos++) {
                        if ((x < 8) || (y < 4)) {
                            this.pixels[screenPos] = backgroundRGB565;
                        }
                        else {
                            this.pixels[screenPos] = backPixels[screenPos - 1288];
                        }
                    }
                }
            }
            try {
                Thread.sleep(66);
            } catch (InterruptedException e) {
                // Ignore.
            }
        }

        System.arraycopy(backPixels, 0, pixels, 0, pixels.length);
    }

    /**
     * Replays the events that happened in the ScriptBuffer. This would usually be called
     * immediately after restoring a saved game file, to do things such as add the add.to.pics,
     * draw the picture, show the picture, etc.
     */
    private void replayScriptEvents() {
        // Mainly for the AddToPicture method, since that adds script events if active.
        state.scriptBuffer.scriptOff();

        for (ScriptBufferEvent scriptBufferEvent : state.scriptBuffer.events) {
            switch (scriptBufferEvent.type) {
                case ADD_TO_PIC:
                    {
                        AnimatedObject picObj = new AnimatedObject(state, -1);
                        picObj.addToPicture(
                            (scriptBufferEvent.data[0] & 0xFF), 
                            (scriptBufferEvent.data[1] & 0xFF), 
                            (scriptBufferEvent.data[2] & 0xFF),
                            (scriptBufferEvent.data[3] & 0xFF), 
                            (scriptBufferEvent.data[4] & 0xFF), 
                            (scriptBufferEvent.data[5] & 0x0F),
                            ((scriptBufferEvent.data[5] >> 4) & 0x0F), 
                            pixels);
                        splitPriorityPixels();
                    }
                    break;

                case DISCARD_PIC:
                    {
                        Picture pic = state.pictures[scriptBufferEvent.resourceNumber];
                        if (pic != null) pic.isLoaded = false;
                    }
                    break;

                case DISCARD_VIEW:
                    {
                        View view = state.views[scriptBufferEvent.resourceNumber];
                        if (view != null) view.isLoaded = false;
                    }
                    break;

                case DRAW_PIC:
                    {
                        drawPicture(scriptBufferEvent.resourceNumber);
                    }
                    break;

                case LOAD_LOGIC:
                    {
                        Logic logic = state.logics[scriptBufferEvent.resourceNumber];
                        if (logic != null) logic.isLoaded = true;
                    }
                    break;

                case LOAD_PIC:
                    {
                        Picture pic = state.pictures[scriptBufferEvent.resourceNumber];
                        if (pic != null) pic.isLoaded = true;
                    }
                    break;

                case LOAD_SOUND:
                    {
                        Sound sound = state.sounds[scriptBufferEvent.resourceNumber];
                        if (sound != null)
                        {
                            soundPlayer.loadSound(sound);
                            sound.isLoaded = true;
                        }
                    }
                    break;

                case LOAD_VIEW:
                    {
                        View view = state.views[scriptBufferEvent.resourceNumber];
                        if (view != null) view.isLoaded = true;
                    }
                    break;

                case OVERLAY_PIC:
                    {
                        overlayPicture(scriptBufferEvent.resourceNumber);
                    }
                    break;
            }
        }

        state.scriptBuffer.scriptOn();
    }

    /**
     * Evaluates the given Condition.
     * 
     * @param condition The Condition to evaluate.
     * 
     * @return The result of evaluating the Condition; either true or false.
     */
    private boolean isConditionTrue(Condition condition) {
        boolean result = false;
        
        switch (condition.operation.opcode) {

            case 1: // equaln
                {
                    result = (state.vars[condition.operands.get(0).asByte()] == condition.operands.get(1).asByte());
                }
                break;

            case 2: // equalv
                {
                    result = (state.vars[condition.operands.get(0).asByte()] == state.vars[condition.operands.get(1).asByte()]);
                }
                break;

            case 3: // lessn
                {
                    result = (state.vars[condition.operands.get(0).asByte()] < condition.operands.get(1).asByte());
                }
                break;

            case 4: // lessv
                {
                    result = (state.vars[condition.operands.get(0).asByte()] < state.vars[condition.operands.get(1).asByte()]);
                }
                break;

            case 5: // greatern
                {
                    result = (state.vars[condition.operands.get(0).asByte()] > condition.operands.get(1).asByte());
                }
                break;

            case 6: // greaterv
                {
                    result = (state.vars[condition.operands.get(0).asByte()] > state.vars[condition.operands.get(1).asByte()]);
                }
                break;

            case 7: // isset
                {
                    result = state.flags[condition.operands.get(0).asByte()];
                }
                break;

            case 8: // issetv
                {
                    result = state.flags[state.vars[condition.operands.get(0).asByte()]];
                }
                break;

            case 9: // has
                {
                    result = (state.objects.objects.get(condition.operands.get(0).asByte()).room == Defines.CARRYING);
                }
                break;

            case 10: // obj.in.room
                {
                    result = (state.objects.objects.get(condition.operands.get(0).asByte()).room == state.vars[condition.operands.get(1).asByte()]);
                }
                break;

            case 11: // posn
                {
                    AnimatedObject aniObj = state.animatedObjects[condition.operands.get(0).asByte()];
                    int x1 = condition.operands.get(1).asByte();
                    int y1 = condition.operands.get(2).asByte();
                    int x2 = condition.operands.get(3).asByte();
                    int y2 = condition.operands.get(4).asByte();
                    result = ((aniObj.x >= x1) && (aniObj.y >= y1) && (aniObj.x <= x2) && (aniObj.y <= y2));
                }
                break;

            case 12: // controller
                {
                    result = state.controllers[condition.operands.get(0).asByte()];
                }
                break;

            case 13: // have.key
                {
                    int key = state.vars[Defines.LAST_CHAR];
                    if (key == 0) {
                        key = userInput.getKey();
                    }
                    if (key > 0) {
                        state.vars[Defines.LAST_CHAR] = (key & 0xFF);
                    }
                    result = (key != 0);
                }
                break;

            case 14: // said
                {
                    result = parser.said(condition.operands.get(0).asInts());
                }
                break;

            case 15: // compare.strings
                {
                    // Compare two strings. Ignore case, whitespace, and punctuation.
                    String str1 = state.strings[condition.operands.get(0).asByte()].toLowerCase().replaceAll("[ \t.,;:\'!-]", "");
                    String str2 = state.strings[condition.operands.get(1).asByte()].toLowerCase().replaceAll("[ \t.,;:\'!-]", "");
                    result = str1.equals(str2);
                }
                break;

            case 16: // obj.in.box
                {
                    AnimatedObject aniObj = state.animatedObjects[condition.operands.get(0).asByte()];
                    int x1 = condition.operands.get(1).asByte();
                    int y1 = condition.operands.get(2).asByte();
                    int x2 = condition.operands.get(3).asByte();
                    int y2 = condition.operands.get(4).asByte();
                    result = ((aniObj.x >= x1) && (aniObj.y >= y1) && ((aniObj.x + aniObj.xSize() - 1) <= x2) && (aniObj.y <= y2));
                }
                break;

            case 17: // center.posn
                {
                    AnimatedObject aniObj = state.animatedObjects[condition.operands.get(0).asByte()];
                    int x1 = condition.operands.get(1).asByte();
                    int y1 = condition.operands.get(2).asByte();
                    int x2 = condition.operands.get(3).asByte();
                    int y2 = condition.operands.get(4).asByte();
                    result = ((aniObj.x + (aniObj.xSize() / 2) >= x1) && (aniObj.y >= y1) && (aniObj.x + (aniObj.xSize() / 2) <= x2) && (aniObj.y <= y2));
                }
                break;

            case 18: // right.posn
                {
                    AnimatedObject aniObj = state.animatedObjects[condition.operands.get(0).asByte()];
                    int x1 = condition.operands.get(1).asByte();
                    int y1 = condition.operands.get(2).asByte();
                    int x2 = condition.operands.get(3).asByte();
                    int y2 = condition.operands.get(4).asByte();
                    result = (((aniObj.x + aniObj.xSize() - 1) >= x1) && (aniObj.y >= y1) && ((aniObj.x + aniObj.xSize() - 1) <= x2) && (aniObj.y <= y2));
                }
                break;

            case 0xfc: // OR
                {
                    result = false;
                    for (Condition orCondition : condition.operands.get(0).asConditions()) {
                        if (isConditionTrue(orCondition)) {
                            result = true;
                            break;
                        }
                    }
                }
                break;

            case 0xfd: // NOT
                {
                    result = !isConditionTrue(condition.operands.get(0).asCondition());
                }
                break;
        }

        return result;
    }

    /**
     * Executes the given Action command.
     * 
     * @param action The Action command to execute.
     * 
     * @return The index of the next Action to execute, or 0 to rescan logics from top, or -1 when at end of Logic.
     */
    private int executeAction(Action action) {
        // Normally the next Action will be the next one in the Actions list, but this
        // can be overwritten by the If and Goto actions.
        int nextActionNum = action.logic.addressToActionIndex.get(action.address) + 1;

        switch (action.operation.opcode) {
            case 0: // return
                return -1;

            case 1: // increment
                {
                    int varNum = action.operands.get(0).asByte();
                    if (state.vars[varNum] < 255) state.vars[varNum]++;
                }
                break;

            case 2: // decrement
                {
                    int varNum = action.operands.get(0).asByte();
                    if (state.vars[varNum] > 0) state.vars[varNum]--;
                }
                break;

            case 3: // assignn
                {
                    int varNum = action.operands.get(0).asByte();
                    int value = action.operands.get(1).asByte();
                    state.vars[varNum] = value;
                }
                break;

            case 4: // assignv
                {
                    int varNum1 = action.operands.get(0).asByte();
                    int varNum2 = action.operands.get(1).asByte();
                    state.vars[varNum1] = state.vars[varNum2];
                }
                break;

            case 5: // addn
                {
                    int varNum = action.operands.get(0).asByte();
                    int value = action.operands.get(1).asByte();
                    state.vars[varNum] += value;
                }
                break;

            case 6: // addv
                {
                    int varNum1 = action.operands.get(0).asByte();
                    int varNum2 = action.operands.get(1).asByte();
                    state.vars[varNum1] += state.vars[varNum2];
                }
                break;

            case 7: // subn
                {
                    int varNum = action.operands.get(0).asByte();
                    int value = action.operands.get(1).asByte();
                     state.vars[varNum] -= value;
                } 
                break;

            case 8: // subv
                {
                    int varNum1 = action.operands.get(0).asByte();
                    int varNum2 = action.operands.get(1).asByte();
                    state.vars[varNum1] -= state.vars[varNum2];
                }
                break;

            case 9: // lindirectv
                {
                    int varNum1 = action.operands.get(0).asByte();
                    int varNum2 = action.operands.get(1).asByte();
                    state.vars[state.vars[varNum1]] = state.vars[varNum2];
                }
                break;

            case 10: // rindirect
                {
                    int varNum1 = action.operands.get(0).asByte();
                    int varNum2 = action.operands.get(1).asByte();
                    state.vars[varNum1] = state.vars[state.vars[varNum2]];
                }
                break;

            case 11: // lindirectn
                {
                    int varNum = action.operands.get(0).asByte();
                    int value = action.operands.get(1).asByte();
                    state.vars[state.vars[varNum]] = value;
                }
                break;

            case 12: // set
                {
                    state.flags[action.operands.get(0).asByte()] = true;
                }
                break;

            case 13: // reset
                {
                    state.flags[action.operands.get(0).asByte()] = false;
                }
                break;

            case 14: // toggle
                {
                    int flagNum = action.operands.get(0).asByte();
                    state.flags[flagNum] = !state.flags[flagNum];
                }
                break;

            case 15: // set.v
                {
                    state.flags[state.vars[action.operands.get(0).asByte()]] = true;
                }
                break;

            case 16: // reset.v
                {
                    state.flags[state.vars[action.operands.get(0).asByte()]] = false;
                }
                break;

            case 17: // toggle.v
                {
                    int flagNum = state.vars[action.operands.get(0).asByte()];
                    state.flags[flagNum] = !state.flags[flagNum];
                }
                break;

            case 18: // new.room
                newRoom(action.operands.get(0).asByte());
                return 0;

            case 19: // new.room.v
                newRoom(state.vars[action.operands.get(0).asByte()]);
                return 0;

            case 20: // load.logics
                {
                    // All logics are already loaded in this interpreter, so nothing to do as such
                    // other than to remember it was "loaded".
                    Logic logic = state.logics[action.operands.get(0).asByte()];
                    if ((logic != null) && !logic.isLoaded) {
                        logic.isLoaded = true;
                        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.LOAD_LOGIC, logic.index);
                    }
                }
                break;

            case 21: // load.logics.v
                {
                    // All logics are already loaded in this interpreter, so nothing to do as such
                    // other than to remember it was "loaded".
                    Logic logic = state.logics[state.vars[action.operands.get(0).asByte()]];
                    if ((logic != null) && !logic.isLoaded) {
                        logic.isLoaded = true;
                        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.LOAD_LOGIC, logic.index);
                    }
                }
                break;

            case 22: // call
                {
                    if (executeLogic(action.operands.get(0).asByte())) {
                        // This means that a rescan from the top of Logic.0 should be done.
                        return 0;
                    }
                }
                break;

            case 23: // call.v
                {
                    if (executeLogic(state.vars[action.operands.get(0).asByte()])) {
                        // This means that a rescan from the top of Logic.0 should be done.
                        return 0;
                    }
                }
                break;

            case 24: // load.pic
                {
                    // All pictures are already loaded in this interpreter, so nothing to do as such
                    // other than to remember it was "loaded".
                    Picture pic = state.pictures[state.vars[action.operands.get(0).asByte()]];
                    if ((pic != null) && !pic.isLoaded) {
                        pic.isLoaded = true;
                        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.LOAD_PIC, pic.index);
                    }
                }
                break;

            case 25: // draw.pic
                {
                    drawPicture(state.vars[action.operands.get(0).asByte()]);
                }
                break;

            case 26: // show.pic
                {
                    showPicture();
                }
                break;

            case 27: // discard.pic
                {
                    // All pictures are kept loaded in this interpreter, so nothing to do as such
                    // other than to remember it was "unloaded".
                    Picture pic = state.pictures[state.vars[action.operands.get(0).asByte()]];
                    if ((pic != null) && pic.isLoaded) {
                        pic.isLoaded = false;
                        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.DISCARD_PIC, pic.index);
                    }
                }
                break;

            case 28: // overlay.pic
                {
                    overlayPicture(state.vars[action.operands.get(0).asByte()]);
                }
                break;

            case 29: // show.pri.screen
                {
                    showPriorityScreen();
                }
                break;

            case 30: // load.view
                {
                    // All views are already loaded in this interpreter, so nothing to do as such
                    // other than to remember it was "loaded".
                    View view = state.views[action.operands.get(0).asByte()];
                    if ((view != null) && !view.isLoaded) {
                        view.isLoaded = true;
                        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.LOAD_VIEW, view.index);
                    }
                }
                break;

            case 31: // load.view.v
                {
                    // All views are already loaded in this interpreter, so nothing to do as such
                    // other than to remember it was "loaded".
                    View view = state.views[state.vars[action.operands.get(0).asByte()]];
                    if ((view != null) && !view.isLoaded) {
                        view.isLoaded = true;
                        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.LOAD_VIEW, view.index);
                    }
                }
                break;

            case 32: // discard.view
                {
                    // All views are kept loaded in this interpreter, so nothing to do as such
                    // other than to remember it was "unloaded".
                    View view = state.views[action.operands.get(0).asByte()];
                    if ((view != null) && view.isLoaded) {
                        view.isLoaded = false;
                        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.DISCARD_VIEW, view.index);
                    }
                }
                break;

            case 33: // animate.obj
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.animate();
                }
                break;

            case 34: // unanimate.all
                {
                    state.restoreBackgrounds();
                    for (AnimatedObject aniObj : state.animatedObjects)
                    {
                        aniObj.animated = false;
                        aniObj.drawn = false;
                    }
                }
                break;

            case 35: // draw
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    if (!aniObj.drawn)
                    {
                        aniObj.update = true;
                        aniObj.findPosition();
                        aniObj.prevX = aniObj.x;
                        aniObj.prevY = aniObj.y;
                        aniObj.previousCel = aniObj.cel();
                        state.restoreBackgrounds(state.updateObjectList);
                        aniObj.drawn = true;
                        state.drawObjects(state.makeUpdateObjectList());
                        aniObj.show(pixels);
                        aniObj.noAdvance = false;
                    }
                }
                break;

            case 36: // erase
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    state.restoreBackgrounds(state.updateObjectList);
                    if (!aniObj.update)
                    {
                        state.restoreBackgrounds(state.stoppedObjectList);
                    }
                    aniObj.drawn = false;
                    if (!aniObj.update)
                    {
                        state.drawObjects(state.makeStoppedObjectList());
                    }
                    state.drawObjects(state.makeUpdateObjectList());
                    aniObj.show(pixels);
                }
                break;

            case 37: // position
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.x = aniObj.prevX = (short)action.operands.get(1).asByte();
                    aniObj.y = aniObj.prevY = (short)action.operands.get(2).asByte();
                }
                break;

            case 38: // position.v
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.x = aniObj.prevX = (short)state.vars[action.operands.get(1).asByte()];
                    aniObj.y = aniObj.prevY = (short)state.vars[action.operands.get(2).asByte()];
                }
                break;

            case 39: // get.posn
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    state.vars[action.operands.get(1).asByte()] = aniObj.x;
                    state.vars[action.operands.get(2).asByte()] = aniObj.y;
                }
                break;

            case 40: // reposition
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.reposition((byte)state.vars[action.operands.get(1).asByte()], (byte)state.vars[action.operands.get(2).asByte()]);
                }
                break;

            case 41: // set.view
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.setView(action.operands.get(1).asByte());
                }
                break;

            case 42: // set.view.v
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.setView(state.vars[action.operands.get(1).asByte()]);
                }
                break;

            case 43: // set.loop
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.setLoop(action.operands.get(1).asByte());
                }
                break;

            case 44: // set.loop.v
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.setLoop(state.vars[action.operands.get(1).asByte()]);
                }
                break;

            case 45: // fix.loop
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.fixedLoop = true;
                }
                break;

            case 46: // release.loop
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.fixedLoop = false;
                }
                break;

            case 47: // set.cel
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.setCel(action.operands.get(1).asByte());
                    aniObj.noAdvance = false;
                }
                break;

            case 48: // set.cel.v
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.setCel(state.vars[action.operands.get(1).asByte()]);
                    aniObj.noAdvance = false;
                }
                break;

            case 49: // last.cel
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    state.vars[action.operands.get(1).asByte()] = (aniObj.numberOfCels() - 1);
                }
                break;

            case 50: // current.cel
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    state.vars[action.operands.get(1).asByte()] = aniObj.currentCel;
                }
                break;

            case 51: // current.loop
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    state.vars[action.operands.get(1).asByte()] = aniObj.currentLoop;
                }
                break;

            case 52: // current.view
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    state.vars[action.operands.get(1).asByte()] = aniObj.currentView;
                }
                break;

            case 53: // number.of.loops
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    state.vars[action.operands.get(1).asByte()] = aniObj.numberOfLoops();
                }
                break;

            case 54: // set.priority
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.fixedPriority = true;
                    aniObj.priority = (byte)action.operands.get(1).asByte();
                }
                break;

            case 55: // set.priority.v
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.fixedPriority = true;
                    aniObj.priority = (byte)state.vars[action.operands.get(1).asByte()];
                }
                break;

            case 56: // release.priority
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.fixedPriority = false;
                }
                break;

            case 57: // get.priority
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    state.vars[action.operands.get(1).asByte()] = aniObj.priority;
                }
                break;

            case 58: // stop.update
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    if (aniObj.update)
                    {
                        state.restoreBackgrounds();
                        aniObj.update = false;
                        state.drawObjects();
                    }
                }
                break;

            case 59: // start.update
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    if (!aniObj.update)
                    {
                        state.restoreBackgrounds();
                        aniObj.update = true;
                        state.drawObjects();
                    }
                }
                break;

            case 60: // force.update
                {
                    // Although this command has a parameter, it seems to get ignored. Instead
                    // every AnimatedObject is redrawn and blitted to the screen.
                    state.restoreBackgrounds();
                    state.drawObjects();
                    state.showObjects(pixels);
                }
                break;

            case 61: // ignore.horizon
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.ignoreHorizon = true;
                }
                break;

            case 62: // observe.horizon
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.ignoreHorizon = false;
                }
                break;

            case 63: // set.horizon
                {
                    state.horizon = action.operands.get(0).asByte();
                }
                break;

            case 64: // object.on.water
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.stayOnWater = true;
                }
                break;

            case 65: // object.on.land
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.stayOnLand = true;
                }
                break;

            case 66: // object.on.anything
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.stayOnLand = false;
                    aniObj.stayOnWater = false;
                }
                break;

            case 67: // ignore.objs
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.ignoreObjects = true;
                }
                break;

            case 68: // observe.objs
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.ignoreObjects = false;
                }
                break;

            case 69: // distance
                {
                    AnimatedObject aniObj1 = state.animatedObjects[action.operands.get(0).asByte()];
                    AnimatedObject aniObj2 = state.animatedObjects[action.operands.get(1).asByte()];
                    state.vars[action.operands.get(2).asByte()] = aniObj1.distance(aniObj2);
                }
                break;

            case 70: // stop.cycling
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.cycle = false;
                }
                break;

            case 71: // start.cycling
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.cycle = true;
                }
                break;

            case 72: // normal.cycle
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.cycleType = CycleType.NORMAL;
                    aniObj.cycle = true;
                }
                break;

            case 73: // end.of.loop
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    int flagNum = action.operands.get(1).asByte();
                    aniObj.cycleType = CycleType.END_LOOP;
                    aniObj.update = true;
                    aniObj.cycle = true;
                    aniObj.noAdvance = true;
                    aniObj.motionParam1 = (short)flagNum;
                    state.flags[flagNum] = false;
                }
                break;

            case 74: // reverse.cycle
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.cycleType = CycleType.REVERSE;
                    aniObj.cycle = true;
                }
                break;

            case 75: // reverse.loop
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    int flagNum = action.operands.get(1).asByte();
                    aniObj.cycleType = CycleType.REVERSE_LOOP;
                    aniObj.update = true;
                    aniObj.cycle = true;
                    aniObj.noAdvance = true;
                    aniObj.motionParam1 = (short)flagNum;
                    state.flags[flagNum] = false;
                }
                break;

            case 76: // cycle.time
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.cycleTimeCount = aniObj.cycleTime = state.vars[action.operands.get(1).asByte()];
                }
                break;

            case 77: // stop.motion
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.direction = 0;
                    aniObj.motionType = MotionType.NORMAL;
                    if (aniObj == state.ego)
                    {
                        state.vars[Defines.EGODIR] = 0;
                        state.userControl = false;
                    }
                }
                break;

            case 78: // start.motion
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.motionType = MotionType.NORMAL;
                    if (aniObj == state.ego)
                    {
                        state.vars[Defines.EGODIR] = 0;
                        state.userControl = true;
                    }
                }
                break;

            case 79: // step.size
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.stepSize = state.vars[action.operands.get(1).asByte()];
                }
                break;

            case 80: // step.time
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.stepTimeCount = aniObj.stepTime = state.vars[action.operands.get(1).asByte()];
                }
                break;

            case 81: // move.obj
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.startMoveObj(
                        action.operands.get(1).asByte(), action.operands.get(2).asByte(), 
                        action.operands.get(3).asByte(), action.operands.get(4).asByte());
                }
                break;

            case 82: // move.obj.v
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.startMoveObj(
                        state.vars[action.operands.get(1).asByte()], state.vars[action.operands.get(2).asByte()],
                        state.vars[action.operands.get(3).asByte()], action.operands.get(4).asByte());
                }
                break;

            case 83: // follow.ego
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.startFollowEgo(action.operands.get(1).asByte(), action.operands.get(2).asByte());
                }
                break;

            case 84: // wander
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.startWander();
                }
                break;

            case 85: // normal.motion
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.motionType = MotionType.NORMAL;
                }
                break;

            case 86: // set.dir
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.direction = (byte)state.vars[action.operands.get(1).asByte()];
                }
                break;

            case 87: // get.dir
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    state.vars[action.operands.get(1).asByte()] = aniObj.direction;
                }
                break;

            case 88: // ignore.blocks
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.ignoreBlocks = true;
                }
                break;

            case 89: // observe.blocks
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.ignoreBlocks = false;
                }
                break;

            case 90: // block
                {
                    state.blocking = true;
                    state.blockUpperLeftX = (short)action.operands.get(0).asByte();
                    state.blockUpperLeftY = (short)action.operands.get(1).asByte();
                    state.blockLowerRightX = (short)action.operands.get(2).asByte();
                    state.blockLowerRightY = (short)action.operands.get(3).asByte();
                }
                break;

            case 91: // unblock
                {
                    state.blocking = false;
                }
                break;

            case 92: // get
                {
                    state.objects.objects.get(action.operands.get(0).asByte()).room = Defines.CARRYING;
                }
                break;

            case 93: // get.v
                {
                    state.objects.objects.get(state.vars[action.operands.get(0).asByte()]).room = Defines.CARRYING;
                }
                break;

            case 94: // drop
                {
                    state.objects.objects.get(action.operands.get(0).asByte()).room = Defines.LIMBO;
                }
                break;

            case 95: // put
                {
                    state.objects.objects.get(action.operands.get(0).asByte()).room = state.vars[action.operands.get(1).asByte()];
                }
                break;

            case 96: // put.v
                {
                    state.objects.objects.get(state.vars[action.operands.get(0).asByte()]).room = state.vars[action.operands.get(1).asByte()];
                }
                break;

            case 97: // get.room.v
                {
                    state.vars[action.operands.get(1).asByte()] = state.objects.objects.get(state.vars[action.operands.get(0).asByte()]).room;
                }
                break;

            case 98: // load.sound
                {
                    // All sounds are already loaded in this interpreter, so nothing to do as such
                    // other than to remember it was "loaded".
                    int soundNum = action.operands.get(0).asByte();
                    Sound sound = state.sounds[soundNum];
                    if ((sound != null) && !sound.isLoaded)
                    {
                        soundPlayer.loadSound(sound);
                        sound.isLoaded = true;
                        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.LOAD_SOUND, sound.index);
                    }
                }
                break;

            case 99: // sound
                {
                    int soundNum = action.operands.get(0).asByte();
                    int endFlag = action.operands.get(1).asByte();
                    state.flags[endFlag] = false;
                    Sound sound = state.sounds[soundNum];
                    if ((sound != null) && (sound.isLoaded))
                    {
                        this.soundPlayer.playSound(sound, endFlag);
                    }
                }
                break;

            case 100: // stop.sound
                {
                    this.soundPlayer.stopSound();
                }
                break;

            case 101: // print
                {
                    this.textGraphics.print(action.logic.messages.get(action.operands.get(0).asByte()));
                }
                break;

            case 102: // print.v
                {
                    this.textGraphics.print(action.logic.messages.get(state.vars[action.operands.get(0).asByte()]));
                }
                break;

            case 103: // display
                {
                    int row = action.operands.get(0).asByte();
                    int col = action.operands.get(1).asByte();
                    String message = action.logic.messages.get(action.operands.get(2).asByte());
                    this.textGraphics.display(message, row, col);
                }
                break;

            case 104: // display.v
                {
                    int row = state.vars[action.operands.get(0).asByte()];
                    int col = state.vars[action.operands.get(1).asByte()];
                    String message = action.logic.messages.get(state.vars[action.operands.get(2).asByte()]);
                    this.textGraphics.display(message, row, col);
                }
                break;

            case 105: // clear.lines
                {
                    int colour = textGraphics.makeBackgroundColour(action.operands.get(2).asByte());
                    textGraphics.clearLines(action.operands.get(0).asByte(), action.operands.get(1).asByte(), colour);
                }
                break;

            case 106: // text.screen
                {
                    textGraphics.textScreen();
                }
                break;

            case 107: // graphics
                {
                    textGraphics.graphicsScreen();
                }
                break;

            case 108: // set.cursor.char
                {
                    String cursorStr = action.logic.messages.get(action.operands.get(0).asByte());
                    state.cursorCharacter = (cursorStr.length() > 0? cursorStr.charAt(0) : (char)0);
                }
                break;

            case 109: // set.text.attribute
                {
                    textGraphics.setTextAttribute(action.operands.get(0).asByte(), action.operands.get(1).asByte());
                }
                break;

            case 110: // shake.screen
                {
                    shakeScreen(action.operands.get(0).asByte());
                }
                break;

            case 111: // configure.screen
                {
                    state.pictureRow = action.operands.get(0).asByte();
                    state.inputLineRow = action.operands.get(1).asByte();
                    state.statusLineRow = action.operands.get(2).asByte();
                }
                break;

            case 112: // status.line.on
                {
                    state.showStatusLine = true;
                    textGraphics.clearLines(state.statusLineRow, state.statusLineRow, 15);
                    textGraphics.updateStatusLine();
                }
                break;

            case 113: // status.line.off
                {
                    state.showStatusLine = false;
                    textGraphics.clearLines(state.statusLineRow, state.statusLineRow, 0);
                }
                break;

            case 114: // set.string
                {
                    state.strings[action.operands.get(0).asByte()] = action.logic.messages.get(action.operands.get(1).asByte());
                }
                break;

            case 115: // get.string
                {
                    textGraphics.getString(action.operands.get(0).asByte(), action.logic.messages.get(action.operands.get(1).asByte()),
                        action.operands.get(2).asByte(), action.operands.get(3).asByte(), action.operands.get(4).asByte());
                }
                break;

            case 116: // word.to.string
                {
                    state.strings[action.operands.get(0).asByte()] = state.recognisedWords.get(action.operands.get(1).asByte());
                }
                break;

            case 117: // parse
                {
                    parser.parseString(action.operands.get(0).asByte());
                }
                break;

            case 118: // get.num
                {
                    state.vars[action.operands.get(1).asByte()] = textGraphics.getNum(action.logic.messages.get(action.operands.get(0).asByte()));
                }
                break;

            case 119: // prevent.input
                {
                    state.acceptInput = false;
                    textGraphics.updateInputLine();
                }
                break;

            case 120: // accept.input
                {
                    state.acceptInput = true;
                    textGraphics.updateInputLine();
                }
                break;

            case 121: // set.key
                {
                    int keyCode = (action.operands.get(0).asByte() + (action.operands.get(1).asByte() << 8));
                    if (userInput.keyCodeMap.containsKey(keyCode))
                    {
                        int controllerNum = action.operands.get(2).asByte();
                        int interKeyCode = userInput.keyCodeMap.get(keyCode);
                        if (state.keyToControllerMap.containsKey(interKeyCode))
                        {
                            state.keyToControllerMap.remove(interKeyCode);
                        }
                        state.keyToControllerMap.put(userInput.keyCodeMap.get(keyCode), controllerNum);
                    }
                }
                break;

            case 122: // add.to.pic
                {
                    AnimatedObject picObj = new AnimatedObject(state, -1);
                    picObj.addToPicture(
                        action.operands.get(0).asByte(), action.operands.get(1).asByte(), action.operands.get(2).asByte(), 
                        action.operands.get(3).asByte(), action.operands.get(4).asByte(), action.operands.get(5).asByte(), 
                        action.operands.get(6).asByte(), pixels);
                    splitPriorityPixels();
                    picObj.show(pixels);
                }
                break;

            case 123: // add.to.pic.v
                {
                    AnimatedObject picObj = new AnimatedObject(state, -1);
                    picObj.addToPicture(
                        state.vars[action.operands.get(0).asByte()], state.vars[action.operands.get(1).asByte()], 
                        state.vars[action.operands.get(2).asByte()], state.vars[action.operands.get(3).asByte()], 
                        state.vars[action.operands.get(4).asByte()], state.vars[action.operands.get(5).asByte()],
                        state.vars[action.operands.get(6).asByte()], pixels);
                    splitPriorityPixels();
                }
                break;

            case 124: // status
                {
                    inventory.showInventoryScreen();
                }
                break;

            case 125: // save.game
                {
                    savedGames.saveGameState();
                }
                break;

            case 126: // restore.game
                {
                    if (savedGames.restoreGameState())
                    {
                        soundPlayer.reset();
                        menu.enableAllMenus();
                        replayScriptEvents();
                        showPicture(false);
                        textGraphics.updateStatusLine();
                        return 0;
                    }
                }
                break;

            case 127: // init.disk
                {
                    // No need to implement this. 
                }
                break;

            case 128: // restart.game
                {
                    if (state.flags[Defines.NO_PRMPT_RSTRT] || textGraphics.windowPrint("Press ENTER to restart\nthe game.\n\nPress ESC to continue\nthis game."))
                    {
                        soundPlayer.reset();
                        state.init();
                        state.flags[Defines.RESTART] = true;
                        menu.enableAllMenus();
                        textGraphics.clearLines(0, 24, 0);
                        return 0;
                    }
                }
                break;

            case 129: // show.obj
                {
                    inventory.showInventoryObject(action.operands.get(0).asByte());
                }
                break;

            case 130: // random.num
                {
                    int minVal = action.operands.get(0).asByte();
                    int maxVal = action.operands.get(1).asByte();
                    state.vars[action.operands.get(2).asByte()] = (((state.random.nextInt(255) % (maxVal - minVal + 1)) + minVal) & 0xFF);
                }
                break;

            case 131: // program.control
                {
                    state.userControl = false;
                }
                break;

            case 132: // player.control
                {
                    state.userControl = true;
                    state.ego.motionType = MotionType.NORMAL;
                }
                break;

            case 133: // obj.status.v
                {
                    AnimatedObject aniObj = state.animatedObjects[state.vars[action.operands.get(0).asByte()]];
                    textGraphics.windowPrint(aniObj.getStatusStr());
                }
                break;

            case 134: // quit
                {
                    int quitAction = (action.operands.size() == 0 ? 1 : action.operands.get(0).asByte());
                    if ((quitAction == 1) || textGraphics.windowPrint("Press ENTER to quit.\nPress ESC to keep playing."))
                    {
                        soundPlayer.shutdown();
                        QuitAction.exit();
                    }
                }
                break;

            case 135: // show.mem
                {
                    // No need to implement this.
                }
                break;

            case 136: // pause
                {
                    // Note: In the original AGI interpreter, pause stopped sound rather than pause
                    soundPlayer.stopSound();
                    this.textGraphics.print("      Game paused.\nPress Enter to continue.");
                }
                break;

            case 137: // echo.line
                {
                    if (state.currentInput.length() < state.lastInput.length())
                    {
                        state.currentInput.append(state.lastInput.substring(state.currentInput.length()));
                    }
                }
                break;

            case 138: // cancel.line
                {
                    state.currentInput.setLength(0);
                }
                break;

            case 139: // init.joy
                {
                    // No need to implement this.
                }
                break;

            case 140: // toggle.monitor
                {
                    // No need to implement this.
                }
                break;

            case 141: // version
                {
                    this.textGraphics.print("Adventure Game Interpreter\n      Version " + state.version);
                }
                break;

            case 142: // script.size
                {
                    state.scriptBuffer.setScriptSize(action.operands.get(0).asByte());
                }
                break;

            // --------------------------------------------------------------------------------------------------
            // ---- AGI version 2.001 in effect ended here. It did have a 143 and 144 but they were different ---
            // --------------------------------------------------------------------------------------------------

            case 143: // set.game.id (was max.drawn in AGI v2.001)
                {
                    state.gameId = action.logic.messages.get(action.operands.get(0).asByte());
                }
                break;

            case 144: // log
                {
                    // No need to implement this.
                }
                break;

            case 145: // set.scan.start
                {
                    state.scanStart[action.logic.index] = action.getActionNumber() + 1;
                }
                break;

            case 146: // reset.scan.start
                {
                    state.scanStart[action.logic.index] = 0;
                }
                break;

            case 147: // reposition.to
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.x = (short)action.operands.get(1).asByte();
                    aniObj.y = (short)action.operands.get(2).asByte();
                    aniObj.repositioned = true;
                    aniObj.findPosition();         // Make sure that this position is OK.
                }
                break;

            case 148: // reposition.to.v
                {
                    AnimatedObject aniObj = state.animatedObjects[action.operands.get(0).asByte()];
                    aniObj.x = (short)state.vars[action.operands.get(1).asByte()];
                    aniObj.y = (short)state.vars[action.operands.get(2).asByte()];
                    aniObj.repositioned = true;
                    aniObj.findPosition();         // Make sure that this position is OK.
                }
                break;

            case 149: // trace.on
                {
                    // No need to implement this.
                }
                break;

            case 150: // trace.info
                {
                    // No need to implement this.
                }
                break;

            case 151: // print.at
                {
                    String message = action.logic.messages.get(action.operands.get(0).asByte());
                    int row = action.operands.get(1).asByte();
                    int col = action.operands.get(2).asByte();
                    int width = action.operands.get(3).asByte();
                    this.textGraphics.printAt(message, row, col, width);
                }
                break;

            case 152: // print.at.v
                {
                    String message = action.logic.messages.get(state.vars[action.operands.get(0).asByte()]);
                    int row = action.operands.get(1).asByte();
                    int col = action.operands.get(2).asByte();
                    int width = action.operands.get(3).asByte();
                    this.textGraphics.printAt(message, row, col, width);
                }
                break;

            case 153: // discard.view.v
                {
                    // All views are kept loaded in this interpreter, so nothing to do as such
                    // other than to remember it was "unloaded".
                    View view = state.views[state.vars[action.operands.get(0).asByte()]];
                    if ((view != null) && view.isLoaded)
                    {
                        view.isLoaded = false;
                        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.DISCARD_VIEW, view.index);
                    }
                }
                break;

            case 154: // clear.text.rect
                {
                    int top = action.operands.get(0).asByte();
                    int left = action.operands.get(1).asByte();
                    int bottom = action.operands.get(2).asByte();
                    int right = action.operands.get(3).asByte();
                    int colour = textGraphics.makeBackgroundColour(action.operands.get(4).asByte());
                    textGraphics.clearRect(top, left, bottom, right, colour);
                }
                break;

            case 155: // set.upper.left
                {
                    // Only used on the Apple. No need to implement.
                }
                break;

            // --------------------------------------------------------------------------------------------------
            // ---- AGI version 2.089 ends with command 155 above, i.e before the menu system was introduced ----
            // --------------------------------------------------------------------------------------------------

            case 156: // set.menu
                {
                    menu.setMenu(action.logic.messages.get(action.operands.get(0).asByte()));
                }
                break;

            case 157: // set.menu.item
                {
                    String menuItemName = action.logic.messages.get(action.operands.get(0).asByte());
                    byte controllerNum = (byte)action.operands.get(1).asByte();
                    menu.setMenuItem(menuItemName, controllerNum);
                }
                break;

            case 158: // submit.menu
                {
                    menu.submitMenu();
                }
                break;

            case 159: // enable.item
                {
                    menu.enableItem(action.operands.get(0).asByte());
                }
                break;

            case 160: // disable.item
                {
                    menu.disableItem(action.operands.get(0).asByte());
                }
                break;

            case 161: // menu.input
                {
                    state.menuOpen = true;
                }
                break;

            // -------------------------------------------------------------------------------------------------
            // ---- AGI version 2.272 ends with command 161 above, i.e after the menu system was introduced ----
            // -------------------------------------------------------------------------------------------------

            case 162: // show.obj.v
                {
                    inventory.showInventoryObject(state.vars[action.operands.get(0).asByte()]);
                }
                break;

            case 163: // open.dialogue
                {
                    // Appears to be something specific to monochrome. No need to implement.
                }
                break;

            case 164: // close.dialogue
                {
                    // Appears to be something specific to monochrome. No need to implement.
                }
                break;

            case 165: // mul.n
                {
                    int varNum = action.operands.get(0).asByte();
                    int value = action.operands.get(1).asByte();
                    state.vars[varNum] *= value;
                }
                break;

            case 166: // mul.v
                {
                    int varNum1 = action.operands.get(0).asByte();
                    int varNum2 = action.operands.get(1).asByte();
                    state.vars[varNum1] *= state.vars[varNum2];
                }
                break;

            case 167: // div.n
                {
                    int varNum = action.operands.get(0).asByte();
                    int value = action.operands.get(1).asByte();
                    state.vars[varNum] /= value;
                }
                break;

            case 168: // div.v
                {
                    int varNum1 = action.operands.get(0).asByte();
                    int varNum2 = action.operands.get(1).asByte();
                    state.vars[varNum1] /= state.vars[varNum2];
                }
                break;

            case 169: // close.window
                {
                    textGraphics.closeWindow();
                }
                break;

            case 170: // set.simple (i.e. simpleName variable for saved games)
                {
                    state.simpleName = action.logic.messages.get(action.operands.get(0).asByte());
                }
                break;

            case 171: // push.script
                {
                    state.scriptBuffer.pushScript();
                }
                break;

            case 172: // pop.script
                {
                    state.scriptBuffer.popScript();
                }
                break;

            case 173: // hold.key
                {
                    state.holdKey = true;
                }
                break;

            // --------------------------------------------------------------------------------------------------
            // ---- AGI version 2.915/2.917 ends with command 173 above                                      ----
            // --------------------------------------------------------------------------------------------------

            case 174: // set.pri.base
                {
                    state.priorityBase = action.operands.get(0).asByte();
                }
                break;

            case 175: // discard.sound
                {
                    // Note: Interpreter 2.936 doesn't persist discard sound to the script event buffer.
                }
                break;

            // --------------------------------------------------------------------------------------------------
            // ---- AGI version 2.936 ends with command 175 above                                            ----
            // --------------------------------------------------------------------------------------------------

            case 176: // hide.mouse
                {
                    // This command isn't supported by PC versions of original AGI Interpreter.
                }
                break;

            case 177: // allow.menu
                {
                    state.menuEnabled = (action.operands.get(0).asByte() != 0);
                }
                break;

            case 178: // show.mouse
                {
                    // This command isn't supported by PC versions of original AGI Interpreter.
                }
                break;

            case 179: // fence.mouse
                {
                    // This command isn't supported by PC versions of original AGI Interpreter.
                }
                break;

            case 180: // mouse.posn
                {
                    // This command isn't supported by PC versions of original AGI Interpreter.
                }
                break;

            case 181: // release.key
                {
                    state.holdKey = false;
                }
                break;

            case 182: // adj.ego.move.to.x.y
                {
                    // This command isn't supported by PC versions of original AGI Interpreter.
                }
                break;

            case 0xfe: // Unconditional branch: else, goto.
                {
                    nextActionNum = ((GotoAction)action).getDestinationActionIndex();
                }
                break;

            case 0xff: // Conditional branch: if.
                {
                    for (Condition condition : action.operands.get(0).asConditions()) {
                        if (!isConditionTrue(condition)) {
                            nextActionNum = ((IfAction)action).getDestinationActionIndex();
                            break;
                        }
                    }
                }
                break;

            default:    // Error has occurred
                break;
        }

        return nextActionNum;
    }

    /**
     * Executes the Logic identified by the given logic number.
     *
     * @param logicNum The number of the Logic to execute.
     * 
     * @return true if logics should be rescanned from the top (i.e. top of Logic 0); otherwise false.
     */
    public boolean executeLogic(int logicNum) {
        // Remember the previous Logic number.
        int previousLogNum = state.currentLogNum;

        // Store the new Logic number in the state so that actions will know this.
        state.currentLogNum = logicNum;

        // Prepare to start executing the Logic.
        Logic logic = state.logics[logicNum];
        int actionNum = state.scanStart[logicNum];

        // Continually execute the Actions in the Logic until one of them tells us to exit.
        do actionNum = executeAction(logic.actions.get(actionNum)); while (actionNum > 0);

        // Restore the previous Logic number before we leave.
        state.currentLogNum = previousLogNum;

        // If ExecuteAction return 0, then it means that a newroom, restore or restart is 
        // happening. In those cases, we need to immediately rescan logics from the top of Logic.0
        return (actionNum == 0);
    }

    /**
     * Performs all the necessary updates to vars, flags, animated objects, controllers, 
     * and other state to prepare for entry in to the next room.
     * 
     * @param roomNum 
     */
    private void newRoom(int roomNum) {
        // Simulate a slow room change if there is a text window open.
        if (textGraphics.isWindowOpen()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        // Turn off sound.
        soundPlayer.reset();

        // Clear the script event buffer ready for next room.
        state.scriptBuffer.initScript();
        state.scriptBuffer.scriptOn();

        // Resets the Logics, Views, Pictures and Sounds back to new room state.
        state.resetResources();

        // Carry over ego's view number.
        // TODO: For some reason in MH2, the ego View can be null at this point. Needs investigation to determine why.
        if (state.ego.view() != null) {
            state.vars[Defines.CURRENT_EGO] = (state.ego.view().index & 0xFF);
        }

        // Reset state for all animated objects.
        for (AnimatedObject aniObj : state.animatedObjects) aniObj.reset();

        // Current room logic is loaded automatically on room change and not directly by load.logic
        Logic logic = state.logics[roomNum];
        logic.isLoaded = true;
        state.scriptBuffer.addScript(ScriptBuffer.ScriptBufferEventType.LOAD_LOGIC, logic.index);

        // If ego collided with a border, set his position in the new room to
        // the appropriate edge of the screen.
        switch (state.vars[Defines.EGOEDGE]) {
            case Defines.TOP:
                state.ego.y = Defines.MAXY;
                break;

            case Defines.RIGHT:
                state.ego.x = Defines.MINX;
                break;

            case Defines.BOTTOM:
                state.ego.y = Defines.HORIZON + 1;
                break;

            case Defines.LEFT:
                state.ego.x = (short)(Defines.MAXX + 1 - state.ego.xSize());
                break;
        }

        // Change the room number.
        state.vars[Defines.PREVROOM] = state.vars[Defines.CURROOM];
        state.vars[Defines.CURROOM] = roomNum;

        // Set flags and vars as appropriate for a new room.
        state.vars[Defines.OBJHIT] = 0;
        state.vars[Defines.OBJEDGE] = 0;
        state.vars[Defines.UNKNOWN_WORD] = 0;
        state.vars[Defines.EGOEDGE] = 0;
        state.flags[Defines.INPUT] = false;
        state.flags[Defines.INITLOGS] = true;
        state.userControl = true;
        state.blocking = false;
        state.horizon = Defines.HORIZON;
        state.clearControllers();

        // Draw the status line, if applicable.
        textGraphics.updateStatusLine();
    }
}
