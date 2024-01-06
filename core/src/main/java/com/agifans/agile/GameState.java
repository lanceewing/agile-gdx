package com.agifans.agile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.agifans.agile.agilib.Game;
import com.agifans.agile.agilib.Logic;
import com.agifans.agile.agilib.Objects;
import com.agifans.agile.agilib.Picture;
import com.agifans.agile.agilib.Sound;
import com.agifans.agile.agilib.View;
import com.agifans.agile.agilib.Words;

/**
 * The GameState class holds all of the data and state for the Game currently 
 * being run by the interpreter.
 */
public class GameState {

    /**
     * The Game whose data we are interpreting.
     */
    private Game game;
    
    /**
     * The VariableData implementation to get and set the AGI variables through.
     */
    private VariableData variableData;

    public Logic[] logics;
    public Picture[] pictures;
    public View[] views;
    public Sound[] sounds;
    public Objects objects;
    public Words words;

    /**
     * Scan start values for each Logic. Index is the Logic number. We normally start 
     * scanning the Logic at position 0, but this can be set to another  value via the
     * set.scan.start AGI command. Note that only loaded logics can have their scan 
     * offset set. When they are unloaded, their scan offset is forgotten. Logic 0 is
     * always loaded, so its scan start is never forgotten.
     */
    public int[] scanStart;

    public boolean[] controllers;
    public boolean[] flags;
    public String[] strings;
    public AnimatedObject[] animatedObjects;
    public AnimatedObject ego;

    /**
     * The List of animated objects that currently have the DRAWN and UPDATE flags set.
     */
    public List<AnimatedObject> updateObjectList;

    /**
     * The List of animated objects that have the DRAWN flag set but not the UPDATE flag.
     */
    public List<AnimatedObject> stoppedObjectList;

    /**
     * A Map between a key event code and the matching controller number.
     */
    public Map<Integer, Integer> keyToControllerMap;

    /**
     * For making random decisions.
     */
    public Random random = new Random();

    /**
     * The Picture that is currently drawn, i.e. the last one for which a draw.pic() 
     * command was executed. This will be a clone of an instance in the Pictures array,
     * which may have subsequently had an overlay drawn on top of it.
     */
    public Picture currentPicture;

    /**
     * The pixel array for the visual data for the current Picture, where the values
     * are the ARGB values. The dimensions of this are 320x168, i.e. two pixels per 
     * AGI pixel. Makes it easier to copy to the main pixels array when required.
     */
    public int[] visualPixels;

    /**
     * The pixel array for the priority data for the current Picture, where the values
     * are from 4 to 15 (i.e. they are not ARGB values). The dimensions of this one
     * are 160x168 as its usage is non-visual.
     */
    public int[] priorityPixels;

    /**
     * The pixel array for the control line data for the current Picture, where the
     * values are from 0 to 4 (i.e. not ARGB values). The dimensions of this one
     * are 160x168 as its usage is non-visual.
     */
    public int[] controlPixels;

    /**
     * Whether or not the picture is currently visible. This is set to true after a
     * show.pic call. The draw.pic and overlay.pic commands both set it to false. It's
     * value is used to determine whether to render the AnimatedObjects.
     */
    public boolean pictureVisible;

    public boolean acceptInput;
    public boolean userControl;
    public boolean graphicsMode;
    public boolean showStatusLine;
    public int statusLineRow;
    public int pictureRow;
    public int inputLineRow;
    public int horizon;
    public int textAttribute;
    public int foregroundColour;
    public int backgroundColour;
    public char cursorCharacter;
    public long animationTicks;
    public boolean gamePaused;
    public int currentLogNum;
    public StringBuilder currentInput;
    public String lastInput;
    public String gameId;
    public String version;
    public int maxDrawn;
    public int priorityBase;
    public String simpleName;
    public boolean menuEnabled;
    public boolean menuOpen;
    public boolean holdKey;

    /**
     * The List of recognised words from the current user input line.
     */
    public List<String> recognisedWords;

    /**
     * Indicates that a block has been set.
     */
    public boolean blocking;

    public short blockUpperLeftX;
    public short blockUpperLeftY;
    public short blockLowerRightX;
    public short blockLowerRightY;

