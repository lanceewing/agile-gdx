package com.agifans.agile.editor.picture.picedit.gui.toolbar;

import com.agifans.agile.EgaPalette;
import com.agifans.agile.agilib.picedit.EditStatus;
import com.agifans.agile.agilib.picedit.ToolType;
import com.agifans.agile.editor.picture.picedit.PicEdit;
import com.agifans.agile.editor.picture.picedit.picture.Picture;
import com.agifans.agile.editor.picture.picedit.types.ColourType;
import com.agifans.agile.util.StringUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;

/**
 * The tool panel that is displayed at the bottom of the picture.
 * 
 * @author Lance Ewing
 */
public class ToolPanel extends FlowPanel {

	private PicEdit picEdit;
	
    /**
     * Constructor for ToolPanel.
     * 
     * @param picEdit The PicEdit application.
     */
    public ToolPanel(final PicEdit picEdit) {
    	this.picEdit = picEdit;
    	
        ToolButton lineButton = new ToolButton("line.png", picEdit, ToolType.LINE);
        ToolButton shortLineButton = new ToolButton("shortline.png", picEdit, ToolType.SHORTLINE);
        ToolButton stepLineButton = new ToolButton("stepline.png", picEdit, ToolType.STEPLINE);
        ToolButton fillButton = new ToolButton("fill.png", picEdit, ToolType.FILL);
        ToolButton airbrushButton = new ToolButton("airbrush.png", picEdit, ToolType.AIRBRUSH);
        ToolButton brushButton = new ToolButton("brush.png", picEdit, ToolType.BRUSH);
        
        FlowPanel buttonContainer = new FlowPanel();
        buttonContainer.setPixelSize(64,  192);
        buttonContainer.add(lineButton);
        buttonContainer.add(shortLineButton);
        buttonContainer.add(stepLineButton);
        buttonContainer.add(fillButton);
        buttonContainer.add(airbrushButton);
        buttonContainer.add(brushButton);
        add(buttonContainer);
        
        final FlowPanel colourPanel = new FlowPanel();
        colourPanel.setPixelSize(64, 64);
        ColourButtonPanel visualButton = new ColourButtonPanel(ColourType.VISUAL, picEdit);
        colourPanel.add(visualButton);
        ColourButtonPanel priorityButton = new ColourButtonPanel(ColourType.PRIORITY, picEdit);
        colourPanel.add(priorityButton);
        this.add(colourPanel);
        
        // Filler for the rest.
        add(new SimplePanel());
    }
    
    /**
     * A panel to hold the ColourButton. This panel will make sure there is a visually
     * sufficient gap between the left side and the checkbox.
     */
    class ColourButtonPanel extends FlowPanel {
        
        /**
         * Constructor for ColourButtonPanel.
         * 
         * @param colourType The type of colour button.
         * @param application The PicEdit application.
         */
        ColourButtonPanel(ColourType colourType, PicEdit application) {
        	setPixelSize(64, 32);
            ColourButton colourButton = new ColourButton(colourType, application);
            colourButton.addStyleName("colourButtonPanel");
            add(colourButton);
        }
    }
    
    /**
     * Colour button class used for the visual and priority colour changing buttons.
     */
    class ColourButton extends CheckBox {
        
        /**
         * The PicEdit application.
         */
        private PicEdit application;
        
        /**
         * The type of colour button.
         */
        private ColourType colourType;
        
        /**
         * Constructor for ColourButton.
         * 
         * @param colourType The type of colour button.
         * @param application The PicEdit application.
         */
        ColourButton(final ColourType colourType, final PicEdit application) {
            this.colourType = colourType;
            this.application = application;
            
            setPixelSize(54, 26);
            setTitle(colourType.getDisplayName());
            update();

            addClickHandler(new ColourButtonClickHandler());
        }
        
