package com.agifans.agile.gwt;

import com.agifans.agile.Defines;
import com.agifans.agile.VariableData;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * GWT implementation of the VariableData interface.
 */
public class GwtVariableData implements VariableData {

    /**
     * This is a slight hack for GWT, to use extra slots in the SharedArray for the
     * total ticks variable and mouse vars, which actually are not AGI variables. We're 
     * just doing this for convenience, rather than creating separate 1 slot SharedArrays.
     */
    private static final int TOTAL_TICKS = 512;
    
    private static final int MOUSE_BUTTON = 513;
    private static final int MOUSE_X = 514;
    private static final int MOUSE_Y = 515;
    private static final int OLD_MOUSE_BUTTON = 516;
    
    private static final int TRUE = 1;
    private static final int FALSE = 0;
    
    /**
     * We store the AGI flags in the same SharedArray, since there are situations where
     * the UI thread needs to change an AGI flag such that the web worker can instantly
     * see (e.g. when a SOUND finishes playing).
     */
    private static final int FLAGS_OFFSET = 256;
    
    /**
     * Stores the value for each of the AGI variables.
     */
    private SharedArray variableArray;
    
    /**
     * Constructor for GwtVariableData.
     */
    public GwtVariableData() {
        this(null);
    }
    
    /**
     * Constructor for GwtVariableData.
     * 
     * @param variableArraySAB A JavaScriptObject holding a SharedArrayBuffer to use for the SharedArray.
     */
    public GwtVariableData(JavaScriptObject variableArraySAB) {
        // If a SharedArrayBuffer was not provided, then we create a new one. This would
        // be the case for the UI thread's GwtVariableData instance. For the web worker,
        // it would instead receive message with the already created SharedArrayBuffer and
        // use that instead. In this way, both sides (UI thread and web worker) and using
        // the same bit of shared memory for the AGI variable data.
        if (variableArraySAB == null) {
            // Uses extra slots at the end for the total ticks and mouse vars.
            variableArraySAB = SharedArray.getStorageForCapacity(
                    Defines.NUMVARS + Defines.NUMFLAGS + 5);
        }
        
        this.variableArray = new SharedArray(variableArraySAB);
    }
    
    @Override
    public int getTotalTicks() {
        return variableArray.get(TOTAL_TICKS);
    }
    
    @Override
    public void setTotalTicks(int totalTicks) {
        variableArray.set(TOTAL_TICKS, totalTicks);
    }
    
    @Override
    public int getVar(int varNum) {
        return (variableArray.get(varNum & 0xFF) & 0xFF);
    }

    @Override
    public void setVar(int varNum, int value) {
        variableArray.set(varNum & 0xFF, value & 0xFF);
    }
    
    @Override
    public boolean getFlag(int flagNum) {
        return (variableArray.get(FLAGS_OFFSET + (flagNum & 0xFF)) == TRUE);
    }

    @Override
    public void setFlag(int flagNum, boolean value) {
        variableArray.set(FLAGS_OFFSET + (flagNum & 0xFF), value? TRUE : FALSE);
    }
    
    @Override
    public void clearState() {
        int arrayLength = Defines.NUMVARS + Defines.NUMFLAGS + 5;
        for (int index=0; index < arrayLength; index++) {
            variableArray.set(index, 0);
        }
    }

    @Override
    public int getMouseX() {
        return variableArray.get(MOUSE_X);
    }

    @Override
    public int getMouseY() {
        return variableArray.get(MOUSE_Y);
    }

    @Override
    public int getMouseButton() {
        return variableArray.get(MOUSE_BUTTON);
    }

    @Override
    public int getOldMouseButton() {
        return variableArray.get(OLD_MOUSE_BUTTON);
    }

    @Override
    public void setMouseX(int mouseX) {
        variableArray.set(MOUSE_X, mouseX);
    }

    @Override
    public void setMouseY(int mouseY) {
        variableArray.set(MOUSE_Y, mouseY);
    }

    @Override
    public void setMouseButton(int mouseButton) {
        variableArray.set(MOUSE_BUTTON, mouseButton);
    }
    
    @Override
    public void setOldMousButton(int oldMouseButton) {
        variableArray.set(OLD_MOUSE_BUTTON, oldMouseButton);
    }

    /**
     * Gets the SharedArrayBuffer used internally by the variable array.
     * 
     * @return The SharedArrayBuffer used internally by the variable array.
     */
    JavaScriptObject getVariableSharedArrayBuffer() {
        return variableArray.getSharedArrayBuffer();
    }
}
