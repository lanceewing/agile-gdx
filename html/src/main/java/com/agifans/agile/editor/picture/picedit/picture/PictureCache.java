package com.agifans.agile.editor.picture.picedit.picture;

import java.util.SortedMap;
import java.util.TreeMap;

import com.agifans.agile.agilib.picedit.BrushShape;
import com.agifans.agile.agilib.picedit.BrushTexture;
import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.agilib.picedit.ToolType;

import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

/**
 * This class is a cache of data related to the Picture drawn up to 
 * each of the navigatable picture positions, i.e. for each position within
 * the picture that the user can navigate to there will be an entry in 
 * this cache that will allow the Picture class to quickly render the
 * picture as it would be drawn for that given position. The idea is that
 * the Picture class does not have to draw the picture from the start 
 * every time the user navigates through the picture code buffer if that
 * data already exists in this cache.
 * 
 * @author Lance Ewing
 */
public class PictureCache {

    /**
     * A map of picture position to 
     */
    private TreeMap<Integer, PictureCacheEntry> cache;

    /**
     * Holds the editing status of the PICEDIT application.
     */
    private EditStatus editStatus;
    
    /**
     * Constructor for PictureCache.
     * 
     * @param editStatus The editing status of the PICEDIT application.
     */
    public PictureCache(EditStatus editStatus) {
        this.cache = new TreeMap<Integer, PictureCacheEntry>();
        this.editStatus = editStatus;
    }

    /**
     * Clears the picture cache.
     */
    public void clear() {
        this.cache.clear();
    }
    
    /**
     * Clears the picture cache from the given picture position.
     * 
     * @param fromPicturePosition The picture position to clear the picture from.
     */
    public void clear(int fromPicturePosition) {
        if (cache.higherKey(fromPicturePosition) != null) {
            SortedMap<Integer, PictureCacheEntry> entriesBelow = cache.headMap(fromPicturePosition);
            cache.clear();
            cache.putAll(entriesBelow);
        }
    }
    
    // Some notes about when the cache will be used or updated.
    //
    //    1. Load picture: Obviously has to draw the new picture from the start.
    //    2. Delete current picture action: Can redraw from current position to end of picture.
    //    3. Enter position: Can use cached image for that position since no change involved.
    //    4. Back one action: Can use cached image.
    //    5. Move forward one action: Can use cached image.
    //    6. Move to end of picture: Can use cached image.
    //    7. Move to start of picture: Blank screen anyway.
    //    8. Escaping out of enter position: Cached image.
    //    9. New picture: Clear all cached images and draw blank screen.
    //    10. Slider position change: Use cached image.
    //    11. Drawing actions use Picture to draw that action only, on top of the current picture. Needs to cache every position from that point to the end? (or invalidate the cache from that point to the end)

    /**
     * Adds a new entry to the picture cache.
     * 
     * @param picturePosition The position that this entry relates to.
     * @param visualScreen The pixel data for the visual screen of the picture.
     * @param priorityScreen The pixel data for the priority screen of the picture.
     * 
     * @return The newly added PictureCacheEntry.
     */
    public PictureCacheEntry addCacheEntry(int picturePosition, Int32Array visualScreen, Int32Array priorityScreen) {
    	// Copy the three screen arrays.
        Int32Array visualScreenCopy = TypedArrays.createInt32Array(visualScreen.length());
        copyInt32Array(visualScreen, visualScreenCopy);
        Int32Array priorityScreenCopy = TypedArrays.createInt32Array(priorityScreen.length());
        copyInt32Array(priorityScreen, priorityScreenCopy);
    	
    	// Create an entry to add to the picture cache for this osition.
    	PictureCacheEntry cacheEntry = new PictureCacheEntry(picturePosition, visualScreenCopy, priorityScreenCopy);
    	
    	this.cache.put(picturePosition, cacheEntry);
    	
    	return cacheEntry;
    }
    
    private native static void copyInt32Array(Int32Array src, Int32Array dest)/*-{
        var len = src.length;
        for (var i = 0; i < len; i++) {
            dest[i] = src[i];
        }
    }-*/;
    
