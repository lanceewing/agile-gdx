package com.agifans.agile;

import com.badlogic.gdx.Gdx;
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
        // TODO: Move this to be closer to actual start?
        this.lastTime = TimeUtils.nanoTime();
    }
    
    /**
     * Initialises the AgileRunner with anything that needs setting up before it starts.
     * 
     * @param pixmap
     */
    public void init(Pixmap pixmap) {
        pixelData.init(pixmap.getWidth(), pixmap.getHeight());
        
        // TODO: Unset this when the AgileRunner is stopped?
        Gdx.input.setInputProcessor(userInput);
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
    
    public abstract void start(String gameUri);
    
    public abstract String selectGame();
    
    public abstract void animationTick();
    
    public abstract void stop();
    
    public abstract boolean isRunning();
    
}
