package com.agifans.agile.agilib.picedit;

import java.util.LinkedList;
import java.util.List;

import com.agifans.agile.EgaPalette;
import com.agifans.agile.agilib.Resource;
import com.agifans.agile.agilib.jagi.awt.Point;

public class Picture extends Resource {

    public static final int WIDTH = 160;
    public static final int HEIGHT = 168;
    
    /**
     * Holds the RGB values for the 16 EGA colours.
     */
    protected final static int[] colours = EgaPalette.colours;
    
    /**
     * Holds the linked list of picture codes for this picture.
     */
    protected LinkedList<PictureCode> pictureCodes;
    
    /**
     * Holds the editing status for this Picture.
     */
    protected EditStatus editStatus;
    
    /**
     * Says whether the picture is currently being loaded from a file.
     */
    protected boolean isLoading;
    
    /**
     * Says whether the picture is currently being drawn.
     */
    protected boolean isDrawing;
    
    /**
     * Holds the current position within the picture code buffer.
     */
    protected int picturePosition;
    
    public Picture() {
        this(new EditStatus());
    }
    
    public Picture(EditStatus editStatus) {
        this.editStatus = editStatus;
        this.pictureCodes = new LinkedList<>();
    }
    
    public Picture(byte[] rawData) {
        this();
        decode(rawData);
    }
    
    /**
     * Changes the EditStatus for this Picture. Intended mainly for when an 
     * overlay picture needs to be added to a pre-existing Picture by AGILE. Not
     * useful as such for the editor.
     * 
     * @param editStatus
     */
    public void setEditStatus(EditStatus editStatus) {
        this.editStatus = editStatus;
    }
    
    /**
     * Gets the EditStatus for this Picture.
     * 
     * @return the EditStatus for this Picture.
     */
    public EditStatus getEditStatus() {
        return editStatus;
    }
    
    /**
     * Decodes the given AGI Picture byte array, building up the list of PictureCodes.
     * 
     * @param rawData The raw byte array to decode.
     */
    public void decode(byte[] rawData) {
        // Convert to int array, as required by PICEDIT picture code.
        int[] rawPictureCodes = new int[rawData.length];
        for (int i=0; i<rawData.length; i++) {
            rawPictureCodes[i] = (rawData[i] & 0xFF);
        }
        loadPicture(rawPictureCodes);
    }
    
    /**
     * Clears the picture code buffer, picture cache and picture screens. This
     * would usually be done when it is a completely new picture, e.g. when 
     * loading a picture or creating a new picture.
     */
    public void clearPicture() {
        clearPictureCodes();
        clearPictureScreens();
        clearPictureCache();
    }
    
    protected void clearPictureScreens() {
    }
    
    protected void clearPictureCache() {
    }
    
    /**
     * Clears the picture code buffer.
     */
    public void clearPictureCodes() {
        if (pictureCodes != null) {
            firePictureCodesRemoved(0, pictureCodes.size() - 1);
        }
        picturePosition = 0;
        pictureCodes = new LinkedList<PictureCode>();
        pictureCodes.add(new PictureCode(PictureCodeType.END));
    }
    
