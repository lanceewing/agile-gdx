package com.agifans.agile.editor.picture.picedit.picture;

import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.List;

import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.agilib.picedit.PictureCode;
import com.agifans.agile.agilib.picedit.PictureCodeType;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

/**
 * This class represents an AGI Picture.
 * 
 * @author Lance Ewing
 */
public class Picture extends com.agifans.agile.agilib.picedit.Picture {

    /**
     * Holds the pixel data for the visual screen of the picture.
     */
    private Int32Array visualScreen;

    /**
     * Holds the pixel data for the priority screen of the picture.
     */
    private Int32Array priorityScreen;
    
    /**
     * Holds the canvas 2d context for the visual screen.
     */
    private Context2d visualContext;
    
    /**
     * Holds the canvas 2d context for the priority screen.
     */
    private Context2d priorityContext;
    
    /**
     * The Image for the visual screen.
     */
    private Canvas visualImage;

    /**
     * The Image for the priority screen.
     */
    private Canvas priorityImage;

    /**
     * Holds the cache of picture state at various picture positions. Used for faster picture draws.
     */
    private PictureCache pictureCache;
    
    /**
     * List of registered PictureChangeListeners.
     */
    private List<PictureChangeListener> pictureChangeListeners;
    
    /**
     * The picture position where the current selection starts.
     */
    private int firstSelectedPosition;
    
    /**
     * The picture position where the current selection ends.
     */
    private int lastSelectedPosition;
    
    /**
     * Constructor for Picture.
     * 
     * @param editStatus the EditStatus containing current editing status.
     */
    public Picture(EditStatus editStatus) {
        super(editStatus);
        
        // Set up visual screen objects.
        visualImage = createImage(WIDTH, HEIGHT);
        visualContext = visualImage.getContext2d();
        visualScreen = TypedArrays.createInt32Array(WIDTH * HEIGHT);
        
        // Set up priority screen objects.
        priorityImage = createImage(WIDTH, HEIGHT);
        priorityContext = priorityImage.getContext2d();
        priorityScreen = TypedArrays.createInt32Array(WIDTH * HEIGHT);
        
        this.pictureCache = new PictureCache(editStatus);
        this.pictureChangeListeners = new ArrayList<PictureChangeListener>();
        
        clearPicture();
        
        this.firstSelectedPosition = -1;
        this.lastSelectedPosition = -1;
    }

    /**
     * Creates a new canvas of the given size.
     * 
     * @param width
     * @param height
     * 
     * @return
     */
    private Canvas createImage(int width, int height) {
        Canvas canvas = Canvas.createIfSupported();
        canvas.getCanvasElement().setWidth(width);
        canvas.getCanvasElement().setHeight(height);
        return canvas;
    }
    
    /**
     * Adds the given PictureChangeListener to the List of listeners that are notified when 
     * the data for this Picture changes.
     * 
     * @param pictureChangeListener The PictureChangeListener to add.
     */
    public void addPictureChangeListener(PictureChangeListener pictureChangeListener) {
        this.pictureChangeListeners.add(pictureChangeListener);
    }
    
    /**
     * Fires a picture codes removed event to all PictureChangeListeners.
     * 
     * @param fromIndex The index where the picture codes started to be removed.
     * @param toIndex The index where the picture codes finished being removed.
     */
    public void firePictureCodesRemoved(int fromIndex, int toIndex) {
        for (PictureChangeListener listener : pictureChangeListeners) {
            listener.pictureCodesRemoved(fromIndex, toIndex);
        }
    }
    
    /**
     * Fires a pictures codes added event to all PictureChangeListeners.
     * 
     * @param fromIndex The index where the picture codes started to be added.
     * @param toIndex The index where the picture codes finished being added.
     */
    public void firePictureCodesAdded(int fromIndex, int toIndex) {
        if (!isLoading) {
            for (PictureChangeListener listener : pictureChangeListeners) {
                listener.pictureCodesAdded(fromIndex, toIndex);
            }
        }
    }
    
    /**
     * Fires a selection interval collapsed event to all PictureChangeListeners.
     */
    public void fireSelectionIntervalCollapsed() {
        for (PictureChangeListener listener : pictureChangeListeners) {
            listener.selectionIntervalCollapsed();
        }
    }
    
