package com.agifans.agile.editor.picture.picedit.gui.frame;

import com.agifans.agile.EgaPalette;
import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.editor.picture.picedit.picture.Picture;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * This panel is responsible for rendering the picture, temporary lines, the
 * background image if it is activated, the bands if it is activated, the
 * dual mode if it is activated and Ego if Ego test mode is activated,
 * essentially everything that is drawn within the picture part of a PictureFrame.
 * 
 * @author Lance Ewing
 */
public class PicturePanel extends SimplePanel {

    /**
     * Holds the RGB values for the 16 EGA colours.
     */
    private final static int[] colours = EgaPalette.colours;
    
    /**
     * The Image for the overlay screen.
     */
    private Canvas overlayScreenImage;

    /**
     * The canvas 2d context for the overlay screen.
     */
    private Context2d overlayScreenContext;
    
    /**
     * The RGBA data array for the overlay screen.
     */
    private Int32Array overlayScreen;
    
    /**
     * The AGI picture being edited.
     */
    private Picture picture;

    /**
     * Holds the current "editing" state of everything within PICEDIT.
     */
    private EditStatus editStatus;

    /**
     * Holds two off screen images that are used interchangeably for offscreen rendering.
     */
    private OffScreenGraphics offScreenGraphics;
    
    /**
     * On-screen graphics content within the PicturePanel. 
     */
    private Canvas onScreenCanvas;
    
    /**
     * On-screen canvas 2d context.
     */
    private Context2d onScreenContext;
    
    /**
     * Holds the RGB pixel data that existed on the overlay screen underneath the temporary line.
     */
    private int[] bgLineData;
    
    /**
     * Constructor for PicturePanel.
     * 
     * @param editStatus The EditStatus holding current picture editor state.
     * @param picture The AGI PICTURE currently being edited.
     */
    public PicturePanel(EditStatus editStatus, Picture picture) {
        this.editStatus = editStatus;
        this.picture = picture;
        this.bgLineData = new int[1024];
        this.bgLineData[0] = 0;

        // Create the canvas that fills the panel. This is the visible "on screen" canvas.
        onScreenCanvas = Canvas.createIfSupported();
        onScreenCanvas.getCanvasElement().setWidth(320 * editStatus.getZoomFactor());
        onScreenCanvas.getCanvasElement().setHeight(168 * editStatus.getZoomFactor());
        onScreenContext = onScreenCanvas.getContext2d();
        add(onScreenCanvas);
        
        createOverlayScreenImage(160, 168);
        
        // TODO: Add back in.
        //createPriorityBandsImage(PictureType.AGI);
        
        setPixelSize(320 * editStatus.getZoomFactor(), Picture.HEIGHT * editStatus.getZoomFactor());
    }
    
    public void setPicture(Picture picture) {
        this.picture = picture;
    }
    
    public Canvas getOnScreenCanvas() {
        return onScreenCanvas;
    }
    
    /**
     * Invoked when a new background image is loaded.
     */
    public void clearOffscreenGraphics() {
        offScreenGraphics.clear();
    }
    
    /**
     * Refreshes the on screen image with the latest offscreen and overlay screen.
     */
    public void refresh() {
        paintOffscreenImage();
        paint();
    }
    
