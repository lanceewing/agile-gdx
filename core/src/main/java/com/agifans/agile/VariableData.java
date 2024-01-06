package com.agifans.agile;

/**
 * An interface for getting and setting AGI variable values. This needs to be implemented
 * by each platform due to the GWT platform requiring that the data be instantaneously 
 * shared between the UI thread and web worker via a SharedArrayBuffer. This is due to 
 * the UI thread being responsible for updating the variables that store the game clock,
 * which the web worker might potentially be waiting for a change in via a tight loop
 * within a LOGIC script. This does happen in some games, and those games would hang if
 * the UI thread and web worker didn't use shared memory for AGI game variables. For Desktop
 * and Android, the UI thread and interpreter thread can share the same int array object, 
 * but for GWT, such sharing is only possible (at present) by using a SharedArrayBuffer.
 */
public interface VariableData {

    /**
     * Gets the value of the AGI variable identified by the variable number.
     * 
     * @param varNum The AGI variable to get the value of.
     * 
     * @return The value of the AGI variable.
     */
    int getVar(int varNum);
    
    /**
     * Sets the value of the AGI variable, identified by the variable number, to the
     * given value.
     * 
     * @param varNum The AGI variable to set the value of.
     * @param value The value to set the AGI variable to.
     */
    void setVar(int varNum, int value);
    
}
