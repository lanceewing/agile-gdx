package com.agifans.agile.editor.picture.picedit.gui.handler;

import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.editor.picture.picedit.PicEdit;
import com.agifans.agile.editor.picture.picedit.picture.Picture;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

/**
 * Handles processing of PICEDIT key pressed events.
 * 
 * @author Lance Ewing
 */
public class KeyboardHandler implements KeyDownHandler {

    /**
     * The PICEDIT application component.
     */
    protected PicEdit application;
    
    /**
     * Constructor for KeyboardHandler.
     * 
     * @param application the PICEDIT application component.
     */
    public KeyboardHandler(PicEdit application) {
        this.application = application;
    }

    /**
     * Processes the given key down event.
     * 
     * @param e the KeyDownEvent representing the key that was pressed down.
     */
    @Override
    public void onKeyDown(KeyDownEvent e) {
        int key = e.getNativeKeyCode();
        EditStatus editStatus = application.getEditStatus();
        Picture picture = application.getPicture();
        
        if (editStatus.isEgoTestEnabled()) {
            // If Ego Test mode enabled, delegate to the EgoTestHandler.
            // TODO: Reintroduce this capability.
            
        } else {
            // Handle picture buffer navigation keys.
            if (key == KeyCodes.KEY_HOME) {
                picture.moveToStartOfPictureBuffer();
            }
            if (key == KeyCodes.KEY_LEFT) {
                picture.moveBackOnePictureAction();
            }
            if (key == KeyCodes.KEY_RIGHT) {
                picture.moveForwardOnePictureAction();
            }
            if (key == KeyCodes.KEY_UP) {
                picture.moveBackOnePictureCode();
            }
            if (key == KeyCodes.KEY_DOWN) {
                picture.moveForwardOnePictureCode();
            }
            if (key == KeyCodes.KEY_END) {
                picture.moveToEndOfPictureBuffer();
            }
        }
        
        // Handle picture action delete key.
        if (key == KeyCodes.KEY_DELETE) {
            picture.deleteSelectedPictureCodes();
        }
    }
}
