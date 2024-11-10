package com.agifans.agile.editor.picture.picedit.gui.handler;

import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.editor.picture.picedit.PicEdit;
import com.agifans.agile.editor.picture.picedit.picture.Picture;

/**
 * Handles processing of PICEDIT key pressed events.
 * 
 * @author Lance Ewing
 */
public class KeyboardHandler implements KeyListener {

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
     * Processes the given key pressed KeyEvent.
     * 
     * @param e the KeyEvent representing the key that was typed.
     */
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        EditStatus editStatus = application.getEditStatus();
        Picture picture = application.getPicture();
        
        if (editStatus.isEgoTestEnabled()) {
            // If Ego Test mode enabled, delegate to the EgoTestHandler.
            // TODO: Reintroduce this capability.
            
        } else {
            // Handle picture buffer navigation keys.
            if (key == KeyEvent.VK_HOME) {
                picture.moveToStartOfPictureBuffer();
            }
            if (key == KeyEvent.VK_LEFT) {
                picture.moveBackOnePictureAction();
            }
            if (key == KeyEvent.VK_RIGHT) {
                picture.moveForwardOnePictureAction();
            }
            if (key == KeyEvent.VK_UP) {
                picture.moveBackOnePictureCode();
            }
            if (key == KeyEvent.VK_DOWN) {
                picture.moveForwardOnePictureCode();
            }
            if (key == KeyEvent.VK_END) {
                picture.moveToEndOfPictureBuffer();
            }
        }
        
        // Handle picture action delete key.
        if (key == KeyEvent.VK_DELETE) {
            picture.deleteSelectedPictureCodes();
        }
    }

    /**
     * Invoked when a key is released.
     * 
     * @param e the key released event.
     */
    public void keyReleased(KeyEvent e) {
    }

    /**
     * Invoked when a key is typed (pressed then released).
     * 
     * @param e the key typed event.
     */
    public void keyTyped(KeyEvent e) {
    }
}
