package com.agifans.agile.agilib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.agifans.agile.VgaPalette;
import com.agifans.agile.agilib.jagi.pic.PictureContext;
import com.agifans.agile.agilib.jagi.pic.PictureException;

/**
 * A wrapper around the JAGI Picture to provide the methods that AGILE needs.
 */
public class Picture extends Resource {
    
    private static final int AGI256_PIC_SIZE = 160 * 168;
    
    private com.agifans.agile.agilib.jagi.pic.Picture jagiPicture;
    
    private PictureContext jagiPictureContext;
    
    public Picture(com.agifans.agile.agilib.jagi.pic.Picture jagiPicture) {
        this.jagiPicture = jagiPicture;
        this.jagiPictureContext = new PictureContext();
    }
    
    public Picture(InputStream is) throws IOException {
        try {
            // At this point, JAGI has already read the 5 byte header, i.e.
            // 0x12 0x34, etc., which means that the InputStream does not contain
            // the length. We therefore have to fully read the resource from 
            // the InputStream so as to create the byte array required by
            // the raw data for the AGI256 resource. 
            int numOfBytesReads;
            byte[] data = new byte[256];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while ((numOfBytesReads = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, numOfBytesReads);
            }
            buffer.flush();
            
            // Check if the resource is of the length expected by an AGI256 picture.
            if (buffer.size() == AGI256_PIC_SIZE) {
                // If so, decode raw data as index values into the VGA palette.
                decodeAGI256(buffer.toByteArray());
            } else {
                throw new RuntimeException("Failed to load AGI PICTURE. Bad AGI256 length.");
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to load AGI PICTURE.", ioe);
        }
    }

    private void decodeAGI256(byte[] resourceRawData) {
        int[] rgba8888Pixels = new int[AGI256_PIC_SIZE];
        
        for (int index=0; index < AGI256_PIC_SIZE; index++) {
            rgba8888Pixels[index] = VgaPalette.colours[((int)resourceRawData[index]) & 0xFF];
        }
        
        jagiPictureContext = new PictureContext();
        jagiPictureContext.setPictureData(rgba8888Pixels);
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
        // This int array is already RGBA8888 values.
        return jagiPictureContext.getPictureData();
    }
    
    public int[] getPriorityPixels() {
        // This int array has the priority values, 0, 1, 2, 3, ... (i.e. not RGBA8888)
        return jagiPictureContext.getPriorityData();
    }
}
