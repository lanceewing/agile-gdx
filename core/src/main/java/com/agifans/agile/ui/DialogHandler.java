package com.agifans.agile.ui;

/**
 * An interface that the different platforms can implement to provide different
 * types of dialog window, e.g. confirm dialog, file chooser, etc.
 */
public interface DialogHandler {

  /**
   * Invoked when AGILE wants to confirm with the user that they really want
   * to continue with a particular action.
   * 
   * @param message The message to be displayed to the user.
   * @param confirmResponseHandler The handler to be invoked with the user's response.
   */
  public void confirm(String message, ConfirmResponseHandler confirmResponseHandler);
  
  /**
   * Invoked when AGILE wants the user to choose a file to open.
   * 
   * @param title Title for the open file dialog.
   * @param startPath The starting path.
   * @param openFileResponseHandler The handler to be invoked with the chosen file (if chosen).
   */
  public void openFileDialog(String title, String startPath, OpenFileResponseHandler openFileResponseHandler);

  
  public void promptForTextInput(String message, String initialValue, TextInputResponseHandler textInputResponseHandler);
  
}
