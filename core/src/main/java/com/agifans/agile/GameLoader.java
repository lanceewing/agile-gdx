package com.agifans.agile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.agifans.agile.agilib.Game;
import com.agifans.agile.agilib.Logic;
import com.agifans.agile.agilib.Logic.Action;
import com.agifans.agile.agilib.Logic.OperandType;
import com.badlogic.gdx.Gdx;

/**
 * Abstract interface for loading AGI games. Each platform supports potentially 
 * different mechanisms.
 */
public abstract class GameLoader {

    private PixelData pixelData;
    
    /**
     * Constructor for GameLoader/
     * 
     * @param pixelData
     */
    public GameLoader(PixelData pixelData) {
        this.pixelData = pixelData;
    }
    
    /**
     * Attempts to load and decode an AGI game from given Map of game files.
     * 
     * @param gameFilesMap The Map containing the game file content (keyed by file name)
     * 
     * @return The loaded AGI game.
     */
    public Game loadGame(Map<String, byte[]> gameFilesMap) {
        Game game = null;
                
        // Use a dummy TextGraphics instance to render the "Loading" text in grand AGI fashion.
        TextGraphics textGraphics = null;
        if (pixelData != null) {
            textGraphics = new TextGraphics(pixelData, null, null);
        }
        try {
            if (gameFilesMap.containsKey("words.tok")) {
                if (pixelData != null) {
                    textGraphics.drawString(pixelData, "Loading... Please wait", 72, 88, 15, 0);
                }
            }
            game = new Game(gameFilesMap);
        }
        finally {
            if (pixelData != null) {
                textGraphics.clearLines(0, 24, 0);
            }
        }
        
        // Game detection logic and update windows title.
        Detection gameDetection = new Detection(game);
        if (Gdx.graphics != null) {
            // We can't set the title in the web worker, so check first that the 
            // graphics field is populated, which for the web worker it won't be.
            Gdx.graphics.setTitle("AGILE v0.0.0.0 | " + gameDetection.gameName);
        }
        
        // Patch game option.
        patchGame(game, gameDetection.gameId, gameDetection.gameName);
        
        game.hasAGIMouse = gameDetection.hasAGIMouse;
        game.hasAGIPal = gameDetection.hasAGIPal;
        game.hasAGI256 = gameDetection.hasAGI256;
        
        // We prefer the game detection game ID over what is set in the game. Normally
        // this will be the same for original Sierra AGI games. For fan made games, 
        // many of which do not set a game ID, it is the detection game ID that allows
        // the saved games to be stored in their own "folder" and therefore not clash
        // with other games.
        game.gameId = gameDetection.gameId;
        
        return game;
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
                
                    case "GR":
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
    
                    case "MH1":
                        if (logic.index == 159) {
                            // Modifies LOGIC.159 to jump to the code that is run when a successful answer is entered.
                            if ((actions.get(134).operation.opcode == 18) && (actions.get(134).operands.get(0).asInt() == 153)) {
                                actions.set(0, logic.new GotoAction(new ArrayList<>(Arrays.asList(logic.new Operand(OperandType.ADDRESS, actions.get(132).address)))));
                                actions.get(0).logic = logic;
                                logic.addressToActionIndex.put(actions.get(0).address, 0);
                            }
                        }
                        break;
    
                    case "KQ4":
                        if (logic.index == 0) {
                            // Changes the new.room(140) to be new.room(96) instead, thus skipping the questions.
                            Action action = actions.get(55);
                            if ((action.operation.opcode == 18) && (action.operands.get(0).asInt() == 140)) {
                                action.operands.set(0, logic.new Operand(OperandType.NUM, 96));
                            }
                        }
                        break;
    
                    case "LLLLL":
                        if (logic.index == 6) {
                            // Modifies LOGIC.6 to jump to the code that is run when all of the trivia questions has been answered correctly.
                            Action action = actions.get(0);                                
                            // Verify that the action is the if-condition to check if the user can enter the game.
                            if (action.operation.opcode == 255 && action.operands.size() == 2) {
                                actions.set(0, logic.new GotoAction(new ArrayList<>(Arrays.asList(logic.new Operand(OperandType.ADDRESS, actions.get(1).address)))));
                                actions.get(0).logic = logic;
                                logic.addressToActionIndex.put(actions.get(0).address, 0);
    
                                // Skips the 'Thank you. And now, slip into your leisure suit and prepare to enter the
                                // "Land of the Lounge Lizards" with "Leisure "Suit Larry!"' message
                                int printIndex = 9;
                                Action printAction = actions.get(printIndex);
    
                                // Verify it's the print function
                                if (printAction.operation.opcode == 101) {
                                    // Go to next command in the logic, which is the new.room command
                                    actions.set(printIndex, logic.new GotoAction(new ArrayList<>(Arrays.asList(logic.new Operand(OperandType.ADDRESS, actions.get(printIndex + 1).address)))));
                                    actions.get(printIndex).logic = logic;
                                    logic.addressToActionIndex.put(actions.get(printIndex).address, printIndex);
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
    
    protected abstract void fetchGameFiles(String gameUri, Consumer<Map<String, byte[]>> gameFilesConsumer);
    
    protected boolean isGameFile(String filename) {
        String lowerCaseName = filename.toLowerCase();
        if (lowerCaseName.matches("^[a-z0-9]*vol[.][0-9]+$") || 
                lowerCaseName.endsWith("dir") || 
                lowerCaseName.equals("agidata.ovl") || 
                lowerCaseName.equals("object") || 
                lowerCaseName.equals("words.tok") || 
                lowerCaseName.matches("^pal[.]10[0-9]$")) {
            return true;
        }
        else {
            return false;
        }
    }
}
