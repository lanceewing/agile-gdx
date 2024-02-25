package com.agifans.agile.gwt;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.agifans.agile.GameLoader;
import com.agifans.agile.PixelData;
import com.akjava.gwt.jszip.JSFile;
import com.akjava.gwt.jszip.JSZip;
import com.google.gwt.core.client.JsArrayString;

/**
 * GWT platform implementation of the GameLoader.
 */
public class GwtGameLoader extends GameLoader {
    
    private OPFSGameFiles opfsGameFiles;
    
    private GameFileMapEncoder gameFileMapEncoder;

    /**
     * Constructor for GwtGameLoader.
     * 
     * @param pixelData
     */
    public GwtGameLoader(PixelData pixelData) {
        super(pixelData);
        
        opfsGameFiles = new OPFSGameFiles();
        gameFileMapEncoder = new GameFileMapEncoder();
    }

    @Override
    public void fetchGameFiles(String gameUri, Consumer<Map<String, byte[]>> gameFilesConsumer) {
        if (gameUri.toLowerCase().endsWith(".zip")) {
            // Must be a ZIP URL for one of the embedded fan made games.
            Map<String, byte[]> gameFileMap = new HashMap<>();
            JSZip jsZip = JSZip.loadFile(gameUri);
            JsArrayString files = jsZip.getFiles();

            for (int i=0; i < files.length(); i++) {
                String fileName = files.get(i);
                if (isGameFile(fileName)) {
                    JSFile gameFile = jsZip.getFile(fileName);
                    gameFileMap.put(fileName.toLowerCase(), gameFile.asUint8Array().toByteArray());
                }
            }
            
            gameFilesConsumer.accept(gameFileMap);
        
        } else {
            // Otherwise, if it isn't a ZIP URL, then it must be in the OPFS.
            opfsGameFiles.readGameFilesData(gameUri, new GwtOpenFileResultsHandler() {
                @Override
                public void onFileResultsReady(GwtOpenFileResult[] openFileResultArray) {
                    if (openFileResultArray.length == 1) {
                        GwtOpenFileResult openFileResult = openFileResultArray[0];
                        Map<String, byte[]> gameFileMap = gameFileMapEncoder.decodeGameFileMap(openFileResult.getFileData());
                        gameFilesConsumer.accept(gameFileMap);
                    }
                }
            });
        }
    }
}
