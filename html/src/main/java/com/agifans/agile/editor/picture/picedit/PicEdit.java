package com.agifans.agile.editor.picture.picedit;

import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.editor.picture.picedit.gui.StatusBarPanel;
import com.agifans.agile.editor.picture.picedit.gui.frame.PictureFrame;
import com.agifans.agile.editor.picture.picedit.gui.frame.PicturePanel;
import com.agifans.agile.editor.picture.picedit.gui.toolbar.ToolPanel;
import com.agifans.agile.editor.picture.picedit.picture.Picture;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
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
    	
        activePictureFrame = new PictureFrame(this, 3, "Untitled");
        // TODO: Needs to be relative to the parent.
        //activePictureFrame.setPopupPosition(20, 20);
        
        // Add the status bar above the picture.
        statusBarPanel = new StatusBarPanel(this);
        statusBarPanel.setPixelSize(320, 20);
        addSouth(statusBarPanel, 320);
        
        DockLayoutPanel desktopPanel = new DockLayoutPanel(Unit.PX);

        activePictureFrame.show();
        
        // Tool panel.
        toolPanel = new ToolPanel(this);
        desktopPanel.addNorth(toolPanel, 32);
        
        pictureCodeScrollPane = new ScrollPanel(activePictureFrame.getPictureCodeList());
        
        SplitLayoutPanel centerSplitPanel = new SplitLayoutPanel();
        centerSplitPanel.addWest(pictureCodeScrollPane, 240);
        // TODO: Add picture frame

        add(centerSplitPanel);
        
        // Start a timer to preform regular screen repaints.
        // TODO: Do we need the equivalent of this? : startRepaintTimer();
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
}
