package com.agifans.agile.lwjgl3;

import java.util.Arrays;

import com.agifans.agile.Defines;
import com.agifans.agile.VariableData;

/**
 * Desktop implementation of the VariableData interface.
 */
public class DesktopVariableData implements VariableData {

    private int totalTicks;
    
    private int[] vars;
    
    private boolean[] flags;
    
    private int mouseX;
    
    private int mouseY;
    
    private int mouseButton;
    
    private int oldMouseButton;
    
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

    @Override
    public void clearState() {
        totalTicks = 0;
        mouseX = 0;
        mouseY = 0;
        mouseButton = 0;
        oldMouseButton = 0;
        Arrays.fill(flags, false);
        Arrays.fill(vars, 0);
    }

    @Override
    public int getMouseX() {
        return mouseX;
    }

    @Override
    public int getMouseY() {
        return mouseY;
    }

    @Override
    public int getMouseButton() {
        return mouseButton;
    }

    @Override
    public int getOldMouseButton() {
        return oldMouseButton;
    }

    @Override
    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    @Override
    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }

    @Override
    public void setMouseButton(int mouseButton) {
        this.mouseButton = mouseButton;
    }

    @Override
    public void setOldMousButton(int oldMouseButton) {
        this.oldMouseButton = oldMouseButton;
    }
}
