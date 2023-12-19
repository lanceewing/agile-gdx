package com.agifans.agile.agilib;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Represents the AGI WORDS.TOK file.
 * 
 * The following word numbers have special meaning.
 * 
 * Word# Meaning
 * ----- -----------------------------------------------------------
 *   0   Words are ignored (e.g. the, at)
 *   1   Anyword
 * 9999  ROL(Rest Of Line) -- it does matter what the rest of the
 *       input list is
 * ----- -----------------------------------------------------------
 * 
 * All other word numbers are free for use.
 */
public class Words extends Resource {

    /**
     * A Map between a word's text and the word number for that word.
     */
    public Map<String, Integer> wordToNumber;

    /**
     * A Map between a word number and the set of words that the word number is for (i.e. the synonym set).
     */
    public Map<Integer, SortedSet<String>> numberToWords;
    
    /**
     * Constructor for Words.
     * 
     * @param jagiWords The JAGI Words object to construct an AGILE Words object from.
     */
    public Words(com.sierra.agi.word.Words jagiWords) {
        this.wordToNumber = new HashMap<String, Integer>();
        this.numberToWords = new HashMap<Integer, SortedSet<String>>();
        for (com.sierra.agi.word.Word jagiWord : jagiWords.words()) {
            addWord(jagiWord.number, jagiWord.text);
        }
    }
    
    /**
     * Adds a new word for the given word text and word number. The word number does not need
     * to be unique. When the word number is already in use, then the new word being added is
     * a synonym for the existing word(s) using that word number.
     * 
     * @param wordNum The word number for the word being added.
     * @param wordText The word text for the word being added.
     */
    public void addWord(int wordNum, String wordText) {
        // Add a mapping from the word text to its word number.
        this.wordToNumber.put(wordText, wordNum);

        // Add the word text to the set of words for the given word number.
        SortedSet<String> words;
        if (this.numberToWords.containsKey(wordNum)) {
            words = this.numberToWords.get(wordNum);
        }
        else {
            words = new TreeSet<String>();
            this.numberToWords.put(wordNum, words);
        }
        words.add(wordText);
    }
}
