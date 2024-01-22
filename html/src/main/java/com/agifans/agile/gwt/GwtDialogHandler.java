package com.agifans.agile.gwt;

import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.Detection;
import com.agifans.agile.agilib.Game;
import com.agifans.agile.ui.ConfirmResponseHandler;
import com.agifans.agile.ui.DialogHandler;
import com.agifans.agile.ui.OpenFileResponseHandler;
import com.agifans.agile.ui.TextInputResponseHandler;
import com.badlogic.gdx.Gdx;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

/**
 * The GWT implementation of the DialogHandler interface.
 */
public class GwtDialogHandler implements DialogHandler {
    
    private OPFSGameFiles opfsGameFiles;
    
    private GameFileMapEncoder gameFileMapEncoder;
    
    /**
     * Constructor for GwtDialogHandler.
     */
    public GwtDialogHandler() {
        gameFileMapEncoder = new GameFileMapEncoder();
        opfsGameFiles = new OPFSGameFiles();
    }

    @Override
    public void confirm(String message, ConfirmResponseHandler confirmResponseHandler) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                showHtmlConfirmBox(message, confirmResponseHandler);
            }
        });
    }

    private final native void showHtmlConfirmBox(String message, ConfirmResponseHandler confirmResponseHandler)/*-{
        if (confirm(message)) {
            confirmResponseHandler.@com.agifans.agile.ui.ConfirmResponseHandler::yes()();
        } else {
            confirmResponseHandler.@com.agifans.agile.ui.ConfirmResponseHandler::no()();
        }
    }-*/;
    
    @Override
    public void openFileDialog(String title, String startPath, OpenFileResponseHandler openFileResponseHandler) {
        // NOTES:
        // - The startPath parameter can't be used with GWT.
        // - The title cannot be used with GWT.
        // - The path cannot be passed back to the core module without changing interface to accept the file content.
        showHtmlOpenFileDialog("DIR", new GwtOpenFileResultsHandler() {
            @Override
            public void onFileResultsReady(GwtOpenFileResult[] openFileResultArray) {
                
                // TODO: Handle ZIP fle.
                
                boolean hasVolFile = false;
                boolean hasDirFile = false;
                String directoryName = null;
                Map<String, byte[]> gameFilesMap = new HashMap<>();
                
                for (GwtOpenFileResult result : openFileResultArray) {
                    String fileName = result.getFileName().toLowerCase();
                    
                    // Use the first file to get the directory name.
                    if ((directoryName == null) && !(result.getFilePath().isEmpty())) {
                        // Note that / is used in Windows as well, so the below is fine.
                        String[] pathParts = result.getFilePath().split("/");
                        if (pathParts.length > 1) {
                            directoryName = pathParts[0];
                        }
                    }
                    
                    if (isGameFile(fileName)) {
                        Int8Array fileDataInt8Array = TypedArrays.createInt8Array(result.getFileData());
                        byte[] gameFileByteArray = new byte[fileDataInt8Array.byteLength()];
                        for (int index=0; index<fileDataInt8Array.byteLength(); index++) {
                            gameFileByteArray[index] = fileDataInt8Array.get(index);
                        }
                        
                        gameFilesMap.put(fileName, gameFileByteArray);
                    
                        if (fileName.matches("^[a-z0-9]*vol.[0-9]+$")) {
                            hasVolFile = true;
                        }
                        if (fileName.endsWith("dir")) {
                            hasDirFile = true;
                        }
                    }
                }
                
                // Check for the minimum set of files required.
                if (gameFilesMap.containsKey("words.tok") && 
                    gameFilesMap.containsKey("object") &&
                    gameFilesMap.containsKey("agidata.ovl") && 
                    hasDirFile && hasVolFile) {
                    try {
                        // Now check that the files are able to be decoded.
                        Game game = new Game(gameFilesMap);
                        Detection detection = new Detection(game);
                        String opfsDirectoryName = null;
                        String gameName = null;
                        
                        if (detection.gameId.equals("unknown")) {
                            if ((directoryName != null) && (!directoryName.isEmpty())) {
                                opfsDirectoryName = directoryName;
                                gameName = directoryName;
                            } else {
                                // Fallback on the game ID with timestamp appended.
                                opfsDirectoryName = game.gameId + "_" + System.currentTimeMillis();
                                gameName = game.gameId;
                            }
                        } else {
                            // Use the game's name, if we've identified it.
                            opfsDirectoryName = slugify(detection.gameName);
                            gameName = detection.gameName;
                            if (gameName.contains("(")) {
                                int bracketIndex = gameName.indexOf('(');
                                gameName = gameName.substring(0, bracketIndex).trim();
                            }
                        }

                        // Use GameFileMapEncoder to encode to single ArrayBuffer and store in OPFS.
                        ArrayBuffer fullGameBuffer = gameFileMapEncoder.encodeGameFileMap(gameFilesMap);
                        opfsGameFiles.writeGameFilesData(opfsDirectoryName, fullGameBuffer);
                        
                        // The Game ID that we pass back is as per the game's LOGIC files, unless it
                        // doesn't set one, in which case it falls back on the Detection game ID.
                        String gameId = ((game.gameId != null) && !game.gameId.isEmpty())? game.gameId : detection.gameId;
                        
                        openFileResponseHandler.openFileResult(true, opfsDirectoryName, gameName, gameId);
                        
                    } catch (RuntimeException e) {
                        Gdx.app.error("onFileResultsReady", e.getMessage());
                        
                        // The game failed to decode, so AGILE will not be able to run it.
                        openFileResponseHandler.openFileResult(false, null, null, null);
                    }
                } else {
                    // The game file map does not contain the minimum set of files.
                    openFileResponseHandler.openFileResult(false, null, null, null);
                }
            }
        });
    }
    
    private final native String slugify(String str)/*-{
        return String(str)
            .normalize('NFKD')
            .replace(/[\u0300-\u036f]/g, '')
            .trim()
            .toLowerCase()
            .replace(/[^a-z0-9 -]/g, '')
            .replace(/\s+/g, '-')
            .replace(/-+/g, '-');
    }-*/;
    
    private boolean isGameFile(String filename) {
        String lowerCaseName = filename.toLowerCase();
        if (lowerCaseName.matches("^[a-z0-9]*vol.[0-9]+$") || 
                lowerCaseName.endsWith("dir") || 
                lowerCaseName.equals("agidata.ovl") || 
                lowerCaseName.equals("object") || 
                lowerCaseName.equals("words.tok")) {
            return true;
        }
        else {
            return false;
        }
    }

    private final native void showHtmlOpenFileDialog(String type, GwtOpenFileResultsHandler resultsHandler)/*-{
        var fileInputElem = document.createElement('input');
        fileInputElem.type = "file";
        
        // The 'type' parameters selects between directory selection vs file selection.
        if (type == 'DIR') {
            // When the webkitdirectory property is true, it means that all files in 
            // the selected directory are automatically included.
            fileInputElem.webkitdirectory = true;
        } else {
            // Otherwise the user must select the individual files themselves, either
            // one file (such as a ZIP), or multiple AGI game files.
            fileInputElem.multiple = true;
        }
        
        // The onchange event occurs after a file is chosen.
        fileInputElem.onchange = function(event) {
            if (this.files.length === 0) {
                // No file was selected, so nothing more to do.
                resultsHandler.@com.agifans.agile.gwt.GwtOpenFileResultsHandler::onFileResultsReady([Lcom/agifans/agile/gwt/GwtOpenFileResult;)([]);
            }
            else {
                // There can be multiple files, so we need to fetch all of them, and
                // only when all have finished loading do we invoke the callback with
                // content of the files.
                Promise.all([].map.call(this.files, function (file) {
                    return new Promise(function (resolve, reject) {
                        var reader = new FileReader();
                        // NOTE 1: loadend called regards of whether it was successful or not.
                        // NOTE 2: file has .name, .size and .lastModified fields.
                        reader.onloadend = function (event) {
                            resolve({
                                fileName: file.name,
                                filePath: file.webkitRelativePath? file.webkitRelativePath : '',
                                fileData: reader.result
                            });
                        };
                        reader.readAsArrayBuffer(file);
                    });
                })).then(function (results) {
                    // The results param is an array of result objects
                    resultsHandler.@com.agifans.agile.gwt.GwtOpenFileResultsHandler::onFileResultsReady([Lcom/agifans/agile/gwt/GwtOpenFileResult;)(results);
                });
            }
        };
        
        // Trigger the display of the open file dialog.
        fileInputElem.click();
    }-*/;

    @Override
    public void promptForTextInput(String message, String initialValue, TextInputResponseHandler textInputResponseHandler) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                showHtmlPromptBox(message, initialValue, textInputResponseHandler);
            }
        });
    }
    
    private final native void showHtmlPromptBox(String message, String initialValue, TextInputResponseHandler textInputResponseHandler)/*-{
        var text = prompt(message, initialValue);
        if (text != null) {
            textInputResponseHandler.@com.agifans.agile.ui.TextInputResponseHandler::inputTextResult(ZLjava/lang/String;)(true, text);
        } else {
            textInputResponseHandler.@com.agifans.agile.ui.TextInputResponseHandler::inputTextResult(ZLjava/lang/String;)(false, null);
        }
    }-*/;
}
