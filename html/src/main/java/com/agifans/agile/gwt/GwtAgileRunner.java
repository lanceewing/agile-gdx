package com.agifans.agile.gwt;

import java.util.Map;

import com.agifans.agile.Agile;
import com.agifans.agile.AgileRunner;
import com.agifans.agile.PixelData;
import com.agifans.agile.SavedGameStore;
import com.agifans.agile.UserInput;
import com.agifans.agile.VariableData;
import com.agifans.agile.WavePlayer;
import com.agifans.agile.config.AppConfigItem;
import com.agifans.agile.worker.MessageEvent;
import com.agifans.agile.worker.MessageHandler;
import com.agifans.agile.worker.Worker;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.client.Window;
import com.google.gwt.webworker.client.ErrorEvent;
import com.google.gwt.webworker.client.ErrorHandler;

/**
 * GWT implementation of the AgileRunner. It uses a web worker to perform the execution
 * of the AGI interpreter animation ticks.
 */
public class GwtAgileRunner extends AgileRunner {

    /**
     * The web worker that will execute the AGI interpreter in the background.
     */
    private Worker worker;
    
    /**
     * Holds a reference to the Audio HTML element that is playing the current sound, or null
     * if there is no sound being played. It is not possible in AGI to play two sounds at 
     * the same time, so we only need this one reference to track sounds.
     */
    private AudioElement currentlyPlayingSound;
    
    /**
     * Indicates that the GWT AgileRunner is in the stopped state, i.e. it was previously
     * running a game but the game has now stopped, e.g. due to the user quitting the game.
     */
    private boolean stopped;
    
    /**
     * Constructor for GwtAgileRunner.
     * 
     * @param userInput
     * @param wavePlayer
     * @param savedGameStore
     * @param pixelData
     * @param variableData
     */
    public GwtAgileRunner(UserInput userInput, WavePlayer wavePlayer, SavedGameStore savedGameStore, 
            PixelData pixelData, VariableData variableData) {
        super(userInput, wavePlayer, savedGameStore, pixelData, variableData);
    }
    
    @Override
    public void start(AppConfigItem appConfigItem) {
        String newURL = Window.Location.createUrlBuilder().setHash("/id/" + appConfigItem.getGameId().toLowerCase()).buildString();
        updateURLWithoutReloading(newURL);
        
        // The game data files have been stored/cached in the OPFS. We load it from
        // there, using the gameUri as the identifier, and then pass it to the worker 
        // to decode.
        GwtGameLoader gameLoader = new GwtGameLoader(pixelData);
        gameLoader.fetchGameFiles(appConfigItem.getFilePath(), gameFilesMap -> createWorker(gameFilesMap));
    }
    
    private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
    
    /**
     * Creates a new web worker to run the AGI game whose data files are in the given Map.
     * 
     * @param gameFileMap A Map containing the AGI game's data file (e.g. DIR and VOL files).
     */
    public void createWorker(Map<String, byte[]> gameFileMap) {
        GameFileMapEncoder gameFileMapEncoder = new GameFileMapEncoder();
        ArrayBuffer gameFileBuffer = gameFileMapEncoder.encodeGameFileMap(gameFileMap);
        
        worker = Worker.create("worker/worker.nocache.js");
        
        final MessageHandler webWorkerMessageHandler = new MessageHandler() {
            @Override
            public void onMessage(MessageEvent event) {
                JavaScriptObject eventObject = event.getDataAsObject();
                
                switch (getEventType(eventObject)) {
                    case "QuitGame":
                        // This message is sent from the worker when the game has ended, usually
                        // due to the user quitting the game.
                        stop();
                        break;
                        
                    case "PlaySound":
                        // Let's make sure there isn't already a sound playing, although there 
                        // shouldn't be tbh.
                        stopCurrentSound();
                        // Get the ArrayBuffer that was instantly transferred from the web worker.
                        ArrayBuffer soundBuffer = getArrayBuffer(eventObject);
                        int endFlag = getNestedInt(eventObject, "endFlag");
                        currentlyPlayingSound = playSound(soundBuffer, endFlag);
                        break;
                        
                    case "StopSound":
                        stopCurrentSound();
                        break;
                        
                    default:
                        // Unknown. Ignore.
                }
            }
        };

        final ErrorHandler webWorkerErrorHandler = new ErrorHandler() {
            @Override
            public void onError(final ErrorEvent pEvent) {
                Gdx.app.error("client onError", "Received message: " + pEvent.getMessage());
            }
        };

        worker.setOnMessage(webWorkerMessageHandler);
        worker.setOnError(webWorkerErrorHandler);
        
        // In order to facilitate the communication with the worker, we must send
        // all SharedArrayBuffer objects to the webworker.
        GwtUserInput gwtUserInput = (GwtUserInput)userInput;
        GwtVariableData gwtVariableData = (GwtVariableData)variableData;
        GwtPixelData gwtPixelData = (GwtPixelData)pixelData;
        JavaScriptObject keyPressQueueSAB = gwtUserInput.getKeyPressQueueSharedArrayBuffer();
        JavaScriptObject keysSAB = gwtUserInput.getKeysSharedArrayBuffer();
        JavaScriptObject oldKeysSAB = gwtUserInput.getOldKeysSharedArrayBuffer();
        JavaScriptObject variableSAB = gwtVariableData.getVariableSharedArrayBuffer();
        JavaScriptObject pixelDataSAB = gwtPixelData.getSharedArrayBuffer();
        
        // We currently send one message to Initialise, using the SharedArrayBuffers,
        // then another message to Start the interpreter with the given game data. The 
        // game data is "transferred", whereas the others are not but rather shared.
        worker.postObject("Initialise", createInitialiseObject(
                keyPressQueueSAB, 
                keysSAB, 
                oldKeysSAB, 
                variableSAB,
                pixelDataSAB));
        worker.postArrayBuffer("Start", gameFileBuffer);
    }
    
