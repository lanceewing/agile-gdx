package com.agifans.agile.lwjgl3;

import com.agifans.agile.AgileRunner;
import com.agifans.agile.QuitAction;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;

public class DesktopAgileRunner extends AgileRunner implements Runnable {
    
    private Thread interpreterThread;
    
    private boolean exit;

    @Override
    public void start() {
        interpreterThread = new Thread(this);
        interpreterThread.start();
    }

    /**
     * Executes the Interpreter instance.
     */
    @Override
    public void run() {
        // Start by loading game. We deliberately do this within the thread and
        // not in the main libgdx UI thread.
        loadGame();
        
        int nanosPerFrame = (1000000000 / 60);     // 60 times a second.
        long lastTime = TimeUtils.nanoTime();
        
        while (true) {
            if (exit) {
                Gdx.app.exit();
                return;
            }
            
            try {
                // Perform one tick of the interpreter.
                long beforeTick = TimeUtils.nanoTime();
                interpreter.tick();
                long duration = (TimeUtils.nanoTime() - beforeTick);
                
                if (duration > nanosPerFrame) {
                    // If the tick took longer than the time per frame, then most likely
                    // it was due to the menu, or a text window, or something similar that
                    // keeps the thread busy. In this scenario, we simply increment lastTime
                    // by the whole duration. We do not play catch up.
                    lastTime += duration;
                }
                else {
                    // Throttle at expected FPS.
                    while (TimeUtils.nanoTime() - lastTime <= 0L) {
                        Thread.yield();
                    }
                    
                    lastTime += nanosPerFrame;
                }
            }
            catch (QuitAction qa) {
                // QuitAction is thrown when the AGI quit() command is executed.
                exit = true;
            }
        }
    }
    
    @Override
    public void stop() {
        exit = true;
    }

    @Override
    public boolean isRunning() {
        return ((interpreterThread != null) && (interpreterThread.isAlive()));
    }
}
