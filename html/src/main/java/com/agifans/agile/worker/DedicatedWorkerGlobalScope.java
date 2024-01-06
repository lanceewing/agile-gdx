package com.agifans.agile.worker;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Extends the gwt-webworker DedicatedWorkerGlobalScope class to support 
 * transferable objects, normal objects, and SharedArrayBuffers.
 */
public class DedicatedWorkerGlobalScope extends com.google.gwt.webworker.client.DedicatedWorkerGlobalScope {

    public static native DedicatedWorkerGlobalScope get() /*-{
        return $self;
    }-*/;
    
    protected DedicatedWorkerGlobalScope() {
        // Constructors must be protected in JavaScriptObject overlays.
    };

    /**
     * This method can be used to send simple objects or a SharedArrayBuffer.
     * 
     * @param name The name of the object. Used for identification of the object.
     * @param object The JS object to send in the postMessage call.
     */
    public final native void postObject(String name, JavaScriptObject object) /*-{
        this.postMessage({name: name, object: object});
    }-*/;
    
    /**
     * This method can be used to send transferable objects, such as ArrayBuffer and
     * ImageBitmap.
     * 
     * @param name The name of the object. Used for identification of the object.
     * @param object The JS object to send in the postMessage call.
     */
    public final native void postTransferableObject(String name, JavaScriptObject object) /*-{
        this.postMessage({name: name, object: object}, [object]);
    }-*/;
    
    public final native void setOnMessage(MessageHandler messageHandler) /*-{
        this.onmessage = function(event) {
            messageHandler.@com.agifans.agile.worker.MessageHandler::onMessage(Lcom/agifans/agile/worker/MessageEvent;)(event);
        }
    }-*/;
}
