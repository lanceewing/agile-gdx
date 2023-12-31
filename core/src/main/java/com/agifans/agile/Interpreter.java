package com.agifans.agile;

import com.agifans.agile.agilib.Game;
import com.badlogic.gdx.Input.Keys;

/**
 * Interpreter is the core class in the AGILE AGI interpreter. It controls the overall interpreter cycle.
 */
public class Interpreter {
    
    /**
     * The GameState class holds all of the data and state for the Game currently 
     * being run by the interpreter.
     */
    private GameState state;

    /**
     * Holds the data and state for user input events, such as keyboard and mouse input.
     */
    private UserInput userInput;

    /**
     * The pixels data for the AGI screen on which the background Picture and 
     * AnimatedObjects will be drawn to.
     */
    private PixelData pixelData;

    /**
     * Provides methods for drawing text on to the AGI screen.
     */
    private TextGraphics textGraphics;

    /**
     * Direct reference to AnimatedObject number one, i.e. ego, the main character.
     */
    private AnimatedObject ego;

    /**
     * Performs the execution of the LOGIC scripts.
     */
    private Commands commands;

    /**
     * Responsible for displaying the menu system.
     */
    private Menu menu;

    /**
     * Responsible for parsing the user input line to match known words.
     */
    private Parser parser;

    /**
     * Responsible for playing Sound resources.
     */
    private SoundPlayer soundPlayer;

    /**
     * Indicates that a thread is currently executing the Tick, i.e. a single interpretation 
     * cycle. This flag exists because there are some AGI commands that wait for something to 
     * happen before continuing. For example, a print window will stay up for a defined timeout
     * period or until a key is pressed. In such cases, the thread can be in the Tick method 
     * for the duration of what would normally be many Ticks. 
     */
    private volatile boolean inTick;
    
    /**
     * Constructor for Interpreter.
     * 
     * @param game
     * @param userInput
     * @param wavePlayer 
     * @param savedGameStore 
     * @param pixels
     */
    public Interpreter(Game game, UserInput userInput, WavePlayer wavePlayer, 
            SavedGameStore savedGameStore, PixelData pixelData) {
        this.state = new GameState(game);
        this.userInput = userInput;
        this.pixelData = pixelData;
        this.textGraphics = new TextGraphics(pixelData, state, userInput);
        this.parser = new Parser(state);
        this.soundPlayer = new SoundPlayer(state, wavePlayer);
        this.menu = new Menu(state, textGraphics, pixelData, userInput);
        this.commands = new Commands(pixelData, state, userInput, textGraphics, parser, soundPlayer, menu, savedGameStore);
        this.ego = state.ego;
        this.state.init();
        this.textGraphics.updateInputLine();
    }    
    
    /**
     * Updates the internal AGI game clock. This method is invoked once a second.
     */
    private void updateGameClock() {
        if (++state.vars[Defines.SECONDS] >= 60) {
            // One minute has passed.
            if (++state.vars[Defines.MINUTES] >= 60) {
                // One hour has passed.
                if (++state.vars[Defines.HOURS] >= 24) {
                    // One day has passed.
                    state.vars[Defines.DAYS]++;
                    state.vars[Defines.HOURS] = 0;
                }

                state.vars[Defines.MINUTES] = 0;
            }

            state.vars[Defines.SECONDS] = 0;
        }
    }

    /**
     * Updates the total tick count and AGI game clock. 
     */
    public void tick() {
        // Regardless of whether we're already in a Tick, we keep counting the number of Ticks.
        state.totalTicks++;

        // Tick is called 60 times a second, so every 60th call, the second clock ticks. We 
        // deliberately do this outside of the main Tick block because some scripts wait for 
        // the clock to reach a certain clock value, which will never happen if the block isn't
        // updated outside of the Tick block.
        if ((state.totalTicks % 60) == 0) {
            updateGameClock();
        }
    }
    