    public void loadPicture(int[] rawPictureCodes) {
        // This stops the change listening from firing. We'll call that at the end of the method instead.
        isLoading = true;
        
        // Make sure we start with a clean picture.
        editStatus.clear();
        this.clearPicture();
        
        // Process the raw int array to create a PictureCodes LinkedList.
        int pictureCode, index = 0, x, y, brushCode = 0;
        while ((pictureCode = rawPictureCodes[index++]) != -1) {
            if (pictureCode != 0xFF) {
                switch (pictureCode) {
                    case 0xF0:
                        addPictureCode(PictureCodeType.SET_VISUAL_COLOR);
                        addPictureCode(PictureCodeType.COLOR_DATA, rawPictureCodes[index++]);
                        break;
                        
                    case 0xF1:
                        addPictureCode(PictureCodeType.SET_VISUAL_COLOR_OFF);
                        break;
                        
                    case 0xF2:
                        addPictureCode(PictureCodeType.SET_PRIORITY_COLOR);
                        addPictureCode(PictureCodeType.COLOR_DATA, rawPictureCodes[index++]);
                        break;
                        
                    case 0xF3:
                        addPictureCode(PictureCodeType.SET_PRIORITY_COLOR_OFF);
                        break;
                        
                    case 0xF4:
                        addPictureCode(PictureCodeType.DRAW_VERTICAL_STEP_LINE);
                        x = pictureCode = rawPictureCodes[index++];
                        y = pictureCode = rawPictureCodes[index++];
                        addPictureCode(x, y);
                        while (true) {
                            if ((y = rawPictureCodes[index++]) >= 0xF0) {
                                break;
                            }
                            addPictureCode(PictureCodeType.Y_POSITION_DATA, y, new Point(x, y));
                            if ((x = rawPictureCodes[index++]) >= 0xF0) {
                                break;
                            }
                            addPictureCode(PictureCodeType.X_POSITION_DATA, x, new Point(x, y));
                        }
                        index--;
                        break;
                        
                    case 0xF5:
                        addPictureCode(PictureCodeType.DRAW_HORIZONTAL_STEP_LINE);
                        x = pictureCode = rawPictureCodes[index++];
                        y = pictureCode = rawPictureCodes[index++];
                        addPictureCode(x, y);
                        while (true) {
                            if ((x = rawPictureCodes[index++]) >= 0xF0) {
                                break;
                            }
                            addPictureCode(PictureCodeType.X_POSITION_DATA, x, new Point(x, y));
                            if ((y = rawPictureCodes[index++]) >= 0xF0) {
                                break;
                            }
                            addPictureCode(PictureCodeType.Y_POSITION_DATA, y, new Point(x, y));
                        }
                        index--;
                        break;
                        
                    case 0xF6:
                        addPictureCode(PictureCodeType.DRAW_LINE);
                        while (true) {
                            if ((x = rawPictureCodes[index++]) >= 0xF0) {
                                break;
                            }
                            if ((y = rawPictureCodes[index++]) >= 0xF0) {
                                break;
                            }
                            addPictureCode(x, y);
                        }
                        index--;
                        break;
                        
                    case 0xF7:
                        addPictureCode(PictureCodeType.DRAW_SHORT_LINE);
                        x = pictureCode = rawPictureCodes[index++];
                        y = pictureCode = rawPictureCodes[index++];
                        addPictureCode(x, y);
                        while ((pictureCode = rawPictureCodes[index++]) < 0xF0) {
                            int dx = ((pictureCode & 0xF0) >> 4) & 0x0F;
                            int dy = (pictureCode & 0x0F);
                            if ((dx & 0x08) > 0) {
                                dx = (-1) * (dx & 0x07);
                            }
                            if ((dy & 0x08) > 0) {
                                dy = (-1) * (dy & 0x07);
                            }
                            x = x + dx;
                            y = y + dy;
                            addPictureCode(PictureCodeType.RELATIVE_POINT_DATA, pictureCode, new Point(x, y));
                        }
                        index--;
                        break;
                        
                    case 0xF8:
                        addPictureCode(PictureCodeType.DRAW_FILL);
                        while (true) {
                            if ((x = rawPictureCodes[index++]) >= 0xF0) {
                                break;
                            }
                            if ((y = rawPictureCodes[index++]) >= 0xF0) {
                                break;
                            }
                            addPictureCode(PictureCodeType.FILL_POINT_DATA, x, y);
                        }
                        index--;
                        break;
                        
                    case 0xF9:
                        addPictureCode(PictureCodeType.SET_BRUSH_TYPE);
                        brushCode = rawPictureCodes[index++];
                        addPictureCode(PictureCodeType.BRUSH_TYPE_DATA, brushCode);
                        break;
                        
                    case 0xFA:
                        addPictureCode(PictureCodeType.DRAW_BRUSH_POINT);
                        while (true) {
                            if ((brushCode & 0x20) > 0) {
                                if ((pictureCode = rawPictureCodes[index++]) >= 0xF0) {
                                    break;
                                }
                                addPictureCode(PictureCodeType.BRUSH_PATTERN_DATA, pictureCode);
                            }
                            if ((x = rawPictureCodes[index++]) >= 0xF0) {
                                break;
                            }
                            if ((y = rawPictureCodes[index++]) >= 0xF0) {
                                break;
                            }
                            addPictureCode(PictureCodeType.BRUSH_POINT_DATA, x, y);
                        }
                        index--;
                        break;
                        
                    case 0xFF:
                        // End of the picture.
                        break;
                        
                    default:
                        // An attempt to load a picture that is corrupt.
                        throw new RuntimeException("Unknown picture code : " + pictureCode);
                }
                
            } else {
                // 0xFF is the end of an AGI picture.
                break;
            }
        }
        
        editStatus.setTool(ToolType.NONE);
        editStatus.setUnsavedChanges(false);
        
        // Now that we've finished loading, trigger an event for the whole Picture.
        isLoading = false;
        firePictureCodesAdded(0, pictureCodes.size());
    }
    
    /**
     * Gets the current picture position.
     * 
     * @return The current picture position.
     */
    public int getPicturePosition() {
        return picturePosition;
    }

    /**
     * Sets the picture position to the given value.
     * 
     * @param picturePosition The new picture position.
     */
    public void setPicturePosition(int picturePosition) {
        this.picturePosition = picturePosition;
    }
    
