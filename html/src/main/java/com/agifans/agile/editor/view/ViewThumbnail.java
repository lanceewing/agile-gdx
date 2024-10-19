package com.agifans.agile.editor.view;

import com.agifans.agile.util.StringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ViewThumbnail extends Composite {

    interface Binder extends UiBinder<Widget, ViewThumbnail> {}
    private static final Binder binder = GWT.create(Binder.class);

    @UiField
    FocusPanel thumbnailLink;
    
    @UiField
    HTMLPanel thumbnailPanel;
    
    @UiField
    SimplePanel viewImageBackground;
    
    @UiField
    Image viewImage;
    
    private ViewEditPanel viewEditPanel;
    
    private ViewThumbnailData viewThumbnailData;
    
    public ViewThumbnail(ViewEditPanel viewEditPanel, String dataUrl, 
            int viewNumber, int width, int height, int transparent) {
        
        this.viewEditPanel = viewEditPanel;
        
        viewThumbnailData = new ViewThumbnailData(viewNumber);
        
        initWidget(binder.createAndBindUi(this));
        
        thumbnailPanel.addStyleName("selector_list-item sprite-selector-item_sprite-selector-item");
        
        viewImage.addStyleName("sprite-selector-item_sprite-image");
        viewImage.addStyleName("viewThumbnail");
        viewImage.setUrl(dataUrl);
        
        // Max width is 80 and max height is 60. Work out the best size to 
        // fit within that but also keep aspect ratio.
        int adjHeight = Math.round((80f / (width * 2)) * height);
        int adjWidth = Math.round((60f / height) * (width * 2));
        if (adjHeight <= 60) {
            adjWidth = 80;
        }
        else if (adjWidth <= 80) {
            adjHeight = 60;
        }
        else {
            adjHeight = 60;
            adjWidth = 80;
        }
        
        viewImage.getElement().getStyle().setPropertyPx("width", adjWidth);
        viewImage.getElement().getStyle().setPropertyPx("height", adjHeight);
        
        //String bgColour = "#" + StringUtils.padLeftZeros(Integer.toHexString(transparent), 8);
        String bgColour = "#D9E3F2";
        viewImageBackground.addStyleName("sprite-selector-item_sprite-image-inner");
        viewImageBackground.getElement().getStyle().setBackgroundColor(bgColour);
    }
    
    @UiFactory
    public ViewThumbnailData getViewThumbnailData() {
        return viewThumbnailData;
    }
    
    public void setSelected(boolean selected) {
        if (selected) {
            thumbnailPanel.addStyleName("sprite-selector-item_is-selected");
        } else {
            thumbnailPanel.removeStyleName("sprite-selector-item_is-selected");
        }
    }
    
    @UiHandler("thumbnailLink")
    public void onThumbnailClicked(ClickEvent event) {
        viewEditPanel.changeSelection(this);
    }
    
    private final native void logToJSConsole(String message)/*-{
        console.log(message);
    }-*/;
}
