package com.agifans.agile.agilib;

import com.sierra.agi.pic.PictureContext;
import com.sierra.agi.pic.PictureException;

/**
 * A wrapper around the JAGI Picture to provide the methods that AGILE needs.
 */
public class Picture extends Resource {

    private com.sierra.agi.pic.Picture jagiPicture;
    
    private PictureContext jagiPictureContext;
    
    public Picture(com.sierra.agi.pic.Picture jagiPicture) {
        this.jagiPicture = jagiPicture;
        this.jagiPictureContext = new PictureContext();
    }
    
    public Picture clone() {
        // It doesn't matter that we're using the same JAGI Picture. The actual
        // drawing state is in the PictureContext, which will be a different
        // instance. The JAGI Picture contains only the Vector of picture codes.
        return new Picture(jagiPicture);
    }
    
    public void drawPicture() {
        drawPicture(jagiPictureContext);
    }
    
    protected void drawPicture(PictureContext jagiPictureContext) {
        try {
            this.jagiPicture.draw(jagiPictureContext);
        } catch (PictureException pe) {
            throw new RuntimeException("Failed to draw JAGI Picture.", pe);
        }
    }
    
    public void overlayPicture(Picture picture) {
        picture.drawPicture(jagiPictureContext);
    }
    
    public int[] getVisualPixels() {
        // This int array is already ARGB values.
        return jagiPictureContext.getPictureData();
    }
    
    public int[] getPriorityPixels() {
        // This int array has the priority values, 0, 1, 2, 3, ... (i.e. not ARGB)
        return jagiPictureContext.getPriorityData();
    }
}
