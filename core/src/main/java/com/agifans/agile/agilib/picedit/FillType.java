package com.agifans.agile.agilib.picedit;

/**
 * Enum representing the different types of fill that can be used when displayed a
 * picture. AGI itself only supports the one type of fill, but for the purposes of
 * making picture editing easier in some contexts, it is nice to be able to change 
 * the way that fills are rendered within the editor. For example, turning off fills
 * completely would allow the user to properly see the edges of the filled area, i.e.
 * the original polygon paths. The shaded fill is another options for achieving this
 * that doesn't altogether hide the fill. The transparent fill would allow background
 * images to still show through so that finer detail can be added on to of a filled
 * area.
 * 
 * @author Lance Ewing
 */
public enum FillType {

    NORMAL,
    TRANSPARENT,
    NONE;
    
}
