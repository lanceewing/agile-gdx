package com.agifans.agile.editor;

import com.agifans.agile.agilib.Game;
import com.agifans.agile.editor.logic.LogicEditPanel;
import com.agifans.agile.editor.picture.PictureEditPanel;
import com.agifans.agile.editor.sound.SoundEditPanel;
import com.agifans.agile.editor.view.ViewEditPanel;
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
    
    @UiField
    ViewEditPanel viewEditPanel;
    
    @UiField
    SoundEditPanel soundEditPanel;
    
    private Game game;
    
    public EditPanel() {
        initWidget(binder.createAndBindUi(this));
    }
    
    public void loadGame(Game game) {
        this.game = game;
        
        logicEditPanel.loadLogics(game);
        pictureEditPanel.loadPictures(game);
        viewEditPanel.loadViews(game);
        soundEditPanel.loadSounds(game);
    }
}
