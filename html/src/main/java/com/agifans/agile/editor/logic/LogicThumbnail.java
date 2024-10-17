package com.agifans.agile.editor.logic;

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

public class LogicThumbnail extends Composite {

    interface Binder extends UiBinder<Widget, LogicThumbnail> {}
    private static final Binder binder = GWT.create(Binder.class);

    @UiField
    FocusPanel thumbnailLink;
    
    @UiField
    HTMLPanel thumbnailPanel;
    
    @UiField
    Image logicImage;
    
    private LogicEditPanel logicEditPanel;
    
    private LogicThumbnailData logicThumbnailData;
    
    public LogicThumbnail(LogicEditPanel logicEditPanel, String dataUrl, int logicNumber) {
        this.logicEditPanel = logicEditPanel;
        
        logicThumbnailData = new LogicThumbnailData(logicNumber);
        
        initWidget(binder.createAndBindUi(this));
        
        thumbnailPanel.addStyleName("selector_list-item sprite-selector-item_sprite-selector-item");
        
        logicImage.addStyleName("sprite-selector-item_sprite-image");
        logicImage.addStyleName("logicThumbnail");
        logicImage.setUrl(dataUrl);
    }
    
    @UiFactory
    public LogicThumbnailData getLogicThumbnailData() {
        return logicThumbnailData;
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
        logicEditPanel.changeSelection(this);
    }
}
