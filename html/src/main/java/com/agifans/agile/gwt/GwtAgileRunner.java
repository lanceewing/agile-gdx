package com.agifans.agile.gwt;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.AgileRunner;
import com.agifans.agile.PixelData;
import com.agifans.agile.SavedGameStore;
import com.agifans.agile.UserInput;
import com.agifans.agile.WavePlayer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.webworker.client.ErrorEvent;
import com.google.gwt.webworker.client.ErrorHandler;
import com.google.gwt.webworker.client.MessageEvent;
import com.google.gwt.webworker.client.MessageHandler;
import com.google.gwt.webworker.client.Worker;

public class GwtAgileRunner extends AgileRunner {

    private Worker worker;
    
    public GwtAgileRunner(UserInput userInput, WavePlayer wavePlayer, SavedGameStore savedGameStore, 
            PixelData pixelData) {
        super(userInput, wavePlayer, savedGameStore, pixelData);
    }
    
    @Override
    public void start(String gameUri) {
        createWorker();
        
        // TODO: This may need to be done by the web worker.
        loadGame(gameUri);
    }

    @Override
    public String selectGame() {
        // TODO: Convert this into a URI format.
        return "games/kq1/";
    }
    
    // NOTE 1: importScripts won't work, because all URLs need to be absolute.
    // NOTE 2: direct URL doesn't work because security headers are not present.
    // NOTE 3: Will probably need to customise gwt-webworker to add SharedArrayBuffer support.
    
    public void createWorker() {
        //JavaScriptObject workerURL = this.getWorkerURL("http://localhost:8080/worker/worker.nocache.js");
        //worker = GwtAgileRunner.create(workerURL);
        //revokeObjectURL(workerURL);
        
        worker = Worker.create("worker/worker.nocache.js");
        
        final MessageHandler webWorkerMessageHandler = new MessageHandler() {
            @Override
            public final void onMessage(final MessageEvent pEvent) {
                Gdx.app.log("client onMessage", "Received message: " + pEvent.getDataAsString());
            }
        };

        final ErrorHandler webWorkerErrorHandler = new ErrorHandler() {
            @Override
            public void onError(final ErrorEvent pEvent) {
                Gdx.app.log("client onError", "Received message: " + pEvent.getMessage());
            }
        };

        worker.setOnMessage(webWorkerMessageHandler);
        worker.setOnError(webWorkerErrorHandler);
        worker.postMessage("Testing, testing, 1, 2, 3");
    }
    
    // The gwt-webworker module does not support a Blob constructor, so we provide one.
    public static native Worker create(JavaScriptObject workerURL)/*-{
        return new Worker(workerURL);
    }-*/;
    
    private native JavaScriptObject getWorkerURL(String url)/*-{
        var content = 'importScripts("' + url + '");';
        return URL.createObjectURL( new Blob( [ content ], { type: "text/javascript" } ) );
    }-*/;
    
    private native void revokeObjectURL(JavaScriptObject workerURL)/*-{
        URL.revokeObjectURL(workerURL);
    }-*/;
    
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
