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
     * Gets the current value of the AGI flag identified by the flag number.
     * 
     * @param flagNum The AGI flag to get the value of.
     * 
     * @return The value of the AGI flag.
     */
    boolean getFlag(int flagNum);
    
    /**
     * Sets the value of the AGI flag, identified by the flag number, to the given value.
     * 
     * @param flagNum The AGI flag to set the value of.
     * @param value The value to set the AGI flag to.
     */
    void setFlag(int flagNum, boolean value);

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
    
    /**
     * Increments the value of the AGI variable identified by the variable number by 1.
     * 
     * @param varNum The AGI variable to increment the value of by 1.
     * 
     * @return The new value of the AGI variable.
     */
    default int incrementVar(int varNum) {
        int value = ((getVar(varNum) + 1) & 0xFF);
        setVar(varNum, value);
        return value;
    }

    /**
     * Gets the AGI game's total ticks value. This is incremented 60 times a second.
     * 
     * @return The AGI game's total ticks value.
     */
    int getTotalTicks();

    /**
     * Sets the AGI game's total ticks value. The only time this would be needed is
     * when restoring a saved game.
     * 
     * @param totalTicks The total ticks value to set
     */
    void setTotalTicks(int totalTicks);
    
    /**
     * Increments the AGI game's total ticks value by 1 and returns the new value.
     * 
     * @return The new total ticks value after incrementing by 1.
     */
    default int incrementTotalTicks() {
        int totalTicks = getTotalTicks() + 1;
        setTotalTicks(totalTicks);
        return totalTicks;
    }
}