    /**
     * Gets the size of the picture (number of picture codes, excluding the end code 0xFF).
     * 
     * @return The size of the picture (number of picture codes, excluding the end code 0xFF).
     */
    public int getSize() {
        return (pictureCodes.size() - 1);
    }
    
    /**
     * Gets the picture code buffer.
     * 
     * @return The picture code buffer.
     */
    public LinkedList<PictureCode> getPictureCodes() {
        return pictureCodes;
    }
    
    /**
     * Gets the PictureCode at the current picture position.
     * 
     * @return The PictureCode at the current picture position.
     */
    public PictureCode getCurrentPictureCode() {
        return pictureCodes.get(picturePosition);
    }
    
    /**
     * Adds a code to the picture code buffer.
     * 
     * @param type The type of PictureCode.
     */
    public void addPictureCode(PictureCodeType type) {
        addPictureCode(type, type.getActionCode(), null);
    }
    
    /**
     * Adds a code of type ABSOLUTE_POINT to the picture code buffer.
     * 
     * @param x The x position of the point.
     * @param y The y position of the point.
     */
    public void addPictureCode(int x, int y) {
        addPictureCode(PictureCodeType.ABSOLUTE_POINT_DATA, ((x << 8) | y), new Point(x, y));
    }
    
    /**
     * Adds a cod of the given point type to the picture code buffer.
     * 
     * @param pointType The type of PictureCode.
     * @param x The x position of the point.
     * @param y The y position of the point.
     */
    public void addPictureCode(PictureCodeType pointType, int x, int y) {
        addPictureCode(pointType, ((x << 8) | y), new Point(x, y));
    }
    
    /**
     * Adds a code to the picture code buffer.
     * 
     * @param type The type of PictureCode.
     * @param code The code to add to the picture code buffer.
     */
    public void addPictureCode(PictureCodeType type, int code) {
        addPictureCode(type, code, null);
    }
    
    /**
     * Adds a code to the picture code buffer.
     * 
     * @param type The type of PictureCode.
     * @param code The code to add to the picture code buffer.
     * @param point Optional Point specifying absolute location that the picture code relates to.
     */
    public void addPictureCode(PictureCodeType type, int code, Point point) {
        clearPictureCacheFromPosition(picturePosition);
        pictureCodes.add(picturePosition, new PictureCode(type, code, point));
        firePictureCodesAdded(picturePosition, picturePosition);
        picturePosition = picturePosition + 1;
        editStatus.setUnsavedChanges(true);
    }

    protected boolean cacheEntryExists(int picturePosition) {
        return false;
    }
    
    protected int restoreStateFromCache(int picturePosition) {
        return 0;
    }
    
    protected void addStateToCache(int picturePosition) {
    }
    
    /**
     * Draws the picture from the beginning up to the current picture position.
     */
    public void drawPicture() {
        int action = 0;
        int index = 0;

        // Tells other parts of the application that want to ask that we're drawing the picture now.
        isDrawing = true;
        
        if (cacheEntryExists(picturePosition)) {
            // Skip straight to the cached position.
            index = restoreStateFromCache(picturePosition);
            
        } else {
            // Clear the picture bitmaps to the original colours.
            clearPictureScreens();
           
            // When drawing from the start, we need to clear everything except for the data.
            editStatus.clear(false);
        }

        if ((picturePosition > 0) && (index < picturePosition)) {
            do {
                boolean isCacheable = true;
                
                // Get the next picture action.
                PictureCode pictureCode = pictureCodes.get(index++);
                action = pictureCode.getCode();
                
                // Process the actions data.
                switch (action) {
                    case 0xF0:
                        editStatus.setVisualColour(pictureCodes.get(index++).getCode());
                        isCacheable = false;
                        break;
                    case 0xF1:
                        editStatus.setVisualColour(EditStatus.VISUAL_OFF);
                        isCacheable = false;
                        break;
                    case 0xF2:
                        editStatus.setPriorityColour(pictureCodes.get(index++).getCode());
                        isCacheable = false;
                        break;
                    case 0xF3:
                        editStatus.setPriorityColour(EditStatus.PRIORITY_OFF);
                        isCacheable = false;
                        break;
                    case 0xF4:
                        editStatus.setTool(ToolType.STEPLINE);
                        index = drawPictureYCorner(pictureCodes, index);
                        break;
                    case 0xF5:
                        editStatus.setTool(ToolType.STEPLINE);
                        index = drawPictureXCorner(pictureCodes, index);
                        break;
                    case 0xF6:
                        editStatus.setTool(ToolType.LINE);
                        index = drawPictureAbsoluteLine(pictureCodes, index);
                        break;
                    case 0xF7:
                        editStatus.setTool(ToolType.SHORTLINE);
                        index = drawPictureRelativeDraw(pictureCodes, index);
                        break;
                    case 0xF8:
                        editStatus.setTool(ToolType.FILL);
                        index = drawPictureFill(pictureCodes, index);
                        break;
                    case 0xF9:
                        editStatus.setBrushCode(pictureCodes.get(index++).getCode());
                        isCacheable = false;
                        break;
                    case 0xFA:
                        editStatus.setTool(ToolType.BRUSH);
                        index = drawPicturePlotBrush(pictureCodes, index);
                        break;
                    case 0xFF:
                        // End of the picture.
                        break;
                    default:
                        // An attempt to load a picture that is corrupt.
                        break;
                }
                
                // Add the current picture state to the picture cache.
                if (isCacheable) {
                    addStateToCache(index);
                }
            } while ((index < picturePosition) && (action != 0xFF));
        }
        
        // If the current picture position is on a data code, then clear the selected tool. We don't allow inserts within a picture action.
        if (getCurrentPictureCode().isDataCode()) {
            editStatus.setTool(ToolType.NONE);
        }
        
        // Tells other parts of the application that want to ask that we are not longer drawing the picture.
        isDrawing = false;
    }

