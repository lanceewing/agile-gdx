package com.agifans.agile.editor.picture.picedit.gui.frame;

import com.agifans.agile.editor.picture.picedit.gui.PictureCodeList;
import com.agifans.agile.editor.picture.picedit.picture.Picture;
import com.google.gwt.user.client.Event;

/**
 * A Slider implementation whose behaviour and state is determined by an 
 * EditStatus instance.
 */
public class PositionSlider extends Slider {

    /**
     * The picture whose position is being adjusted.
     */
    private Picture picture;
    
    private PictureCodeList pictureCodeList;
    
    /**
     * The value when it last changed.
     */
    private int lastValueChange;
    
    /**
     * Constructor for PositionSlider.
     * 
     * @param picture
     * @param pictureCodeList 
     */
    public PositionSlider(Picture picture, PictureCodeList pictureCodeList) {
        this.picture = picture;
        this.pictureCodeList = pictureCodeList;
        
        super.setMin(0);
        super.setStep(1);
    }
    
    public void setMin(float min) {
        // Ignored. Will always be 0.
    }
    
    public void setStep(float step) {
        // Ignored. Will always be 1.
    }
    
    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        
        if ("change".equals(event.getType()) || 
            "input".equals(event.getType())) {
            
            int newValue = (int)getValue();
            
            if (newValue != lastValueChange) {
                // This second check is so that we don't redraw picture if picture is already at the position.
                if (newValue != picture.getPicturePosition()) {
                    picture.setPicturePosition(newValue);
                    picture.drawPicture();
                    
                    pictureCodeList.stateChanged();
                }
                
                lastValueChange = newValue;
            }
        }
    }
}
