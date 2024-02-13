package com.agifans.agile.lwjgl3;

import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.Agile;
import com.agifans.agile.AgileRunner;
import com.agifans.agile.Interpreter;
import com.agifans.agile.PixelData;
import com.agifans.agile.QuitAction;
import com.agifans.agile.SavedGameStore;
import com.agifans.agile.UserInput;
import com.agifans.agile.VariableData;
import com.agifans.agile.WavePlayer;
import com.agifans.agile.agilib.Game;
import com.agifans.agile.config.AppConfigItem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

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
        
        // We fetch the files via a generic callback mechanism, mainly to support GWT,
        // but no reason we can't code it for Desktop as ell.
        Map<String, byte[]> gameFilesMap = new HashMap<>();
        gameLoader.fetchGameFiles(gameUri, map -> gameFilesMap.putAll(map));
        Game game = gameLoader.loadGame(gameFilesMap);
        
        // Create the Interpreter class that will run the AGI game.
        Interpreter interpreter = new Interpreter(game, userInput, wavePlayer, 
                savedGameStore, pixelData, variableData);
        
        while (true) {
            if (exit) {
                // Returning from the method will stop the thread cleanly.
                pixelData.clearState();
                variableData.clearState();
                wavePlayer.reset();
                break;
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
        
        if ((interpreterThread != null) && interpreterThread.isAlive()) {
            // If the thread is still running, and is either waiting on the wait() above,
            // or it is sleeping within the UserInput or TextGraphics classes, then this
            // interrupt call will wake it up, the QuitAction will be thrown, and then the
            // thread will cleanly and safely stop.
            interpreterThread.interrupt();
        }
    }
    
    @Override
    public void reset() {
        exit = false;
        interpreterThread = null;
    }
    
    @Override
    public boolean hasStopped() {
        return ((interpreterThread != null) && !interpreterThread.isAlive());
    }

    @Override
    public void saveScreenshot(Agile agile, AppConfigItem appConfigItem, Pixmap screenPixmap) {
        String friendlyAppName = appConfigItem != null ? appConfigItem.getName().replaceAll("[ ,\n/\\:;*?\"<>|!]", "_")
                : "shot";
        if (Gdx.app.getType().equals(ApplicationType.Desktop)) {
            try {
                StringBuilder filePath = new StringBuilder("agile_screens/");
                filePath.append(friendlyAppName);
                filePath.append("_");
                filePath.append(System.currentTimeMillis());
                filePath.append(".png");
                PixmapIO.writePNG(Gdx.files.external(filePath.toString()), screenPixmap);
            } catch (Exception e) {
                // Ignore.
            }
        }
    }
}