    /**
     * Returns true if the picture is currently being drawn; otherwise false.
     * 
     * @return true if the picture is currently being drawn; otherwise false.
     */
    public boolean isBeingDrawn() {
        return isDrawing;
    }
    
    /**
     * Draws a yCorner (drawing action 0xF4).
     * 
     * @param picturesCodes the List of picture codes to draw Y corners from.
     * @param index the index within the List to start processing from.
     * 
     * @return the index of the next picture action.
     */
    public int drawPictureYCorner(List<PictureCode> pictureCodes, int index) {
        int code, x1, x2, y1, y2;
        
        PictureCode pictureCode = pictureCodes.get(index++);
        code = pictureCode.getCode();
        x1 = (code & 0xFF00) >> 8;
        y1 = (code & 0x00FF);

        // A line must always have a least one point.
        putPixel(x1, y1);
        
        while (index <= picturePosition) {
            y2 = pictureCodes.get(index++).getCode();
            if (y2 >= 0xF0) {
                break;
            }
            drawLine(x1, y1, x1, y2);
            y1 = y2;
            if (index > picturePosition) {
                break;
            }
            x2 = pictureCodes.get(index++).getCode();
            if (x2 >= 0xF0) {
                break;
            }
            drawLine(x1, y1, x2, y1);
            x1 = x2;
        }

        return (index - 1);
    }

    /**
     * Draws an xCorner (drawing action 0xF5).
     * 
     * @param picturesCodes the List of picture codes to draw X corners from.
     * @param index the index within the List to start processing from.
     * 
     * @return the index of the next picture action.
     */
    public int drawPictureXCorner(List<PictureCode> pictureCodes, int index) {
        int code, x1, x2, y1, y2;

        PictureCode pictureCode = pictureCodes.get(index++);
        code = pictureCode.getCode();
        x1 = (code & 0xFF00) >> 8;
        y1 = (code & 0x00FF);

        // A line must always have a least one point.
        putPixel(x1, y1);
        
        while (index <= picturePosition) {
            x2 = pictureCodes.get(index++).getCode();
            if (x2 >= 0xF0) {
                break;
            }
            drawLine(x1, y1, x2, y1);
            x1 = x2;
            if (index > picturePosition) {
                break;
            }
            y2 = pictureCodes.get(index++).getCode();
            if (y2 >= 0xF0) {
                break;
            }
            drawLine(x1, y1, x1, y2);
            y1 = y2;
        }

        return (index - 1);
    }

    /**
     * Draws long lines to actual locations (cf. relative) (drawing action 0xF6).
     * 
     * @param picturesCodes the List of picture codes to draw absolute lines from.
     * @param index the index within the List to start processing from. 
     * 
     * @return the index of the next picture action.
     */
    public int drawPictureAbsoluteLine(List<PictureCode> pictureCodes, int index) {
        int code, x1, y1, x2, y2;

        PictureCode pictureCode = pictureCodes.get(index++);
        code = pictureCode.getCode();
        x1 = (code & 0xFF00) >> 8;
        y1 = (code & 0x00FF);

        // A line must always have a least one point.
        putPixel(x1, y1);
        
        while (index <= picturePosition) {
            pictureCode = pictureCodes.get(index++);
            if (pictureCode.getType() != PictureCodeType.ABSOLUTE_POINT_DATA) {
                break;
            }
            code = pictureCode.getCode();
            x2 = (code & 0xFF00) >> 8;
            y2 = (code & 0x00FF);
            drawLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
        }

        return (index - 1);
    }

