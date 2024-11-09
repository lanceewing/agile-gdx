package com.agifans.agile.agilib.picedit;

import java.util.HashMap;
import java.util.Map;

/**
 * An enum representing the different types of picture code.
 * 
 * @author Lance Ewing
 */
public enum PictureCodeType {

    // These are the different types of action code.
    SET_VISUAL_COLOR(0xF0, "SetVisualColor"),
    SET_VISUAL_COLOR_OFF(0xF1, "SetVisualColorOff"),
    SET_PRIORITY_COLOR(0xF2, "SetPriorityColor"),
    SET_PRIORITY_COLOR_OFF(0xF3, "SetPriorityColorOff"),
    DRAW_VERTICAL_STEP_LINE(0xF4, "DrawStepLine"),
    DRAW_HORIZONTAL_STEP_LINE(0xF5, "DrawStepLine"),
    DRAW_LINE(0xF6, "DrawAbsoluteLine"),
    DRAW_SHORT_LINE(0xF7, "DrawRelativeLine"),
    DRAW_FILL(0xF8, "DrawFill"),
    SET_BRUSH_TYPE(0xF9, "SetBrushType"),
    DRAW_BRUSH_POINT(0xFA, "DrawBrush"),
    
    // These are the different types of data codes associated with action codes.
    COLOR_DATA(-1, ""),
    ABSOLUTE_POINT_DATA(-1, ""),
    RELATIVE_POINT_DATA(-1, ""),
    FILL_POINT_DATA(-1, ""),
    BRUSH_POINT_DATA(-1, ""),
    X_POSITION_DATA(-1, ""),
    Y_POSITION_DATA(-1, ""),
    BRUSH_TYPE_DATA(-1, ""),
    BRUSH_PATTERN_DATA(-1, ""),
    
    // This is a special code meaning the end of the picture.
    END(0xFF, "End");

    /**
     * The action code for this PictureCodeType (only applicable for 0xF0 to 0xFA).
     */
    private int actionCode;
    
    /**
     * The text to display for this PictureCodeType.
     */
    private String displayableText;
    
    /**
     * A Map of PictureCodeTypes keyed by the picture action code.
     */
    static Map<Integer, PictureCodeType> pictureCodeTypes = new HashMap<Integer, PictureCodeType>();
    static {
        pictureCodeTypes.put(0xF0, SET_VISUAL_COLOR);
        pictureCodeTypes.put(0xF1, SET_VISUAL_COLOR_OFF);
        pictureCodeTypes.put(0xF2, SET_PRIORITY_COLOR);
        pictureCodeTypes.put(0xF3, SET_PRIORITY_COLOR_OFF);
        pictureCodeTypes.put(0xF4, DRAW_VERTICAL_STEP_LINE);
        pictureCodeTypes.put(0xF5, DRAW_HORIZONTAL_STEP_LINE);
        pictureCodeTypes.put(0xF6, DRAW_LINE);
        pictureCodeTypes.put(0xF7, DRAW_SHORT_LINE);
        pictureCodeTypes.put(0xF8, DRAW_FILL);
        pictureCodeTypes.put(0xF9, SET_BRUSH_TYPE);
        pictureCodeTypes.put(0xFA, DRAW_BRUSH_POINT);
        pictureCodeTypes.put(0xFF, END);
    }
    
    /**
     * Constructor for PictureCodeType.
     * 
     * @param actionCode The action code for this PictureCodeType (only applicable for 0xF0 to 0xFA).
     * @param displayableText The text to display for this PictureCodeType.
     */
    PictureCodeType(int actionCode, String displayableText) {
        this.actionCode = actionCode;
        this.displayableText = displayableText;
    }

    /**
     * Gets the action code (only applicable for 0xF0-0xFF)
     * 
     * @return The action code.
     */
    public int getActionCode() {
        return actionCode;
    }
    
    /**
     * Gets the text to display for this PictureCodeType.
     * 
     * @return The text to display for this PictureCodeType.
     */
    public String getDisplayableText() {
        return displayableText;
    }
    
    /**
     * Gets the PictureCodeType that matches the given action code.
     * 
     * @param actionCode The picture action code to get the PictureCodeType for.
     * 
     * @return The corresponding PictureCodeType.
     */
    public static PictureCodeType getPictureCodeType(int actionCode) {
        return pictureCodeTypes.get(actionCode);
    }
}
