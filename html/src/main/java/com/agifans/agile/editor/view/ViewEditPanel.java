package com.agifans.agile.editor.view;

import com.agifans.agile.agilib.Game;
import com.agifans.agile.agilib.View;
import com.agifans.agile.agilib.View.Cel;
import com.agifans.agile.agilib.View.Loop;
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
    
    public void loadViews(Game game) {
        this.game = game;
        
        View[] views = game.views;
        int viewNumber = 0;
        
        for (View view : views) {
            if (view != null) {
                if ((view.loops != null) && (!view.loops.isEmpty())) {
                    Loop loop = view.loops.get(0);
                    if ((loop.cels != null) && (!loop.cels.isEmpty())) {
                        Cel cel = loop.cels.get(0);
                        
                        Uint8ClampedArray pixelArray = TypedArrays.createUint8ClampedArray(cel.getPixelData().length * 4);
                        for (int index=0, viewIndex=0; index < pixelArray.byteLength(); index += 4) {
                            int rgba8888Colour = cel.getPixelData()[viewIndex++];
                            pixelArray.set(index + 0, (rgba8888Colour >> 24) & 0xFF);
                            pixelArray.set(index + 1, (rgba8888Colour >> 16) & 0xFF);
                            pixelArray.set(index + 2, (rgba8888Colour >>  8) & 0xFF);
                            pixelArray.set(index + 3, (rgba8888Colour >>  0) & 0xFF);
                        }
                        
                        String imgDataUrl = convertPixelsToDataUrl(pixelArray, cel.getWidth(), cel.getHeight());
                        
                        ViewThumbnail thumbnail = new ViewThumbnail(this, imgDataUrl, viewNumber++);
                        if (viewNumber == 1) {
                            changeSelection(thumbnail);
                        }
                        viewsVerticalPanel.add(thumbnail);
                    }   
                }
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
