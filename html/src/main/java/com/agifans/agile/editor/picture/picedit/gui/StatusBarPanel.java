package com.agifans.agile.editor.picture.picedit.gui;

import com.agifans.agile.agilib.picedit.BrushType;
import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.agilib.picedit.ToolType;
import com.agifans.agile.editor.picture.picedit.PicEdit;
import com.agifans.agile.editor.picture.picedit.picture.Picture;
import com.agifans.agile.util.StringUtils;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * The status bar that appears below the picture showing the current mouse position,
 * tool, visual colour, priority colour, etc.
 * 
 * @author Lance Ewing
 */
public class StatusBarPanel extends DockLayoutPanel {

	private PicEdit picEdit;
	
	private StatusBarSection toolNamePanel;
	private StatusBarSection xPositionPanel;
	private StatusBarSection yPositionPanel;
	private StatusBarSection priBandPanel;
	private StatusBarSection positionPanel;
	
    /**
     * Constructor for StatusBarPanel.
     * 
     * @param editStatus The EditStatus holding current picture editor state.
     */
    public StatusBarPanel(final PicEdit picEdit) {
    	super(Unit.PX);
        
    	this.picEdit = picEdit;
    	
    	addStyleName("statusBarPanel");
    	
        toolNamePanel = new StatusBarSection(200, buildToolName(picEdit), "statusToolName");
        xPositionPanel = new StatusBarSection(75, buildXPosition(picEdit), "statusXPosition");
        yPositionPanel = new StatusBarSection(75, buildYPosition(picEdit), "statusYPosition");
        priBandPanel = new StatusBarSection(200, buildPriorityBand(picEdit), "statusPriorityBand");
        positionPanel = new StatusBarSection(200, buildBytePosition(picEdit), "statusBytePosition");
        
        HorizontalPanel mainPanel = new HorizontalPanel();
        mainPanel.addStyleName("statusBarMainPanel");
        mainPanel.add(toolNamePanel);
        mainPanel.add(positionPanel);
        mainPanel.add(xPositionPanel);
        mainPanel.add(yPositionPanel);
        mainPanel.add(priBandPanel);
        
        // Filler panel in the middle, everything else to the left.
        SimplePanel fillerPanel = new SimplePanel();
        fillerPanel.addStyleName("statusBarPanelFiller");
        add(fillerPanel);
        addWest(mainPanel, 750d);
    }
    
    public void update() {
    	toolNamePanel.setText(buildToolName(picEdit));
    	positionPanel.setText(buildBytePosition(picEdit));
    	xPositionPanel.setText(buildXPosition(picEdit));
    	yPositionPanel.setText(buildYPosition(picEdit));
    	priBandPanel.setText(buildPriorityBand(picEdit));
    }
    
	private String buildToolName(PicEdit application) {
    	EditStatus editStatus = application.getEditStatus();
        String toolName = null;
        if (editStatus.getTool().equals(ToolType.AIRBRUSH) || editStatus.getTool().equals(ToolType.BRUSH)) {
        	toolName = BrushType.getBrushTypeForBrushCode(editStatus.getBrushCode()).getDisplayName();
        } else {
        	toolName = application.getEditStatus().getTool().toString();
        }
        return toolName;
	}
	
	private String buildXPosition(PicEdit application) {
		return StringUtils.format("X: {0}", 
				StringUtils.padRightSpaces(Integer.toString(application.getEditStatus().getMouseX()), 3));
	}
	
	private String buildYPosition(PicEdit application) {
		return StringUtils.format("Y: {0}", 
				StringUtils.padRightSpaces(Integer.toString(application.getEditStatus().getMouseY()), 3));
	}
	
	private String buildPriorityBand(PicEdit application) {
		return StringUtils.format("PriBand: {0}", application.getEditStatus().getPriorityColour());
	}
	
	private String buildBytePosition(PicEdit application) {
        Picture picture = application.getPicture();
        int picturePosition = picture.getPicturePosition();
        int pictureSize = picture.getPictureCodes().size() - 1;
		return StringUtils.format("Position: {0}/{1}", picturePosition, pictureSize);
	}
    
    class StatusBarSection extends Label {
        StatusBarSection(int width, String text, String additionalStyleName) {
            super(text);
            setPixelSize(width, 20);
            addStyleName("statusBarSection");
            addStyleName(additionalStyleName);
        }
    }
}
