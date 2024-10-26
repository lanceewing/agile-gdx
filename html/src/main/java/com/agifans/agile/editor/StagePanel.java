package com.agifans.agile.editor;

import com.agifans.agile.Agile;
import com.agifans.agile.DebugInfo;
import com.agifans.agile.GameScreen;
import com.agifans.agile.agilib.Game;
import com.agifans.agile.gwt.GwtLauncher;
import com.badlogic.gdx.backends.gwt.GwtGraphics;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication.LoadingListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class StagePanel extends ResizeComposite {

    interface Binder extends UiBinder<Widget, StagePanel> { }
    private static final Binder binder = GWT.create(Binder.class);

    @UiField
    FocusPanel playLink;
    
    @UiField
    FocusPanel stopLink;
    
    @UiField
    FocusPanel pauseLink;
    
    @UiField
    VerticalPanel agileCanvasPanel;
    
    @UiField
    HTMLPanel stageWrapperPanel;
    
    private EditPanel editPanel;
    
    private GwtGraphics graphics;
    
    private GwtLauncher agileLauncher;
    
    private DebugInfo debugInfo;
    
    public StagePanel() {
        initWidget(binder.createAndBindUi(this));
        
        agileCanvasPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        agileCanvasPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        agileCanvasPanel.addStyleName("agileCanvasPanel");
        
        stageWrapperPanel.addStyleName("stage_stage");
        
        debugInfo = new DebugInfo();
        
        agileLauncher = new GwtLauncher(agileCanvasPanel, 480, 364, debugInfo);
        agileLauncher.setLoadingListener(new LoadingListener() {
            public void beforeSetup() {}
            public void afterSetup() {
                graphics = (GwtGraphics)Gdx.graphics;
                onResize();
            }
        });
        agileLauncher.onModuleLoad();
    }
    
    public void setEditPanel(EditPanel editPanel) {
        this.editPanel = editPanel;
    }
    
    public void onResize() {
        if (graphics != null) {
            graphics.setWindowedMode(
                    stageWrapperPanel.getOffsetWidth(), 
                    (int)(stageWrapperPanel.getOffsetWidth() / 1.32f));
        }
    }
    
    private final native void logToJSConsole(String message)/*-{
        console.log(message);
    }-*/;
    
    class ResizeListener implements ResizeHandler {
        @Override
        public void onResize (ResizeEvent event) {
            if (graphics != null) {
                graphics.setWindowedMode(
                        stageWrapperPanel.getOffsetWidth(), 
                        (int)(stageWrapperPanel.getOffsetWidth() / 1.32f));
            }
        }
    }
    
    @UiHandler("playLink")
    public void onPlayButtonClicked(ClickEvent event) {
        Agile agile = agileLauncher.getAgile();
        if ((agile != null) && (!agile.getAgileRunner().isRunning())) {
            Game game = editPanel.getGame();
            if (game != null) {
                GameScreen gameScreen = agile.getGameScreen();
                gameScreen.initGame(null, true);
                agile.setScreen(gameScreen);
                agile.getAgileRunner().start(game.getGameFilesMap());
            }
        }
    }
    
    @UiHandler("stopLink")
    public void onStopButtonClicked(ClickEvent event) {
        Agile agile = agileLauncher.getAgile();
        if ((agile != null) && (agile.getAgileRunner().isRunning())) {
            agile.getAgileRunner().stop();
        }
    }
    
    @UiHandler("pauseLink")
    public void onPauseButtonClicked(ClickEvent event) {
        Agile agile = agileLauncher.getAgile();
        if ((agile != null) && (agile.getAgileRunner().isRunning())) {
            if (!agile.getAgileRunner().isPaused()) {
                agile.getAgileRunner().pause();
            } else {
                agile.getAgileRunner().resume();
            }
        }
    }
}
