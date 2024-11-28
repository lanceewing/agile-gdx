package com.agifans.agile.editor.picture.picedit;

import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.editor.picture.picedit.gui.StatusBarPanel;
import com.agifans.agile.editor.picture.picedit.gui.frame.PictureFrame;
import com.agifans.agile.editor.picture.picedit.gui.frame.PicturePanel;
import com.agifans.agile.editor.picture.picedit.gui.toolbar.ToolPanel;
import com.agifans.agile.editor.picture.picedit.picture.Picture;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The main class for the PICEDIT application.
 * 
 * @author Lance Ewing
 */
public final class PicEdit extends DockLayoutPanel {

    /**
     * The most recently active picture window.
     */
    private PictureFrame activePictureFrame;
    
    /**
     * The scroll panel for the currently active picture code list.
     */
    private ScrollPanel pictureCodeScrollPane;
    
    /**
     * The tool panel containing all of the picture drawing tools.
     */
    private ToolPanel toolPanel;
    
    /**
     * The status bar at the bottom of the PICEDIT screen.
     */
    private StatusBarPanel statusBarPanel;
    
    /**
     * Constructor for PicEdit.
     */
    public PicEdit() {
    	super(Unit.PX);
    	
    	setWidth("100%");
    	setHeight("100%");
    	
    	addStyleName("picEditLayoutPanel");
    	
        activePictureFrame = new PictureFrame(this, 3, "Untitled");
        // TODO: Needs to be relative to the parent.
        //activePictureFrame.setPopupPosition(20, 20);
        
               
        activePictureFrame.show();
        
        // Tool panel.
        toolPanel = new ToolPanel(this);
        
        pictureCodeScrollPane = new ScrollPanel(activePictureFrame.getPictureCodeList());
        pictureCodeScrollPane.addStyleName("pictureCodeScrollPane");
        pictureCodeScrollPane.setHeight("100%");
        
        VerticalPanel toolbarPicturePanel = new VerticalPanel();
        toolbarPicturePanel.setHeight("100%");
        toolbarPicturePanel.addStyleName("toolbarPicturePanel");
        toolbarPicturePanel.add(toolPanel);
        
        SplitLayoutPanel centerSplitPanel = new SplitLayoutPanel();
        centerSplitPanel.addStyleName("pictureCenterSplitPanel");
        centerSplitPanel.addWest(pictureCodeScrollPane, 150);
        centerSplitPanel.add(toolbarPicturePanel);
        
        // TODO: Add picture frame

        add(centerSplitPanel);
        
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
        
        getPictureFrame().getPicturePanel().refresh();
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
        return activePictureFrame;
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
    
    private final native void logToJSConsole(String message)/*-{
        console.log(message);
    }-*/;
}
