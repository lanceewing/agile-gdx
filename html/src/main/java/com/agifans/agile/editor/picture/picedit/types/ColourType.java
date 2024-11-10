package com.agifans.agile.editor.picture.picedit.types;

/**
 * Enum to represent the different types of colour.
 *  
 * @author Lance Ewing
 */
public enum ColourType {
    
    VISUAL("Visual"), 
    PRIORITY("Priority"), 
    CONTROL("Control");
    
    /**
     * The display name for the ColourType.
     */
    private String displayName;
    
    /**
     * Constructor for ColourType.
     * 
     * @param displayName The display name for the ColourType.
     */
    ColourType(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the display name for the ColourType.
     * 
     * @return The display name for the ColourType.
     */
    public String getDisplayName() {
        return displayName;
    }
}
