package com.agifans.agile.lwjgl3;

import com.agifans.agile.UserInput;

/**
 * Desktop implementation of UserInput. The bulk of the code is in the superclass. The
 * main bit that isn't is the storage of the key press events. This is primarily due to
 * GWT needing to store it in a SharedArrayBuffer, whereas Desktop needs it to be in 
 * a standard LinkedList.
 */
public class DesktopUserInput extends UserInput {

    // TODO: Implement storage of the key press events.
    
}
