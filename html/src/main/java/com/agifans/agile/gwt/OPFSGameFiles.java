package com.agifans.agile.gwt;

import com.google.gwt.typedarrays.shared.ArrayBuffer;

/**
 * Provides access to the Origin Private File System (OPFS) for the storage/caching 
 * of AGI game data files, for AGI games that have been imported into the HTML5/GWT/WEB
 * version of AGILE. This JS code is intended to be used by the UI thread and therefore
 * differs from the OPFS storage used for the saved games. It therefore cannot use the 
 * synchronous access handles for the files and must perform all reading and writing of 
 * the data files in an asynchronous way with await/async and promises. This in turn 
 * means that the write operation is a kind of fire and forget operation as far as 
 * storage goes, i.e. the calls are async, so it happens in the background. Likewise, 
 * the read is also async, and so it returns immediately upon calling it, which means 
 * that a callback function must be provided to get the read in data.
 */
public class OPFSGameFiles {
    
    public final native void writeGameFilesData(String gameDirectoryName, ArrayBuffer gameDataArrayBuffer)/*-{
        // Get a handle to the OPFS root dir for the AGILE website.
        navigator.storage.getDirectory().then(function(opfsRoot) {
            // Get and store a handle to the "Game Files" sub directory.
            opfsRoot.getDirectoryHandle('Game Files', {create: true}).then(function(gameFilesDir) {
                // Create an OPFS sub-directory and data file for this game.
                gameFilesDir.getDirectoryHandle(gameDirectoryName, {create: true}).then(function(gameDir) {
                    gameDir.getFileHandle('GAMEFILES.DAT', {create: true}).then(function(gameDataFileHandle) {
                        // Open a writable stream for the file.
                        gameDataFileHandle.createWritable().then(function(gameDataFileStream) {
                            // Write the game data out and then close.
                            gameDataFileStream.write(gameDataArrayBuffer).then(function() {
                                gameDataFileStream.close()
                            });
                        });
                    });
                });
            });
        });
    }-*/;
    
    public final native void readGameFilesData(String gameDirectoryName, GwtOpenFileResultsHandler resultsHandler)/*-{
        // Get a handle to the OPFS root dir for the AGILE website.
        navigator.storage.getDirectory().then(function(opfsRoot) {
            // Get and store a handle to the "Game Files" sub directory.
            opfsRoot.getDirectoryHandle('Game Files', {create: true}).then(function(gameFilesDir) {
                // Get handle to the game's data file.
                gameFilesDir.getDirectoryHandle(gameDirectoryName, {create: true}).then(function(gameDir) {
                    gameDir.getFileHandle('GAMEFILES.DAT', {create: true}).then(function(gameDataFileHandle) {
                        gameDataFileHandle.getFile().then(function(gameDataFile) {
                            // Get the file content as a Uint8Array.
                            gameDataFile.arrayBuffer().then(function(gameDataArrayBuffer) {
                                var results = [{
                                        fileName: 'GAMEFILES.DAT',
                                        filePath: gameDirectoryName,
                                        fileData: gameDataArrayBuffer
                                    }];
                                
                                // Call the results handler with the data array.
                                resultsHandler.@com.agifans.agile.gwt.GwtOpenFileResultsHandler::onFileResultsReady([Lcom/agifans/agile/gwt/GwtOpenFileResult;)(results);
                            });
                        });
                    });
                });
            });
        });
    }-*/;
}