    /**
     * Draws short lines relative to last position.  (drawing action 0xF7).
     * 
     * @param picturesCodes the List of picture codes to draw relative lines from.
     * @param index the index within the List to start processing from. 
     * 
     * @return the index of the next picture action.
     */
    public int drawPictureRelativeDraw(List<PictureCode> pictureCodes, int index) {
        int x1, y1, disp;
        int dx, dy;

        PictureCode pictureCode = pictureCodes.get(index++);
        int code = pictureCode.getCode();
        x1 = (code & 0xFF00) >> 8;
        y1 = (code & 0x00FF);

        // A line must always have a least one point.
        putPixel(x1, y1);
        
        while (index <= picturePosition) {
            disp = pictureCodes.get(index++).getCode();
            if (disp >= 0xF0) {
                break;
            }
            dx = ((disp & 0xF0) >> 4) & 0x0F;
            dy = (disp & 0x0F);
            if ((dx & 0x08) > 0) {
                dx = (-1) * (dx & 0x07);
            }
            if ((dy & 0x08) > 0) {
                dy = (-1) * (dy & 0x07);
            }
            drawLine(x1, y1, x1 + dx, y1 + dy);
            x1 += dx;
            y1 += dy;
        }

        return (index - 1);
    }

    /**
     * AGI flood fill. (drawing action 0xF8).
     * 
     * @param picturesCodes the List of picture codes to draw fills from.
     * @param index the index within the List to start processing from. 
     * 
     * @return the index of the next picture action.
     */
    public int drawPictureFill(List<PictureCode> pictureCodes, int index) {
        int code, x1, y1;

        while (index <= picturePosition) {
            PictureCode pictureCode = pictureCodes.get(index++);
            if (pictureCode.getType() != PictureCodeType.FILL_POINT_DATA) {
                break;
            }
            code = pictureCode.getCode();
            x1 = (code & 0xFF00) >> 8;
            y1 = (code & 0x00FF);
            fill(x1, y1);
        }

        return (index - 1);
    }

    /**
     * Plots points and various brush patterns. (drawing action 0xF8).
     *
     * @param picturesCodes the List of picture codes to plot brushes from.
     * @param index the index within the List to start processing from. 
     * 
     * @return the index of the next picture action.
     */
    public int drawPicturePlotBrush(List<PictureCode> pictureCodes, int index) {
        int code, x1, y1, patNum = 0;

        int patCode = editStatus.getBrushCode();

        while (index <= picturePosition) {
            if ((patCode & 0x20) > 0) {
                if ((patNum = pictureCodes.get(index++).getCode()) >= 0xF0) {
                    break;
                }
                patNum = (patNum >> 1 & 0x7f);
            }
            if (index > picturePosition) {
                break;
            }
            PictureCode pictureCode = pictureCodes.get(index++);
            if (pictureCode.getType() != PictureCodeType.BRUSH_POINT_DATA) {
                break;
            }
            code = pictureCode.getCode();
            x1 = (code & 0xFF00) >> 8;
            y1 = (code & 0x00FF);
            plotPattern(patNum, x1, y1);
        }

        return (index - 1);
    }

    /**
     * Draws a single pixel on the AGI picture.
     * 
     * @param x The X position of the pixel.
     * @param y The Y position of the pixel.
     */
    public void putPixel(int x, int y) {
        if ((x >= WIDTH) || (y >= HEIGHT)) {
            return;
        }
        
        int index = (y << 7) + (y << 5) + x;
        
        if (editStatus.isVisualDrawEnabled()) {
            editStatus.getVisualScreen()[index] = colours[editStatus.getVisualColour()];
        }
        if (editStatus.isPriorityDrawEnabled()) {
            editStatus.getPriorityScreen()[index] = colours[editStatus.getPriorityColour()];
            editStatus.getPriorityCodes()[index] = editStatus.getPriorityColour();
        }
    }
    
    /**
     * Draws a single pixel on the visual screen of the AGI picture.
     * 
     * @param x The X position of the pixel.
     * @param y The Y position of the pixel.
     */
    public void putVisualPixel(int x, int y) {
        if ((x >= WIDTH) || (y >= HEIGHT)) {
            return;
        }
        
        int index = (y << 7) + (y << 5) + x;
        
        editStatus.getVisualScreen()[index] = colours[editStatus.getVisualColour()];
    }
    
    /**
     * Gets the visual screen pixel at the given position.
     * 
     * @param x The X position of the pixel to get.
     * @param y The Y position of the pixel to get.
     * 
     * @return The visual screen pixel at the given position.
     */
    public int getVisualPixel(int x, int y) {
        return editStatus.getVisualScreen()[(y << 7) + (y << 5) + x];
    }
    
