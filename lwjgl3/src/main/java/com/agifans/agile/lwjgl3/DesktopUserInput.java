package com.agifans.agile.lwjgl3;

import java.util.LinkedList;
import java.util.Queue;

import com.agifans.agile.UserInput;

/**
 * Desktop implementation of UserInput. The bulk of the code is in the superclass. The
 * main bit that isn't is the storage of the key press events. This is primarily due to
 * GWT needing to store it in a SharedArrayBuffer, whereas Desktop needs it to be in 
 * a standard LinkedList.
 */
public class DesktopUserInput extends UserInput {

    /**
     * A queue of all key presses that the user has made.
     */
    private Queue<Integer> keyPressQueue;

    /**
     * Current state of every key on the keyboard.
     */
    private boolean[] keys;

    /**
     * Stores the state of every key on the previous cycle.
     */
    private boolean[] oldKeys;
    
    /**
     * Constructor for DesktopUserInput.
     */
    public DesktopUserInput() {
        super();
        this.keys = new boolean[256];
        this.oldKeys = new boolean[256];
        this.keyPressQueue = new LinkedList<Integer>();
    }
    
    @Override
    public boolean keys(int keycode) {
        return keys[keycode];
    }

    @Override
    public boolean oldKeys(int keycode) {
        return oldKeys[keycode];
    }

    @Override
    public void setKey(int keycode, boolean value) {
        keys[keycode] = value;
    }

    @Override
    protected void setOldKey(int keycode, boolean value) {
        oldKeys[keycode] = value;
    }

    @Override
    protected boolean keyPressQueueIsEmpty() {
        synchronized (keyPressQueue) {
            return keyPressQueue.isEmpty();
        }
    }
    
    @Override
    protected Integer keyPressQueuePoll() {
        synchronized (keyPressQueue) {
            return keyPressQueue.poll();
        }
    }
    
    @Override
    protected boolean keyPressQueueAdd(Integer key) {
        synchronized (keyPressQueue) {
            return keyPressQueue.add(key);
        }
    }
}
