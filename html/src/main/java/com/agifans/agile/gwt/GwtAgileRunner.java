package com.agifans.agile.gwt;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.AgileRunner;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class GwtAgileRunner extends AgileRunner {

    @Override
    public void start() {
        // TODO: This may need to be done by the web worker.
        loadGame();
    }

    @Override
    public void animationTick() {
        // TODO: Implement proper web worker implementation.
        interpreter.animationTick();
    }

    @Override
    public void stop() {
        // TODO: Implement proper web worker implementation.
    }

    @Override
    public boolean isRunning() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Map<String, byte[]> fetchGameFiles(String gameUri) {
        Map<String, byte[]> gameFileMap = new HashMap<>();
        
        Gdx.app.debug("fetchGameFiles", "Attempting to list game folder.");
        
        // TODO: Map gameUri to an internal path. Expected to be a directory.
        FileHandle gameDirectory = Gdx.files.internal("games/kq1/");
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