    /**
     * Paints the picture panel on the offscreen Graphics. This is kept separate from the 
     * paint() method for performance reasons. The screen refresh timer invokes this method
     * at regular intervals.
     */
    public void paintOffscreenImage() {
        // NOTE: Called by a screen refresh timer.
    	if (offScreenGraphics != null) {
	    	Context2d offScreenGC = offScreenGraphics.getActiveGraphics();
	    	
	        // Draw the background image (if there is one) to the offscreen image.
	        //if ((this.backgroundImage != null) && (editStatus.isBackgroundEnabled())) {
	        //    offScreenGC.drawImage(this.backgroundImage, 0, 0, 320, editStatus.getPictureType().getHeight(), this);
	        //} else {
	    	
	            // Otherwise use the default background colour for the corresponding AGI screen (visual/priority).
	            if (editStatus.isDualModeEnabled()) {
	                offScreenGC.setFillStyle(EgaPalette.toCssRgba(EgaPalette.red));
	            } else if (!editStatus.isPriorityShowing()) {
	                offScreenGC.setFillStyle(EgaPalette.toCssRgba(EgaPalette.white));
	            } else if (editStatus.isBandsOn()) {
	                offScreenGC.setFillStyle(EgaPalette.toCssRgba(EgaPalette.darkgrey));
	            } else {
	                offScreenGC.setFillStyle(EgaPalette.toCssRgba(EgaPalette.red));
	            }
	            offScreenGC.fillRect(0, 0, 320, Picture.HEIGHT);
	            
	        //}
	
	        if (editStatus.isDualModeEnabled()) {
	            
//	            // Dual mode is when the priority and visual screens mix.
//	            offScreenGC.drawImage(picture.getPriorityImage(), 0, 0, 320, editStatus.getPictureType().getHeight(), this);
//	
//	            // To create the effect demonstrated by Joakim in APE, we need a solid white.
//	            BufferedImage tmpVisualImage = new BufferedImage(320, editStatus.getPictureType().getHeight(), BufferedImage.TYPE_INT_ARGB);
//	            Graphics tmpVisualGraphics = tmpVisualImage.getGraphics();
//	            tmpVisualGraphics.setColor(EgaPalette.WHITE);
//	            tmpVisualGraphics.fillRect(0, 0, 320, editStatus.getPictureType().getHeight());
//	            tmpVisualGraphics.drawImage(picture.getVisualImage(), 0, 0, 320, editStatus.getPictureType().getHeight(), this);
//	
//	            // Build a RescapeOp to perform the 50% transparency.
//	            float[] scales = { 1f, 1f, 1f, 0.5f };
//	            float[] offsets = new float[4];
//	            RescaleOp rop = new RescaleOp(scales, offsets, null);
//	
//	            // Draw the visual screen on top of the priority screen with 50% transparency.
//	            offScreenGC.drawImage(tmpVisualImage, rop, 0, 0);
	
	        } else {
	            if (editStatus.isPriorityShowing()) {
	                offScreenGC.drawImage(picture.getPriorityImage().getCanvasElement(), 0, 0, 320, Picture.HEIGHT);
	            } else {
	                offScreenGC.drawImage(picture.getVisualImage().getCanvasElement(), 0, 0, 320, Picture.HEIGHT);
	            }
	        }
	
	        if (editStatus.isBandsOn()) {
	            //offScreenGC.drawImage(this.bandsImage, 0, 0, 320, editStatus.getPictureType().getHeight(), this);
	        }
	        
	        if (editStatus.isEgoTestEnabled()) {
	            //egoTestHandler.drawEgo(offScreenGC, 1);
	        }
	        
	        offScreenGraphics.toggle();
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
     * Paints the PICEDIT screen.
     * 
     * @param g the Graphics object to paint on.
     */
    public void paint() {
    	if (offScreenGraphics == null) {
    		offScreenGraphics = new OffScreenGraphics();
    	}
    	
        // Display the off screen image to the user, stretched by the zoom factor.
        onScreenContext.drawImage(
                offScreenGraphics.getImage().getCanvasElement(), 0, 0, 320 * editStatus.getZoomFactor(), 
                Picture.HEIGHT * editStatus.getZoomFactor());
        
        // Draw the overlay screen on top of everything else. This is mainly for the temporary lines.
        if (editStatus.isLineBeingDrawn()) {
            // Sync the pixel array with the canvas, then draw.
            copyPixelsToContext2d(overlayScreen, overlayScreenContext);
        	onScreenContext.drawImage(
        	        overlayScreenImage.getCanvasElement(), 0, 0, 320 * editStatus.getZoomFactor(), 
        	        Picture.HEIGHT * editStatus.getZoomFactor());
        } else if (bgLineData[0] != 0) {
        	// Clear temporary line if line is no longer being drawn.
        	clearTemporaryLine();
        }
        
        // Highlight the current selection if the zoom factor is big enough.
        if (editStatus.getZoomFactor() > 1) {
            //highlightSelection(g);
        }
    }

    // TODO: Implement highlight selection
    //    /**
    //     * Highlights the currently selected picture codes by drawing boxes around the points.
    //     * 
    //     * @param graphics The Graphics2D to draw the highlight boxes on.
    //     */
    //    private void highlightSelection(Graphics graphics) {
    //        // TODO: This falls over if the selection is at the end of the picture and the selection is deleted.
    //        int firstSelectedPosition = picture.getFirstSelectedPosition();
    //        int lastSelectedPosition = picture.getLastSelectedPosition();
    //        
    //        if ((firstSelectedPosition > -1) && (lastSelectedPosition > -1)) {
    //            List<PictureCode> pictureCodes = picture.getPictureCodes();
    //            for (int picturePosition = firstSelectedPosition; picturePosition <= lastSelectedPosition; picturePosition++) {
    //                PictureCode pictureCode = pictureCodes.get(picturePosition);
    //                // It only makes sense to do something for data codes, and only if they're points.
    //                if (pictureCode.isDataCode()) {
    //                    Point point = pictureCode.getPoint();
    //
    //                    if (point != null) {
    //                        // Calculate the x and y position of the point, scaling for zoom factor.
    //                        // TODO: Need to adjust this code for SCI0.
    //                        int x = (point.x << 1) * editStatus.getZoomFactor();
    //                        int y = (point.y) * editStatus.getZoomFactor();
    //                        
    //                        graphics.setColor(Color.RED);
    //                        graphics.drawLine(x - 2, y - 2, (x - 2) + (editStatus.getZoomFactor() << 1) + 3, (y - 2) + editStatus.getZoomFactor() + 3);
    //                        graphics.drawLine(x - 2, (y - 2) + editStatus.getZoomFactor() + 3, (x - 2) + (editStatus.getZoomFactor() << 1) + 3, y - 2);
    //                    }
    //                }
    //            }
    //        }
    //    }
    
//    /**
//     * Creates the Image that is displayed when the show priority bands feature
//     * is turned on.
//     * 
//     * @param pictureType The type of picture being edited (AGI/SCI0).
//     */
//    private void createPriorityBandsImage(PictureType pictureType) {
//        bandsImage = new BufferedImage(pictureType.getWidth(), pictureType.getHeight(), BufferedImage.TYPE_INT_ARGB);
//        Graphics bandsGraphics = bandsImage.getGraphics();
//
//        // Draw the bands onto the image so it is ready to be displayed when
//        // needed.
//        if (pictureType.equals(PictureType.SCI0)) {
//            int currentPriorityBand = 0;
//            
//            // For SCI0, the top 42 lines are for priority 0. The other 14 bands
//            // get an even share of the 148 remaining lines (which, btw, doesn't
//            // divide evenly, so the bands are not even as then are in AGI).
//            for (int y = 0; y < 190; y++) {
//                int priorityBand = ((int) ((y - 42) / ((190 - 42) / 14))) + 1;
//
//                if (priorityBand != currentPriorityBand) {
//                    currentPriorityBand = priorityBand;
//                    bandsGraphics.setColor(EgaPalette.COLOR_OBJECTS[priorityBand]);
//                    bandsGraphics.drawLine(0, y, 319, y);
//                }
//            }
//
//        } else if (pictureType.equals(PictureType.AGI)) {
//            int currentPriorityBand = 4;
//            
//            for (int y = 0; y < 168; y++) {
//                // For AGI it is evenly split, 168 lines split 14 ways.
//                int priorityBand = (y / 12) + 1;
//
//                // Make sure priority band is 4 or above for AGI since the
//                // bottom four priority colours are reserved as control lines.
//                if (priorityBand < 4) {
//                    priorityBand = 4;
//                }
//
//                if (priorityBand != currentPriorityBand) {
//                    currentPriorityBand = priorityBand;
//                    bandsGraphics.setColor(EgaPalette.COLOR_OBJECTS[priorityBand]);
//                    bandsGraphics.drawLine(0, y, 319, y);
//                }
//            }
//        }
//    }

    /**
     * Clears the overlay screen that temporary lines are drawn to.
     */
    public void clearOverlayScreen() {
        // Set all bytes to 0, which will be transparent.
        for (int i=0; i<overlayScreen.length(); i++) {
            overlayScreen.set(i, EgaPalette.transparent);
        }
    }

    /**
     * Creates the overlay image on which the temporary lines are drawn.
     */
    private void createOverlayScreenImage(int width, int height) {
        overlayScreenImage = createImage(width, height);
        overlayScreenContext = overlayScreenImage.getContext2d();
        overlayScreen = TypedArrays.createInt32Array(width * height);
        clearOverlayScreen();
    }

//    /**
//     * Sets the background image.
//     * 
//     * @param backgroundImage the background image.
//     */
//    public void setBackgroundImage(Image backgroundImage) {
//        this.backgroundImage = backgroundImage;
//    }
    
    /**
     * Clears the previously drawn temporary line by redrawing the pixels that where
     * behind it prior to the line being drawn. Usually this would be transparent 
     * pixels, but it supports other things being on the overlay screen... just in 
     * case this is ever needed.
     */
    private void clearTemporaryLine() {
        // Redraw the pixels that were behind the previous temporary line.
        int[] lineData = this.bgLineData;
        int bgLineLength = lineData[0];
        if (bgLineLength > 0) {
            for (int i = 1; i < bgLineLength;) {
                int index = lineData[i++];
                overlayScreen.set(index, lineData[i++]);
            }
            
            // Start again with a fresh array.
            this.bgLineData = new int[1024];
            this.bgLineData[0] = 0;
        }
    }
    
    /**
     * Draws a temporary picture line. These are the lines that are drawn while
     * a line drawing tool is active (line, pen, step). The line follows the
     * mouse's movements and allows the user to see where the line is going to
     * fall if they click in that position.
     * 
     * @param x1 Start X Coordinate.
     * @param y1 Start Y Coordinate.
     * @param x2 End X Coordinate.
     * @param y2 End Y Coordinate.
     * @param c the colour of the line.
     */
    public final void drawTemporaryLine(int x1, int y1, int x2, int y2, int c) {
        int x, y, index, endIndex, rgbCode;

        // Calculate flash index up front before x1/y1/x2/y2 are adjusted.
        int flashIndex = (y2 << 7) + (y2 << 5) + x2;
        
        // Redraw the pixels that were behind the previous temporary line.
        clearTemporaryLine();

        // Start storing at index 1. We'll use 0 for the length.
        int bgIndex = 1;
        
        // Vertical Line.
        if (x1 == x2) {
            if (y1 > y2) {
                y = y1;
                y1 = y2;
                y2 = y;
            }

            index = (y1 << 7) + (y1 << 5) + x1;
            endIndex = (y2 << 7) + (y2 << 5) + x2;
            rgbCode = colours[c];

            for (; index <= endIndex; index += 160) {
                bgLineData[bgIndex++] = index;
                bgLineData[bgIndex++] = overlayScreen.get(index);
                overlayScreen.set(index, rgbCode);
            }
        }
        // Horizontal Line.
        else if (y1 == y2) {
            if (x1 > x2) {
                x = x1;
                x1 = x2;
                x2 = x;
            }

            index = (y1 << 7) + (y1 << 5) + x1;
            endIndex = (y2 << 7) + (y2 << 5) + x2;
            rgbCode = colours[c];

            for (; index <= endIndex; index++) {
                bgLineData[bgIndex++] = index;
                bgLineData[bgIndex++] = overlayScreen.get(index);
                overlayScreen.set(index, rgbCode);
            }

        } else {
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
            index = (y1 << 7) + (y1 << 5) + x1;
            rgbCode = colours[c];

            bgLineData[bgIndex++] = index;
            bgLineData[bgIndex++] = overlayScreen.get(index);
            overlayScreen.set(index, rgbCode);

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

                index = (y << 7) + (y << 5) + x;
                bgLineData[bgIndex++] = index;
                bgLineData[bgIndex++] = overlayScreen.get(index);
                overlayScreen.set(index, rgbCode);
                count--;
            } while (count > 0);
        }

        // Make the end of the temporary line flash so that it is obvious where the mouse is.
        int brightness = (int) ((System.currentTimeMillis() >> 1) & 0xFF);
        int brightnessRgba = ((brightness << 24) | (brightness << 16) | (brightness << 8) | 0xFF);
        overlayScreen.set(flashIndex, brightnessRgba);
        
        // Store the length of the stored pixel data in first slot.
        bgLineData[0] = bgIndex;
    }
    
    private Canvas createImage(int width, int height) {
        Canvas canvas = Canvas.createIfSupported();
        canvas.getCanvasElement().setWidth(width);
        canvas.getCanvasElement().setHeight(height);
        return canvas;
    }
    
    /**
     * Holds two off screen images that are used interchangeably for offscreen rendering of 
     * the PicturePanel content.
     */
    class OffScreenGraphics {
    	
        /**
         * The offscreen images used to prepare the PICEDIT screen before displaying it.
         */
    	private Canvas[] offScreenImages;

        /**
         * The Graphics instances associated with each offscreen image.
         */
        private Context2d[] offScreenGCs;

        /**
         * The currently active offscreen Image/Graphics index.
         */
        private int activeGraphics;
        
        OffScreenGraphics() {
        	clear();
        }
        
        void clear() {
            this.offScreenImages = new Canvas[2];
            this.offScreenGCs = new Context2d[2];
            this.offScreenImages[0] = PicturePanel.this.createImage(320, Picture.HEIGHT);
            this.offScreenImages[1] = PicturePanel.this.createImage(320, Picture.HEIGHT);
            this.offScreenGCs[0] = offScreenImages[0].getContext2d();
            this.offScreenGCs[1] = offScreenImages[1].getContext2d();
            this.activeGraphics = 0;
        }
        
        Canvas getImage() {
        	return offScreenImages[(activeGraphics + 1) % 2];
        }
        
        Context2d getActiveGraphics() {
        	return offScreenGCs[activeGraphics];
        }
        
        void toggle() {
        	activeGraphics = ((activeGraphics + 1) % 2);
        }
    }
}