        /**
         * Updates the checkbox label to reflect current state. The label is a span where the text
         * is the colour type display name and the background colour indicates the currently active
         * colour.
         */
        public void update() {
            String foreground = EgaPalette.toCssRgba(EgaPalette.grey);
            String background = "transparent";
            int colourCode = -1;
            
            switch (colourType) {
                case VISUAL:
                    if (application.getEditStatus().isVisualDrawEnabled()) {
                        colourCode = application.getEditStatus().getVisualColour();
                        background = EgaPalette.toCssRgba(EgaPalette.colours[colourCode]);
                    }
                    break;
                case PRIORITY:
                    if (application.getEditStatus().isPriorityDrawEnabled()) {
                        colourCode = application.getEditStatus().getPriorityColour();
                        background = EgaPalette.toCssRgba(EgaPalette.colours[colourCode]);
                    }
                    break;
            }
            
            if ((colourCode >= 0) && (colourCode <= 8)) {
            	foreground = EgaPalette.toCssRgba(EgaPalette.white);
            } else if ((colourCode >= 9) && (colourCode <= 15)) {
            	foreground = EgaPalette.toCssRgba(EgaPalette.black);
            }
            
         	setHTML(StringUtils.format(
         			"<span style=\"color:{0};background-color:{1};\">{2}</span>", 
         			foreground, background, colourType.getDisplayName().charAt(0)));

         	// Check the checkbox if the EditStatus indicates it should be checked. Note that
         	// the change event is deliberately not fired, because this is updated within the 
         	// context of navigating through the picture. 
         	setValue(isSelected(), false);
         	
         	// Disable the checkbox if the EditStatus indicates it should be.
         	setEnabled(isEnabled());
        }
        
        /**
         * Determines whether the ColourButton checkbox is checked or not based on the
         * visual or priority enabled flag in the EditStatus.
         * 
         * @return true if the ColourButton is selected; otherwise false.
         */
        public boolean isSelected() {
            switch (colourType) {
                case VISUAL:
                    return application.getEditStatus().isVisualDrawEnabled();
                case PRIORITY:
                    return application.getEditStatus().isPriorityDrawEnabled();
                default:
                    return false;
            }
        }
        
        /**
         * Indicates if the button can be selected or triggered by an input device, such
         * as a mouse pointer.
         *
         * @return <code>true</code> if the button is enabled
         */
        public boolean isEnabled() {
            boolean enabled = false;
            
            if (application != null) {
                // Tools are only active when at least one picture frame is displayed,
                // and the current position is not on a data code. We don't want the
                // user to use the tools within the middle of an existing action.
                enabled = !application.getPicture().getCurrentPictureCode().isDataCode();
            }
            
            return enabled;
        }
        
        class ColourButtonClickHandler implements ClickHandler {

			@Override
			public void onClick(ClickEvent event) {
                // We will only process mouse clicks if at least one picture frame is
                // being displayed and the current position is not on a data code. We 
                // don't want the user to use the tools within the middle of an existing action.
                if (!application.getPicture().getCurrentPictureCode().isDataCode()) {

                    boolean clickInColourBox = event.getX() > 30;
                    EditStatus editStatus = application.getEditStatus();
                    Picture picture = application.getPicture();
                    
                    switch (colourType) {
                        case VISUAL:
                            if (editStatus.isVisualDrawEnabled() && !clickInColourBox) {
                                // If click is on the visual button but not in the colour box and visual
                                // drawing is currently on then turn off visual drawing.
                                picture.processVisualColourOff();
                            } else {
                                // Pop up colour chooser.
                                ColourChooserDialog dialog = new ColourChooserDialog(ColourButton.this);
                                dialog.show();
                                
                                // Process the chosen visual colour.
                                if (dialog.getChosenColour() != -1) {
                                    picture.processVisualColourChange(dialog.getChosenColour());
                                }
                            }
                            break;
                        case PRIORITY:
                            if (editStatus.isPriorityDrawEnabled() && !clickInColourBox) {
                                // If click is on the priority button but not in the colour box and priority
                                // drawing is currently on then turn off priority drawing.
                                picture.processPriorityColourOff();
                            } else {
                                // Pop up colour chooser.
                                ColourChooserDialog dialog = new ColourChooserDialog(ColourButton.this);
                                dialog.show();
                                
                                // Process the chosen priority colour.
                                if (dialog.getChosenColour() != -1) {
                                    picture.processPriorityColourChange(dialog.getChosenColour());
                                }
                            }
                            break;
                    }
                }
                
                update();
			}
        }
    }        
    
