package com.agifans.agile.editor.picture.picedit.gui.toolbar;

import com.agifans.agile.EgaPalette;
import com.agifans.agile.agilib.jagi.awt.Point;
import com.agifans.agile.editor.picture.picedit.gui.toolbar.ToolPanel.ColourButton;
import com.agifans.agile.editor.picture.picedit.picture.Picture;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Colour chooser dialog used when selecting visual, priority and control colours.
 * 
 * @author Lance Ewing
 */
public class ColourChooserDialog extends PopupPanel  {
    
    private static final int BOX_SIZE = 34;

    private ColourButton colourButton;
    
    private Picture picture;
    
    /**
     * Holds the colour that the user clicked on.
     */
    private int chosenColour = -1;
    
    /**
     * Constructor for ColourChooserDialog.
     * 
     * @param colourButton The button component under which the palette will be drawn.
     * @param picture 
     */
    public ColourChooserDialog(ColourButton colourButton, Picture picture) {
        this(new Point(colourButton.getAbsoluteLeft(), colourButton.getAbsoluteTop() + colourButton.getOffsetHeight()), picture);
        this.colourButton = colourButton;
    }
    
    /**
     * Constructor for ColourChooserDialog.
     * 
     * @param point The point at which the palette will be drawn.
     * @param picture 
     */
    public ColourChooserDialog(Point point, Picture picture) {
        this.picture = picture;
        
        setModal(true);
        setPixelSize(BOX_SIZE * 4, BOX_SIZE * 4);
        setPopupPosition(point.x, point.y);
        
        ColourChooserPanel palettePanel = new ColourChooserPanel();
        add(palettePanel);
        
        palettePanel.getCanvas().addClickHandler(new ColourChooserClickHandler());
        palettePanel.getCanvas().addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() == 0x1B) {
                    ColourChooserDialog.this.hide();
                }
			}
		});
    }
    
    /**
     * Returns the colour that the user clicked on.
     * 
     * @return The colour that the user clicked on.
     */
    public int getChosenColour() {
        return chosenColour;
    }
    
    /**
     * The panel that holds the colour palette.
     */
    class ColourChooserPanel extends SimplePanel {
      
    	private Canvas canvas;
    	
        ColourChooserPanel() {
        	canvas = Canvas.createIfSupported();
        	canvas.getCanvasElement().setWidth(BOX_SIZE * 4);
        	canvas.getCanvasElement().setHeight(BOX_SIZE * 4);
        	draw();
        	add(canvas);
        }
        
        public Canvas getCanvas() {
        	return canvas;
        }
        
        public void draw() {
            int colourCode = 0;
            Context2d context2d = canvas.getContext2d();
            for (int row = 0; row < BOX_SIZE * 4; row = row + BOX_SIZE) {
                for (int column=0; column < BOX_SIZE * 4; column = column + BOX_SIZE) {
                	context2d.setFillStyle(EgaPalette.toCssRgba(EgaPalette.colours[colourCode++]));
                	context2d.fillRect(column, row, BOX_SIZE, BOX_SIZE);
                }
            }
        }
    }
    
    /**
     * Handler that processes the mouse click event on the colour chooser palette.
     */
    class ColourChooserClickHandler implements ClickHandler {
    	
		@Override
		public void onClick(ClickEvent event) {
            if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            	chosenColour = (((int)(event.getY() / BOX_SIZE)) * 4) + (event.getX() / BOX_SIZE);
            	
            	switch (colourButton.getColourType()) {
                    case PRIORITY:
                        picture.processPriorityColourChange(chosenColour);
                        break;
                        
                    case VISUAL:
                        picture.processVisualColourChange(chosenColour);
                        break;
            	}
            	
            	colourButton.update();
            }
            ColourChooserDialog.this.hide();
		}
    }
}
