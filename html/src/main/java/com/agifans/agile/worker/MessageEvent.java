package com.agifans.agile.worker;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Extends the gwt-webworker MessageEvent class in order to provide the ability to
 * get the message data as an object.
 */
public class MessageEvent extends com.google.gwt.webworker.client.MessageEvent {

    public final native JavaScriptObject getDataAsObject() /*-{
        return this.data;
    }-*/;
    
}