    private native static void copyPixelsToContext2d(Int32Array rgbaPixels, Context2d ctx)/*-{
        var len = rgbaPixels.length;
        var imgData = ctx.createImageData(160, 168);
        var data = imgData.data;
        
        // Split RGBA value into separate bytes, as required by canvas image data array.
        for (var i = 0, index = 0; i < len; i++, index += 4) {
            var rgba8888Colour = rgbaPixels[i];
            data[index + 0] = (rgba8888Colour >> 24) & 0xff;
            data[index + 1] = (rgba8888Colour >> 16) & 0xff;
            data[index + 2] = (rgba8888Colour >>  8) & 0xff;
            data[index + 3] = (rgba8888Colour >>  0) & 0xff;
        }
        
        ctx.putImageData(imgData, 0, 0);
    }-*/;

    /**
     * Clears the visual and priority screens.
     */
    public void clearPictureScreens() {
        // Clears them to be transparent, as per picedit.
        
        int visualLength = visualScreen.length();
        for (int index=0; index < visualLength; index++) {
            visualScreen.set(index, 0x00000000);
        }
        copyPixelsToContext2d(visualScreen, visualContext);

        int priorityLength = priorityScreen.length();
        for (int index=0; index < priorityLength; index++) {
            priorityScreen.set(index, 0x00000000);
        }
        copyPixelsToContext2d(priorityScreen, priorityContext);
    }
    
    /**
     * Clears the picture cache.
     */
    public void clearPictureCache() {
        this.pictureCache.clear();
    }
    
    /**
     * Sets the selection interval for the Picture, which is the start and end of the 
     * picture positions that are currently selected. Selection can occur either by using
     * the selection tool or by using the left hand side "Commands" JList.
     * 
     * @param firstSelectedPosition The start of the interval.
     * @param lastSelectedPosition The end of the interval.
     */
    public void setSelectionInterval(int firstSelectedPosition, int lastSelectedPosition) {
        this.firstSelectedPosition = firstSelectedPosition;
        this.lastSelectedPosition = lastSelectedPosition;
    }
    
    /**
     * Collapses the selection interval by setting the start and end to the current
     * position, which would normally be what the end of the selection would have been
     * set to.
     */
    public void collapseSelectionInterval() {
        this.firstSelectedPosition = this.lastSelectedPosition = this.picturePosition;
        fireSelectionIntervalCollapsed();
    }
    
    /**
     * Gets the first picture position in the selection interval.
     * 
     * @return The first picture position in the selection interval.
     */
    public int getFirstSelectedPosition() {
        return firstSelectedPosition;
    }
    
    /**
     * Gets the last picture position in the selection interval.
     * 
     * @return The last picture position in the selection interval.
     */
    public int getLastSelectedPosition() {
        return lastSelectedPosition;
    }
    
    /**
     * Processes the setting of a new visual colour.
     * 
     * @param newVisualColour The new visual colour.
     */
    public void processVisualColourChange(int newVisualColour) {
        editStatus.setVisualColour(newVisualColour);
        this.addPictureCode(PictureCodeType.SET_VISUAL_COLOR);
        this.addPictureCode(PictureCodeType.COLOR_DATA, newVisualColour);
    }
    
    /**
     * Processes the turning off of the visual colour.
     */
    public void processVisualColourOff() {
        editStatus.setVisualColour(EditStatus.VISUAL_OFF);
        this.addPictureCode(PictureCodeType.SET_VISUAL_COLOR_OFF);
    }
    
    /**
     * Processes the setting of a new priority colour.
     * 
     * @param newPriorityColour The new priority colour.
     */
    public void processPriorityColourChange(int newPriorityColour) {
        editStatus.setPriorityColour(newPriorityColour);
        this.addPictureCode(PictureCodeType.SET_PRIORITY_COLOR);
        this.addPictureCode(PictureCodeType.COLOR_DATA, newPriorityColour);
    }
    
