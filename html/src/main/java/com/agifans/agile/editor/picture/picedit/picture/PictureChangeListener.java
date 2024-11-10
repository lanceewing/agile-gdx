package com.agifans.agile.editor.picture.picedit.picture;

/**
 * Classes that want to be notified of changes to the Picture can implement this
 * interface and then register themselves with the Picture.
 * 
 * @author Lance Ewing
 */
public interface PictureChangeListener {

    /**
     * Invoked when one or more PictureCodes are added to the Picture.
     * 
     * @param fromIndex The index at which the codes started to be added.
     * @param toIndex The index at which the codes finished being added.
     */
    void pictureCodesAdded(int fromIndex, int toIndex);
    
    /**
     * Invoked when one or more Picturecodes are removed from the Picture.
     * 
     * @param fromIndex The index at which the codes started to be removed.
     * @param toIndex The index at which the codes finished being removed.
     */
    void pictureCodesRemoved(int fromIndex, int toIndex);
    
    /**
     * Invoked when there was a multi position selection interval in play
     * and a picture navigation action is forcing this to be collapsed to 
     * a single position, which will always be the end of the interval.
     */
    void selectionIntervalCollapsed();
}
