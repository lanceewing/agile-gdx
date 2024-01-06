package com.agifans.agile.gwt;

import com.agifans.agile.Defines;
import com.agifans.agile.VariableData;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * GWT implementation of the VariableData interface.
 */
public class GwtVariableData implements VariableData {

    /**
     * This is a slight hack for GWT, to use an extra slot in the SharedArray for the
     * total ticks variable, which is actually not an AGI variable. We're just doing this
     * for convenience, rather than creating a separate 1 slot SharedArray.
     */
    private static final int TOTAL_TICKS = 256;
    
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
            // Uses an extra slot at the end for the total ticks.
            variableArraySAB = SharedArray.getStorageForCapacity(Defines.NUMVARS + 1);
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
}
