package com.agifans.agile.worker;

import com.google.gwt.webworker.client.DedicatedWorkerEntryPoint;
import com.google.gwt.webworker.client.MessageEvent;
import com.google.gwt.webworker.client.MessageHandler;

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
 * and since ImageBitmap is transferrable, it can be transferred pretty much 
 * instantly.
 */
public class AgileWebWorker extends DedicatedWorkerEntryPoint implements MessageHandler {

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
        // transferrable objects and the SharedArrayBuffer.
        this.postMessage("Worker received: " + event.getDataAsString());
        
    }

    @Override
    public void onWorkerLoad() {
        this.setOnMessage(this);
    }
}
