package com.agifans.agile.lwjgl3;

import com.agifans.agile.Defines;
import com.agifans.agile.VariableData;

/**
 * Desktop implementation of the VariableData interface.
 */
public class DesktopVariableData implements VariableData {

    private int[] vars;
    
    /**
     * Constructor for DesktopVariableData.
     */
    public DesktopVariableData() {
        this.vars = new int[Defines.NUMVARS];
    }
    
    @Override
    public int getVar(int varNum) {
        return (vars[varNum] & 0xFF);
    }

    @Override
    public void setVar(int varNum, int value) {
        vars[varNum] = (value & 0xFF);
    }
}
