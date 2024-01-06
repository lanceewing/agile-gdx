package com.agifans.agile.worker;

/**
 * MessageHandler interface used by both sides of the web worker communication. This 
 * interface is used over the gwt-webworker interface as it also supports objects.
 */
public interface MessageHandler {
    void onMessage(MessageEvent event);
}
