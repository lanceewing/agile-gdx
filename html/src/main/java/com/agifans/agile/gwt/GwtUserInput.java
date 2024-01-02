package com.agifans.agile.gwt;

import com.agifans.agile.UserInput;

/**
 * GWT UserInput implementation. The bulk of the code is in the superclass, which
 * is platform independent. The main bit that is not is the storage of the key press
 * events. For GWT, the key press queue needs to be stored in a SharedArrayBuffer so
 * that the data is automatically shared between the UI thread and the web worker.
 */
public class GwtUserInput extends UserInput {

    // TODO: Implement the SharedArrayBuffer for the storage of the key press events.
    
    // TODO: Explore whether a LinkedList implementation could be wrapped around a SharedArrayBuffer.
}
