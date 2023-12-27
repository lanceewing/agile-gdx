package com.agifans.agile.agilib.jagi.res;

public class DirectoryNotFoundException extends ResourceException {

    /**
     * Creates new <code>DirectoryNotFoundException</code> without detail message.
     */
    public DirectoryNotFoundException() {
        super();
    }

    /**
     * Constructs an <code>DirectoryNotFoundException</code> with the specified detail message.
     *
     * @param msg Detail message.
     */
    public DirectoryNotFoundException(String msg) {
        super(msg);
    }
}
