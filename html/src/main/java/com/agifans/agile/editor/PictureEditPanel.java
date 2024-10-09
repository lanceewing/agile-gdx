package com.agifans.agile.editor;

import com.agifans.agile.agilib.Game;
import com.agifans.agile.agilib.Picture;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PictureEditPanel extends Composite {

    interface Binder extends UiBinder<HorizontalPanel, PictureEditPanel> { }
    private static final Binder binder = GWT.create(Binder.class);
    
    @UiField
    HorizontalPanel horizontalPanel;
    
    @UiField
    ScrollPanel picturesScrollPanel;
    
    @UiField
    VerticalPanel picturesVerticalPanel;
    
    /**
     * Current game whose pictures are being shown.
     */
    private Game game;
    
    public PictureEditPanel() {
        initWidget(binder.createAndBindUi(this));
    }
    
    public void loadPictures(Game game) {
        this.game = game;
        
        Picture[] pictures = game.pictures;
        
        
    }
}
