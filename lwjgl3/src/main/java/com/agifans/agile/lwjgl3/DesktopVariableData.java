package com.agifans.agile.lwjgl3;

import com.agifans.agile.Defines;
import com.agifans.agile.VariableData;

/**
 * Desktop implementation of the VariableData interface.
 */
public class DesktopVariableData implements VariableData {

    private int totalTicks;
    
    private int[] vars;
    
    public boolean[] flags;
    
    /**
     * Constructor for DesktopVariableData.
     */
    public DesktopVariableData() {
        this.vars = new int[Defines.NUMVARS];
        this.flags = new boolean[Defines.NUMFLAGS];
    }
    
    @Override
    public boolean getFlag(int flagNum) {
        return flags[flagNum];
    }

    @Override
    public void setFlag(int flagNum, boolean value) {
        flags[flagNum] = value;
    }

    @Override
    public int getVar(int varNum) {
        return (vars[varNum] & 0xFF);
    }

    @Override
    public void setVar(int varNum, int value) {
        vars[varNum] = (value & 0xFF);
    }

    @Override
    public int getTotalTicks() {
        return totalTicks;
    }

    @Override
    public void setTotalTicks(int totalTicks) {
        this.totalTicks = totalTicks;
    }
}
