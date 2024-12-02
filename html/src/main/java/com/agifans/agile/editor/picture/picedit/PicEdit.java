package com.agifans.agile.editor.picture.picedit;

import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.editor.picture.picedit.gui.StatusBarPanel;
import com.agifans.agile.editor.picture.picedit.gui.frame.NavigationButtonType;
import com.agifans.agile.editor.picture.picedit.gui.frame.PictureFrame;
import com.agifans.agile.editor.picture.picedit.gui.frame.PicturePanel;
import com.agifans.agile.editor.picture.picedit.gui.frame.PositionSlider;
import com.agifans.agile.editor.picture.picedit.gui.toolbar.ToolPanel;
import com.agifans.agile.editor.picture.picedit.picture.Picture;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

/**
 * The main class for the PICEDIT application.
 * 
 * @author Lance Ewing
 */
public final class PicEdit extends DockLayoutPanel {

    /**
     * The most recently active picture window.
     */
    private PictureFrame pictureFrame;
    
    /**
     * The scroll panel for the currently active picture code list.
     */
    private ScrollPanel pictureCodeScrollPanel;
    
    /**
     * The scroll panel for the picture itself.
     */
    private ScrollPanel pictureScrollPanel;
    
    /**
     * The tool panel containing all of the picture drawing tools.
     */
    private ToolPanel toolPanel;
    
    /**
     * The navigation panel used to navigate within the picture codes.
     */
    private DockLayoutPanel navigationPanel;
    
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
     * The status bar at the bottom of the PICEDIT screen.
     */
    private StatusBarPanel statusBarPanel;
    
    /**
     * Constructor for PicEdit.
     */
    public PicEdit() {
    	super(Unit.PX);
    	
    	// Expand to fill the whole edit area.
    	setWidth("100%");
    	setHeight("100%");
    	
    	addStyleName("picEditLayoutPanel");
    	
        pictureFrame = new PictureFrame(this, 3, "Untitled");
        
        // Tool panel.
        toolPanel = new ToolPanel(this);
        
        // Contains scrollable list of picture codes, vertically scrollable.
        pictureCodeScrollPanel = new ScrollPanel(pictureFrame.getPictureCodeList());
        pictureCodeScrollPanel.addStyleName("pictureCodeScrollPanel");
        pictureCodeScrollPanel.setHeight("100%");
        
        // Contains panel for picture, scrollable in both directions.
        pictureScrollPanel = new ScrollPanel(pictureFrame);
        pictureScrollPanel.addStyleName("pictureScrollPanel");
        
        navigationPanel = new DockLayoutPanel(Unit.PX);
        navigationPanel.addStyleName("navigationPanel");
        navigationPanel.setHeight("32px");
        backButton = new NavigationButton("Back24.gif", NavigationButtonType.BACK);
        backButton.addStyleName("backButton");
        navigationPanel.addWest(backButton, 36);
        positionSlider = new PositionSlider(pictureFrame);
        positionSlider.addStyleName("positionSlider");
        navigationPanel.add(positionSlider);
        forwardButton = new NavigationButton("Forward24.gif", NavigationButtonType.FORWARD);
        forwardButton.addStyleName("forwardButton");
        navigationPanel.addEast(forwardButton, 36);
        
        DockLayoutPanel toolbarPicturePanel = new DockLayoutPanel(Unit.PX);
        toolbarPicturePanel.setHeight("100%");
        toolbarPicturePanel.addStyleName("toolbarPicturePanel");
        toolbarPicturePanel.addNorth(toolPanel, 40);
        toolbarPicturePanel.add(pictureScrollPanel);
        toolbarPicturePanel.addSouth(navigationPanel, 32);
        
        // Split panel with picture codes on left, and toolbar & picture on right.
        SplitLayoutPanel centerSplitPanel = new SplitLayoutPanel();
        centerSplitPanel.addStyleName("pictureCenterSplitPanel");
        centerSplitPanel.addWest(pictureCodeScrollPanel, 150);
        centerSplitPanel.add(toolbarPicturePanel);
        
        // Main picture code/picture split panel takes central position.
        add(centerSplitPanel);
        
        // Status bar is at the bottom.
        statusBarPanel = new StatusBarPanel(this);
        statusBarPanel.setHeight("20px");
        statusBarPanel.setWidth("100%");
        addSouth(statusBarPanel, 20);
        
        // Start a timer to perform regular screen repaints.
        repaintTimer(0);
    }
    
    private long frameCount = 0;
    
    /**
     * Starts a timer to trigger regular screen repaints. 
     * 
     * @param timestamp
     */
    public void repaintTimer(double timestamp) {
        // Immediately request another invocation on the next animation frame.
        AnimationScheduler.get().requestAnimationFrame(new AnimationCallback() {
            @Override
            public void execute(double timestamp) {
                repaintTimer(timestamp);
            }
        });
        
        pictureFrame.update();
        toolPanel.update();
        statusBarPanel.update();
    }
    
    /**
     * Gets the Picture that is currently being edited with PicEdit.
     * 
     * @return The Picture that is currently being edited.
     */
    public Picture getPicture() {
        return getPictureFrame().getPicture();
    }
    
    /**
     * Gets the EditStatus for this PicEdit application. This object contains the
     * editing state of everything within PicEdit.
     * 
     * @return The EditStatus.
     */
    public EditStatus getEditStatus() {
        return getPictureFrame().getEditStatus();
    }
    
    /**
     * Returns the panel that holds the picture.
     * 
     * @return The panel that holds the picture.
     */
    public PicturePanel getPicturePanel() {
        return getPictureFrame().getPicturePanel();
    }
    
    /**
     * Gets the currently active PictureFrame.
     * 
     * @return The currently active PictureFrame.
     */
    public PictureFrame getPictureFrame() {
        return pictureFrame;
    }
    
    /**
     * Gets the position slider used by PicEdit.
     * 
     * @return
     */
    public PositionSlider getPositionSlider() {
        return positionSlider;
    }
    
    /**
     * Gets the navigation back button.
     * 
     * @return
     */
    public NavigationButton getBackButton() {
        return backButton;
    }
    
    /**
     * Gets the navigation forward button.
     * 
     * @return
     */
    public NavigationButton getForwardButton() {
        return forwardButton;
    }
    
    /**
     * Resizes the screen according to the new zoom factor.
     * 
     * @param zoomFactor the new zoom factor.
     */
    public void resizeScreen(int zoomFactor) {
        // TODO: Reintroduce.
        //getPictureFrame().resizeForZoomFactor(zoomFactor);
    }
    
    /**
     * Buttons used for picture navigation.
     */
    public class NavigationButton extends PushButton implements ClickHandler {
        
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
                    getPicture().moveForwardOnePictureAction();
                    break;
                case BACK:
                    getPicture().moveBackOnePictureAction();
                    break;
            }
        }
    }
    
    private final native void logToJSConsole(String message)/*-{
        console.log(message);
    }-*/;
}