    /**
     * Executes a single AGI interpreter animation tick. This method is invoked 60 times a
     * second, but the rate at which the logics are run and the animation updated is determined
     * by the animation interval variable.
     */
    public void animationTick() {
        // Only one thread can be running the core interpreter cycle at a time.
        if (!inTick) {
            inTick = true;

            // Proceed only if the animation tick count has reached the set animation interval x 3.
            if (++state.animationTicks < (state.vars[Defines.ANIMATION_INT] * 3)) {
                inTick = false;
                return;
            }

            // Reset animation tick count.
            state.animationTicks = 0;

            // Clear controllers and get user input.
            processUserInput();

            // Update input line text on every cycle.
            textGraphics.updateInputLine(false);

            // If ego is under program control, override user input as to his direction.
            if (!state.userControl) {
                state.vars[Defines.EGODIR] = ego.direction;
            }
            else {
                ego.direction = (byte)state.vars[Defines.EGODIR];
            }

            // Calculate the direction in which objects will move, based on their MotionType. We do
            // this here, i.e. call UpdateObjectDirections() before starting the logic scan, to
            // allow ego's direction to be known to the logics even when ego is on a move.obj().
            updateObjectDirections();

            // Store score and sound state prior to scanning LOGIC 0, so we can determine if they change.
            int previousScore = state.vars[Defines.SCORE];
            boolean soundStatus = state.flags[Defines.SOUNDON];

            // Continue scanning LOGIC 0 while the return value is true (which is what indicates a rescan).
            while (commands.executeLogic(0)) {
                state.vars[Defines.OBJHIT] = 0;
                state.vars[Defines.OBJEDGE] = 0;
                state.vars[Defines.UNKNOWN_WORD] = 0;
                state.flags[Defines.INPUT] = false;
                previousScore = state.vars[Defines.SCORE];
            }

            // Set ego's direction from the variable.
            ego.direction = (byte)state.vars[Defines.EGODIR];

            // Update the status line, if the score or sound status have changed.
            if ((state.vars[Defines.SCORE] != previousScore) || (soundStatus != state.flags[Defines.SOUNDON])) {
                // If the SOUND ON flag is off, then immediately stop any currently playing sound.
                if (!state.flags[Defines.SOUNDON]) soundPlayer.stopSound();

                textGraphics.updateStatusLine();
            }

            state.vars[Defines.OBJHIT] = 0;
            state.vars[Defines.OBJEDGE] = 0;

            // Clear the restart, restore, & init logics flags.
            state.flags[Defines.INITLOGS] = false;
            state.flags[Defines.RESTART] = false;
            state.flags[Defines.RESTORE] = false;

            // If in graphics mode, animate the AnimatedObjects.
            if (state.graphicsMode) {
                animateObjects();
            }

            // If there is an open text window, we render it now.
            if (textGraphics.isWindowOpen()) {
                textGraphics.drawWindow();
            }

            // Store what the key states were in this cycle before leaving.
            for (int i = 0; i < 256; i++) userInput.oldKeys[i] = userInput.keys[i];

            inTick = false;
        }
    }

    /**
     * Fully shuts down the SoundPlayer.
     */
    public void shutdownSound() {
        soundPlayer.shutdown();
    }

    /**
     * Animates each of the AnimatedObjects that are currently on the screen. This 
     * involves the cell cycling, the movement, and the drawing to the screen.
     */
    private void animateObjects() {
        // Ask each AnimatedObject to update its loop and cell number if required.
        for (AnimatedObject aniObj : state.animatedObjects) {
            aniObj.updateLoopAndCel();
        }

        state.vars[Defines.EGOEDGE] = 0;
        state.vars[Defines.OBJHIT] = 0;
        state.vars[Defines.OBJEDGE] = 0;

        // Restore the backgrounds of the previous drawn cels for each AnimatedObject.
        state.restoreBackgrounds(state.updateObjectList);

        // Ask each AnimatedObject to move if it needs to.
        for (AnimatedObject aniObj : state.animatedObjects) {
            aniObj.updatePosition();
        }

        // Draw the AnimatedObjects to screen in priority order.
        state.drawObjects(state.makeUpdateObjectList());
        state.showObjects(pixelData, state.updateObjectList);

        // Clear the 'must be on water or land' bits for ego.
        state.ego.stayOnLand = false;
        state.ego.stayOnWater = false;
    }

    /**
     * Asks every AnimatedObject to calculate their direction based on their current state.
     */
    private void updateObjectDirections() {
        for (AnimatedObject aniObj : state.animatedObjects) {
            aniObj.updateDirection();
        }
    }

