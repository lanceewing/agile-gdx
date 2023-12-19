package com.agifans.agile;

import java.io.File;
import java.util.List;

import com.agifans.agile.agilib.Game;
import com.agifans.agile.agilib.Logic;
import com.agifans.agile.agilib.Logic.Action;
import com.agifans.agile.agilib.Logic.OperandType;

import com.badlogic.gdx.Gdx;

/**
 * Performs the actual loading and then running of the AGI game. This is an abstract
 * class since the code needs to be run in a background thread/worker, which is something
 * that is best handled by the platform specific code. Most of the code is in this class
 * though, but the launching of the background thread/worker, and its main timing loop,
 * is implemented in the sub-classes.
 */
public abstract class AgileRunner {
    
    protected Interpreter interpreter;
    
    private String gameFolder;
    private WavePlayer wavePlayer;
    private UserInput userInput;
    private short[] pixels;
    
    public void init(String gameFolder, UserInput userInput, WavePlayer wavePlayer, short[] pixels) {
        this.gameFolder = gameFolder;
        this.userInput = userInput;
        this.wavePlayer = wavePlayer;
        this.pixels = pixels;
    }
    
    /**
     * Attempts to load an AGI game from the game folder.
     */
    protected void loadGame() {
        Game game = null;
        
        // Use a dummy TextGraphics instance to render the "Loading" text in grand AGI fashion.
        TextGraphics textGraphics = new TextGraphics(pixels, null, null);
        try {
            // TODO: Change to libgdx files??
            File wordsFile = new File(gameFolder + "\\WORDS.TOK");
            if (wordsFile.exists()) {
                textGraphics.drawString(pixels, "Loading... Please wait", 72, 88, 15, 0);
            }
            game = new Game(gameFolder);
        }
        finally {
            textGraphics.clearLines(0, 24, 0);
        }
        
        // Game detection logic and update windows title.
        Detection gameDetection = new Detection(game);
        Gdx.graphics.setTitle(String.format("AGILE v0.0.0.0 | %s", gameDetection.gameName));
        
        // Patch game option.
        patchGame(game, gameDetection.gameId, gameDetection.gameName);
        
        // Create the Interpreter to run this Game.
        this.interpreter = new Interpreter(game, userInput, wavePlayer, pixels);
    }
    
    /**
     * Patches the given games's Logic scripts, so that the starting question is skipped.
     * 
     * @param game Game to patch the Logics for.
     * @param gameId The detected game ID.
     * @param gameName The detected game name.
     * 
     * @return The patched Game.
     */
    private Game patchGame(Game game, String gameId, String gameName) {
        for (Logic logic : game.logics) {
            if (logic != null) {
                List<Action> actions = logic.actions;
    
                switch (gameId) {
                
                    case "goldrush":
                        // Gold Rush version 3.0 doesn't have copy protection
                        if (gameName.contains("3.0")) {
                            break;      
                        }
                        if (logic.index == 129) {
                            // Changes the new.room(125) to be new.room(73) instead, thus skipping the questions.
                            Action action = actions.get(27);
                            if ((action.operation.opcode == 18) && (action.operands.get(0).asInt() == 125)) {
                                action.operands.set(0, logic.new Operand(OperandType.NUM, 73));
                            }
                        }
                        break;
    
                    case "mh1":
                        if (logic.index == 159) {
                            // Modifies LOGIC.159 to jump to the code that is run when a successful answer is entered.
                            if ((actions.get(134).operation.opcode == 18) && (actions.get(134).operands.get(0).asInt() == 153)) {
                                actions.set(0, logic.new GotoAction(List.of(logic.new Operand(OperandType.ADDRESS, actions.get(132).address))));
                                actions.get(0).logic = logic;
                            }
                        }
                        break;
    
                    case "kq4":
                        if (logic.index == 0) {
                            // Changes the new.room(140) to be new.room(96) instead, thus skipping the questions.
                            Action action = actions.get(55);
                            if ((action.operation.opcode == 18) && (action.operands.get(0).asInt() == 140)) {
                                action.operands.set(0, logic.new Operand(OperandType.NUM, 96));
                            }
                        }
                        break;
    
                    case "lsl1":
                        if (logic.index == 6) {
                            // Modifies LOGIC.6 to jump to the code that is run when all of the trivia questions has been answered correctly.
                            Action action = actions.get(0);                                
                            // Verify that the action is the if-condition to check if the user can enter the game.
                            if (action.operation.opcode == 255 && action.operands.size() == 2) {
                                actions.set(0, logic.new GotoAction(List.of(logic.new Operand(OperandType.ADDRESS, actions.get(1).address))));
                                actions.get(0).logic = logic;
    
                                // Skips the 'Thank you. And now, slip into your leisure suit and prepare to enter the
                                // "Land of the Lounge Lizards" with "Leisure "Suit Larry!"' message
                                int printIndex = 9;
                                Action printAction = actions.get(printIndex);
    
                                // Verify it's the print function
                                if (printAction.operation.opcode == 101) {
                                    // Go to next command in the logic, which is the new.room command
                                    actions.set(printIndex, logic.new GotoAction(List.of(logic.new Operand(OperandType.ADDRESS, actions.get(printIndex + 1).address))));
                                    actions.get(printIndex).logic = logic;
                                }
                            }                               
                        }
                        break;
    
                    default:
                        break;
                }
            }
        }

        return game;
    }
    
    public abstract void start();
    
    public abstract void stop();
    
    public abstract boolean isRunning();
    
}
