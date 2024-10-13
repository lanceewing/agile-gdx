package com.agifans.agile.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class PictureThumbnail extends Composite {

    interface Binder extends UiBinder<Widget, PictureThumbnail> {}
    private static final Binder binder = GWT.create(Binder.class);

    @UiField
    HTMLPanel thumbnailPanel;
    
    @UiField
    Image pictureImage;
    
    private PictureThumbnailData pictureThumbnailData;
    
    public PictureThumbnail(String dataUrl, int pictureNumber) {
        pictureThumbnailData = new PictureThumbnailData(pictureNumber);
        
        initWidget(binder.createAndBindUi(this));
        
        thumbnailPanel.addStyleName("selector_list-item sprite-selector-item_sprite-selector-item");
        
        pictureImage.addStyleName("sprite-selector-item_sprite-image");
        pictureImage.addStyleName("pictureThumbnail");
        pictureImage.setUrl(dataUrl);
    }
    
    @UiFactory
    public PictureThumbnailData getPictureThumbnailData() {
        return pictureThumbnailData;
    }
    
    public void setSelected(boolean selected) {
        if (selected) {
            thumbnailPanel.addStyleName("sprite-selector-item_is-selected");
        } else {
            thumbnailPanel.removeStyleName("sprite-selector-item_is-selected");
        }
    }
}