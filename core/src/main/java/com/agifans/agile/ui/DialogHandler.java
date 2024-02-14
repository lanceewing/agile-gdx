package com.agifans.agile.ui;

import com.agifans.agile.config.AppConfigItem;

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
   * @param appConfigItem Optional selected game that is being imported.
   * @param fileType The type of file to be selected, e.g. DIR or ZIP.
   * @param title Title for the open file dialog.
   * @param startPath The starting path.
   * @param openFileResponseHandler The handler to be invoked with the chosen file (if chosen).
   */
  public void openFileDialog(AppConfigItem appConfigItem, String fileType, String title, String startPath, OpenFileResponseHandler openFileResponseHandler);

  /**
   * Invoked when AGILE wants to ask the user to input some text.
   * 
   * @param message The message to display that asks/says what text to enter.
   * @param initialValue The initial value to put in the input field.
   * @param textInputResponseHandler The handler to be invoked with the user's response.
   */
  public void promptForTextInput(String message, String initialValue, TextInputResponseHandler textInputResponseHandler);
  
  /**
   * Invoked when AGILE wants to display a mesage to the user.
   * 
   * @param message The message to display to the user.
   */
  public void showMessageDialog(String message);
  
  /**
   * Invoked when AGILE wants to ask what type of game import to perform.
   * 
   * @param appConfigItem Optional selected game that is being imported.
   * @param importTypeResponseHandler The handler to be invoked with the user's response.
   */
  public void promptForImportType(AppConfigItem appConfigItem, ImportTypeResponseHandler importTypeResponseHandler);
}