    /**
     * Contains a transcript of events leading to the current state in the current room.
     */
    public ScriptBuffer scriptBuffer;

    /**
     * Returns true if the AGI game files are V3; otherwise false.
     */
    public boolean isAGIV3() { return (game.v3GameSig != null); }

    /**
     * Constructor for GameState.
     *
     * @param game The Game from which we'll get all of the game data.
     * @param variableData The VariableData implementation to get and set AGI variables through.
     */
    public GameState(Game game, VariableData variableData) {
        this.game = game;
        this.variableData = variableData;
        this.flags = new boolean[Defines.NUMFLAGS];
        this.strings = new String[Defines.NUMSTRINGS];
        this.controllers = new boolean[Defines.NUMCONTROL];
        this.scanStart = new int[256];
        this.logics = new Logic[256];
        this.pictures = new Picture[256];
        this.views = new View[256];
        this.sounds = new Sound[256];
        this.objects = new Objects(game.objects);
        this.words = game.words;
        this.maxDrawn = 15;
        this.priorityBase = 48;
        this.statusLineRow = 21;
        this.inputLineRow = 23;
        this.currentInput = new StringBuilder();
        this.lastInput = "";
        this.simpleName = "";
        this.gameId = (game.v3GameSig != null? game.v3GameSig : "UNKNOWN");
        this.version = (game.version.equals("Unknown")? "2.917" : game.version);
        this.menuEnabled = true;
        this.holdKey = false;
        this.keyToControllerMap = new HashMap<>();
        this.recognisedWords = new ArrayList<>();
        this.scriptBuffer = new ScriptBuffer(this);

        this.visualPixels = new int[320 * 168];
        this.priorityPixels = new int[160 * 168];
        this.controlPixels = new int[160 * 168];

        // Create and initialise all of the AnimatedObject entries.
        this.animatedObjects = new AnimatedObject[Defines.NUMANIMATED];
        for (int i=0; i < Defines.NUMANIMATED; i++) {
            this.animatedObjects[i] = new AnimatedObject(this, i);
        }
        this.ego = this.animatedObjects[0];

        this.updateObjectList = new ArrayList<AnimatedObject>();
        this.stoppedObjectList = new ArrayList<AnimatedObject>();

        // Store resources in arrays for easy lookup.
        this.logics = this.game.logics;
        this.pictures = this.game.pictures;
        this.views = this.game.views;
        this.sounds = this.game.sounds;

        // Logic 0 is always marked as loaded. It never gets unloaded.
        logics[0].isLoaded = true;
    }

    /**
     * Performs the initialisation of the state of the game being interpreted. Usually called whenever
     * the game starts or restarts.
     */
    public void init() {
        clearStrings();
        clearVars();
        setVar(Defines.MACHINE_TYPE, 0);  // IBM PC
        setVar(Defines.MONITOR_TYPE, 3);  // EGA
        setVar(Defines.INPUTLEN, Defines.MAXINPUT + 1);
        setVar(Defines.NUM_VOICES, 3);

        // The game would usually set this, but no harm doing it here (2 = NORMAL).
        setVar(Defines.ANIMATION_INT, 2);

        // Set to the maximum memory amount as recognised by AGI.
        setVar(Defines.MEMLEFT, 255);

        clearFlags();
        flags[Defines.HAS_NOISE] = true;
        flags[Defines.INITLOGS] = true;
        flags[Defines.SOUNDON] = true;

        // Set the text attribute to default (black on white), and display the input line.
        foregroundColour = 15;
        backgroundColour = 0;

        horizon = Defines.HORIZON;
        userControl = true;
        blocking = false;

        clearVisualPixels();
        graphicsMode = true;
        acceptInput = false;
        showStatusLine = false;
        currentLogNum = 0;
        currentInput.setLength(0);
        lastInput = "";
        simpleName = "";
        clearControllers();
        menuEnabled = true;
        holdKey = false;

        for (AnimatedObject aniObj : animatedObjects) {
            aniObj.reset(true);
        }

        stoppedObjectList.clear();
        updateObjectList.clear();

        this.objects = new Objects(game.objects);
    }

