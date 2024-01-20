package com.agifans.agile.gwt;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

public class GwtOpenFileResult extends JavaScriptObject {  
    protected GwtOpenFileResult() {
        // required protected constructor for JavaScriptObject
    }
    
    public final native String getFileName() /*-{
        return this.fileName;
    }-*/;
    
    public final native String getFilePath() /*-{
        return this.filePath;
    }-*/;
    
    public final native ArrayBuffer getFileData() /*-{
        return this.fileData;
    }-*/;
}