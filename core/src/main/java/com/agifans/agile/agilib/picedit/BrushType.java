package com.agifans.agile.agilib.picedit;


/**
 * An enum representing all the possible types of brush.
 * 
 * @author Lance Ewing
 */
public enum BrushType {

    SQUARE_SOLID_0(0, BrushShape.SQUARE, BrushTexture.SOLID),
    SQUARE_SOLID_1(1, BrushShape.SQUARE, BrushTexture.SOLID),
    SQUARE_SOLID_2(2, BrushShape.SQUARE, BrushTexture.SOLID),
    SQUARE_SOLID_3(3, BrushShape.SQUARE, BrushTexture.SOLID),
    SQUARE_SOLID_4(4, BrushShape.SQUARE, BrushTexture.SOLID),
    SQUARE_SOLID_5(5, BrushShape.SQUARE, BrushTexture.SOLID),
    SQUARE_SOLID_6(6, BrushShape.SQUARE, BrushTexture.SOLID),
    SQUARE_SOLID_7(7, BrushShape.SQUARE, BrushTexture.SOLID),
    CIRCLE_SOLID_0(0, BrushShape.CIRCLE, BrushTexture.SOLID),
    CIRCLE_SOLID_1(1, BrushShape.CIRCLE, BrushTexture.SOLID),
    CIRCLE_SOLID_2(2, BrushShape.CIRCLE, BrushTexture.SOLID),
    CIRCLE_SOLID_3(3, BrushShape.CIRCLE, BrushTexture.SOLID),
    CIRCLE_SOLID_4(4, BrushShape.CIRCLE, BrushTexture.SOLID),
    CIRCLE_SOLID_5(5, BrushShape.CIRCLE, BrushTexture.SOLID),
    CIRCLE_SOLID_6(6, BrushShape.CIRCLE, BrushTexture.SOLID),
    CIRCLE_SOLID_7(7, BrushShape.CIRCLE, BrushTexture.SOLID),  
    SQUARE_SPRAY_0(0, BrushShape.SQUARE, BrushTexture.SPRAY),
    SQUARE_SPRAY_1(1, BrushShape.SQUARE, BrushTexture.SPRAY),
    SQUARE_SPRAY_2(2, BrushShape.SQUARE, BrushTexture.SPRAY),
    SQUARE_SPRAY_3(3, BrushShape.SQUARE, BrushTexture.SPRAY),
    SQUARE_SPRAY_4(4, BrushShape.SQUARE, BrushTexture.SPRAY),
    SQUARE_SPRAY_5(5, BrushShape.SQUARE, BrushTexture.SPRAY),
    SQUARE_SPRAY_6(6, BrushShape.SQUARE, BrushTexture.SPRAY),
    SQUARE_SPRAY_7(7, BrushShape.SQUARE, BrushTexture.SPRAY),
    CIRCLE_SPRAY_0(0, BrushShape.CIRCLE, BrushTexture.SPRAY),
    CIRCLE_SPRAY_1(1, BrushShape.CIRCLE, BrushTexture.SPRAY),
    CIRCLE_SPRAY_2(2, BrushShape.CIRCLE, BrushTexture.SPRAY),
    CIRCLE_SPRAY_3(3, BrushShape.CIRCLE, BrushTexture.SPRAY),
    CIRCLE_SPRAY_4(4, BrushShape.CIRCLE, BrushTexture.SPRAY),
    CIRCLE_SPRAY_5(5, BrushShape.CIRCLE, BrushTexture.SPRAY),
    CIRCLE_SPRAY_6(6, BrushShape.CIRCLE, BrushTexture.SPRAY),
    CIRCLE_SPRAY_7(7, BrushShape.CIRCLE, BrushTexture.SPRAY)
    ;
    
    /**
     * The size of the brush (0-7).
     */
    private int size;
    
    /**
     * The shape of the brush (SQUARE or CIRCLE).
     */
    private BrushShape shape;
    
    /**
     * The texture of the brush (SOLID or SPRARY).
     */
    private BrushTexture texture;
    
    /**
     * Constructor for BrushType.
     * 
     * @param size The size of the brush.
     * @param shape The shape of the brush.
     * @param texture The texture of the brush.
     */
    BrushType(int size, BrushShape shape, BrushTexture texture) {
        this.size = size;
        this.shape = shape;
        this.texture = texture;
    }
  
    /**
     * Gets the size of the brush.
     * 
     * @return the size of the brush.
     */
    public int getSize() {
        return size;
    }
  
    /**
     * Gets the shape of the brush.
     * 
     * @return The shape of the brush.
     */
    public BrushShape getShape() {
        return shape;
    }
  
    /**
     * Gets the texture of the brush.
     * 
     * @return The texture of the brush.
     */
    public BrushTexture getTexture() {
        return texture;
    }
  
    /**
     * Gets the brush code for this type of brush.
     * 
     * @return The brush code for this type of brush.
     */
    public int getBrushCode() {
        int brushCode = (size & 0x07);
        if (shape == BrushShape.SQUARE) {
            brushCode |= 0x10;
        }
        if (texture == BrushTexture.SPRAY) {
            brushCode |= 0x20;
        }
        return brushCode;
    }
    
    /**
     * Gets the display name for this brush, for use in such things as tool
     * tips and status lines.
     * 
     * @return The display name for this brush type.
     */
    public String getDisplayName() {
        StringBuilder displayName = new StringBuilder();
        if (shape.equals(BrushShape.SQUARE)) {
            displayName.append("Square ");
        } else {
            displayName.append("Circle ");
        }
        if (texture.equals(BrushTexture.SPRAY)) {
            displayName.append("Airbrush ");
        } else {
            displayName.append("Brush ");
        }
        displayName.append(size);
        return displayName.toString();
    }
    
    /**
     * Gets the BrushType that matches the given brush code.
     * 
     * @param brushCode The brush code to get the BrushType for.
     * 
     * @return The matching BrushType.
     */
    public static BrushType getBrushTypeForBrushCode(int brushCode) {
    	BrushShape brushShape = ((brushCode & 0x10) > 0 ? BrushShape.SQUARE : BrushShape.CIRCLE);
        BrushTexture brushTexture = ((brushCode & 0x20) > 0 ? BrushTexture.SPRAY : BrushTexture.SOLID);
        int brushSize = (brushCode & 0x07);
        
        for (BrushType brushType : BrushType.values()) {
        	if ((brushType.getShape().equals(brushShape)) && 
        		(brushType.getTexture().equals(brushTexture)) && 
        		(brushType.getSize() == brushSize)) {
        		
        		return brushType;
        	}
        }
        
        return null;
    }
}
