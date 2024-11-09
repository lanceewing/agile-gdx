package com.agifans.agile.agilib;

import com.agifans.agile.VgaPalette;
import com.agifans.agile.agilib.picedit.EditStatus;

/**
 * A wrapper around the PICEDIT Picture to provide the methods that AGILE needs.
 */
public class Picture extends com.agifans.agile.agilib.picedit.Picture {
    
    private static final int AGI256_PIC_SIZE = 160 * 168;
    
    public Picture() {
        super();
    }
    
    public Picture(byte[] rawData) {
        super();
        
        try {
            // Assume it is a normal AGI picture first.
            decode(rawData);
            
        } catch(Exception e) {
            e.printStackTrace();
            
            if (rawData.length == AGI256_PIC_SIZE) {
                // This probably means that it is an AGI256 picture, so let's load
                // the raw data instead, so that the AGILE interpreter can use it
                // directly.// If so, decode raw data as index values into the VGA palette.
                decodeAGI256(rawData);
            } else {
                throw new RuntimeException("Failed to load AGI PICTURE. Bad AGI256 length.");
            }
        }
    }
    
    private void decodeAGI256(byte[] resourceRawData) {
        int[] visualScreen = editStatus.getVisualScreen();
        for (int index=0; index < AGI256_PIC_SIZE; index++) {
            visualScreen[index] = VgaPalette.colours[((int)resourceRawData[index]) & 0xFF];
        }
    }
    
    /**
     * Clones this Picture, which basically means that it starts with a clean EditStatus
     * again and copies the picture code list.
     * 
     * @return The cloned Picture.
     */
    public Picture clone() {
        Picture clone = new Picture();
        clone.getPictureCodes().addAll(this.pictureCodes);
        clone.setPicturePosition(this.picturePosition);
        return clone;
    }
    
    /**
     * Draws the given Picture on top of this Picture.
     * 
     * @param overlayPicture The Picture to draw on top of this Picture.
     */
    public void overlayPicture(Picture overlayPicture) {
        EditStatus backupEditStatus = overlayPicture.getEditStatus();
        overlayPicture.setEditStatus(editStatus);
        overlayPicture.drawPicture();
        overlayPicture.setEditStatus(backupEditStatus);
    }
    
    public int[] getVisualPixels() {
        // This int array is already RGBA8888 values.
        return editStatus.getVisualScreen();
    }
    
    public int[] getPriorityPixels() {
        // This int array has the priority values, 0, 1, 2, 3, ... (i.e. not RGBA8888)
        return editStatus.getPriorityCodes();
    }
}
