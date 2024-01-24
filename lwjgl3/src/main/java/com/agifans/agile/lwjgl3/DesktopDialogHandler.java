package com.agifans.agile.lwjgl3;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import com.agifans.agile.Detection;
import com.agifans.agile.agilib.Game;
import com.agifans.agile.ui.ConfirmResponseHandler;
import com.agifans.agile.ui.DialogHandler;
import com.agifans.agile.ui.OpenFileResponseHandler;
import com.agifans.agile.ui.TextInputResponseHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Desktop implementation of the DialogHandler interface.
 */
public class DesktopDialogHandler implements DialogHandler {

    @Override
    public void confirm(final String message, final ConfirmResponseHandler responseHandler) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                int output = JOptionPane.showConfirmDialog(null, message, "Please confirm", JOptionPane.YES_NO_OPTION);
                if (output != 0) {
                    responseHandler.no();
                } else {
                    responseHandler.yes();
                }
            }
        });
    }

    @Override
    public void openFileDialog(String title, final String startPath,
            final OpenFileResponseHandler openFileResponseHandler) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                JFileChooser jfc = null;
                if (startPath != null) {
                    jfc = new JFileChooser(startPath);
                } else {
                    jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                }
                jfc.setDialogTitle("Select an AGI game folder");
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setAcceptAllFileFilterUsed(false);

                int returnValue = jfc.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    String filePath = jfc.getSelectedFile().getPath();
                    FileHandle fileHandle = new FileHandle(filePath);
                    String gameName = fileHandle.nameWithoutExtension();
                    
                    Game game = decodeGame(filePath);
                    
                    if (game != null) {
                        // Game successfully decoded, so we can store this in app config.
                        Detection detection = new Detection(game);
                        
                        // If game is recognised, use detected name but without version part.
                        if (!detection.gameName.equals("Unrecognised game")) {
                            gameName = detection.gameName;
                            if (gameName.contains("(")) {
                                int bracketIndex = gameName.indexOf('(');
                                gameName = gameName.substring(0, bracketIndex).trim();
                            }
                        }
                        
                        openFileResponseHandler.openFileResult(true, filePath, gameName, detection.gameId);
                    } else {
                        // Doesn't appear to be a valid AGI game folder.
                        openFileResponseHandler.openFileResult(false, null, null, null);
                    }
                } else {
                    openFileResponseHandler.openFileResult(false, null, null, null);
                }
            }
        });
    }
    
    /**
     * Attempts to load and decode the game, as a way to both validate that it is a
     * valid AGI game folder, and also, if valid, obtain the game's name and ID, so 
     * that the AppConfigItem can be created.
     * 
     * @param filePath The path of the directory containing the AGI game files.
     * 
     * @return The decoded Game, if successfully decoded; otherwise null.
     */
    private Game decodeGame(String filePath) {
        File directory = new File(filePath);
        if (!directory.exists()) {
            return null;
        }
        
        DesktopGameLoader gameLoader = new DesktopGameLoader(null);
        Map<String, byte[]> gameFilesMap = new HashMap<>();
        gameLoader.fetchGameFiles(directory.toURI().toString(), map -> gameFilesMap.putAll(map));
        
        // TODO: Check for VOL and DIR files.
        if (gameFilesMap.containsKey("words.tok") && 
            gameFilesMap.containsKey("object")) {
            // Seems to be an AGI game directory. Let's try to decode it.
            try {
                return new Game(gameFilesMap);
            } catch (RuntimeException e) {
                // Decode failed, so can't be run by AGILE.
                return null;
            }
        } else {
            // Missing core files.
            return null;
        }
    }

    @Override
    public void promptForTextInput(final String message, final String initialValue,
            final TextInputResponseHandler textInputResponseHandler) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                String text = (String) JOptionPane.showInputDialog(null, message, "Please enter value",
                        JOptionPane.INFORMATION_MESSAGE, null, null, initialValue != null ? initialValue : "");

                if (text != null) {
                    textInputResponseHandler.inputTextResult(true, text);
                } else {
                    textInputResponseHandler.inputTextResult(false, null);
                }
            }
        });
    }
}
