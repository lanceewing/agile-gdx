package com.agifans.agile.gwt;

import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.Detection;
import com.agifans.agile.agilib.Game;
import com.agifans.agile.config.AppConfigItem;
import com.agifans.agile.ui.ConfirmResponseHandler;
import com.agifans.agile.ui.DialogHandler;
import com.agifans.agile.ui.ImportType;
import com.agifans.agile.ui.ImportTypeResponseHandler;
import com.agifans.agile.ui.OpenFileResponseHandler;
import com.agifans.agile.ui.TextInputResponseHandler;
import com.akjava.gwt.jszip.JSFile;
import com.akjava.gwt.jszip.JSZip;
import com.badlogic.gdx.Gdx;
import com.google.gwt.core.client.JsArrayString;
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
        var dialog = new $wnd.Dialog();
        dialog.confirm(message).then(function (res) {
            if (res) {
                confirmResponseHandler.@com.agifans.agile.ui.ConfirmResponseHandler::yes()();
            } else {
                confirmResponseHandler.@com.agifans.agile.ui.ConfirmResponseHandler::no()();
            }
        });
    }-*/;
    
    @Override
    public void promptForImportType(AppConfigItem appConfigItem, ImportTypeResponseHandler importTypeResponseHandler) {
        String gameName = (appConfigItem != null? "\"" + appConfigItem.getName() + "\"" : "an AGI game");
        String[] values = ImportType.getDescriptions();
        String message = (appConfigItem != null? 
                "For legal reasons, you must import your own copy of \n" + gameName + "\n\n" : "") + 
                "Please select the type of import:";
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                showHtmlPromptForImportType(message, values, new PromptForOptionsResponseHandler() {
                    @Override
                    public void selectedOptionResult(boolean success, String optionText) {
                        if (success) {
                            ImportType importType = ImportType.getImportTypeByDescription(optionText);
                            importTypeResponseHandler.importTypeResult(true, importType);
                        } else {
                            importTypeResponseHandler.importTypeResult(false, null);
                        }
                    }
                });
            }
        });
    }
    
    private final native void showHtmlPromptForImportType(String message, String[] options, PromptForOptionsResponseHandler promptForOptionsResponseHandler)/*-{
        var dialog = new $wnd.Dialog();
        dialog.promptForOption(message, options).then(function (res) {
            if (res) {
                promptForOptionsResponseHandler.@com.agifans.agile.gwt.GwtDialogHandler.PromptForOptionsResponseHandler::selectedOptionResult(ZLjava/lang/String;)(true, res.option);
            } else {
                promptForOptionsResponseHandler.@com.agifans.agile.gwt.GwtDialogHandler.PromptForOptionsResponseHandler::selectedOptionResult(ZLjava/lang/String;)(false, null);
            }
        });
    }-*/;
    
    public static interface PromptForOptionsResponseHandler {
        void selectedOptionResult(boolean success, String optionText);
    }
    
    @Override
    public void openFileDialog(AppConfigItem appConfigItem, String fileType, String title, String startPath, OpenFileResponseHandler openFileResponseHandler) {
        // NOTES:
        // - The startPath parameter can't be used with GWT.
        // - The title cannot be used with GWT.
        // - The path cannot be passed back to the core module without changing interface to accept the file content.
        showHtmlOpenFileDialog(fileType, new GwtOpenFileResultsHandler() {
            @Override
            public void onFileResultsReady(GwtOpenFileResult[] openFileResultArray) {
                boolean hasVolFile = false;
                boolean hasDirFile = false;
                String directoryName = null;
                Map<String, byte[]> gameFilesMap = new HashMap<>();
                
                if ((openFileResultArray.length == 1) && 
                        (openFileResultArray[0].getFileName().toLowerCase().endsWith(".zip"))) {
                    // A ZIP file was selected.
                    GwtOpenFileResult result = openFileResultArray[0];
                    JSZip jsZip = JSZip.loadFromArrayBuffer(result.getFileData());
                    JsArrayString files = jsZip.getFiles();

                    for (int i=0; i < files.length(); i++) {
                        String fileName = files.get(i);
                        if (isGameFile(fileName)) {
                            JSFile gameFile = jsZip.getFile(fileName);
                            gameFilesMap.put(fileName.toLowerCase(), gameFile.asUint8Array().toByteArray());
                        
                            if (fileName.matches("^[a-z0-9]*vol.[0-9]+$")) {
                                hasVolFile = true;
                            }
                            if (fileName.endsWith("dir")) {
                                hasDirFile = true;
                            }
                        }
                    }
                    
                } else {
                    // Folder of files selected.
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
                }
                
                // Check for the minimum set of files required.
                if (gameFilesMap.containsKey("words.tok") && 
                    gameFilesMap.containsKey("object") &&
                    hasDirFile && hasVolFile) {
                    try {
                        // Now check that the files are able to be decoded.
                        Game game = new Game(gameFilesMap);
                        Detection detection = new Detection(game);
                        String opfsDirectoryName = null;
                        String gameName = null;
                        
                        if (detection.gameName.equals("Unrecognised game")) {
                            if ((directoryName != null) && (!directoryName.isEmpty())) {
                                opfsDirectoryName = directoryName;
                                gameName = directoryName;
                            } else {
                                // Fallback on the game ID, which, for unknown games, is derived from the
                                // MD5 hash string.
                                opfsDirectoryName = detection.gameId;
                                gameName = detection.gameId;
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
                        
                        openFileResponseHandler.openFileResult(true, opfsDirectoryName, gameName, detection.gameId);
                        
                    } catch (RuntimeException e) {
                        // The game failed to decode, so AGILE will not be able to run it.
                        showMessageDialog("AGILE is unable to run the selected game. Please try another one.");
                        openFileResponseHandler.openFileResult(false, null, null, null);
                    }
                } else {
                    showMessageDialog("The selected folder or file does not appear to contain an AGI game.");
                    
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
                lowerCaseName.equals("words.tok") ||
                lowerCaseName.matches("^pal[.]10[0-9]$")) {
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
        } else if (type == 'ZIP') {
            fileInputElem.accept = '.zip';
        } else {
            // Otherwise the user must select the individual files themselves
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
        var dialog = new $wnd.Dialog();
        dialog.prompt(message, initialValue).then(function (res) {
            if (res) {
                textInputResponseHandler.@com.agifans.agile.ui.TextInputResponseHandler::inputTextResult(ZLjava/lang/String;)(true, res.prompt);
            } else {
                textInputResponseHandler.@com.agifans.agile.ui.TextInputResponseHandler::inputTextResult(ZLjava/lang/String;)(false, null);
            }
        });
    }-*/;

    @Override
    public void showMessageDialog(String message) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                showHtmlMessageBox(message);
            }
        });
    }
    
    private final native void showHtmlMessageBox(String message)/*-{
        var dialog = new $wnd.Dialog();
        dialog.alert(message);
    }-*/;
}
