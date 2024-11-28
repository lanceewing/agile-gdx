package com.agifans.agile.editor.picture.picedit.gui.handler;

import com.agifans.agile.agilib.jagi.awt.Point;
import com.agifans.agile.agilib.picedit.BrushTexture;
import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.agilib.picedit.PictureCodeType;
import com.agifans.agile.agilib.picedit.StepType;
import com.agifans.agile.agilib.picedit.ToolType;
import com.agifans.agile.editor.picture.picedit.PicEdit;
import com.agifans.agile.editor.picture.picedit.gui.frame.PictureFrame;
import com.agifans.agile.editor.picture.picedit.gui.frame.PicturePanel;
import com.agifans.agile.editor.picture.picedit.gui.toolbar.ColourChooserDialog;
import com.agifans.agile.editor.picture.picedit.picture.Picture;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelHandler;

/**
 * Handles processing of mouse click and mouse move events for a PictureFrame.
 * 
 * @author Lance Ewing
 */
public class MouseHandler implements 
        MouseMoveHandler, MouseWheelHandler, MouseOverHandler, 
        MouseUpHandler, MouseDownHandler, MouseOutHandler {

    /**
     * The PICEDIT application component.
     */
    protected PicEdit application;
    
    /**
     * The picture frame that this MouseHandler is for.
     */
    private PictureFrame pictureFrame;

    /**
     * Helps to protect against clashes between mouse wheel rotation and clicks.
     */
    private int wheelCounter;
    
    /**
     * Constructor for MouseHandler.
     * 
     * @param pictureFrame The PictureFrame that this MouseHandler is for.
     * @param application The PICEDIT application component.
     */
    public MouseHandler(final PictureFrame pictureFrame, final PicEdit application) {
        this.pictureFrame = pictureFrame;
        this.application = application;
    }

    /**
     * Returns the current mouse position, adjusted to the AGI screen coordinates.
     * 
     * @param event The MouseEvent to get the adjusted Point for.
     * 
     * @return the current mouse position.
     */
    @SuppressWarnings("rawtypes")
    public Point getPoint(MouseEvent mouseEvent) {
        EditStatus editStatus = application.getEditStatus();

        int x = mouseEvent.getX() / editStatus.getZoomFactor();
        int y = mouseEvent.getY() / editStatus.getZoomFactor();

        return new Point(x, y);
    }
    
    @SuppressWarnings("rawtypes")
    public Point getLocationOnScreen(MouseEvent mouseEvent) {
        return new Point(mouseEvent.getScreenX(), mouseEvent.getScreenY());
    }
    
    @Override
    public void onMouseDown(MouseDownEvent event) {
        // Pressed.
        Point mousePoint = getPoint(event);
        EditStatus editStatus = application.getEditStatus();

        if (editStatus.isMenuActive()) {
            // If menu was active and we received a mouse click, then set menu active false again.
            editStatus.setMenuActive(false);
        } else {
            // Otherwise process mouse click as per normal.
            processMouseClick(mousePoint, event.getNativeButton());
            
            // Mouse wheel button, AKA. the middle button. This is not a picture related action, so
            // we process outside of the normal processMouseClick.
            if (event.getNativeButton() == NativeEvent.BUTTON_MIDDLE) {
                // Reset the current tool if line is being drawn. Doesn't make sense to keep line
                // drawing enabled while colour is being chosen.
                if (editStatus.isLineBeingDrawn()) {
                    editStatus.resetTool();
                }
                
                Point eventPoint = getLocationOnScreen(event);
                Point dialogPoint = new Point(eventPoint.x - 34, eventPoint.y - 34);
                
                // Pop up colour chooser.
                ColourChooserDialog dialog = new ColourChooserDialog(dialogPoint, application.getPicture());
                dialog.setVisible(true);
                
                // This helps to protect against clashes between wheel clicks and rotation.
                wheelCounter = 0;
            }
        }
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        // Released. Not used.
    }

    @Override
    public void onMouseOver(MouseOverEvent event) {
        // Entering the canvas. Not used.
    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        // Leaving the canvas. Not used.
    }

    @Override
    public void onMouseWheel(com.google.gwt.event.dom.client.MouseWheelEvent event) {
        EditStatus editStatus = application.getEditStatus();
        
        wheelCounter += event.getDeltaY();
        
        if (wheelCounter < -1) {
            // Zoom in.
            int zoomFactor = editStatus.getZoomFactor();
            if (zoomFactor < 5) {
                application.resizeScreen(zoomFactor + 1);
            }
            wheelCounter = 0;
        } else if (wheelCounter > 1) {
            // Zoom out.
            int zoomFactor = editStatus.getZoomFactor();
            if (zoomFactor > 1) {
                application.resizeScreen(zoomFactor - 1);
            }
            wheelCounter = 0;
        }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        processMouseMove(getPoint(event));
    }

    /**
     * Processes the movement of the mouse.
     * 
     * @param mousePoint the Point where the mouse currently is.
     */
    public void processMouseMove(Point mousePoint) {
        EditStatus editStatus = application.getEditStatus();
        PicturePanel picturePanel = application.getPicturePanel();
        
        // Update the status line on every mouse movement.
        editStatus.updateMousePoint(mousePoint);

        int x = editStatus.getMouseX();
        int y = editStatus.getMouseY();
        
        if (editStatus.getNumOfClicks() > 0) {
            // Make sure that the mouse cursor can't leave the picture while a line
            // is being drawn.
            Point clickPoint = editStatus.getClickPoint();
            int clickX = (int) clickPoint.getX();
            int clickY = (int) clickPoint.getY();
            int lineColour = editStatus.getTemporaryLineColour();

            if (editStatus.isLineActive()) {
            	picturePanel.drawTemporaryLine(clickX, clickY, x, y, lineColour);
            }
            if (editStatus.isStepActive()) {
                int dX = 0;
                int dY = 0;

                switch (editStatus.getNumOfClicks()) {
                    case 1:
                        dX = x - clickX;
                        dY = y - clickY;
                        if (Math.abs(dX) > Math.abs(dY)) {
                            y = clickY;
                        } else {
                            x = clickX;
                        }
                        picturePanel.drawTemporaryLine(clickX, clickY, x, y, lineColour);
                        break;

                    default:
                        if ((editStatus.isXCornerActive() && ((editStatus.getNumOfClicks() % 2) == 0)) || (editStatus.isYCornerActive() && ((editStatus.getNumOfClicks() % 2) > 0))) {
                            // X and Y corners toggle different direction based on number of clicks.	
                            x = clickX;
                        } else {
                            y = clickY;
                        }
                        picturePanel.drawTemporaryLine(clickX, clickY, x, y, lineColour);
                        break;
                }
            }
            if (editStatus.isPenActive()) {
                x = clickX + adjustForPen(x - clickX, 6);
                y = clickY + adjustForPen(y - clickY, 7);
                picturePanel.drawTemporaryLine(clickX, clickY, x, y, lineColour);
            }
            
            // Move the mouse to the tool restricted x/y position (if applicable).
            if ((x != editStatus.getMouseX()) || (y != editStatus.getMouseY())) {
                // Make sure the EditStatus has the tool adjusted mouse point.
                editStatus.setMousePoint(new Point(x, y));
            }
        }
    }

    /**
     * Processes the given mouse click.
     * 
     * @param mousePoint the Point where the mouse click occurred.
     * @param mouseButton the mouse button that was clicked.
     */
    public void processMouseClick(Point mousePoint, int mouseButton) {
        int x = (int) mousePoint.getX();
        int y = (int) mousePoint.getY();
        
        EditStatus editStatus = application.getEditStatus();
        Picture picture = application.getPicture();

        // Is it the LEFT mouse button?
        if (mouseButton == NativeEvent.BUTTON_LEFT) {
            // Is a tool active?
            if (editStatus.getTool() != ToolType.NONE) {
                // Register the new left mouse click in the edit status.
                editStatus.addClickPoint();

                // Get the 'adjusted' X & Y position back from the edit status.
                Point pictureClickPoint = editStatus.getClickPoint();
                x = (int) pictureClickPoint.getX();
                y = (int) pictureClickPoint.getY();

                // Get the previous mouse click point for use with Line, Pen, Step.
                int previousX = 0;
                int previousY = 0;
                Point previousClickPoint = editStatus.getPreviousClickPoint();
                if (previousClickPoint != null) {
                    previousX = (int) previousClickPoint.getX();
                    previousY = (int) previousClickPoint.getY();
                }

                // If a given tool is active then update the AGI picture.
                if (editStatus.isFillActive()) {
                    picture.fill(x, y);
                    if (editStatus.isFirstClick()) {
                        picture.addPictureCode(PictureCodeType.DRAW_FILL);
                    }
                    picture.addPictureCode(PictureCodeType.FILL_POINT_DATA, x, y);
                } else if (editStatus.isLineActive()) {
                    switch (editStatus.getNumOfClicks()) {
                        case 1:
                            picture.addPictureCode(PictureCodeType.DRAW_LINE);
                            picture.addPictureCode(x, y);
                            picture.putPixel(x, y);
                            break;
                        default:
                            picture.addPictureCode(x, y);
                            picture.drawLine(previousX, previousY, x, y);
                            break;
                    }
                } else if (editStatus.isPenActive()) {
                    int disp = 0;
                    int dX = 0;
                    int dY = 0;

                    switch (editStatus.getNumOfClicks()) {
                        case 1:
                            picture.addPictureCode(PictureCodeType.DRAW_SHORT_LINE);
                            picture.addPictureCode(x, y);
                            picture.putPixel(x, y);
                            break;
                        default:
                            dX = adjustForPen(x - previousX, 6);
                            dY = adjustForPen(y - previousY, 7);
                            x = previousX + dX;
                            y = previousY + dY;

                            if (dX < 0) {
                                disp = (0x80 | ((((-1) * dX) - 0) << 4));
                            } else {
                                disp = (dX << 4);
                            }
                            if (dY < 0) {
                                disp |= (0x08 | (((-1) * dY) - 0));
                            } else {
                                disp |= dY;
                            }
                            picture.addPictureCode(PictureCodeType.RELATIVE_POINT_DATA, disp, new Point(x, y));
                            picture.drawLine(previousX, previousY, x, y);
                            editStatus.setClickPoint(new Point(x, y));
                            break;
                    }
                } else if (editStatus.isBrushActive()) {
                    int patNum = 0;

                    if (editStatus.isFirstClick()) {
                        picture.addPictureCode(PictureCodeType.SET_BRUSH_TYPE);
                        picture.addPictureCode(PictureCodeType.BRUSH_TYPE_DATA, editStatus.getBrushCode());
                        picture.addPictureCode(PictureCodeType.DRAW_BRUSH_POINT);
                    }
                    patNum = (((new java.util.Random().nextInt(255)) % 0xEE) >> 1) & 0x7F;
                    picture.plotPattern(patNum, x, y);
                    if (editStatus.getBrushTexture() == BrushTexture.SPRAY) {
                        picture.addPictureCode(PictureCodeType.BRUSH_PATTERN_DATA, patNum << 1);
                    }
                    picture.addPictureCode(PictureCodeType.BRUSH_POINT_DATA, x, y);
                } else if (editStatus.isStepActive()) {
                    int dX = 0;
                    int dY = 0;

                    switch (editStatus.getNumOfClicks()) {
                        case 1:
                            break;

                        case 2:
                            dX = x - previousX;
                            dY = y - previousY;
                            if (Math.abs(dX) > Math.abs(dY)) { /* X or Y corner */
                                y = previousY;
                                editStatus.setStepType(StepType.XCORNER);
                                picture.addPictureCode(PictureCodeType.DRAW_HORIZONTAL_STEP_LINE);
                                picture.addPictureCode(previousX, previousY);
                                picture.addPictureCode(PictureCodeType.X_POSITION_DATA, x, new Point(x, y));
                            } else {
                                x = previousX;
                                editStatus.setStepType(StepType.YCORNER);
                                picture.addPictureCode(PictureCodeType.DRAW_VERTICAL_STEP_LINE);
                                picture.addPictureCode(previousX, previousY);
                                picture.addPictureCode(PictureCodeType.Y_POSITION_DATA, y, new Point(x, y));
                            }
                            picture.drawLine(previousX, previousY, x, y);
                            editStatus.setClickPoint(new Point(x, y));
                            break;

                        default:
                            if ((editStatus.isXCornerActive() && ((editStatus.getNumOfClicks() % 2) > 0)) || (editStatus.isYCornerActive() && ((editStatus.getNumOfClicks() % 2) == 0))) {
                                // X and Y corners toggle different direction based on number of clicks.	
                                x = previousX;
                                picture.addPictureCode(PictureCodeType.Y_POSITION_DATA, y, new Point(x, y));
                            } else {
                                y = previousY;
                                picture.addPictureCode(PictureCodeType.X_POSITION_DATA, x, new Point(x, y));
                            }
                            picture.drawLine(previousX, previousY, x, y);
                            editStatus.setClickPoint(new Point(x, y));
                            break;
                    }
                }
            }
        }
        
        // Is it the RIGHT mouse button?
        if (mouseButton == NativeEvent.BUTTON_RIGHT) {
            // Right-clicking on the AGI picture will clear the current tool selection.
            if ((editStatus.getNumOfClicks() == 1) && (editStatus.isStepActive())) {
                // Single point line support for the Step tool.
                
                // Get the 'adjusted' X & Y position back from the edit status.
                editStatus.addClickPoint();
                Point pictureClickPoint = editStatus.getClickPoint();
                x = (int) pictureClickPoint.getX();
                y = (int) pictureClickPoint.getY();

                // Get the previous mouse click point for use with single point Step.
                int previousX = 0;
                int previousY = 0;
                Point previousClickPoint = editStatus.getPreviousClickPoint();
                if (previousClickPoint != null) {
                    previousX = (int) previousClickPoint.getX();
                    previousY = (int) previousClickPoint.getY();
                }
                
                // The X/Y corner decision for single point is based on where right click was.
                int dX = x - previousX;
                int dY = y - previousY;
                if (Math.abs(dX) > Math.abs(dY)) { /* X or Y corner */
                    picture.addPictureCode(PictureCodeType.DRAW_HORIZONTAL_STEP_LINE);
                    picture.addPictureCode(previousX, previousY);
                } else {
                    picture.addPictureCode(PictureCodeType.DRAW_VERTICAL_STEP_LINE);
                    picture.addPictureCode(previousX, previousY);
                }
                
                picture.putPixel(previousX, previousY);
            }
            if (editStatus.getNumOfClicks() > 0) {
                // If a tool is active (i.e. has a least one click) then right click resets 
                // number of clicks, which allows the user to move to new location.
                editStatus.resetTool();
            } else {
                // If no clicks performed yet then a right click sets tool to None.
                editStatus.setTool(ToolType.NONE);
            }
        }
        
        // Update active status of the position slider based on whether a line is being
        // drawn or not. It should not be possible to use the slider if line drawing is
        // enabled.
        pictureFrame.getPositionSlider().setEnabled(!editStatus.isLineBeingDrawn());
    }
    
    /**
     * The Pen tool has a very short distance between two points. This
     * method is used for checking those limits.
     * 
     * @param value the value to adjust. Will either be an X or Y value.
     * @param limit the limit in both directs that to enforce.
     */
    private int adjustForPen(int value, int limit) {
        if (value > limit) {
            value = limit;
        }
        if (value < -limit) {
            value = -limit;
        }
        return value;
    }
}
