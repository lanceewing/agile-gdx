package com.agifans.agile.ui;

/**
 * Interface that is called by a DialogHandler config() implementation when the user has 
 * chosen either Yes or No.
 */
public interface ConfirmResponseHandler {

  public void yes();
 
  public void no();
  
}