    /**
     * Creates a JavaScript object, wrapping the objects to send to the web worker to
     * initialise the Interpreter.
     * 
     * @param keyPressQueueSAB 
     * @param keysSAB 
     * @param oldKeysSAB 
     * @param variableSAB
     * 
     * @return The created object.
     */
    private native JavaScriptObject createInitialiseObject(
            JavaScriptObject keyPressQueueSAB, 
            JavaScriptObject keysSAB, 
            JavaScriptObject oldKeysSAB, 
            JavaScriptObject variableSAB,
            JavaScriptObject pixelDataSAB)/*-{
        return { 
            keyPressQueueSAB: keyPressQueueSAB,
            keysSAB: keysSAB,
            oldKeysSAB: oldKeysSAB,
            variableSAB: variableSAB,
            pixelDataSAB: pixelDataSAB
        };
    }-*/;
    
    private native String getEventType(JavaScriptObject obj)/*-{
        return obj.name;
    }-*/;
    
    private native JavaScriptObject getEmbeddedObject(JavaScriptObject obj)/*-{
        return obj.object;
    }-*/;
    
    private native ArrayBuffer getArrayBuffer(JavaScriptObject obj)/*-{
        return obj.buffer;
    }-*/;
    
    private native int getNestedInt(JavaScriptObject obj, String fieldName)/*-{
        return obj.object[fieldName];
    }-*/;
    
    private native AudioElement playSound(ArrayBuffer soundBuffer, int endFlag)/*-{
        var that = this;
        var soundArray = new Int8Array(soundBuffer);
        var audio = new Audio();
        audio.src = URL.createObjectURL(new Blob([soundArray], {type: "audio/wav"}));
        audio.onended = function(event) {
            that.@com.agifans.agile.gwt.GwtAgileRunner::soundEnded(I)(endFlag);
        };
        audio.play();
        return audio;
    }-*/;
    
    /**
     * Invoked by the Audio element's "ended" event when the sound has finished 
     * playing. The endFlag parameter specifies the AGI flag number to set to true
     * in response to this.
     *  
     * @param endFlag The AGI flag number to set to true.
     */
    private void soundEnded(int endFlag) {
        // The GWT VariableData implementation uses shared memory, so this is seen
        // by the web worker almost instantly, meaning that any LOGIC code that is 
        // waiting for it can continue.
        variableData.setFlag(endFlag, true);
    }
    
    private void stopCurrentSound() {
        if (currentlyPlayingSound != null) {
            // Using pause() is apparently as close to stop as they have.
            currentlyPlayingSound.pause();
            // Then we remove the reference to allow JS garbage collection.
            currentlyPlayingSound = null;
        }
    }
    
    @Override
    public void animationTick() {
        if ((variableData.getInTick() == false) && (worker != null)) {
            // Signals to web worker to start tick.
            variableData.setInTick(true);  // NOTE: Set to false by web worker.
        }
    }

    @Override
    public void stop() {
        // Ensure that any playing sound is stopped, and then kill off the web 
        // worker immediately.
        worker.terminate();
        stopCurrentSound();
        pixelData.clearState();
        variableData.clearState();
        stopped = true;
    }

    @Override
    public void reset() {
        // Resets to the original state, as if a game has not been previously run.
        stopped = false;
        worker = null;
        
        String newURL = Window.Location.createUrlBuilder()
                .setHash(null)
                .buildString();
        updateURLWithoutReloading(newURL);
    }

    @Override
    public boolean hasStopped() {
        return ((worker != null) && stopped);
    }

    @Override
    public void saveScreenshot(Agile agile, AppConfigItem appConfigItem, Pixmap pixmap) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean hasTouchScreen() {
        return hasTouchScreenHtml();
    }
    
    private native boolean hasTouchScreenHtml() /*-{
        if ("maxTouchPoints" in navigator) {
            return navigator.maxTouchPoints > 0;
        } else if ("msMaxTouchPoints" in navigator) {
            return navigator.msMaxTouchPoints > 0;
        } else {
            return false;
        }
    }-*/;

    @Override
    public boolean isMobile() {
        return isMobileHtml();
    }
    
    private native boolean isMobileHtml() /*-{
        if (navigator.userAgentData) {
            return navigator.userAgentData.mobile;
        } else {
            // Fall back to user-agent parsing, as some browsers don't support above yet.
            if (navigator.platform.indexOf("Win") != -1) return false;
            if (navigator.platform.indexOf("Mac") != -1) return false;
            if (navigator.platform.indexOf("Android") != -1) return true;
            if (navigator.platform.indexOf("iPhone") != -1) return true;
            if (navigator.platform.indexOf("iPad") != -1) return true;
            // For other devices, we'll use touch screen logic.
            if ("maxTouchPoints" in navigator) {
                return navigator.maxTouchPoints > 0;
            } else if ("msMaxTouchPoints" in navigator) {
                return navigator.msMaxTouchPoints > 0;
            } else {
                return false;
            }
        }
    }-*/;
}
