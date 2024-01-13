package com.agifans.agile.gwt;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.Int8Array;

/**
 * GWT wrapper around a simple JS file that provides access to the Origin Private
 * File System (OPFS). To set up synchronous access to the files, the first few steps
 * require the use of async/await JS syntax, which GWT native JS methods don't support. 
 * Thus the reason for the small external JS file. We can do anything we want (within
 * reason) in an external JS file, as GWT doesn't process it. Our customer OPFSSavedGames
 * class is loaded into the global object and then we access it using the native JS
 * methods in this GWT wrapper class. This class can only be used in a web worker, as
 * the synchronous access to the OPFS files is only supported in a web worker.
 */
public class OPFSSavedGames extends JavaScriptObject {

    protected OPFSSavedGames() {
        // Constructor must not be public for a JavaScriptObject sub-class.
    }
    
    public final static native OPFSSavedGames newOPFSSavedGames()/*-{
        return new $self.OPFSSavedGames();
    }-*/;
    
    public final native void init(String gameId)/*-{
        this.init(gameId);
    }-*/;
    
    public final native Int8Array readSavedGameData(int gameNum)/*-{
        return this.readSavedGameData(gameNum);
    }-*/;
    
    public final native void writeSavedGameData(int gameNum, Int8Array savedGameData)/*-{
        this.writeSavedGameData(gameNum, savedGameData);
    }-*/;
    
    public final native long getSavedGameTimestamp(int gameNum)/*-{
        return this.getSavedGameTimestamp(gameNum);
    }-*/; 
}
