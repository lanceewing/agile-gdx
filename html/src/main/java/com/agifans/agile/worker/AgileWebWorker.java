package com.agifans.agile.worker;

import com.agifans.agile.Interpreter;
import com.agifans.agile.QuitAction;
import com.agifans.agile.agilib.Game;
import com.agifans.agile.gwt.GameFileMapEncoder;
import com.agifans.agile.gwt.GwtGameLoader;
import com.agifans.agile.gwt.GwtPixelData;
import com.agifans.agile.gwt.GwtSavedGameStore;
import com.agifans.agile.gwt.GwtUserInput;
import com.agifans.agile.gwt.GwtVariableData;
import com.agifans.agile.gwt.GwtWavePlayer;
import com.agifans.agile.gwt.OPFSGameFiles;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.webworker.client.DedicatedWorkerEntryPoint;

/**
 * A web worker that runs the background AGILE interpreter "thread", which is 
 * separate from the UI thread. This prevents the UI thread from being blocked, as
 * the AGI interpreter does certain things that greatly prolongs the standard
 * interpreter "tick". For example, when the menu is active, it waits for key presses
 * and doesn't return from the current "tick" until the menu is closed. The same 
 * happens for the Inventory screen, the Help screen, and for normal text windows
 * that appear. There are even LOGIC scripts in some games that will wait for key
 * presses, or for the AGI game block to reach a certain value, before leaving the 
 * LOGIC script. Without using a web worker, the UI thread would be blocked in such
 * cases, which would hang the whole web page.
 * 
 * The web worker does not have direct access to the HTML5 canvas used by libgdx,
 * or to the key events. There therefore needs to be two-way communication between
 * the two. Unfortunately the standard mechanism for the UI thread to send something
 * to the web worker, i.e. via postMessage, will not be appropriate for sending 
 * the keyboard event data. This is because, as mentioned above, the AGI interpreter
 * often waits for a key press, which blocks the web worker thread, which means it
 * can't process the incoming messages, since the web worker is also single threaded.
 * Luckily JavaScript does have another communication mechanism, which is the 
 * SharedArrayBuffer, and it seems ideal for a queue of key presses. It is a shared
 * buffer of memory that is visible to both sides automatically without the need
 * to transfer it.
 * 
 * Likewise, the pixel data is also stored in a SharedArrayBuffer, so that the UI 
 * thread can render the most up to date pixels for each frame. This is as per what 
 * the original AGI interpreter does, so we need to match it. We can't do something
 * like send back the pixels at the end of each "Tick", because there are scenarios
 * where pixel changes occur mid "Tick" that need to be visible.
 * 
 * For the transfer of the game files to the web work, this can be done via postMessage
 * and since ArrayBuffer is transferable, it can be transferred pretty much 
 * instantly. It is kind of a reference transfer (not a copy, since the ArrayBuffer
 * becomes unusable on the web worker side as soon as it is transferred).
 * 
 * Note that GWT supports ArrayBuffer by default but the SharedArrayBuffer requires
 * us to implement native JS methods, since it is only very recently been supported
 * by all browsers (March 2023).
 */
public class AgileWebWorker extends DedicatedWorkerEntryPoint implements MessageHandler {

    private DedicatedWorkerGlobalScope scope;
    
    // The web worker has its own instance of each of these. It is not the same instance
    // as in the AgileWorker. Instead part of the data is either shared, or transferred
    // between the client and work.
    private GwtUserInput userInput;
    private GwtPixelData pixelData;
    private GwtSavedGameStore savedGameStore;
    private GwtWavePlayer wavePlayer;
    private GwtVariableData variableData;
    private GwtGameLoader gameLoader;
    
    /**
     * The actual AGI interpreter implementation that runs the AGI game.
     */
    private Interpreter interpreter;
    
    /**
     * Used to store game data files.
     */
    private OPFSGameFiles opfsGameFiles;
    
    /**
     * The total tick count at the time of the last animation tick.
     */
    private int lastTotalTickCount;
    
