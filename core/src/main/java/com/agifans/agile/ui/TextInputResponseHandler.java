package com.agifans.agile.ui;

/**
 * Interface that is called by a DialogHandler promptForTextInput() implementation when the user has 
 * entered some text and clicked OK.
 * 
 * @author Lance Ewing
 */
public interface TextInputResponseHandler {

  public void inputTextResult(boolean success, String text); 
  
}
