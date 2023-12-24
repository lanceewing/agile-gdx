package com.agifans.agile.lwjgl3;

import com.agifans.agile.AgileRunner;
import com.agifans.agile.QuitAction;
import com.badlogic.gdx.Gdx;

public class DesktopAgileRunner extends AgileRunner implements Runnable {
    
    private Thread interpreterThread;
    
    private boolean exit;

    @Override
    public void start() {
        interpreterThread = new Thread(this);
        interpreterThread.start();
    }
    
    @Override
    public void animationTick() {
        synchronized (this) {
            notify();
        }
    }

    @Override
    public void run() {
        // Start by loading game. We deliberately do this within the thread and
        // not in the main libgdx UI thread.
        loadGame();
        
        while (true) {
            if (exit) {
                Gdx.app.exit();
                return;
            }
            
            try {
                synchronized (this) {
                    wait();
                }
                
                // Perform one tick of the interpreter.
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
