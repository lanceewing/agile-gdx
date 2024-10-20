package com.agifans.agile.editor.logic;

import com.agifans.agile.agilib.Game;
import com.agifans.agile.agilib.Logic;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LogicEditPanel extends Composite {

    interface Binder extends UiBinder<HorizontalPanel, LogicEditPanel> { }
    private static final Binder binder = GWT.create(Binder.class);
    
    @UiField
    HorizontalPanel horizontalPanel;
    
    @UiField
    ScrollPanel logicsScrollPanel;
    
    @UiField
    VerticalPanel logicsVerticalPanel;
    
    @UiField
    SimplePanel logicDetailPanel;
    
    /**
     * Reference to the currently selected logic thumbnail.
     */
    private LogicThumbnail selectedThumbnail;
    
    /**
     * Current game whose views are being shown.
     */
    private Game game;
    
    public LogicEditPanel() {
        initWidget(binder.createAndBindUi(this));
        
        logicsScrollPanel.addStyleName("logicsScrollPanel");
        logicsVerticalPanel.addStyleName("logicsVerticalPanel");
        horizontalPanel.addStyleName("logicsHorizonalPanel");
        logicDetailPanel.addStyleName("logicDetailPanel");
    }
    
    public void loadLogics(Game game) {
        this.game = game;
        
        Logic[] logics = game.logics;
        int logicNumber = 0;
        
        for (Logic logic : logics) {
            if (logic != null) {
                LogicThumbnail thumbnail = new LogicThumbnail(
                        this, "/editor/blocks.svg", logicNumber++);
                if (logicNumber == 1) {
                    changeSelection(thumbnail);
                }
                logicsVerticalPanel.add(thumbnail);
            }
        }
    }
    
    public void changeSelection(LogicThumbnail thumbnail) {
        if (selectedThumbnail != null) {
            selectedThumbnail.setSelected(false);
        }
        thumbnail.setSelected(true);
        selectedThumbnail = thumbnail;
    }
}
