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
    
    //private SpriteBatch batch;
    
    // Screens
    private GameScreen gameScreen;
    private HomeScreen homeScreen;
    
    // Platform specific implementations.
    private AgileRunner agileRunner;
    private WavePlayer wavePlayer;
    private SavedGameStore savedGameStore;
    
    /**
     * Constructor for Agile.
     * 
     * @param agileRunner
     * @param wavePlayer 
     * @param savedGameStore 
     */
    public Agile(AgileRunner agileRunner, WavePlayer wavePlayer, SavedGameStore savedGameStore) {
        this.agileRunner = agileRunner;
        this.wavePlayer = wavePlayer;
        this.savedGameStore = savedGameStore;
    }
    
    @Override
    public void create() {
        
        //batch = new SpriteBatch();
        
        homeScreen = new HomeScreen();
        gameScreen = new GameScreen(agileRunner, wavePlayer, savedGameStore);
        
        setScreen(gameScreen);
        
        //startGame(selectGame());
        
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
    }

//    /**
//     * Starts the AGI game contained in the given game folder.
//     * 
//     * @param gameFolder The folder from which we'll get all of the game data.
//     */
//    private void startGame(String gameFolder) {
//        // Register the key event handlers for keyUp, keyDown, and keyTyped.
//        UserInput userInput = new UserInput();
//        Gdx.input.setInputProcessor(userInput);
//        
//        agileRunner.init(gameFolder, userInput, wavePlayer, savedGameStore, gameScreen.getPixels());
//        
//        // Start up the AgileRunner to run the interpreter in the background.
//        agileRunner.start();
//    }
//    
//    /**
//     * Selects am AGI game folder to run.
//     * 
//     * @return The folder containing the AGI game's resources.
//     */
//    private String selectGame() {
//        // TODO: Implement selection logic. This is a placeholder for now.
//        // return "C:\\dev\\agi\\winagi\\kq4agi";
//        // return "file:/C:/dev/agi/winagi/kq4agi/";
//        // return "file://localhost/C:/dev/agi/winagi/kq4agi/";
//        return "file:///C:/dev/agi/winagi/kq1/";
//    }
        
//    @Override
//    public void render() {
//        // Update screen.
//        gameScreen.render();
//        
//        // Render.
//        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        batch.begin();
//        batch.draw(gameScreen.getDrawScreen(), 0, 0, 960, 600);
//        batch.end();
//        
//        // Trigger tick.
//        agileRunner.tick();
//    }

    @Override
    public void dispose() {
        super.dispose();
        gameScreen.dispose();
        homeScreen.dispose();
    }
}
