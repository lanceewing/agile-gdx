package com.agifans.agile.agilib.picedit;

import com.agifans.agile.agilib.jagi.awt.Point;

/**
 * This class holds a single picture code, which is a single value within the 
 * picture code buffer. A picture code can be either a data code or a action 
 * code. A data value is either an 8-bit value or a 16-bit value. A data code 
 * is a data byte related to the previous action code.
 * 
 * @author Lance Ewing
 */
public class PictureCode {

    /**
     * The type of PictureCode.
     */
    private PictureCodeType type;
    
    /**
     * The raw code value as stored in the AGI picture.
     */
    private int code;

    /**
     * The absolute location within the Picture that this code is related to. Only
     * applicable to data codes that represent a point. For other types of picture
     * code, the value will be null.
     */
    private Point point;
    
    /**
     * Constructor for PictureCode.
     * 
     * @param type The type of PictureCode.
     */
    public PictureCode(PictureCodeType type) {
        this.type = type;
        this.code = type.getActionCode();
    }
    
    /**
     * Constructor for PictureCode.
     * 
     * @param type The type of PictureCode.
     * @param code The raw code value.
     * @param point Optional Point indicating absolute location that the picture code relates to.
     */
    public PictureCode(PictureCodeType type, int code, Point point) {
        this.type = type;
        this.code = code;
        this.point = point;
    }

    /**
     * Gets the raw code value.
     * 
     * @return The raw code value.
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the type of PictureCode.
     * 
     * @return The type of PictureCode.
     */
    public PictureCodeType getType() {
        return type;
    }
    
    /**
     * Gets he absolute location within the Picture that this code is related to. Only
     * applicable to data codes that represent a point. For other types of picture
     * code, the value returned will be null.
     * 
     * @return The absolute location within the Picture that this code is related to.
     */
    public Point getPoint() {
      return point;
    }
    
    /**
     * Returns true if this is an action code; otherwise false.
     * 
     * @return true if this is an action code; otherwise false.
     */
    public boolean isActionCode() {
        // TODO: This may not be safe for SCI0. Needs investigation over a selection of SCI0 pictures.
        return ((code >= 0xF0) && (code < 0xFF));
    }

    /**
     * Returns true if the PictureCode is the end of picture code.
     * 
     * @return true if the PictureCode is the end of picture code.
     */
    public boolean isEndCode() {
        return (type == PictureCodeType.END);
    }
    
    /**
     * Returns true if this is a data code; otherwise false.
     * 
     * @return true if this is a data code; otherwise false.
     */
    public boolean isDataCode() {
        return !isActionCode() && !isEndCode();
    }
    
    /**
     * Returns true if the PictureCode represents an absolute point.
     * 
     * @return true if the PictureCode represents an absolute point.
     */
    public boolean isAbsolutePoint() {
        return (type == PictureCodeType.ABSOLUTE_POINT_DATA) ||
               (type == PictureCodeType.BRUSH_POINT_DATA) || 
               (type == PictureCodeType.FILL_POINT_DATA);
    }
}
