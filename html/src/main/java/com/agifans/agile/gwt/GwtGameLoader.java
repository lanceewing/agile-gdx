package com.agifans.agile.gwt;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.GameLoader;
import com.agifans.agile.PixelData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

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
        
        Gdx.app.debug("fetchGameFiles", "Attempting to list game folder.");
        
        // TODO: Map gameUri to an internal path. Expected to be a directory.
        FileHandle gameDirectory = Gdx.files.internal(gameUri);
        if (gameDirectory != null) {
            FileHandle[] gameFiles = gameDirectory.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return isGameFile(name);
                }
            });
                
            for (FileHandle gameFile : gameFiles) {
                Gdx.app.debug("fetchGameFiles", "Reading game file " + gameFile.name());
                gameFileMap.put(gameFile.name().toLowerCase(), gameFile.readBytes());
            }
        }
        else {
            Gdx.app.error("fetchGameFiles", "Failed to list game directory!");
        }

        return gameFileMap;
    }
}
