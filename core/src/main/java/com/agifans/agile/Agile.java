package com.agifans.agile;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

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
     * Constructor for Agile.
     * 
     * @param agileRunner
     */
    public Agile(AgileRunner agileRunner) {
        this.agileRunner = agileRunner;
    }
    
    @Override
    public void create() {
        homeScreen = new HomeScreen();
        gameScreen = new GameScreen(agileRunner);
        
        setScreen(gameScreen);
        
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
    }

    @Override
    public void dispose() {
        super.dispose();
        gameScreen.dispose();
        homeScreen.dispose();
    }
}
