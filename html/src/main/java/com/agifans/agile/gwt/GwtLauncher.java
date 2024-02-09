package com.agifans.agile.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Panel;
import com.agifans.agile.Agile;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
    
    @Override
    public GwtApplicationConfiguration getConfig () {
        // Resizable application, uses available space in browser with no padding:
        GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
        cfg.padVertical = 0;
        cfg.padHorizontal = 0;
        return cfg;
        // If you want a fixed size application, comment out the above resizable section,
        // and uncomment below:
        //return new GwtApplicationConfiguration(640, 480);
    }

    @Override
    public ApplicationListener createApplicationListener () {
        GwtDialogHandler gwtDialogHandler = new GwtDialogHandler();
    	GwtAgileRunner gwtAgileRunner = new GwtAgileRunner(
    	        new GwtUserInput(), new GwtWavePlayer(), new GwtSavedGameStore(),
    	        new GwtPixelData(), new GwtVariableData());
        return new Agile(gwtAgileRunner, gwtDialogHandler);
    }
    
    @Override
    public Preloader.PreloaderCallback getPreloaderCallback() {
        return createPreloaderPanel(GWT.getHostPageBaseURL() + "agile_title.png");
    }

    @Override
    protected void adjustMeterPanel(Panel meterPanel, Style meterStyle) {
        meterPanel.setStyleName("gdx-meter");
        meterStyle.setProperty("backgroundColor", "#FDB400");
        meterStyle.setProperty("backgroundImage", "none");
    }
}
