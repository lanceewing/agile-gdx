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
    
    // Platform specific implementations.
    private AgileRunner agileRunner;
    private WavePlayer wavePlayer;
    private SavedGameStore savedGameStore;
    private PixelData pixelData;
    
    /**
     * Constructor for Agile.
     * 
     * @param agileRunner
     * @param wavePlayer 
     * @param savedGameStore 
     * @param pixelData 
     */
    public Agile(AgileRunner agileRunner, WavePlayer wavePlayer, SavedGameStore savedGameStore,
            PixelData pixelData) {
        this.agileRunner = agileRunner;
        this.wavePlayer = wavePlayer;
        this.savedGameStore = savedGameStore;
        this.pixelData = pixelData;
        
    }
    
    @Override
    public void create() {
        homeScreen = new HomeScreen();
        gameScreen = new GameScreen(agileRunner, wavePlayer, savedGameStore, pixelData);
        
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
