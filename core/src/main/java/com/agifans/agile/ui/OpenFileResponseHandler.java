package com.agifans.agile.ui;

/**
 * Interface that is called by a DialogHandler openFileDialog() implementation when 
 * the user has either selected a file, or cancelled file selection.
 */
public interface OpenFileResponseHandler {

  void openFileResult(boolean success, String filePath, String gameName);
  
}