    /**
     * Draws a single pixel on the priority screen of the AGI picture.
     * 
     * @param x The X position of the pixel.
     * @param y The Y position of the pixel.
     */
    public void putPriorityPixel(int x, int y) {
        if ((x >= WIDTH) || (y >= HEIGHT)) {
            return;
        }
        
        int index = (y << 7) + (y << 5) + x;
        
        editStatus.getPriorityScreen()[index] = colours[editStatus.getPriorityColour()];
        editStatus.getPriorityCodes()[index] = editStatus.getPriorityColour();
    }
    
    /**
     * Gets the priority screen pixel at the given position.
     * 
     * @param x The X position of the pixel to get.
     * @param y The Y position of the pixel to get.
     * 
     * @return The priority screen pixel at the given position.
     */
    public int getPriorityPixel(int x, int y) {
        return editStatus.getPriorityScreen()[(y << 7) + (y << 5) + x];
    }
    
    /**
     * Draws a line.
     * 
     * @param x1 Start X Coordinate.
     * @param y1 Start Y Coordinate.
     * @param x2 End X Coordinate.
     * @param y2 End Y Coordinate.
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        int x, y;

        // Vertical Line.
        if (x1 == x2) {
            if (y1 > y2) {
                y = y1;
                y1 = y2;
                y2 = y;
            }
            for (; y1 <= y2; y1++) {
                putPixel(x1, y1);
            }
        }
        // Horizontal Line.
        else if (y1 == y2) {
            if (x1 > x2) {
                x = x1;
                x1 = x2;
                x2 = x;
            }
            for (; x1 <= x2; x1++) {
                putPixel(x1, y1);
            }
        } else {
            // Diagonal line
            int deltaX = x2 - x1;
            int deltaY = y2 - y1;
            int stepX = 1;
            int stepY = 1;
            int detDelta;
            int errorX;
            int errorY;
            int count;

            if (deltaY < 0) {
                stepY = -1;
                deltaY = -deltaY;
            }

            if (deltaX < 0) {
                stepX = -1;
                deltaX = -deltaX;
            }

            if (deltaY > deltaX) {
                count = deltaY;
                detDelta = deltaY;
                errorX = deltaY / 2;
                errorY = 0;
            } else {
                count = deltaX;
                detDelta = deltaX;
                errorX = 0;
                errorY = deltaX / 2;
            }

            x = x1;
            y = y1;

            putPixel(x, y);

            do {
                errorY = (errorY + deltaY);
                if (errorY >= detDelta) {
                    errorY -= detDelta;
                    y += stepY;
                }

                errorX = (errorX + deltaX);
                if (errorX >= detDelta) {
                    errorX -= detDelta;
                    x += stepX;
                }

                putPixel(x, y);
                count--;
                
            } while (count > 0);
            
            putPixel(x, y);
        }
    }

    /**
     * Performs a fill at the given position on the picture.
     * 
     * @param x the X position to fill at.
     * @param y the Y position to fill at.
     */
    public void fill(int x, int y) {
        // If both visual and priority drawing is disabled, then return immediately.
        if (!editStatus.isVisualDrawEnabled() && !editStatus.isPriorityDrawEnabled()) {
            return;
        }
        // If the visual fill colour is white, then return immediately.
        if (editStatus.getVisualColour() == 15) {
            return;
        }
        // If it is priority only fill, and fill colour is 4, then return immediately.
        if (!editStatus.isVisualDrawEnabled() && (editStatus.getPriorityColour() == 4)) {
            return;
        }

        int white = EgaPalette.white;
        int red = EgaPalette.red;
        
        FastQueue queue = new FastQueue();
        
        // Visual and priority fill.
        if (editStatus.isVisualDrawEnabled()) {
            if (editStatus.isPriorityDrawEnabled()) {
                
                // Enqueue the starting point.
                queue.enqueue(x);
                queue.enqueue(y);
                
                while (!queue.isEmpty()) {
                    // Get next point from the queue.
                    x = queue.dequeue();
                    y = queue.dequeue();

                    // Check if we can fill at this point
                    if (getVisualPixel(x, y) == white) {
                        
                        // Yes we can, so put the pixel.
                        putPixel(x, y);
                        
                        // Check then enqueue the four points around the pixel.
                        if ((x > 0) && (getVisualPixel(x - 1, y) == white)) {
                            queue.enqueue(x - 1);
                            queue.enqueue(y);
                        }
                        if ((x < (WIDTH-1)) && (getVisualPixel(x + 1, y) == white)) {
                            queue.enqueue(x + 1);
                            queue.enqueue(y);
                        }
                        if ((y > 0) && (getVisualPixel(x, y - 1) == white)) {
                            queue.enqueue(x);
                            queue.enqueue(y - 1);
                        }
                        if ((y < (HEIGHT-1)) && (getVisualPixel(x, y + 1) == white)) {
                            queue.enqueue(x);
                            queue.enqueue(y + 1);
                        }
                    }
                }
            }
            
            // Visual only fill.
            else {
            
                // Enqueue the starting point.
                queue.enqueue(x);
                queue.enqueue(y);
                
                while (!queue.isEmpty()) {
                    // Get next point from the queue.
                    x = queue.dequeue();
                    y = queue.dequeue();

                    // Check if we can fill at this point
                    if (getVisualPixel(x, y) == white) {
                        
                        // Yes we can, so put the pixel.
                        putVisualPixel(x, y);
                        
                        // Check then enqueue the four points around the pixel.
                        if ((x > 0) && (getVisualPixel(x - 1, y) == white)) {
                            queue.enqueue(x - 1);
                            queue.enqueue(y);
                        }
                        if ((x < (WIDTH-1)) && (getVisualPixel(x + 1, y) == white)) {
                            queue.enqueue(x + 1);
                            queue.enqueue(y);
                        }
                        if ((y > 0) && (getVisualPixel(x, y - 1) == white)) {
                            queue.enqueue(x);
                            queue.enqueue(y - 1);
                        }
                        if ((y < (HEIGHT-1)) && (getVisualPixel(x, y + 1) == white)) {
                            queue.enqueue(x);
                            queue.enqueue(y + 1);
                        }
                    }
                }
            }
        
        // Priority only fill.
        } else if (editStatus.isPriorityDrawEnabled()) {
            
            // Enqueue the starting point.
            queue.enqueue(x);
            queue.enqueue(y);
            
            while (!queue.isEmpty()) {
                // Get next point from the queue.
                x = queue.dequeue();
                y = queue.dequeue();

                // Check if we can fill at this point
                if (getPriorityPixel(x, y) == red) {
                    
                    // Yes we can, so put the pixel.
                    putPriorityPixel(x, y);
                    
                    // Check then enqueue the four points around the pixel.
                    if ((x > 0) && (getPriorityPixel(x - 1, y) == red)) {
                        queue.enqueue(x - 1);
                        queue.enqueue(y);
                    }
                    if ((x < (WIDTH-1)) && (getPriorityPixel(x + 1, y) == red)) {
                        queue.enqueue(x + 1);
                        queue.enqueue(y);
                    }
                    if ((y > 0) && (getPriorityPixel(x, y - 1) == red)) {
                        queue.enqueue(x);
                        queue.enqueue(y - 1);
                    }
                    if ((y < (HEIGHT-1)) && (getPriorityPixel(x, y + 1) == red)) {
                        queue.enqueue(x);
                        queue.enqueue(y + 1);
                    }
                }
            }
        }
    }