    /**
     * Processes the turning off of the priority colour.
     */
    public void processPriorityColourOff() {
        editStatus.setPriorityColour(EditStatus.PRIORITY_OFF);
        this.addPictureCode(PictureCodeType.SET_PRIORITY_COLOR_OFF);
    }
    
    /**
     * Works backwards if necessary to work out what the current picture action or tool
     * is, i.e. if the current position is on a data code rather than an action code then
     * it will navigation backwards to find the association action code.
     * 
     * @return The current action code that the current picture position is within.
     */
    public PictureCode getCurrentPictureAction() {
      if (pictureCodes.size() == 1) {
          return null;
      }
      int position = picturePosition;
      while ((position > 0) && !pictureCodes.get(position).isActionCode()) {
          position--;
      }
      return pictureCodes.get(position);
    }

    public PictureCode getNextPictureAction() {
        PictureCode pictureCode = null;
        if (picturePosition < pictureCodes.size() - 1) {
            int position = picturePosition + 1;
            while ((position < pictureCodes.size()) && !pictureCodes.get(position).isActionCode()) {
                position++;
            }
            if (position < pictureCodes.size()) {
                pictureCode = pictureCodes.get(position);
            }
        }
        return pictureCode;
    }
    
    public PictureCode incrementPicturePosition() {
        picturePosition++;
        if (picturePosition >= pictureCodes.size()) {
            picturePosition = pictureCodes.size() - 1;
            return null;
        } else {
            return pictureCodes.get(picturePosition);
        }
    }

    public PictureCode decrementPicturePosition() {
        picturePosition--;
        if (picturePosition < 0) {
            picturePosition = 0;
        }
        return pictureCodes.get(picturePosition);
    }

    public PictureCode deleteAtPicturePosition() {
        PictureCode pictureCode = null;
        if (picturePosition < (pictureCodes.size() - 1)) {
            pictureCodes.remove(picturePosition);
            firePictureCodesRemoved(picturePosition, picturePosition);
            if (picturePosition < (pictureCodes.size() - 1)) {
                pictureCode = pictureCodes.get(picturePosition);
            }
            editStatus.setUnsavedChanges(true);
        }
        return pictureCode;
    }
    
    /**
     * Attempts to delete the pictures codes from the given 'from' picture code index to the
     * give 'to' picture code index. It may be that the delete is not completely possible 
     * since the outcome might leave the picture broken. So this is taken in to account so that
     * the end result is always a working picture.
     * 
     * @param fromPosition The picture position to delete picture codes from.
     * @param toPosition The picture position to delete picture codes to.
     */
    public void deletePictureCodes(int fromPosition, int toPosition) {
        int numOfCodesToRemove = (toPosition - fromPosition) + 1;
        for (int count = 0; count < numOfCodesToRemove; count++) {
            if (!getCurrentPictureCode().isEndCode()) {
                pictureCodes.remove(fromPosition);
                firePictureCodesRemoved(fromPosition, fromPosition);
            }
        }
        
        // TODO: Detect when the starting point has been removed for a relative or step line action and convert the next point in to an absolute starting point.
        
        editStatus.setUnsavedChanges(true);
        pictureCache.clear(fromPosition);
        drawPicture();
    }
    
    /**
     * Deletes the currently selected picture codes.
     */
    public void deleteSelectedPictureCodes() {
        deletePictureCodes(firstSelectedPosition, lastSelectedPosition);
    }
    
    /**
     * Process movement back one picture action through the picture code buffer. A
     * picture action is the full set of codes for a tool, e.g. all the points for a 
     * single draw absolute action. Picture actions are highlighted in black on the 
     * picture code JList.
     */
    public void moveBackOnePictureAction() {
        // Move back through the codes until we find an Action code.
        PictureCode pictureCode = null;
        picturePosition = firstSelectedPosition;
        do {
            pictureCode = decrementPicturePosition();
        } while ((pictureCode != null) && !pictureCode.isActionCode() && (picturePosition > 0));
        drawPicture();
    }

    /**
     * Process movement backward one picture code, i.e. picture position minus 1.
     */
    public void moveBackOnePictureCode() {
        picturePosition = firstSelectedPosition;
        decrementPicturePosition();
        drawPicture();
    }
    
