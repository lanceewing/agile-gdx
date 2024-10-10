package com.agifans.agile.editor;

import com.agifans.agile.agilib.Game;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ResizeComposite;

public class EditPanel extends ResizeComposite {

    interface Binder extends UiBinder<ScrolledTabLayoutPanel, EditPanel> { }
    private static final Binder binder = GWT.create(Binder.class);
    
    @UiField
    ScrolledTabLayoutPanel tabs;
    
    @UiField
    LogicEditPanel logicEditPanel;
    
    @UiField
    PictureEditPanel pictureEditPanel;
    
    private Game game;
    
    public EditPanel() {
        initWidget(binder.createAndBindUi(this));
    }
    
    public void loadGame(Game game) {
        this.game = game;
        
        pictureEditPanel.loadPictures(game);
    }
}
