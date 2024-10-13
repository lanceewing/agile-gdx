package com.agifans.agile.editor.view;

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
import com.google.gwt.user.client.ui.Widget;

public class ViewThumbnail extends Composite {

    interface Binder extends UiBinder<Widget, ViewThumbnail> {}
    private static final Binder binder = GWT.create(Binder.class);

    @UiField
    FocusPanel thumbnailLink;
    
    @UiField
    HTMLPanel thumbnailPanel;
    
    @UiField
    Image viewImage;
    
    private ViewEditPanel viewEditPanel;
    
    private ViewThumbnailData viewThumbnailData;
    
    public ViewThumbnail(ViewEditPanel viewEditPanel, String dataUrl, int viewNumber) {
        this.viewEditPanel = viewEditPanel;
        
        viewThumbnailData = new ViewThumbnailData(viewNumber);
        
        initWidget(binder.createAndBindUi(this));
        
        thumbnailPanel.addStyleName("selector_list-item sprite-selector-item_sprite-selector-item");
        
        viewImage.addStyleName("sprite-selector-item_sprite-image");
        viewImage.addStyleName("viewThumbnail");
        viewImage.setUrl(dataUrl);
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
}
