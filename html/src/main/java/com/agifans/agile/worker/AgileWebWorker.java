package com.agifans.agile.worker;

import com.agifans.agile.Interpreter;
import com.agifans.agile.agilib.Game;
import com.agifans.agile.gwt.GwtGameLoader;
import com.agifans.agile.gwt.GwtPixelData;
import com.agifans.agile.gwt.GwtSavedGameStore;
import com.agifans.agile.gwt.GwtUserInput;
import com.agifans.agile.gwt.GwtVariableData;
import com.agifans.agile.gwt.GwtWavePlayer;
import com.google.gwt.core.client.JavaScriptObject;
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
 * For the transfer of pixels back to the UI thread, this can be done via postMessage
 * and since ImageBitmap is transferable, it can be transferred pretty much 
 * instantly. It is kind of a reference transfer (not a copy, since the ImageBitmap
 * becomes unusable on the web worker side as soon as it is transferred). 
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
    
    private Interpreter interpreter;
    
    /**
     * Incoming messages from the UI thread are for two purposes: One is to set things 
     * up, and then once both sides are up and running, the UI thread then starts sending
     * "Tick" messages, which request the web worker to perform a tick. The UI thread 
     * will only send a "Tick" message if it knows that the web worker finished the last 
     * tick, otherwise it skips sending the message, due to the web worker already being 
     * busy. This requires the web worker to send back "TickComplete" messages when a 
     * tick has reached completion, which it needs to do anyway, in order to transfer the 
     * pixel ImageBitmap to the UI thread for rendering.
     * 
     * @param event The incoming message from the UI thread.
     */
    @Override
    public void onMessage(MessageEvent event) {        
        JavaScriptObject eventObject = event.getDataAsObject();
        
        switch (getEventType(eventObject)) {
            case "Initialise":
                JavaScriptObject keyPressQueueSAB = getNestedObject(eventObject, "keyPressQueueSAB");
                JavaScriptObject keysSAB =  getNestedObject(eventObject, "keysSAB");
                JavaScriptObject oldKeysSAB =  getNestedObject(eventObject, "oldKeysSAB");
                JavaScriptObject variableSAB =  getNestedObject(eventObject, "variableSAB");
                userInput = new GwtUserInput(keyPressQueueSAB, keysSAB, oldKeysSAB);
                variableData = new GwtVariableData(variableSAB);
                // TODO: Should we pass dimensions from UI thread?
                pixelData = new GwtPixelData();
                pixelData.init(320, 200);
                wavePlayer = new GwtWavePlayer();
                savedGameStore = new GwtSavedGameStore();
                gameLoader = new GwtGameLoader(pixelData);
                Game game = gameLoader.loadGame(getNestedString(eventObject, "gameUri"));
                interpreter = new Interpreter(
                        game, userInput, wavePlayer, savedGameStore, 
                        pixelData, variableData);
                break;
                
            case "Tick":
                interpreter.animationTick();
                
                // Gets the up to date pixels as an ImageBitmap, which is a transferable
                // object, so can be passed to the UI thread instantly.
                JavaScriptObject imageBitmap = pixelData.getImageBitmap();
                postTransferableObject("TickComplete", imageBitmap);
                
                // TODO: Add support for sound data, as separate message. Big amount of sample data, so needs to be transferable.
                break;
                
            default:
                // Unknown message. Ignore.
        }
    }
    
    private native String getEventType(JavaScriptObject obj)/*-{
        return obj.name;
    }-*/;
    
    private native JavaScriptObject getNestedObject(JavaScriptObject obj, String fieldName)/*-{
        return obj.object[fieldName];
    }-*/;
    
    private native String getNestedString(JavaScriptObject obj, String fieldName)/*-{
        return obj.object[fieldName];
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
        this.scope = DedicatedWorkerGlobalScope.get();
        
        this.setOnMessage(this);
    }
}
