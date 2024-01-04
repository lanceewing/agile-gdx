package com.agifans.agile.gwt;

import com.agifans.agile.UserInput;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * GWT UserInput implementation. The bulk of the code is in the superclass, which
 * is platform independent. The main bit that is not is the storage of the key press
 * events. For GWT, the key press queue needs to be stored in a SharedArrayBuffer so
 * that the data is automatically shared between the UI thread and the web worker.
 */
public class GwtUserInput extends UserInput {

    private static final int TRUE = 1;
    private static final int FALSE = 0;
    
    /**
     * A queue of all key presses that the user has made.
     */
    private SharedQueue keyPressQueue;
    
    /**
     * Current state of every key on the keyboard.
     */
    private SharedArray keys;
    
    /**
     * Stores the state of every key on the previous cycle.
     */
    private SharedArray oldKeys;
    
    /**
     * Constructor for GwtUserInput.
     */
    public GwtUserInput() {
        this(null, null, null);
    }
    
    /**
     * Constructor for GwtUserInput.
     * 
     * @param keyPressSAB
     * @param keysSAB
     * @param oldKeysSAB
     */
    public GwtUserInput(JavaScriptObject keyPressSAB, JavaScriptObject keysSAB, JavaScriptObject oldKeysSAB) {
        super();
        
        if (keyPressSAB == null) {
            keyPressSAB = SharedQueue.getStorageForCapacity(256);
        }
        if (keysSAB == null) {
            keysSAB = SharedArray.getStorageForCapacity(256);
        }
        if (oldKeysSAB == null) {
            oldKeysSAB = SharedArray.getStorageForCapacity(256);
        }
        
        this.keyPressQueue = new SharedQueue(keyPressSAB);
        this.keys = new SharedArray(keysSAB);
        this.oldKeys = new SharedArray(oldKeysSAB);
    }
    
    @Override
    public boolean keys(int keycode) {
        return (keys.get(keycode) == TRUE);
    }

    @Override
    public boolean oldKeys(int keycode) {
        return (oldKeys.get(keycode) == TRUE);
    }

    @Override
    protected void setKey(int keycode, boolean value) {
        keys.set(keycode, value? TRUE : FALSE);
    }

    @Override
    protected void setOldKey(int keycode, boolean value) {
        oldKeys.set(keycode, value? TRUE : FALSE);
    }

    @Override
    protected boolean keyPressQueueIsEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected Integer keyPressQueuePoll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean keyPressQueueAdd(Integer key) {
        // TODO Auto-generated method stub
        return false;
    }
}