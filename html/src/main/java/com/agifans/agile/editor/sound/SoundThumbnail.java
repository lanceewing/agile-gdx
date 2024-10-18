package com.agifans.agile.editor.sound;

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

public class SoundThumbnail extends Composite {

    interface Binder extends UiBinder<Widget, SoundThumbnail> {}
    private static final Binder binder = GWT.create(Binder.class);

    @UiField
    FocusPanel thumbnailLink;
    
    @UiField
    HTMLPanel thumbnailPanel;
    
    @UiField
    Image soundImage;
    
    private SoundEditPanel soundEditPanel;
    
    private SoundThumbnailData soundThumbnailData;
    
    public SoundThumbnail(SoundEditPanel soundEditPanel, String dataUrl, int soundNumber) {
        this.soundEditPanel = soundEditPanel;
        
        soundThumbnailData = new SoundThumbnailData(soundNumber);
        
        initWidget(binder.createAndBindUi(this));
        
        thumbnailPanel.addStyleName("selector_list-item sprite-selector-item_sprite-selector-item");
        
        soundImage.addStyleName("sprite-selector-item_sprite-image");
        soundImage.addStyleName("soundThumbnail");
        soundImage.setUrl(dataUrl);
    }
    
    @UiFactory
    public SoundThumbnailData getSoundThumbnailData() {
        return soundThumbnailData;
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
        soundEditPanel.changeSelection(this);
    }
}
