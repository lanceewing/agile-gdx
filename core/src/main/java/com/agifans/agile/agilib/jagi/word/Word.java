/*
 *  Word.java
 *  Adventure Game Interpreter Word Package
 *
 *  Created by Dr. Z
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.word;

/**
 * Represent a word.
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class Word implements Comparable {
    /**
     * Word number.
     */
    public int number;

    /**
     * Word textual representation.
     */
    public String text;

    public String toString() {
        return text + " (" + number + ")";
    }

    public int compareTo(Object o) {
        return text.compareTo(((Word) o).text);
    }
}