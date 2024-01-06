package com.agifans.agile.worker;

import com.agifans.agile.PixelData;
import com.agifans.agile.SavedGameStore;
import com.agifans.agile.UserInput;
import com.agifans.agile.WavePlayer;
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
 * presses before leaving the LOGIC script. Without using a web worker, the UI 
 * thread would be blocked in such cases, which would hang the whole web page.
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
 * instantly.
 */
public class AgileWebWorker extends DedicatedWorkerEntryPoint implements MessageHandler {

    private DedicatedWorkerGlobalScope scope;
    
    // The web worker has its own instance of each of these. It is not the same instance
    // as in the AgileWorker. Instead part of the data is either shared, or transferred
    // between the client and work.
    private UserInput userInput;
    private PixelData pixelData;
    private SavedGameStore savedGameStore;
    private WavePlayer waveplayer;
    
    // NOTE 1: GwtUserInput should use a LinkedList implementation based on SharedArrayBuffer.
    
    /**
     * Incoming messages from the UI thread are mainly to set things up. Once both 
     * sides are up and running, the UI thread no longer sends messages but communicates
     * solely via the SharedArrayBuffer. 
     * 
     * @param event The incoming message from the UI thread.
     */
    @Override
    public void onMessage(MessageEvent event) {
        
        // The gwt-webworker module works well for Strings, but doesn't support
        // transferable objects and the SharedArrayBuffer.
        this.postMessage("Worker received: " + event.getDataAsString());
        
    }

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