    /**
     * Resets the four resources types back to their new room state. The main reason for doing
     * this is to support the script event buffer.
     */
    public void resetResources() {
        for (int i = 0; i < 256; i++) {
            // For Logics and Views, number 0 is never unloaded.
            if (i > 0) {
                if (logics[i] != null) logics[i].isLoaded = false;
            }
            if (views[i] != null) views[i].isLoaded = false;
            if (pictures[i] != null) pictures[i].isLoaded = false;
            if (sounds[i] != null) sounds[i].isLoaded = false;
        }
    }

    /**
     * Restores all of the background save areas for the most recently drawn AnimatedObjects.
     */
    public void restoreBackgrounds() {
        // If no list specified, then restore update list then stopped list.
        restoreBackgrounds(updateObjectList);
        restoreBackgrounds(stoppedObjectList);
    }
    
    /**
     * Restores all of the background save areas for the most recently drawn AnimatedObjects.
     *
     * @param restoreList
     */
    public void restoreBackgrounds(List<AnimatedObject> restoreList) {
        // Restore the backgrounds of the previous drawn cels for each AnimatedObject.
        for (int i = restoreList.size(); --i >= 0;) {
            restoreList.get(i).restoreBackPixels();
        }
    }

    /**
     * Draws all of the drawn AnimatedObjects in their priority / Y position order. This method
     * does not actually render the objects to the screen but rather to the "back" screen, or 
     * "off" screen version of the visual screen.
     */
    public void drawObjects() {
        // If no list specified, then draw stopped list then update list.
        drawObjects(makeStoppedObjectList());
        drawObjects(makeUpdateObjectList());
    }
    
    /**
     * Draws all of the drawn AnimatedObjects in their priority / Y position order. This method
     * does not actually render the objects to the screen but rather to the "back" screen, or 
     * "off" screen version of the visual screen.
     * 
     * @param objectDrawList
     */
    public void drawObjects(List<AnimatedObject> objectDrawList) {
        // Draw the AnimatedObjects to screen in priority order.
        for (AnimatedObject aniObj : objectDrawList) {
            aniObj.draw();
        }
    }

    /**
     * Shows all AnimatedObjects by blitting the bounds of their current cel to the screen 
     * pixels. Also updates the Stopped flag and previous position as per the original AGI 
     * interpreter behaviour.
     * 
     * @param pixelData The screen pixels to blit the AnimatedObjects to.
     */
    public void showObjects(PixelData pixelData) {
        // If no list specified, then draw stopped list then update list.
        showObjects(pixelData, stoppedObjectList);
        showObjects(pixelData, updateObjectList);
    }
    
    /**
     * Shows all AnimatedObjects by blitting the bounds of their current cel to the screen 
     * pixels. Also updates the Stopped flag and previous position as per the original AGI 
     * interpreter behaviour.
     * 
     * @param pixelData The screen pixels to blit the AnimatedObjects to.
     * @param objectShowList
     */
    public void showObjects(PixelData pixelData, List<AnimatedObject> objectShowList) {
        for (AnimatedObject aniObj : objectShowList)
        {
            aniObj.show(pixelData);

            // Check if the AnimatedObject moved this cycle and if it did then set the flags accordingly. The
            // position of an AnimatedObject is updated only when the StepTimeCount hits 0, at which point it 
            // reloads from StepTime. So if the values are equal, this is a step time reload cycle and therefore
            // the AnimatedObject's position would have been updated and it is appropriate to update Stopped flag.
            if (aniObj.stepTimeCount == aniObj.stepTime)
            {
                if ((aniObj.x == aniObj.prevX) && (aniObj.y == aniObj.prevY))
                {
                    aniObj.stopped = true;
                }
                else
                {
                    aniObj.prevX = aniObj.x;
                    aniObj.prevY = aniObj.y;
                    aniObj.stopped = false;
                }
            }
        }
    }