    /**
     * Simple queue for storing queued points during AGI fill operation.
     */
    static class FastQueue {
        
        private static final int MAX_SIZE = 8000;
        
        private int[] data;
        private int eIndex;
        private int dIndex;
        
        FastQueue() {
            data = new int[MAX_SIZE];
            eIndex = 0;
            dIndex = 0;
        }
        
        void clear() {
            eIndex = dIndex = 0;
        }
        
        boolean isEmpty() {
            return eIndex == dIndex;
        }
        
        void enqueue(int val) {
            if (eIndex + 1 == dIndex || (eIndex + 1 == MAX_SIZE && dIndex == 0)) {
                throw new RuntimeException("Queue overflow");
            }
            data[eIndex++] = val;
            if (eIndex == MAX_SIZE) {
                eIndex = 0;
            }
        }
        
        int dequeue() {
            if (dIndex == MAX_SIZE) {
                dIndex = 0;
            }
            if (dIndex == eIndex) {
                throw new RuntimeException("The queue is empty");
            }
            return data[dIndex++];
        }
    }
    
    /** Circle Bitmaps */
    public static final short circles[][] = new short[][] { { 0x80 }, { 0xfc }, { 0x5f, 0xf4 }, { 0x66, 0xff, 0xf6, 0x60 }, { 0x23, 0xbf, 0xff, 0xff, 0xee, 0x20 }, { 0x31, 0xe7, 0x9e, 0xff, 0xff, 0xde, 0x79, 0xe3, 0x00 }, { 0x38, 0xf9, 0xf3, 0xef, 0xff, 0xff, 0xff, 0xfe, 0xf9, 0xf3, 0xe3, 0x80 }, { 0x18, 0x3c, 0x7e, 0x7e, 0x7e, 0xff, 0xff, 0xff, 0xff, 0xff, 0x7e, 0x7e, 0x7e, 0x3c, 0x18 } };

