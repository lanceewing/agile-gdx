package com.agifans.agile.gwt;

import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.GameLoader;
import com.agifans.agile.PixelData;
import com.akjava.gwt.jszip.JSFile;
import com.akjava.gwt.jszip.JSZip;
import com.google.gwt.core.client.JsArrayString;

/**
 * GWT platform implementation of the GameLoader.
 */
public class GwtGameLoader extends GameLoader {

    /**
     * Constructor for GwtGameLoader.
     * 
     * @param pixelData
     */
    public GwtGameLoader(PixelData pixelData) {
        super(pixelData);
    }

    @Override
    public Map<String, byte[]> fetchGameFiles(String gameUri) {
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
        
        return gameFileMap;
    }
}
