package com.agifans.agile.editor.picture.picedit.gui.toolbar;

import com.agifans.agile.EgaPalette;
import com.agifans.agile.agilib.jagi.awt.Point;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Colour chooser dialog used when selecting visual, priority and control colours.
 * 
 * @author Lance Ewing
 */
public class ColourChooserDialog extends PopupPanel  {

    /**
     * Holds the colour that the user clicked on.
     */
    private int chosenColour = -1;
    
    /**
     * Constructor for ColourChooserDialog.
     * 
     * @param button The button component under which the palette will be drawn.
     */
    public ColourChooserDialog(Widget button) {
        this(new Point(button.getAbsoluteLeft(), button.getAbsoluteTop() + button.getOffsetHeight()));
    }
    
    /**
     * Constructor for ColourChooserDialog.
     * 
     * @param point The point at which the palette will be drawn.
     */
    public ColourChooserDialog(Point point) {
        setModal(true);
        setPixelSize(68, 68);
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
        	canvas.getCanvasElement().setWidth(64);
        	canvas.getCanvasElement().setHeight(64);
        	draw();
        	add(canvas);
        }
        
        public Canvas getCanvas() {
        	return canvas;
        }
        
        public void draw() {
            int colourCode = 0;
            Context2d context2d = canvas.getContext2d();
            for (int row = 0; row < 64; row = row + 16) {
                for (int column=0; column < 64; column = column + 16) {
                	context2d.setFillStyle(EgaPalette.toCssRgba(EgaPalette.colours[colourCode++]));
                	context2d.fillRect(column, row, 16, 16);
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
            	chosenColour = (((int)(event.getY() / 16)) * 4) + (event.getX() / 16);
            }
            ColourChooserDialog.this.hide();
		}
    }
}
