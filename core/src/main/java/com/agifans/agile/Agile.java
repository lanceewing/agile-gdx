package com.agifans.agile;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Agile extends ApplicationAdapter {
    
    private SpriteBatch batch;
    private GameScreen screen;
    private AgileRunner agileRunner;
    private WavePlayer wavePlayer;
    
    /**
     * Constructor for Agile.
     * 
     * @param agileRunner
     * @param wavePlayer 
     */
    public Agile(AgileRunner agileRunner, WavePlayer wavePlayer) {
        this.agileRunner = agileRunner;
        this.wavePlayer = wavePlayer;
    }
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        screen = new GameScreen();
        startGame(selectGame());
    }

    /**
     * Starts the AGI game contained in the given game folder.
     * 
     * @param gameFolder The folder from which we'll get all of the game data.
     */
    private void startGame(String gameFolder) {
        // Register the key event handlers for keyUp, keyDown, and keyTyped.
        UserInput userInput = new UserInput();
        Gdx.input.setInputProcessor(userInput);
        
        agileRunner.init(gameFolder, userInput, wavePlayer, screen.getPixels());
        
        // Start up the AgileRunner to run the interpreter in the background.
        agileRunner.start();
    }
    
    /**
     * Selects am AGI game folder to run.
     * 
     * @return The folder containing the AGI game's resources.
     */
    private String selectGame() {
        // TODO: Implement selection logic. This is a placeholder for now.
        // TODO: Game clock should stop when in menus or window showing, as should animations.
        return "C:\\dev\\agi\\winagi\\kq3";
    }
        
    @Override
    public void render() {
        // Update screen.
        screen.render();
        
        // Render.
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(screen.getDrawScreen(), 0, 0, 960, 600);
        batch.end();
        
        // Trigger tick.
        agileRunner.tick();
    }

    @Override
    public void dispose() {
        agileRunner.stop();
        batch.dispose();
        screen.dispose();
    }
}
