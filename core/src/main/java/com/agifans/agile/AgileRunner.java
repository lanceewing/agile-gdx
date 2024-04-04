package com.agifans.agile;

import com.agifans.agile.config.AppConfigItem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Performs the actual loading and then running of the AGI game. This is an abstract
 * class since the code needs to be run in a background thread/worker, which is something
 * that is best handled by the platform specific code. Most of the code is in this class
 * though, but the launching of the background thread/worker, and its main timing loop,
 * is implemented in the sub-classes.
 */
public abstract class AgileRunner {
    
    private static final int NANOS_PER_FRAME = (1000000000 / 60);
    
    protected GameScreen gameScreen;
    
    protected WavePlayer wavePlayer;
    protected SavedGameStore savedGameStore;
    protected UserInput userInput;
    protected PixelData pixelData;
    protected VariableData variableData;
    
    private long lastTime;
    private long deltaTime;
    
    public AgileRunner(UserInput userInput, WavePlayer wavePlayer, SavedGameStore savedGameStore, 
            PixelData pixelData, VariableData variableData) {
        this.userInput = userInput;
        this.wavePlayer = wavePlayer;
        this.savedGameStore = savedGameStore;
        this.pixelData = pixelData;
        this.variableData = variableData;
        
        // The WavePlayer needs the VariableData interface so that it can set the sound 
        // end flag.
        this.wavePlayer.setVariableData(variableData);
        
        // TODO: Move this to be closer to actual start?
        this.lastTime = TimeUtils.nanoTime();
    }
    
    /**
     * Initialises the AgileRunner with anything that needs setting up before it starts.
     * 
     * @param gameScreen 
     * @param pixmap
     */
    public void init(GameScreen gameScreen, Pixmap pixmap) {
        this.gameScreen = gameScreen;
        
        // TODO: This is for screenshots. Removed when done.
        userInput.setGameScreen(gameScreen);
        
        pixelData.init(pixmap.getWidth(), pixmap.getHeight());
        
        // These are keys that we want to catch and not let the web browser 
        // respond to.
        Gdx.input.setCatchKey(Input.Keys.TAB, true);
        Gdx.input.setCatchKey(Input.Keys.ESCAPE, true);
        Gdx.input.setCatchKey(Input.Keys.F1, true);
        Gdx.input.setCatchKey(Input.Keys.F2, true);
        Gdx.input.setCatchKey(Input.Keys.F3, true);
        Gdx.input.setCatchKey(Input.Keys.F4, true);
        Gdx.input.setCatchKey(Input.Keys.F5, true);
        Gdx.input.setCatchKey(Input.Keys.F6, true);
        Gdx.input.setCatchKey(Input.Keys.F7, true);
        Gdx.input.setCatchKey(Input.Keys.F8, true);
        Gdx.input.setCatchKey(Input.Keys.F9, true);
        Gdx.input.setCatchKey(Input.Keys.F10, true);
        // F11 in the browser is full screen, which is what AGILE does anyway, so its fine.
        Gdx.input.setCatchKey(Input.Keys.F12, true);
        Gdx.input.setCatchKey(Input.Keys.CONTROL_LEFT, true);
        Gdx.input.setCatchKey(Input.Keys.CONTROL_RIGHT, true);
        Gdx.input.setCatchKey(Input.Keys.ALT_LEFT, true);
        Gdx.input.setCatchKey(Input.Keys.ALT_RIGHT, true);
        // TODO: There may be others to add.
    }
    
    /**
     * Returns the UserInput implementation class instance in use by AGILE. 
     * 
     * @return
     */
    public UserInput getUserInput() {
        return userInput;
    }
    
    /**
     * Returns the VariableData implementation class instance in use by AGILE.
     * 
     * @return
     */
    public VariableData getVariableData() {
        return variableData;
    }
    
    /**
     * Updates Pixmap with the latest local changes within our implementation specific
     * PixelData.
     * 
     * @param pixmap
     */
    public void updatePixmap(Pixmap pixmap) {
        pixelData.updatePixmap(pixmap);
    }
    
    /**
     * Invoked by the main UI thread to trigger an AGI tick. The first part, i.e. updating the
     * total ticks and the AGI game clock, is done within the UI thread. The actual animation tick
     * is done within the background thread/worker.
     */
    public void tick() {
        // Calculate the time since the last call.
        long currentTime = TimeUtils.nanoTime();
        deltaTime += (currentTime - lastTime);
        lastTime = currentTime;

        // We can't be certain that this method is being invoked at exactly 60 times a
        // second, or that a call hasn't been skipped, so we adjust as appropriate based
        // on the delta time and play catch up if needed. This should avoid drift in the
        // AGI clock and keep the animation smooth.
        while (deltaTime > NANOS_PER_FRAME) {
            deltaTime -= NANOS_PER_FRAME;
            
            // Regardless of whether we're already in an animation tick, we keep counting the number of Ticks.
            int newTotalTicks = variableData.incrementTotalTicks();

            // Tick is called 60 times a second, so every 60th call, the second clock ticks. We 
            // deliberately do this outside of the main Tick block because some scripts wait for 
            // the clock to reach a certain clock value, which will never happen if the block isn't
            // updated outside of the Tick block.
            if ((newTotalTicks % 60) == 0) {
                updateGameClock();
            }

            // The animation tick is the platform specific bit, as it needs to be run 
            // outside of the UI thread, which is done differently depending on the 
            // platform.
            animationTick();
        }
    }
    
    /**
     * Updates the internal AGI game clock. This method is invoked once a second. We 
     * do this in the AgileRunner base class, running within the UI thread, because 
     * these internal game clocks variables need to be updated at a constant rate 
     * regardless of whether the interpreter thread/worker is busy doing something, such
     * as waiting for a key press.
     */
    private void updateGameClock() {
        if (variableData.incrementVar(Defines.SECONDS) >= 60) {
            // One minute has passed.
            if (variableData.incrementVar(Defines.MINUTES) >= 60) {
                // One hour has passed.
                if (variableData.incrementVar(Defines.HOURS) >= 24) {
                    // One day has passed.
                    variableData.incrementVar(Defines.DAYS);
                    variableData.setVar(Defines.HOURS, 0);
                }

                variableData.setVar(Defines.MINUTES, 0);
            }

            variableData.setVar(Defines.SECONDS, 0);
        }
    }
    
    public abstract void start(AppConfigItem appConfigItem);
    
    public abstract void animationTick();
    
    public abstract void stop();

    public abstract void reset();
    
    public abstract boolean hasStopped();
    
    public abstract void saveScreenshot(Agile agile, AppConfigItem appConfigItem, Pixmap pixmap);
    
    public abstract boolean hasTouchScreen();
    
    public abstract boolean isMobile();
}
