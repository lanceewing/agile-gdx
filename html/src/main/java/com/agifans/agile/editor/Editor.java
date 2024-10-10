package com.agifans.agile.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.agifans.agile.agilib.Game;
import com.agifans.agile.gwt.GwtGameLoader;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * GWT entry point for the AGI editor module.
 */
public class Editor implements EntryPoint {

    // GWT uses the Java logging API and converts it into a JavaScript equivalent.
    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    interface Binder extends UiBinder<DockLayoutPanel, Editor> { }
    
    private static final Binder binder = GWT.create(Binder.class);
    
    @UiField TopPanel topPanel;
    @UiField EditPanel editPanel;
    @UiField StagePanel stagePanel;
    
    @Override
    public void onModuleLoad() {
        
        // Create the UI defined in Editor.ui.xml.
        DockLayoutPanel outer = binder.createAndBindUi(this);
        
        // Get rid of scrollbars, and clear out the window's built-in margin,
        // because we want to take advantage of the entire client area.
        Window.enableScrolling(false);
        Window.setMargin("0px");
        
        // Special-case stuff to make topPanel overhang a bit.
        Element topElem = outer.getWidgetContainerElement(topPanel);
        topElem.getStyle().setZIndex(2);
        topElem.getStyle().setOverflow(Overflow.VISIBLE);
        
        
        // Add the outer panel to the RootLayoutPanel, so that it will be
        // displayed.
        RootLayoutPanel root = RootLayoutPanel.get();
        root.add(outer);
        
        // TODO: This is just to test for now. Needs more thought.
        Map<String, byte[]> gameFilesMap = new HashMap<>();
        GwtGameLoader gameLoader = new GwtGameLoader(null);
        gameLoader.fetchGameFiles("/games/ruby.zip", map -> gameFilesMap.putAll(map));
        Game game = gameLoader.loadGame(gameFilesMap);
        
        logger.log(Level.INFO, "game: " + game);
        
        logToJSConsole("game: " + (game == null? null : game.toString()));
        
        editPanel.loadGame(game);
    }
    
    private final native void logToJSConsole(String message)/*-{
        console.log(message);
    }-*/;
}