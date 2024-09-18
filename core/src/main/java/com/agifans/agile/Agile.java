package com.agifans.agile;

import java.util.Map;

import com.agifans.agile.config.AppConfigItem;
import com.agifans.agile.ui.DialogHandler;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;

/**
 * The main entry point in to the cross-platform part of the Agile interpreter. A multi-screen
 * libGDX application needs to extend the Game class, which is what we do here. It allows us to
 * have other screens, such as various menu screens.
 */
public class Agile extends Game {
    
    // Screens
    private GameScreen gameScreen;
    private HomeScreen homeScreen;
    
    // Platform specific AgileRunner implementation.
    private AgileRunner agileRunner;
    
    /**
     * Invoked by AGILE whenever it would like to show a dialog, such as when it needs
     * the user to confirm an action, or to choose a file.
     */
    private DialogHandler dialogHandler;
    
    /**
     * For desktop, contains command line args. For HTML5, the hash and/or query parameters.
     */
    private Map<String, String> args;
    
    /**
     * AGILE's saved preferences.
     */
    private Preferences preferences;
    
    /**
     * Constructor for Agile.
     * 
     * @param agileRunner 
     * @param dialogHandler 
     * @param args 
     */
    public Agile(AgileRunner agileRunner, DialogHandler dialogHandler, Map<String, String> args) {
        this.agileRunner = agileRunner;
        this.dialogHandler = dialogHandler;
        this.args = args;
    }
    
    @Override
    public void create() {
        preferences = Gdx.app.getPreferences("agile.preferences");
        homeScreen = new HomeScreen(this, dialogHandler);
        gameScreen = new GameScreen(this, agileRunner, dialogHandler);
        
        AppConfigItem appConfigItem = null;
        
        if ((args != null) && (args.size() > 0)) {
            if (args.containsKey("id")) {
                appConfigItem = homeScreen.getAppConfigItemByGameId(args.get("id"));
            }
            if (args.containsKey("uri")) {
                appConfigItem = homeScreen.getAppConfigItemByGameUri(args.get("uri"));
            }
            if (args.containsKey("path")) {
                String filePath = args.get("path");
                appConfigItem = new AppConfigItem();
                appConfigItem.setFilePath(filePath);
                if ((filePath.toLowerCase().endsWith(".zip"))) {
                    appConfigItem.setFileType("ZIP");
                } else {
                    appConfigItem.setFileType("DIR");
                }
            }
        }
        
        setScreen(homeScreen);
        
        if (appConfigItem != null) {
            homeScreen.processGameSelection(appConfigItem);
        }
        
        // Stop the Android back key from immediately exiting the app.
        Gdx.input.setCatchKey(Keys.BACK, true);
    }

    /**
     * Gets the HomeScreen.
     * 
     * @return the HomeScreen.
     */
    public HomeScreen getHomeScreen() {
        return homeScreen;
    }
    
    /**
     * Gets the GameScreen.
     * 
     * @return the GameScreen.
     */
    public GameScreen getGameScreen() {
        return gameScreen;
    }
    
    /**
     * Gets the Preferences for AGILE.
     * 
     * @return The Preferences for AGILE.
     */
    public Preferences getPreferences() {
        return preferences;
    }
    
    /**
     * Gets the AgileRunner.
     * 
     * @return the AgileRunner.
     */
    public AgileRunner getAgileRunner() {
        return agileRunner;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        gameScreen.dispose();
        homeScreen.dispose();
        preferences.flush();
    }
}
