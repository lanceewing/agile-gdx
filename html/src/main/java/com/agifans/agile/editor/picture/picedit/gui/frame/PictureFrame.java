package com.agifans.agile.editor.picture.picedit.gui.frame;

import com.agifans.agile.EgaPalette;
import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.editor.picture.picedit.PicEdit;
import com.agifans.agile.editor.picture.picedit.gui.PictureCodeList;
import com.agifans.agile.editor.picture.picedit.gui.handler.KeyboardHandler;
import com.agifans.agile.editor.picture.picedit.gui.handler.MouseHandler;
import com.agifans.agile.editor.picture.picedit.picture.Picture;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * An internal picture frame to display in the desktop pane. There is one 
 * such frame for each picture that is loaded. It is in these frames that
 * the pictures are displayed.
 * 
 * @author Lance Ewing
 */
public class PictureFrame extends DialogBox {

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
     * The slider that sets the picture position.
     */
    private PositionSlider positionSlider;
    
    /**
     * The back navigation button. Goes back one picture action.
     */
    private NavigationButton backButton;
    
    /**
     * The forward navigation button. Goes forward one picture action.
     */
    private NavigationButton forwardButton;
    
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
     * The JList of picture codes for this PictureFrames Picture.
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
        this.defaultPictureName = defaultPictureName;
        this.editStatus = new EditStatus();
        this.editStatus.setZoomFactor(initialZoomFactor);
        this.picture = new Picture(editStatus);
        this.pictureCodeList = new PictureCodeList(picture);
        this.picture.addPictureChangeListener(pictureCodeList);
        
        setModal(false);
        
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
        
        // Picture scroll panel is in the middle of the dialog.
        dockLayoutPanel.add(pictureScrollPanel);
        
        DockLayoutPanel bottomPanel = new DockLayoutPanel(Unit.PX);
        backButton = new NavigationButton("Back24.gif", NavigationButtonType.BACK);
        bottomPanel.addWest(backButton, 24);
        forwardButton = new NavigationButton("Forward24.gif", NavigationButtonType.FORWARD);
        bottomPanel.addEast(forwardButton, 24);
        
        positionSlider = new PositionSlider(picture, pictureCodeList);
        bottomPanel.add(positionSlider);
        
        dockLayoutPanel.addSouth(bottomPanel, 24);
        
        setPixelSize(320, 168 + 24);
    }
    
    /**
     * Buttons used for picture navigation.
     */
    class NavigationButton extends PushButton implements ClickHandler {
        
        private NavigationButtonType type;
        
        NavigationButton(String iconImageName, NavigationButtonType type) {
            super(new Image("/editor/picedit/" + iconImageName));
            this.type = type;            
            setPixelSize(24, 24);
            addClickHandler(this);
        }

        /**
         * Processes the navigation button clicks.
         * 
         * @param event The ClickEvent for the button click.
         */
        @Override
        public void onClick(ClickEvent event) {
            switch (type) {
                case FORWARD:
                    application.getPicture().moveForwardOnePictureAction();
                    break;
                case BACK:
                    application.getPicture().moveBackOnePictureAction();
                    break;
            }
        }
    }
    
    public EditStatus getEditStatus() {
        return editStatus;
    }
    
    public Picture getPicture() {
        return picture;
    }
    
    public PicturePanel getPicturePanel() {
        return picturePanel;
    }
    
    public PositionSlider getPositionSlider() {
        return positionSlider;
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
     * Paints the PictureFrame.
     */
    public void paint() {
        // Update slider enabled status based on whether line is being drawn or not. Slider
        // cannot be used if line drawing is active.
        positionSlider.setEnabled(!editStatus.isLineBeingDrawn());
        forwardButton.setEnabled(!editStatus.isLineBeingDrawn());
        backButton.setEnabled(!editStatus.isLineBeingDrawn());
        
        // Make sure the slider is up to date with the picture position.
        positionSlider.setValue(picture.getPicturePosition());
        
        // Update the title to show the current picture name.
        StringBuilder title = new StringBuilder();
        if (editStatus.hasUnsavedChanges()) {
          title.append("*");
        }
        title.append(defaultPictureName);
        this.setTitle(title.toString());
    }
}
