package com.agifans.agile.editor.picture.picedit.gui.frame;

import com.agifans.agile.EgaPalette;
import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.editor.picture.picedit.PicEdit;
import com.agifans.agile.editor.picture.picedit.gui.PictureCodeList;
import com.agifans.agile.editor.picture.picedit.gui.handler.KeyboardHandler;
import com.agifans.agile.editor.picture.picedit.gui.handler.MouseHandler;
import com.agifans.agile.editor.picture.picedit.picture.Picture;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * An internal picture frame to display in the desktop pane. There is one 
 * such frame for each picture that is loaded. It is in these frames that
 * the pictures are displayed.
 * 
 * @author Lance Ewing
 */
public class PictureFrame extends SimplePanel {

    /**
     * The PicEdit application.
     */
    private PicEdit application;
    
    /**
     * The scroll pane that holds the picture panel.
     */
    private ScrollPanel pictureScrollPanel;
    
    /**
     * The panel containing the picture being edited.
     */
    private PicturePanel picturePanel;
    
    /**
     * Holds the current edit status of the picture in this frame.
     */
    private EditStatus editStatus;
    
    /**
     * The picture that is displayed in this frame.
     */
    private Picture picture;
    
    /**
     * The mouse handler for this picture frame.
     */
    private MouseHandler mouseHandler;
    
    /**
     * The keyboard handler for this picture frame.
     */
    private KeyboardHandler keyboardHandler;
    
    /**
     * The initial default name for the picture prior to the first save.
     */
    private String defaultPictureName;
    
    /**
     * The list of picture codes for this PictureFrames Picture.
     */
    private PictureCodeList pictureCodeList;
    
    /**
     * The DockLayoutPanel handling the layout within the PictureFrame.
     */
    private DockLayoutPanel dockLayoutPanel;
    
    /**
     * Constructor for PictureFrame.
     * 
     * @param application The PicEdit application.
     * @param initialZoomFactor The initial zoome factor for this picture frame.
     * @param defaultPictureName The initial default name for the picture prior to the first save.
     */
    public PictureFrame(final PicEdit application, int initialZoomFactor, String defaultPictureName) {
        this.application = application;
        this.addStyleName("pictureFrame");
        this.defaultPictureName = defaultPictureName;
        this.editStatus = new EditStatus();
        this.editStatus.setZoomFactor(initialZoomFactor);
        this.picture = new Picture(editStatus);
        this.pictureCodeList = new PictureCodeList(picture);
        this.picture.addPictureChangeListener(pictureCodeList);
        this.dockLayoutPanel = new DockLayoutPanel(Unit.PX);
        this.picturePanel = new PicturePanel(editStatus, picture);
        
        mouseHandler = new MouseHandler(this, application);
        picturePanel.getOnScreenCanvas().addMouseWheelHandler(mouseHandler);
        picturePanel.getOnScreenCanvas().addMouseMoveHandler(mouseHandler);
        picturePanel.getOnScreenCanvas().addMouseUpHandler(mouseHandler);
        picturePanel.getOnScreenCanvas().addMouseDownHandler(mouseHandler);
        picturePanel.getOnScreenCanvas().addMouseOverHandler(mouseHandler);
        
        keyboardHandler = new KeyboardHandler(application);
        picturePanel.getOnScreenCanvas().addKeyDownHandler(keyboardHandler);
        
        // Add the panel that holds the picture that is being edited.
        pictureScrollPanel = new ScrollPanel(picturePanel);
        pictureScrollPanel.getElement().getStyle().setBackgroundColor(EgaPalette.toCssRgba(EgaPalette.grey));
        
        // Picture scroll panel is in the middle.
        dockLayoutPanel.add(pictureScrollPanel);
        
        setPixelSize(320, 168 + 24);
        
        add(dockLayoutPanel);
    }
    
    public EditStatus getEditStatus() {
        return editStatus;
    }
    
    public Picture getPicture() {
        return picture;
    }
    
    public void setPicture(Picture picture) {
        this.picture = picture;
    }
    
    public PicturePanel getPicturePanel() {
        return picturePanel;
    }
    
    public PositionSlider getPositionSlider() {
        return application.getPositionSlider();
    }
    
    /**
     * Gets the picture code JList component that holds the list of human readable picture codes.
     * 
     * @return The picture code JList component that holds the list of human readable picture codes.
     */
    public PictureCodeList getPictureCodeList() {
        return pictureCodeList;
    }
   
    /**
     * Updates the state and re-renders.
     */
    public void update() {
        getPicturePanel().refresh();
        
        // Update slider enabled status based on whether line is being drawn or not. Slider
        // cannot be used if line drawing is active.
        getPositionSlider().setEnabled(!editStatus.isLineBeingDrawn());
        
        application.getForwardButton().setEnabled(!editStatus.isLineBeingDrawn());
        application.getBackButton().setEnabled(!editStatus.isLineBeingDrawn());
        
        // Make sure the slider is up to date with the picture position.
        getPositionSlider().setValue(picture.getPicturePosition());
        
        // Update the title to show the current picture name.
        StringBuilder title = new StringBuilder();
        if (editStatus.hasUnsavedChanges()) {
          title.append("*");
        }
        title.append(defaultPictureName);
        setTitle(title.toString());
    }
}