    /** Splatter Brush Bitmaps */
    public static final short splatterMap[] = new short[] { 0x20, 0x94, 0x02, 0x24, 0x90, 0x82, 0xa4, 0xa2, 0x82, 0x09, 0x0a, 0x22, 0x12, 0x10, 0x42, 0x14, 0x91, 0x4a, 0x91, 0x11, 0x08, 0x12, 0x25, 0x10, 0x22, 0xa8, 0x14, 0x24, 0x00, 0x50, 0x24, 0x04 };

    /** Starting Bit Position */
    public static final short splatterStart[] = new short[] { 0x00, 0x18, 0x30, 0xc4, 0xdc, 0x65, 0xeb, 0x48, 0x60, 0xbd, 0x89, 0x05, 0x0a, 0xf4, 0x7d, 0x7d, 0x85, 0xb0, 0x8e, 0x95, 0x1f, 0x22, 0x0d, 0xdf, 0x2a, 0x78, 0xd5, 0x73, 0x1c, 0xb4, 0x40, 0xa1, 0xb9, 0x3c, 0xca, 0x58, 0x92, 0x34, 0xcc, 0xce, 0xd7, 0x42, 0x90, 0x0f, 0x8b, 0x7f, 0x32, 0xed, 0x5c, 0x9d, 0xc8, 0x99, 0xad, 0x4e, 0x56, 0xa6, 0xf7, 0x68, 0xb7, 0x25, 0x82, 0x37, 0x3a, 0x51, 0x69, 0x26, 0x38, 0x52, 0x9e, 0x9a, 0x4f, 0xa7, 0x43, 0x10, 0x80, 0xee, 0x3d, 0x59, 0x35, 0xcf, 0x79, 0x74, 0xb5, 0xa2, 0xb1, 0x96, 0x23, 0xe0, 0xbe, 0x05, 0xf5, 0x6e, 0x19, 0xc5, 0x66, 0x49, 0xf0, 0xd1, 0x54, 0xa9, 0x70, 0x4b, 0xa4, 0xe2, 0xe6, 0xe5, 0xab, 0xe4, 0xd2, 0xaa, 0x4c, 0xe3, 0x06, 0x6f, 0xc6, 0x4a, 0xa4, 0x75, 0x97, 0xe1 };

    /**
     * Plots a brush pattern. Draws pixels, circles, squares, or splatter 
     * brush patterns depending on the pattern code.
     * 
     * @param patNum the pattern number to use.
     * @param x the X position to plot at. 
     * @param y the Y position to plot at.
     */
    public void plotPattern(int patNum, int x, int y) {
        int circlePos = 0;
        int x1, y1, penSize, bitPos = splatterStart[patNum];
        int patCode = editStatus.getBrushCode();

        penSize = (patCode & 7);

        if (x < ((penSize / 2) + 1)) {
            x = ((penSize / 2) + 1);

        } else if (x > 160 - ((penSize / 2) + 1)) {
            x = 160 - ((penSize / 2) + 1);
        }

        if (y < penSize) {
            y = penSize;

        } else if (y >= 168 - penSize) {
            y = 167 - penSize;
        }

        for (y1 = y - penSize; y1 <= y + penSize; y1++) {
            for (x1 = x - ((int) Math.ceil((float) penSize / 2)); x1 <= x + ((int) Math.floor((float) penSize / 2)); x1++) {
                if ((patCode & 0x10) > 0) { /* Square */
                    if ((patCode & 0x20) > 0) {
                        if (((splatterMap[bitPos >> 3] >> (7 - (bitPos & 7))) & 1) > 0) {
                            putPixel(x1, y1);
                        }
                        bitPos++;
                        if (bitPos == 0xff) {
                            bitPos = 0;
                        }
                    } else {
                        putPixel(x1, y1);
                    }
                } else { /* Circle */
                    if (((circles[patCode & 7][circlePos >> 3] >> (7 - (circlePos & 7))) & 1) > 0) {
                        if ((patCode & 0x20) > 0) {
                            if (((splatterMap[bitPos >> 3] >> (7 - (bitPos & 7))) & 1) > 0) {
                                putPixel(x1, y1);
                            }
                            bitPos++;
                            if (bitPos == 0xff) {
                                bitPos = 0;
                            }
                        } else {
                            putPixel(x1, y1);
                        }
                    }
                    circlePos++;
                }
            }
        }
    }
    
    public void clearPictureCacheFromPosition(int picturePosition) {
    }
    
    public void firePictureCodesRemoved(int fromIndex, int toIndex) {
    }
    
    public void firePictureCodesAdded(int fromIndex, int toIndex) {
    }
}
