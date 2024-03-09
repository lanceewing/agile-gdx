package com.agifans.agile.lwjgl3;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import com.agifans.agile.Detection;
import com.agifans.agile.agilib.Game;
import com.agifans.agile.config.AppConfigItem;
import com.agifans.agile.ui.ConfirmResponseHandler;
import com.agifans.agile.ui.DialogHandler;
import com.agifans.agile.ui.ImportTypeResponseHandler;
import com.agifans.agile.ui.ImportType;
import com.agifans.agile.ui.OpenFileResponseHandler;
import com.agifans.agile.ui.TextInputResponseHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Desktop implementation of the DialogHandler interface.
 */
public class DesktopDialogHandler implements DialogHandler {
    
    private boolean dialogOpen;

    @Override
    public void confirm(final String message, final ConfirmResponseHandler responseHandler) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                dialogOpen = true;
                int output = JOptionPane.showConfirmDialog(null, message, "Please confirm", JOptionPane.YES_NO_OPTION);
                dialogOpen = false;
                if (output != 0) {
                    responseHandler.no();
                } else {
                    responseHandler.yes();
                }
            }
        });
    }

    @Override
    public void promptForImportType(AppConfigItem appConfigItem, ImportTypeResponseHandler importTypeResponseHandler) {
        String gameName = (appConfigItem != null? "\"" + appConfigItem.getName() + "\"" : "an AGI game");
        String[] values = ImportType.getDescriptions();
        dialogOpen = true;
        Object selectedOption = JOptionPane.showInputDialog(
                null, 
                (appConfigItem != null? 
                        "For legal reasons, you must import your own copy of \n" + gameName + "\n\n" : "") + 
                "Please select the type of import:", 
                "Import " + gameName, 
                JOptionPane.DEFAULT_OPTION, 
                null, 
                values, 
                values[0]);
        dialogOpen = false;
        if (selectedOption != null) {
            ImportType importType = ImportType.getImportTypeByDescription(selectedOption.toString());
            importTypeResponseHandler.importTypeResult(true, importType);
        } else {
            importTypeResponseHandler.importTypeResult(false, null);
        }
    }

    @Override
    public void openFileDialog(AppConfigItem appConfigItem, String fileType, String title, final String startPath,
            final OpenFileResponseHandler openFileResponseHandler) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                String gameNameIn = (appConfigItem != null? "'" + appConfigItem.getName() + "'" : "the AGI game");
                JFileChooser jfc = null;
                if (startPath != null) {
                    jfc = new JFileChooser(startPath);
                } else {
                    jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                }
                if ("DIR".equals(fileType)) {
                    jfc.setDialogTitle("Select the folder containg " + gameNameIn);
                    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    jfc.setAcceptAllFileFilterUsed(false);
                } else {
                    // All other types are normal files.
                    jfc.setDialogTitle("Select the " + fileType + " file containing " + gameNameIn);
                    jfc.setAcceptAllFileFilterUsed(false);
                    jfc.setFileFilter(new FileNameExtensionFilter("ZIP files", "zip"));
                }

                dialogOpen = true;
                int returnValue = jfc.showOpenDialog(null);
                dialogOpen = false;
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
                                // TODO: Add check of name against appConfigItem, if provided.
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
                showMessageDialog("AGILE is unable to run the selected game. Please try another one.");
                return null;
            }
        } else {
            // Missing core files.
            showMessageDialog("The selected folder or file does not appear to contain an AGI game.");
            return null;
        }
    }

    @Override
    public void promptForTextInput(final String message, final String initialValue,
            final TextInputResponseHandler textInputResponseHandler) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                dialogOpen = true;
                String text = (String) JOptionPane.showInputDialog(null, message, "Please enter value",
                        JOptionPane.INFORMATION_MESSAGE, null, null, initialValue != null ? initialValue : "");
                dialogOpen = false;
                
                if (text != null) {
                    textInputResponseHandler.inputTextResult(true, text);
                } else {
                    textInputResponseHandler.inputTextResult(false, null);
                }
            }
        });
    }

    @Override
    public void showMessageDialog(String message) {
        dialogOpen = true;
        JOptionPane.showMessageDialog(null, message);
        dialogOpen = false;
    }
    
    @Override
    public boolean isDialogOpen() {
        return dialogOpen;
    }
}
