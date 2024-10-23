package com.agifans.agile.editor;

import com.agifans.agile.gwt.GwtLauncher;
import com.badlogic.gdx.backends.gwt.GwtGraphics;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication.LoadingListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
    VerticalPanel agileCanvasPanel;
    
    @UiField
    HTMLPanel stageWrapperPanel;
    
    private GwtGraphics graphics;
    
    public StagePanel() {
        initWidget(binder.createAndBindUi(this));
        
        agileCanvasPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        agileCanvasPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        agileCanvasPanel.addStyleName("agileCanvasPanel");
        
        stageWrapperPanel.addStyleName("stage_stage");
        
        GwtLauncher agileLauncher = new GwtLauncher(agileCanvasPanel, 480, 364);
        agileLauncher.setLoadingListener(new LoadingListener() {
            public void beforeSetup() {}
            public void afterSetup() {
                logToJSConsole("afterSetup called");
                graphics = (GwtGraphics)Gdx.graphics;
                if (graphics != null) {
                    logToJSConsole("graphics is not null");
                }
                onResize();
            }
        });
        agileLauncher.onModuleLoad();
    }
    
    public void onResize() {
        if (graphics != null) {
            logToJSConsole("calling setWindowedMode");
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
}