    /**
     * Process movement forward one picture action through the picture code buffer. A
     * picture action is the full set of codes for a tool, e.g. all the points for a 
     * single draw absolute action. Picture actions are highlighted in black on the 
     * picture code JList.
     */
    public void moveForwardOnePictureAction() {
        if (picturePosition < (pictureCodes.size() - 1)) {
            PictureCode pictureCode = null;
            do {
                pictureCode = incrementPicturePosition();
            } while ((pictureCode != null) && !pictureCode.isActionCode());

            drawPicture();
        }
    }

    /**
     * Process movement forward one picture code, i.e. picture position plus 1.
     */
    public void moveForwardOnePictureCode() {
        if (picturePosition < (pictureCodes.size() - 1)) {
            if ((lastSelectedPosition - firstSelectedPosition) != 1) {
                picturePosition = firstSelectedPosition;
                incrementPicturePosition();
                drawPicture();
            } else {
                // If there is a selection interval in place and it is only two
                // picture codes long, then we're already where we need to be  as 
                // far as picture position is concerned, but we need the selection interval
                // be collapsed to a single position, i.e. the end of the interval.
                collapseSelectionInterval();
            }
        }
    }
    
    /**
     * Process movement to the start of the picture code buffer.
     */
    public void moveToStartOfPictureBuffer() {
        setPicturePosition(0);
        drawPicture();
    }

    /**
     * Process movement to the end of the picture code buffer.
     */
    public void moveToEndOfPictureBuffer() {
        if (picturePosition < (pictureCodes.size() - 1)) {
            setPicturePosition(pictureCodes.size() - 1);
            drawPicture();
        }
    }

    /**
     * Process deletion of the current picture action, i.e. the picture
     * action at the current picture position.
     */
    public void deleteCurrentPictureAction() {
        PictureCode pictureCode = deleteAtPicturePosition();
        while ((pictureCode != null) && (pictureCode.isDataCode())) {
            pictureCode = deleteAtPicturePosition();
        }
        pictureCache.clear(picturePosition);
        drawPicture();
    }

    /**
     * Returns the visual image to be drawn on the screen.
     * 
     * @return the visual image to be drawn on the screen.
     */
    public Canvas getVisualImage() {
        // Sync the main pixel array up to the canvas then return it.
        copyPixelsToContext2d(visualScreen, visualContext); 
        return visualImage;
    }

    /**
     * Returns the priority image to be drawn on the screen.
     * 
     * @return the priority image to be drawn on the screen.
     */
    public Canvas getPriorityImage() {
        // Sync the main pixel array up to the canvas then return it.
        copyPixelsToContext2d(priorityScreen, priorityContext);
        return priorityImage;
    }
    
    /**
     * Gets the raw RGB byte array for the priority screen.
     * 
     * @return The raw RGB byte array for the priority screen.
     */
    public Int32Array getPriorityScreen() {
        return priorityScreen;
    }
    
    /**
     * Loads the picture from the given array of raw AGI Picture data.
     */
    public void loadPicture(int[] rawPictureCodes) {
        super.loadPicture(rawPictureCodes);
        
        drawPicture();
    }
    
    /**
     * 'Saves' the AGI picture, returning the data as a byte array.
     * 
     * @param pictureFile the File to write the AGI picture out to.
     */
    public int[] savePicture() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Write each of the picture codes out to the byte array output stream..
        for (PictureCode pictureCode : this.getPictureCodes()) {
            if (pictureCode.isAbsolutePoint()) {
                int code = pictureCode.getCode();
                int x = (code & 0xFF00) >> 8;
                int y = (code & 0x00FF);
                out.write(x);
                out.write(y);
            } else {
                out.write(pictureCode.getCode());
            }
        }
        
        // Convert to unsigned int array.
        int index = 0;
        int[] data = new int[out.size()];
        for (byte b : out.toByteArray()) {
            data[index++] = (b & 0xFF);
        }
        
        editStatus.setUnsavedChanges(false);
        
        return data;
    }
    
    private native static void copyInt32Array(Int32Array src, Int32Array dest)/*-{
        var len = src.length;
        for (var i = 0; i < len; i++) {
            dest[i] = src[i];
        }
    }-*/;
}
