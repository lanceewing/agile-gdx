package com.agifans.agile.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class PictureThumbnail extends Composite {

    interface Binder extends UiBinder<Widget, PictureThumbnail> {}
    private static final Binder binder = GWT.create(Binder.class);

    @UiField
    Image pictureImage;
    
    public PictureThumbnail(String dataUrl) {
        initWidget(binder.createAndBindUi(this));
        
        pictureImage.addStyleName("sprite-selector-item_sprite-image");
        pictureImage.addStyleName("pictureThumbnail");
        pictureImage.setUrl(dataUrl);
    }
    
}
