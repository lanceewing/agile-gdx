package com.agifans.agile.editor.picture.picedit.gui.toolbar;

import com.agifans.agile.EgaPalette;
import com.agifans.agile.agilib.picedit.BrushShape;
import com.agifans.agile.agilib.picedit.BrushTexture;
import com.agifans.agile.agilib.picedit.BrushType;
import com.agifans.agile.editor.picture.picedit.picture.Picture;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.OutlineStyle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Brush chooser dialog used when selecting brush and airbrush shapes and sizes.
 * 
 * @author Lance Ewing
 */
public class BrushChooserDialog extends PopupPanel {

    /**
     * Holds the brush that the user clicked on.
     */
    private BrushType chosenBrush;

    /**
     * Constructor for BrushChooserDialog.
     * 
     * @param button   The button wdget under which the dialog will be drawn.
     * @param airBrush true if this is the air brush variant of the brush; otherwise
     *                 false.
     */
    public BrushChooserDialog(Widget button, final boolean airBrush) {
        setModal(true);
        setPixelSize(140, 140);
        setPopupPosition(button.getAbsoluteLeft(), button.getAbsoluteTop());

        BrushChooserButtonPanel brushChooserButtonPanel = new BrushChooserButtonPanel(airBrush);
        add(brushChooserButtonPanel);
    }

    /**
     * Plots a brush for the given parameters.
     * 
     * @param x          The X position to plot the brush at.
     * @param y          The Y position to plot the brush at.
     * @param penSize    The size of the brush (0-7).
     * @param isSquare   true if the brush is square; false if circle.
     * @param isAirBrush true if the brush is an airbrush; false if solid brush.
     * @param graphics   The Graphics to use to draw the brush with.
     */
    public void plotBrush(int x, int y, int penSize, boolean isSquare, boolean isAirBrush, Context2d graphics) {
        int circlePos = 0;
        int bitPos = Picture.splatterStart[10];

        graphics.setFillStyle(EgaPalette.toCssRgba(EgaPalette.black));
        
        for (int y1 = (y + 8) - penSize; y1 <= (y + 8) + penSize; y1++) {
            for (int x1 = (x + 8) - penSize; x1 <= (x + 8) + penSize; x1 += 2) {
                if (isSquare) {
                    if (isAirBrush) {
                        if (((Picture.splatterMap[bitPos >> 3] >> (7 - (bitPos & 7))) & 1) > 0) {
                            graphics.fillRect((x1 << 1) - 1, (y1 << 1), 4, 2);
                        }
                        bitPos++;
                        if (bitPos == 0xff) {
                            bitPos = 0;
                        }
                    } else {
                        // Not an airbrush implies a solid brush.
                        graphics.fillRect((x1 << 1) - 1, (y1 << 1), 4, 2);
                    }
                } else {
                    // Not a square implies circle.
                    if (((Picture.circles[penSize][circlePos >> 3] >> (7 - (circlePos & 7))) & 1) > 0) {
                        if (isAirBrush) {
                            if (((Picture.splatterMap[bitPos >> 3] >> (7 - (bitPos & 7))) & 1) > 0) {
                                graphics.fillRect((x1 << 1) - 1, (y1 << 1), 4, 2);
                            }
                            bitPos++;
                            if (bitPos == 0xff) {
                                bitPos = 0;
                            }
                        } else {
                            // Not an airbrush implies a solid brush.
                            graphics.fillRect((x1 << 1) - 1, (y1 << 1), 4, 2);
                        }
                    }
                    circlePos++;
                }
            }
        }
    }

    /**
     * Returns the brush that the user clicked on.
     * 
     * @return The brush that the user clicked on.
     */
    public BrushType getChosenBrush() {
        return chosenBrush;
    }

    /**
     * Panel for holding the brush buttons.
     */
    class BrushChooserButtonPanel extends FlowPanel {

        /**
         * Constructor for BrushChooserButtonPanel.
         */
        BrushChooserButtonPanel(boolean airBrush) {
            setPixelSize(140, 140);

            getElement().getStyle().setBackgroundColor(EgaPalette.toCssRgba(EgaPalette.grey));
            getElement().getStyle().setOutlineStyle(OutlineStyle.RIDGE);

            if (airBrush) {
                this.add(new BrushChooserButton(BrushType.CIRCLE_SPRAY_0));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SPRAY_4));
                this.add(new BrushChooserButton(BrushType.SQUARE_SPRAY_0));
                this.add(new BrushChooserButton(BrushType.SQUARE_SPRAY_4));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SPRAY_1));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SPRAY_5));
                this.add(new BrushChooserButton(BrushType.SQUARE_SPRAY_1));
                this.add(new BrushChooserButton(BrushType.SQUARE_SPRAY_5));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SPRAY_2));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SPRAY_6));
                this.add(new BrushChooserButton(BrushType.SQUARE_SPRAY_2));
                this.add(new BrushChooserButton(BrushType.SQUARE_SPRAY_6));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SPRAY_3));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SPRAY_7));
                this.add(new BrushChooserButton(BrushType.SQUARE_SPRAY_3));
                this.add(new BrushChooserButton(BrushType.SQUARE_SPRAY_7));
            } else {
                this.add(new BrushChooserButton(BrushType.CIRCLE_SOLID_0));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SOLID_4));
                this.add(new BrushChooserButton(BrushType.SQUARE_SOLID_0));
                this.add(new BrushChooserButton(BrushType.SQUARE_SOLID_4));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SOLID_1));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SOLID_5));
                this.add(new BrushChooserButton(BrushType.SQUARE_SOLID_1));
                this.add(new BrushChooserButton(BrushType.SQUARE_SOLID_5));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SOLID_2));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SOLID_6));
                this.add(new BrushChooserButton(BrushType.SQUARE_SOLID_2));
                this.add(new BrushChooserButton(BrushType.SQUARE_SOLID_6));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SOLID_3));
                this.add(new BrushChooserButton(BrushType.CIRCLE_SOLID_7));
                this.add(new BrushChooserButton(BrushType.SQUARE_SOLID_3));
                this.add(new BrushChooserButton(BrushType.SQUARE_SOLID_7));
            }
        }

        /**
         * Toggle button used for showing the various brush types for selection.
         */
        class BrushChooserButton extends ToggleButton {
            
            private BrushType brushType;

            private Canvas canvas;
            
            BrushChooserButton(BrushType brushType) {
                this.brushType = brushType;
                
                setPixelSize(34, 34);
                setTitle(brushType.getDisplayName());
                
                canvas = Canvas.createIfSupported();
                canvas.getCanvasElement().setWidth(34);
                canvas.getCanvasElement().setHeight(34);
                
                plotBrush(0, 0, brushType.getSize(), brushType.getShape().equals(BrushShape.SQUARE),
                        brushType.getTexture().equals(BrushTexture.SPRAY), canvas.getContext2d());
                
                add(canvas);
                
                addClickHandler(new BrushChooserClickHandler());
                
                addKeyPressHandler(new KeyPressHandler() {
                    public void onKeyPress(KeyPressEvent event) {
                        if (event.getCharCode() == 0x1B) {
                            BrushChooserDialog.this.hide();
                        }
                    }
                });
            }

            /**
             * ClickHandler for BrushChooserButtons. Stores the selected brush, or hides the chooser
             * dialog, depending on which button was clicked.
             */
            class BrushChooserClickHandler implements ClickHandler {
                public void onClick(ClickEvent event) {
                    if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
                        chosenBrush = brushType;
                    }
                    BrushChooserDialog.this.hide();
                }
            }
        }
    }
}