    /**
     * Tool button class used for all buttons on the internal frames tool bar panel.
     */
    class ToolButton extends ToggleButton {
        
    	private PicEdit picEdit;
    	
    	private ToolType tool;
    	
        /**
         * Constructor for PictureTool.
         * 
         * @param iconImageName The name of the image file for the button icon.
         * @param picEdit The PicEdit application.
         * @param clickHandler The click handler that processes actions on this button.
         * @param tool The tool that this ToolButton is associated with.
         */
        ToolButton(String iconImageName, PicEdit picEdit, ToolType tool) {
        	super(new Image("/editor/picedit/" + iconImageName));
        	
            setPixelSize(32, 32);
            setTitle(tool.toString());

            this.picEdit = picEdit;
            this.tool = tool;
            
            addClickHandler(new ToolPanelClickHandler(picEdit, this));
        }
        
        public ToolType getTool() {
        	return tool;
        }
        
        /**
         * Updates the state of the button to reflect the current edit status.
         */
        public void update() {
        	setEnabled(isEnabled());
        }
        
        /**
         * Indicates if the button can be selected or triggered by an input device, such
         * as a mouse pointer.
         *
         * @return <code>true</code> if the button is enabled
         */
        public boolean isEnabled() {
            boolean enabled = false;
            
            if (picEdit != null) {
                if (tool.equals(ToolType.AIRBRUSH) || 
                    tool.equals(ToolType.BRUSH) || 
                    tool.equals(ToolType.FILL) || 
                    tool.equals(ToolType.LINE) || 
                    tool.equals(ToolType.SHORTLINE) || 
                    tool.equals(ToolType.STEPLINE)) {
                  
                    // Tools are only active when at least one picture frame is displayed,
                    // and the current position is not on a data code. We don't want the
                    // user to use the tools within the middle of an existing action.
                    enabled = !picEdit.getPicture().getCurrentPictureCode().isDataCode();
                }
            }
          
            return enabled;
        }
    }
    
    class ToolPanelClickHandler implements ClickHandler {

        protected PicEdit picEdit;
    	
        protected ToolButton toolButton;
        
        /**
         * Constructor for ToolPanelClickHandler.
         * 
         * @param picEdit The PicEdit application.
         */
        public ToolPanelClickHandler(PicEdit picEdit, ToolButton toolButton) {
            this.picEdit = picEdit;
            this.toolButton = toolButton;
        }
        
		@Override
		public void onClick(ClickEvent event) {
            switch (toolButton.getTool()) {
                case BRUSH:
                    // Pop up brush chooser.
                    BrushChooserDialog brushDialog = new BrushChooserDialog(toolButton, false);
                    brushDialog.setVisible(true);
                    if (brushDialog.getChosenBrush() != null) {
                    	picEdit.getEditStatus().setBrushCode(brushDialog.getChosenBrush().getBrushCode());
                    }
                    break;
                case AIRBRUSH:
                    // Pop up brush chooser.
                    BrushChooserDialog airBrushDialog = new BrushChooserDialog(toolButton, true);
                    airBrushDialog.setVisible(true);
                    if (airBrushDialog.getChosenBrush() != null) {
                    	picEdit.getEditStatus().setBrushCode(airBrushDialog.getChosenBrush().getBrushCode());
                    }
                    break;
            }
            
            // Process the selected tool.
            picEdit.getEditStatus().setTool(toolButton.getTool());
		}
    }
}
