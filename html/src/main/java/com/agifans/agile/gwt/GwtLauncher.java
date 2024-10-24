package com.agifans.agile.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;

import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.Agile;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
    
    private Panel rootPanel;
    
    private int initialWidth;
    
    private int initialHeight;
    
    private Boolean debugMode;
    
    private Agile agile;
    
    public GwtLauncher() {
    }
    
    public GwtLauncher(Panel rootPanel, int initialWidth, int initialHeight) {
        this.rootPanel = rootPanel;
        this.initialWidth = initialWidth;
        this.initialHeight = initialHeight;
        this.debugMode = true;
    }
    
    @Override
    public GwtApplicationConfiguration getConfig () {
        // Resizable application, uses available space in browser with no padding:
        //GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
        //cfg.padVertical = 0;
        //cfg.padHorizontal = 0;
        if (rootPanel != null) {
            // Editor mode.
            GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(initialWidth, initialHeight, false);
            cfg.padVertical = 0;
            cfg.padHorizontal = 0;
            cfg.rootPanel = rootPanel;
            return cfg;
        } else {
            // Normal non-editor mode.
            GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
            cfg.padVertical = 0;
            cfg.padHorizontal = 0;
            return cfg;
        }
        // If you want a fixed size application, comment out the above resizable section,
        // and uncomment below:
        //return new GwtApplicationConfiguration(640, 480);
    }

    @Override
    public ApplicationListener createApplicationListener () {
        Map<String, String> argsMap = new HashMap<>();
        
        String urlPath = Window.Location.getPath();
        
        if ("/".equals(urlPath) || "".equals(urlPath)) {
            // A game path was not included, so check for a hash instead.
            // HTML5 version supports /id/ and /uri/ hash values, e.g. #/id/kq2
            String hash = Window.Location.getHash().toLowerCase();
            
            if ((hash != null) && (hash.length() > 0)) {
                if (hash.startsWith("#/id/") && !hash.endsWith("/")) {
                    String gameId = hash.substring(hash.lastIndexOf('/') + 1);
                    argsMap.put("id", gameId);
                }
                if (hash.startsWith("#/uri/") && !hash.endsWith("/")) {
                    String gameUri = hash.substring(hash.lastIndexOf('/') + 1);
                    argsMap.put("uri", gameUri);
                }
            }
        } else {
            String uri = "";
            
            // If a path is included, assume it is to launch a game.
            if (urlPath.startsWith("/play/")) {
                uri = urlPath.substring(6);
            } else {
                uri = urlPath.substring(1);
            }
            
            // Check for and remove trailing slash
            if (uri.endsWith("/")) {
                uri = uri.substring(0, uri.lastIndexOf('/'));
            }
            
            argsMap.put("uri", uri);
        }
        
        if (Boolean.TRUE.equals(debugMode)) {
            argsMap.put("debugMode", "true");
        }
        
        GwtDialogHandler gwtDialogHandler = new GwtDialogHandler();
    	GwtAgileRunner gwtAgileRunner = new GwtAgileRunner(
    	        new GwtUserInput(), new GwtWavePlayer(), new GwtSavedGameStore(),
    	        new GwtPixelData(), new GwtVariableData());
    	agile = new Agile(gwtAgileRunner, gwtDialogHandler, argsMap);
    	
        return agile;
    }
    
    public Agile getAgile() {
        return agile;
    }
    
    @Override
    public Preloader.PreloaderCallback getPreloaderCallback() {
        return createPreloaderPanel("/agile_title.png");
    }

    @Override
    protected void adjustMeterPanel(Panel meterPanel, Style meterStyle) {
        meterPanel.setStyleName("gdx-meter");
        meterStyle.setProperty("backgroundColor", "#FDB400");
        meterStyle.setProperty("backgroundImage", "none");
    }
}