    /**
     * Processes the user's input.
     */
    private void processUserInput() {
        state.clearControllers();
        state.flags[Defines.INPUT] = false;
        state.flags[Defines.HADMATCH] = false;
        state.vars[Defines.UNKNOWN_WORD] = 0;
        state.vars[Defines.LAST_CHAR] = 0;

        // If opening of the menu was "triggered" in the last cycle, we open it now before processing the rest of the input.
        if (state.menuOpen) {
            menu.menuInput();
        }

        // F12 shows the priority and control screens.
        if (userInput.keys[(int)Keys.F12] && !userInput.oldKeys[(int)Keys.F12]) {
            commands.showPriorityScreen();
        }

        // Handle arrow keys.
        if (state.userControl) {
            if (state.holdKey) {
                // In "hold key" mode, the ego direction directly reflects the direction key currently being held down.
                byte direction = 0;
                if (userInput.keys[(int)Keys.UP]) direction = 1;
                if (userInput.keys[(int)Keys.PAGE_UP]) direction = 2;
                if (userInput.keys[(int)Keys.RIGHT]) direction = 3;
                if (userInput.keys[(int)Keys.PAGE_DOWN]) direction = 4;
                if (userInput.keys[(int)Keys.DOWN]) direction = 5;
                if (userInput.keys[(int)Keys.END]) direction = 6;
                if (userInput.keys[(int)Keys.LEFT]) direction = 7;
                if (userInput.keys[(int)Keys.HOME]) direction = 8;
                state.vars[Defines.EGODIR] = direction;
            }
            else {
                // Whereas in "release key" mode, the direction key press will toggle movement in that direction.
                byte direction = 0;
                if (userInput.keys[(int)Keys.UP] && !userInput.oldKeys[(int)Keys.UP]) direction = 1;
                if (userInput.keys[(int)Keys.PAGE_UP] && !userInput.oldKeys[(int)Keys.PAGE_UP]) direction = 2;
                if (userInput.keys[(int)Keys.RIGHT] && !userInput.oldKeys[(int)Keys.RIGHT]) direction = 3;
                if (userInput.keys[(int)Keys.PAGE_DOWN] && !userInput.oldKeys[(int)Keys.PAGE_DOWN]) direction = 4;
                if (userInput.keys[(int)Keys.DOWN] && !userInput.oldKeys[(int)Keys.DOWN]) direction = 5;
                if (userInput.keys[(int)Keys.END] && !userInput.oldKeys[(int)Keys.END]) direction = 6;
                if (userInput.keys[(int)Keys.LEFT] && !userInput.oldKeys[(int)Keys.LEFT]) direction = 7;
                if (userInput.keys[(int)Keys.HOME] && !userInput.oldKeys[(int)Keys.HOME]) direction = 8;
                if (direction > 0) {
                    state.vars[Defines.EGODIR] = (state.vars[Defines.EGODIR] == direction ? (byte)0 : direction);
                }
            }
        }

        // Check all waiting characters.
        int ch;
        while ((ch = userInput.getKey()) > 0) {
            
            // Check controller matches. They take precedence.
            if (state.keyToControllerMap.containsKey(ch)) {
                state.controllers[state.keyToControllerMap.get(ch)] = true;
            }
            else if ((ch & 0xF0000) == UserInput.ASCII) {  // Standard char from a keypress event.
                char character = (char)(ch & 0xFF);
                
                state.vars[Defines.LAST_CHAR] = character;

                if (state.acceptInput) {
                    // Handle enter and backspace for user input line.
                    switch (character) {
                        case Character.ENTER:
                            if (state.currentInput.length() > 0) {
                                parser.parse(state.currentInput.toString());
                                state.lastInput = state.currentInput.toString();
                                state.currentInput.setLength(0);
                            }
                            break;
    
                        case Character.BACKSPACE:
                            if (state.currentInput.length() > 0) {
                                state.currentInput.delete(state.currentInput.length() - 1, state.currentInput.length());
                            }
                            break;
                            
                        default:
                            // Handle normal characters for user input line.
                            if ((state.strings[0].length() + (state.cursorCharacter > 0 ? 1 : 0) + state.currentInput.length()) < Defines.MAXINPUT) {
                                state.currentInput.append(character);
                            }
                            break;
                    }
                }
            }
        }
    }
}