    /**
     * Incoming messages from the UI thread are for two purposes: One is to set things 
     * up, and then once both sides are up and running, the UI thread then starts sending
     * "Tick" messages, which request the web worker to perform a tick. The UI thread 
     * will only send a "Tick" message if it knows that the web worker finished the last 
     * tick, otherwise it skips sending the message, due to the web worker already being 
     * busy. This requires the web worker to send back "TickComplete" messages when a 
     * tick has reached completion..
     * 
     * @param event The incoming message from the UI thread.
     */
    @Override
    public void onMessage(MessageEvent event) {        
        JavaScriptObject eventObject = event.getDataAsObject();
        
        switch (getEventType(eventObject)) {
            case "ImportGame":
                ArrayBuffer importGameDataBuffer = getArrayBuffer(eventObject);
                String opfsDirectionName = getNestedString(eventObject, "directoryName");
                opfsGameFiles.writeGameFilesData(opfsDirectionName, importGameDataBuffer);
                break;
        
            case "Initialise":
                JavaScriptObject keyPressQueueSAB = getNestedObject(eventObject, "keyPressQueueSAB");
                JavaScriptObject keysSAB = getNestedObject(eventObject, "keysSAB");
                JavaScriptObject oldKeysSAB = getNestedObject(eventObject, "oldKeysSAB");
                JavaScriptObject variableSAB = getNestedObject(eventObject, "variableSAB");
                JavaScriptObject pixelDataSAB = getNestedObject(eventObject, "pixelDataSAB");
                userInput = new GwtUserInput(keyPressQueueSAB, keysSAB, oldKeysSAB);
                variableData = new GwtVariableData(variableSAB);
                pixelData = new GwtPixelData(pixelDataSAB);
                wavePlayer = new GwtWavePlayer();
                savedGameStore = new GwtSavedGameStore();
                break;
                
            case "Start":
                ArrayBuffer gameDataBuffer = getArrayBuffer(eventObject);
                GameFileMapEncoder gameFileMapDecoder = new GameFileMapEncoder();
                gameLoader = new GwtGameLoader(pixelData);
                Game game = gameLoader.loadGame(gameFileMapDecoder.decodeGameFileMap(gameDataBuffer));
                savedGameStore.initialise(game.gameId);
                interpreter = new Interpreter(
                        game, userInput, wavePlayer, savedGameStore, 
                        pixelData, variableData);
                lastTotalTickCount = variableData.getTotalTicks();
                performAnimationTick(0);
                break;
                
            default:
                // Unknown message. Ignore.
        }
    }
    
    public void performAnimationTick(double timestamp) {
        try {
            int currentTotalTicks = variableData.getTotalTicks();
            int numOfTicksToRun = (currentTotalTicks - this.lastTotalTickCount);
            this.lastTotalTickCount = currentTotalTicks;
            
            // Catch up with ticks, if we are behind.
            while ((numOfTicksToRun-- > 0) && (variableData.getTotalTicks() == currentTotalTicks)) {
                // Perform one animation tick.
                interpreter.animationTick();
            }
            
            requestNextAnimationFrame();
            
        } catch (QuitAction qa) {
            // The user has quit the game, so notify the UI thread of this.
            postObject("QuitGame", JavaScriptObject.createObject());
        }
    }
    
    public native void exportPerformAnimationTick() /*-{
        var that = this;
        $self.performAnimationTick = $entry(function(timestamp) {
          that.@com.agifans.agile.worker.AgileWebWorker::performAnimationTick(D)(timestamp);
        });
    }-*/;

    private native void requestNextAnimationFrame()/*-{
        $self.requestAnimationFrame($self.performAnimationTick);
    }-*/;
    
    private native String getEventType(JavaScriptObject obj)/*-{
        return obj.name;
    }-*/;
    
    private native JavaScriptObject getNestedObject(JavaScriptObject obj, String fieldName)/*-{
        return obj.object[fieldName];
    }-*/;
    
    private native String getNestedString(JavaScriptObject obj, String fieldName)/*-{
        return obj.object[fieldName];
    }-*/;
    
    private native ArrayBuffer getArrayBuffer(JavaScriptObject obj)/*-{
        return obj.buffer;
    }-*/;
    
    protected final void postObject(String name, JavaScriptObject object) {
        getGlobalScope().postObject(name, object);
    }
    
    protected final void postTransferableObject(String name, JavaScriptObject object) {
        getGlobalScope().postTransferableObject(name, object);
    }
    
    @Override
    protected DedicatedWorkerGlobalScope getGlobalScope() {
        return scope;
    }
    
    protected final void setOnMessage(MessageHandler messageHandler) {
        getGlobalScope().setOnMessage(messageHandler);
    }
    
    @Override
    public void onWorkerLoad() {
        exportPerformAnimationTick();
        
        this.scope = DedicatedWorkerGlobalScope.get();
        this.opfsGameFiles = new OPFSGameFiles();
        
        this.importScript("/opfs-saved-games.js");
        
        this.setOnMessage(this);
    }
    
    private final native void logToJSConsole(String message)/*-{
        console.log(message);
    }-*/;
}
