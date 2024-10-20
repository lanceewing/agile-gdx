package com.agifans.agile.editor.sound;

import com.agifans.agile.agilib.Game;
import com.agifans.agile.agilib.Sound;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SoundEditPanel extends Composite {

    interface Binder extends UiBinder<HorizontalPanel, SoundEditPanel> { }
    private static final Binder binder = GWT.create(Binder.class);
    
    @UiField
    HorizontalPanel horizontalPanel;
    
    @UiField
    ScrollPanel soundsScrollPanel;
    
    @UiField
    VerticalPanel soundsVerticalPanel;
    
    @UiField
    SimplePanel soundDetailPanel;
    
    /**
     * Reference to the currently selected sound thumbnail.
     */
    private SoundThumbnail selectedThumbnail;
    
    /**
     * Current game whose sounds are being shown.
     */
    private Game game;
    
    public SoundEditPanel() {
        initWidget(binder.createAndBindUi(this));
        
        soundsScrollPanel.addStyleName("soundsScrollPanel");
        soundsVerticalPanel.addStyleName("soundsVerticalPanel");
        horizontalPanel.addStyleName("soundsHorizontalPanel");
        soundDetailPanel.addStyleName("soundDetailPanel");
    }
    
    public void loadSounds(Game game) {
        this.game = game;
        
        Sound[] sounds = game.sounds;
        int soundNumber = 0;
        
        for (Sound sound : sounds) {
            if (sound != null) {
                SoundThumbnail thumbnail = new SoundThumbnail(
                        this, "/editor/sound.svg", soundNumber++);
                if (soundNumber == 1) {
                    changeSelection(thumbnail);
                }
                soundsVerticalPanel.add(thumbnail);
            }
        }
    }
    
    public void changeSelection(SoundThumbnail thumbnail) {
        if (selectedThumbnail != null) {
            selectedThumbnail.setSelected(false);
        }
        thumbnail.setSelected(true);
        selectedThumbnail = thumbnail;
    }
}
