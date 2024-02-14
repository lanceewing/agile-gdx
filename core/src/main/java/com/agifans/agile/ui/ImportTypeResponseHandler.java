package com.agifans.agile.ui;

/**
 * Interface that is called by a DialogHandler promptForImportType() implementation when 
 * the user selected an option and clicked OK, or clicked Cancel (in which case the 
 * import type is null).
 * 
 * @author Lance Ewing
 */
public interface ImportTypeResponseHandler {

    public void importTypeResult(boolean success, ImportType importType); 

}
