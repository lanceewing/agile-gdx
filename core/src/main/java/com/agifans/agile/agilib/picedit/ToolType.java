package com.agifans.agile.agilib.picedit;

/**
 * Enum for the available tool types.
 *  
 * @author Lance Ewing
 */
public enum ToolType {

    NONE("None"), 
    LINE("Line"), 
    SHORTLINE("Short line"), 
    STEPLINE("Step line"), 
    AIRBRUSH("Airbrush"),
    BRUSH("Brush"), 
    FILL("Fill")
    ;

    private String displayName;

    /**
     * Constructor for ToolType.
     * 
     * @param displayName the name of the tool that is displayed in the status bar.
     */
    ToolType(String displayName) {
        this.displayName = displayName;
    }

    public String toString() {
        return displayName;
    }
}
