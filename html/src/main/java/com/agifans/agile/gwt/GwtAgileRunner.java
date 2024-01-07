package com.agifans.agile.gwt;

import java.util.Map;

import com.agifans.agile.AgileRunner;
import com.agifans.agile.PixelData;
import com.agifans.agile.SavedGameStore;
import com.agifans.agile.UserInput;
import com.agifans.agile.VariableData;
import com.agifans.agile.WavePlayer;
import com.agifans.agile.worker.MessageEvent;
import com.agifans.agile.worker.MessageHandler;
import com.agifans.agile.worker.Worker;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.webworker.client.ErrorEvent;
import com.google.gwt.webworker.client.ErrorHandler;

/**
 * GWT implementation of the AgileRunner. It uses a web worker to perform the execution
 * of the AGI interpreter animation ticks.
 */
public class GwtAgileRunner extends AgileRunner {

    private Worker worker;
    
    private Pixmap pixmap;
    
    /**
     * Indicates that the worker is currently executing the tick, i.e. a single interpretation 
     * cycle. This flag exists because there are some AGI commands that wait for something to 
     * happen before continuing. For example, a print window will stay up for a defined timeout
     * period or until a key is pressed. In such cases, the thread can be performing a tick 
     * for the duration of what would normally be many ticks. 
     */
    private boolean inTick;
    
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
    
    /**
     * Initialises the AgileRunner with anything that needs setting up before it starts.
     * 
     * @param pixmap
     */
    @Override
    public void init(Pixmap pixmap) {
        super.init(pixmap);
        
        // GwtAgileRunner needs to store the Pixmap so it can apply an incoming ImageBitmap.
        this.pixmap = pixmap;
    }
    
    @Override
    public void start(String gameUri) {
        // The libGDX Gdx.files.internal only seems to work in UI thread, so we
        // load the files into a map within the UI thread and then pass to the worker
        // to decode.
        createWorker((new GwtGameLoader(pixelData)).fetchGameFiles(gameUri));
    }

    @Override
    public String selectGame() {
        // TODO: Convert this into a URI format.
        return "games/kq1/";
    }
    
    public void createWorker(Map<String, byte[]> gameFileMap) {
        GameFileMapEncoder gameFileMapEncoder = new GameFileMapEncoder();
        ArrayBuffer gameFileBuffer = gameFileMapEncoder.encodeGameFileMap(gameFileMap);
        
        worker = Worker.create("worker/worker.nocache.js");
        
        final MessageHandler webWorkerMessageHandler = new MessageHandler() {
            @Override
            public void onMessage(MessageEvent event) {
                JavaScriptObject eventObject = event.getDataAsObject();
                
                switch (getEventType(eventObject)) {
                    case "TickComplete":
                        // Allows the next tick to be triggered. We only allow one tick at
                        // a time, otherwise the web worker would get a flood of Tick messages
                        // when it is busy waiting for a key or similar.
                        inTick = false;
                        // ImageBitmap will always be present, so fall through to UpdatePixels.
                        
                    case "UpdatePixels":
                        JavaScriptObject imageBitmap = getEmbeddedObject(eventObject);
                        ((GwtPixelData)pixelData).updatePixmapWithImageBitmap(pixmap, imageBitmap);
                        break;
                        
                    case "PlaySound":
                        break;
                        
                    // TODO: Could potentially pass back saved games as well.
                        
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
        JavaScriptObject keyPressQueueSAB = gwtUserInput.getKeyPressQueueSharedArrayBuffer();
        JavaScriptObject keysSAB = gwtUserInput.getKeysSharedArrayBuffer();
        JavaScriptObject oldKeysSAB = gwtUserInput.getOldKeysSharedArrayBuffer();
        JavaScriptObject variableSAB = gwtVariableData.getVariableSharedArrayBuffer();
        
        // We currently send one message to Initialise, using the SharedArrayBuffers,
        // then another message to Start the interpreter with the given game data. The 
        // game data is "transferred", whereas the others are not but rather shared.
        worker.postObject("Initialise", createInitialiseObject(
                keyPressQueueSAB, 
                keysSAB, 
                oldKeysSAB, 
                variableSAB));
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
            JavaScriptObject variableSAB)/*-{
        return { 
            keyPressQueueSAB: keyPressQueueSAB,
            keysSAB: keysSAB,
            oldKeysSAB: oldKeysSAB,
            variableSAB: variableSAB
        };
    }-*/;
    
    private native String getEventType(JavaScriptObject obj)/*-{
        return obj.name;
    }-*/;
    
    private native JavaScriptObject getEmbeddedObject(JavaScriptObject obj)/*-{
        return obj.object;
    }-*/;
    
    @Override
    public void animationTick() {
        if (!inTick) {
            inTick = true;  // NOTE: Set to false by "TickComplete" message.
            
            // Send a message to the web worker to tell it to perform an animation tick, 
            // but only if it isn't already in an animation tick.
            worker.postObject("Tick", JavaScriptObject.createObject());
        }
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
}
