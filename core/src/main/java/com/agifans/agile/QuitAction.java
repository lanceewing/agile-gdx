package com.agifans.agile;

/**
 * Not really an Exception as such. This is how we exit out of AGILE from the
 * quit() AGI command. Rather than doing an immediate System.exit or something
 * similar, the Interpreter will instead throw an instance of QuitAction, 
 * indicating that the Interpreter should exit cleanly.
 */
public class QuitAction extends RuntimeException {
 
    public static void exit() {
        throw new QuitAction();
    }
}
