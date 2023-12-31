package com.agifans.agile.lwjgl3;

import com.agifans.agile.AgileRunner;
import com.agifans.agile.Interpreter;
import com.agifans.agile.PixelData;
import com.agifans.agile.QuitAction;
import com.agifans.agile.SavedGameStore;
import com.agifans.agile.UserInput;
import com.agifans.agile.VariableData;
import com.agifans.agile.WavePlayer;
import com.agifans.agile.agilib.Game;
import com.badlogic.gdx.Gdx;

public class DesktopAgileRunner extends AgileRunner {
    
    private Thread interpreterThread;
    
    private boolean exit;
    
    public DesktopAgileRunner(UserInput userInput, WavePlayer wavePlayer, 
            SavedGameStore savedGameStore, PixelData pixelData, VariableData variableData) {
        super(userInput, wavePlayer, savedGameStore, pixelData, variableData);
    }

    @Override
    public void start(String gameUri) {
        interpreterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                runGame(gameUri);
            }
        });
        interpreterThread.start();
    }
    
    /**
     * Selects am AGI game folder to run.
     * 
     * @return The folder containing the AGI game's resources.
     */
    @Override
    public String selectGame() {
        // TODO: Implement selection logic. This is a placeholder for now.
        // return "C:\\dev\\agi\\winagi\\kq4agi";
        // return "file:/C:/dev/agi/winagi/kq4agi/";
        // return "file://localhost/C:/dev/agi/winagi/kq4agi/";
        return "file:///C:/dev/agi/winagi/kq1/";
    }
    
    @Override
    public void animationTick() {
        synchronized (this) {
            notify();
        }
    }

    private void runGame(String gameUri) {
        // Start by loading game. We deliberately do this within the thread and
        // not in the main libgdx UI thread.
        DesktopGameLoader gameLoader = new DesktopGameLoader(pixelData);
        Game game = gameLoader.loadGame(gameLoader.fetchGameFiles(gameUri));
        
        // Create the Interpreter class that will run the AGI game.
        Interpreter interpreter = new Interpreter(game, userInput, wavePlayer, 
                savedGameStore, pixelData, variableData);
        
        while (true) {
            if (exit) {
                Gdx.app.exit();
                return;
            }
            
            try {
                // Wait for the UI thread to notify us to perform an animation tick.
                synchronized (this) {
                    wait();
                }
                
                // Perform one animation tick of the AGI interpreter.
                interpreter.animationTick();
            }
            catch (QuitAction qa) {
                // QuitAction is thrown when the AGI quit() command is executed.
                exit = true;
            }
            catch (InterruptedException e) {
                // Nothing to do.
            }
        }
    }
    
    @Override
    public void stop() {
        exit = true;
        
        if (interpreterThread.isAlive()) {
            // If the thread is still running, and is either waiting on the wait() above,
            // or it is sleeping within the UserInput or TextGraphics classes, then this
            // interrupt call will wake it up, the QuitAction will be thrown, and then the
            // thread will cleanly and safely stop.
            interpreterThread.interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return ((interpreterThread != null) && (interpreterThread.isAlive()));
    }
}