    /**
     * Returns a List of the AnimatedObjects to draw, in the order in which they should be
     * drawn. It gets the list of candidate AnimatedObjects from the given GameState and 
     * then for each object that is in a Drawn state, it adds them to the list to be draw
     * and then sorts that list by a combination of Y position and priority state, which
     * results in the List to be drawn in the order they should be drawn. The updating param
     * determines what the value of the Update flag should be in order to include an object
     * in the list.
     * 
     * @param objsToDraw >
     * @param updating The value of the UPDATE flag to check for when adding to list
     */
    public List<AnimatedObject> makeObjectDrawList(List<AnimatedObject> objsToDraw, boolean updating) {
        objsToDraw.clear();

        for (AnimatedObject aniObj : this.animatedObjects) {
            if (aniObj.drawn && (aniObj.update == updating)) {
                objsToDraw.add(aniObj);
            }
        }

        // Sorts them by draw order.
        objsToDraw.sort(null);

        return objsToDraw;
    }

    /**
     * Recreates and then returns the list of animated objects that are currently
     * being updated, in draw order.
     */
    public List<AnimatedObject> makeUpdateObjectList() {
        return makeObjectDrawList(updateObjectList, true);
    }

    /**
     * Recreates and the returns the list of animated objects that are currently
     * not being updated, in draw order.
     */
    public List<AnimatedObject> makeStoppedObjectList() {
        return makeObjectDrawList(stoppedObjectList, false);
    }

    /**
     * Clears the VisualPixels screen to it's initial black state.
     */
    public void clearVisualPixels() {
        for (int i=0; i < this.visualPixels.length; i++) {
            this.visualPixels[i] = EgaPalette.colours[0];
        }
    }

    /**
     * Clears all of the AGI variables to be zero.
     */
    public void clearVars() {
        for (int i = 0; i < Defines.NUMVARS; i++) {
            variableData.setVar(i, 0);
        }
    }
    
    /**
     * Gets the value of the AGI variable identified by the variable number.
     * 
     * @param varNum The AGI variable to get the value of.
     * 
     * @return The value of the AGI variable.
     */
    public int getVar(int varNum) {
        return variableData.getVar(varNum);
    }
    
    /**
     * Increments the value of the AGI variable identified by the variable number by 1.
     * 
     * @param varNum The AGI variable to increment the value of by 1.
     * 
     * @return The new value of the AGI variable.
     */
    public int incrementVar(int varNum) {
        return variableData.incrementVar(varNum);
    }
    
    /**
     * Decrements the value of the AGI variable identified by the variable number by 1.
     * 
     * @param varNum The AGI variable to decrement the value of by 1.
     * 
     * @return The new value of the AGI variable.
     */
    public int decrementVar(int varNum) {
        int value = ((getVar(varNum) - 1) & 0xFF);
        setVar(varNum, value);
        return value;
    }
    
    /**
     * Sets the value of the AGI variable, identified by the variable number, to the
     * given value.
     * 
     * @param varNum The AGI variable to set the value of.
     * @param value The value to set the AGI variable to.
     */
    public void setVar(int varNum, int value) {
        variableData.setVar(varNum, value);
    }

    /**
     * Gets the AGI game's total ticks value. This is incremented 60 times a second.
     * 
     * @return The AGI game's total ticks value.
     */
    public int getTotalTicks() {
        return variableData.getTotalTicks();
    }
    
    /**
     * Increments the AGI game's total ticks value by 1 and returns the new value.
     * 
     * @return The new total ticks value after incrementing by 1.
     */
    public int incrementTotalTicks() {
        return variableData.incrementTotalTicks();
    }
    
    /**
     * Sets the AGI game's total ticks value. The only time this would be needed is
     * when restoring a saved game.
     * 
     * @param totalTicks The total ticks value to set
     */
    public void setTotalTicks(int totalTicks) {
        variableData.setTotalTicks(totalTicks);
    }
    
    /**
     * Clears all of the AGI flags to be false.
     */
    public void clearFlags() {
        for (int i = 0; i < Defines.NUMFLAGS; i++) {
            flags[i] = false;
        }
    }

    /**
     * Clears all of the AGI controllers to be false.
     */
    public void clearControllers() {
        for (int i = 0; i < Defines.NUMCONTROL; i++) {
            controllers[i] = false;
        }
    }

    /**
     * Clears all of the AGI Strings to be empty.
     */
    public void clearStrings() {
        for (int i = 0; i < Defines.NUMSTRINGS; i++) {
            strings[i] = "";
        }
    }
}