    /**
     * Gets the cache entry at the given picture position, or the closest position
     * below the given position. If the picture position in the returned cache entry
     * does not match the picture position of the method parameter then there was no
     * matching entry and the closest entry below that has been returned instead. If
     * no entry exists below the picturePosition then null is returned.
     * 
     * @param picturePosition The picture position to get the cache entry for.
     * 
     * @return The cache entry, as described above.
     */
    public PictureCacheEntry getCacheEntry(int picturePosition) {
    	PictureCacheEntry cacheEntry = this.cache.get(picturePosition);
    	if (cacheEntry == null) {
    		Integer closestPosition = this.cache.lowerKey(picturePosition);
    		if (closestPosition != null) {
    			cacheEntry = this.cache.get(closestPosition);
    		}
    	}

    	return cacheEntry;
    }
    
    /**
     * An entry in the picture cache is of this type. It contains the screen
     * data as it is at the associated picture position and also a subset of
     * the EditStatus attributes that are relevant to that picture position.
     */
    public class PictureCacheEntry {

    	/**
    	 * The position that this entry relates to.
    	 */
    	private int picturePosition;
    	
        /**
         * Holds the pixel data for the visual screen of the picture.
         */
        private Int32Array visualScreen;

        /**
         * Holds the pixel data for the priority screen of the picture.
         */
        private Int32Array priorityScreen;
        
        // This is the subset of data from the EditStatus that the Picture class
        // alters when drawing the picture. For this reason it needs to be cached
        // along with the screen data. This is so that the EditStatus can be adjusted
        // to match the given picture position.
        private ToolType tool;
        private int visualColour;
        private int priorityColour;
        private int brushSize;
        private BrushShape brushShape;
        private BrushTexture brushTexture;

        /**
         * Constructor for PictureCache.
         * 
         * @param picturePosition The picture position that this entry relates to.
	     * @param visualScreen The pixel data for the visual screen of the picture.
	     * @param priorityScreen The pixel data for the priority screen of the picture.
         */
        public PictureCacheEntry(int picturePosition, Int32Array visualScreen, Int32Array priorityScreen) {
        	this.picturePosition = picturePosition;
        	this.visualScreen = visualScreen;
        	this.priorityScreen = priorityScreen;
        	
        	this.tool = editStatus.getTool();
        	this.visualColour = editStatus.getVisualColour();
        	this.priorityColour = editStatus.getPriorityColour();
        	this.brushSize = editStatus.getBrushSize();
        	this.brushShape = editStatus.getBrushShape();
        	this.brushTexture = editStatus.getBrushTexture();
        }
        
        public int getPicturePosition() {
			return picturePosition;
		}

		public void setPicturePosition(int picturePosition) {
			this.picturePosition = picturePosition;
		}

		public Int32Array getVisualScreen() {
            return visualScreen;
        }
        
        public void setVisualScreen(Int32Array visualScreen) {
            this.visualScreen = visualScreen;
        }
        
        public Int32Array getPriorityScreen() {
            return priorityScreen;
        }
        
        public void setPriorityScreen(Int32Array priorityScreen) {
            this.priorityScreen = priorityScreen;
        }
        
        public ToolType getTool() {
            return tool;
        }
        
        public void setTool(ToolType tool) {
            this.tool = tool;
        }
        
        public int getVisualColour() {
            return visualColour;
        }
        
        public void setVisualColour(int visualColour) {
            this.visualColour = visualColour;
        }
        
        public int getPriorityColour() {
            return priorityColour;
        }
        
        public void setPriorityColour(int priorityColour) {
            this.priorityColour = priorityColour;
        }
        
        public int getBrushSize() {
            return brushSize;
        }
        
        public void setBrushSize(int brushSize) {
            this.brushSize = brushSize;
        }
        
        public BrushShape getBrushShape() {
            return brushShape;
        }
        
        public void setBrushShape(BrushShape brushShape) {
            this.brushShape = brushShape;
        }
        
        public BrushTexture getBrushTexture() {
            return brushTexture;
        }
        
        public void setBrushTexture(BrushTexture brushTexture) {
            this.brushTexture = brushTexture;
        }
    }

    /**
     * Returns the size of the cache.
     * 
     * @return The size of the cache.
     */
    public int size() {
        return cache.size();
    }
}
