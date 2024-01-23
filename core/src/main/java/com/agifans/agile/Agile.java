package com.agifans.agile;

import com.agifans.agile.config.AppConfigItem;
import com.agifans.agile.config.AppConfigItem.FileLocation;
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
     * Command line args. Mainly applicable to desktop.
     */
    private String[] args;
    
    /**
     * AGILE's saved preferences.
     */
    private Preferences preferences;
    
    /**
     * AGILES's application screenshot storage.
     */
    private Preferences screenshotStore;
    
    /**
     * Constructor for Agile.
     * 
     * @param agileRunner 
     * @param dialogHandler 
     * @param args 
     */
    public Agile(AgileRunner agileRunner, DialogHandler dialogHandler, String... args) {
        this.agileRunner = agileRunner;
        this.dialogHandler = dialogHandler;
        this.args = args;
    }
    
    @Override
    public void create() {
        preferences = Gdx.app.getPreferences("agile.preferences");
        screenshotStore = Gdx.app.getPreferences("agile_screens.store");
        homeScreen = new HomeScreen(this, dialogHandler);
        gameScreen = new GameScreen(this, agileRunner, dialogHandler);
        
        if ((args != null) && (args.length > 0)) {
            AppConfigItem appConfigItem = new AppConfigItem();
            appConfigItem.setFilePath(args[0]);
            if ((args[0].toLowerCase().endsWith(".zip"))) {
                appConfigItem.setFileType("ZIP");
            }
            appConfigItem.setFileLocation(FileLocation.ABSOLUTE);
            GameScreen machineScreen = getGameScreen();
            machineScreen.initGame(appConfigItem);
            setScreen(machineScreen);
        } else {
            setScreen(homeScreen);
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
     * Gets the screenshot store for AGILE. 
     * 
     * @return The screenshot store for AGILE.
     */
    public Preferences getScreenshotStore() {
        return screenshotStore;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        gameScreen.dispose();
        homeScreen.dispose();
    }
}
