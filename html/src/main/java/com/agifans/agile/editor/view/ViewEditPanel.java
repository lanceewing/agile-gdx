package com.agifans.agile.editor.view;

import com.agifans.agile.agilib.Game;
import com.agifans.agile.agilib.View;
import com.google.gwt.core.client.GWT;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8ClampedArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewEditPanel extends Composite {

    interface Binder extends UiBinder<HorizontalPanel, ViewEditPanel> { }
    private static final Binder binder = GWT.create(Binder.class);
    
    @UiField
    HorizontalPanel horizontalPanel;
    
    @UiField
    ScrollPanel viewsScrollPanel;
    
    @UiField
    VerticalPanel viewsVerticalPanel;
    
    /**
     * Reference to the currently selected view thumbnail.
     */
    private ViewThumbnail selectedThumbnail;
    
    /**
     * Current game whose views are being shown.
     */
    private Game game;
    
    public ViewEditPanel() {
        initWidget(binder.createAndBindUi(this));
        
        viewsScrollPanel.addStyleName("viewsScrollPanel");
        viewsVerticalPanel.addStyleName("viewsVerticalPanel");
        horizontalPanel.addStyleName("viewsHorizonalPanel");
    }
    
    public void loadPictures(Game game) {
        this.game = game;
        
        View[] views = game.views;
        int pictureNumber = 0;
        
        for (View view : views) {
            if (view != null) {
                
                
    //                Uint8ClampedArray pixelArray = TypedArrays.createUint8ClampedArray(picClone.getVisualPixels().length * 4);
    //                for (int index=0, pictureIndex=0; index < pixelArray.byteLength(); index += 4) {
    //                    int rgba8888Colour = picClone.getVisualPixels()[pictureIndex++];
    //                    pixelArray.set(index + 0, (rgba8888Colour >> 24) & 0xFF);
    //                    pixelArray.set(index + 1, (rgba8888Colour >> 16) & 0xFF);
    //                    pixelArray.set(index + 2, (rgba8888Colour >>  8) & 0xFF);
    //                    pixelArray.set(index + 3, (rgba8888Colour >>  0) & 0xFF);
    //                }
    //                
    //                String imgDataUrl = convertPixelsToDataUrl(pixelArray, 160, 160);
    //                
    //                PictureThumbnail thumbnail = new PictureThumbnail(this, imgDataUrl, pictureNumber++);
    //                if (pictureNumber == 1) {
    //                    changeSelection(thumbnail);
    //                }
    //                picturesVerticalPanel.add(thumbnail);
                
            }
        }
    }
    
    public void changeSelection(ViewThumbnail thumbnail) {
        if (selectedThumbnail != null) {
            selectedThumbnail.setSelected(false);
        }
        thumbnail.setSelected(true);
        selectedThumbnail = thumbnail;
    }
    
    private final native String convertPixelsToDataUrl(ArrayBufferView pixels, int width, int height)/*-{
        var canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        var ctx = canvas.getContext('2d');
        var imgData = ctx.createImageData(width, height);
        var data = imgData.data;
        for (var i = 0, len = width * height * 4; i < len; i++) {
            data[i] = pixels[i] & 0xff;
        }
        ctx.putImageData(imgData, 0, 0);
        return canvas.toDataURL('image/png');
    }-*/;
    
    private final native void logToJSConsole(String message)/*-{
        console.log(message);
    }-*/;
}
