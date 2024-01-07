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
        return keyPressQueue.isEmpty();
    }

    @Override
    protected Integer keyPressQueuePoll() {
        int keyCode = keyPressQueue.poll();
        // We don't appear to be able to use a return type of Integer for the native
        // JS poll method, as that leads to unpredictable behaviour, e.g. it mysteriously
        // becomes undefined when it should contain a number. For that reason, we use -1
        // to indicate absence of a waiting key and convert that to null to match what
        // the other platforms are doing.
        return (keyCode == -1? null : keyCode);
    }

    @Override
    protected boolean keyPressQueueAdd(Integer key) {
        return keyPressQueue.add(key);
    }
    
    /**
     * Gets the SharedArrayBuffer that the key press queue is using internally for
     * storage.
     * 
     * @return The SharedArrayBuffer used internally by the key press queue.
     */
    JavaScriptObject getKeyPressQueueSharedArrayBuffer() {
        return keyPressQueue.getSharedArrayBuffer();
    }
    
    /**
     * Gets the SharedArrayBuffer that the key state array is using internally for storage.
     * 
     * @return The SharedArrayBuffer used internally by the key state array.
     */
    JavaScriptObject getKeysSharedArrayBuffer() {
        return keys.getSharedArrayBuffer();
    }
    
    /**
     * Gets the SharedArrayBuffer that the old key state array is using internally 
     * for storage.
     * 
     * @return The SharedArrayBuffer used internally by the old key state array.
     */
    JavaScriptObject getOldKeysSharedArrayBuffer() {
        return oldKeys.getSharedArrayBuffer();
    }
}