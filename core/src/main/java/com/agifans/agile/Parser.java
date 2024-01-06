package com.agifans.agile;

import java.util.ArrayList;
import java.util.List;

/**
 * The Parser class is responsible for parsing the user input line to match known words and 
 * also to implement the 'said' and 'parse' commands.
  */
public class Parser {

    /**
     * The List of word numbers for the recognised words from the current user input line.
     */
    private List<Integer> recognisedWordNumbers;

    /**
     * These are the characters that separate words in the user input string (although
     * usually it would be space).
     */
    private String SEPARATORS = "[ ,.?!();:\\[\\]{}]+";

    /**
     * A regex matching the characters to be deleted from the user input string.
     */
    private String IGNORE_CHARS = "['`\\-\"]";

    /**
     * Special word number that matches any word.
     */
    private int ANYWORD = 1;

    /**
     * Special word number that matches the rest of the line.
     */
    private int REST_OF_LINE = 9999;

    /**
     * The GameState class holds all of the data and state for the Game currently 
     * being run by the interpreter.
     */
    private GameState state;

    /**
     * Constructor for Parser.
     *
     * @param state The GameState class holds all of the data and state for the Game currently being run.
     */
    public Parser(GameState state) {
        this.state = state;
        this.recognisedWordNumbers = new ArrayList<Integer>();
    }

    /**
     * Parses the given user input line value. This is the method invoked by the main keyboard 
     * processing logic. After execution of this method, the RecognisedWords List will contain
     * the words that were recognised from the input line, and the recognisedWordNumbers List
     * will contain the word numbers for the recognised words. If the RecognisedWords List 
     * contains one more item than the recognisedWordNumbers List then the additional word
     * will actually be an unrecognised word and the UNKNOWN_WORD var will contain the index
     * of that word within the List + 1. The INPUT flag will be set if the RecognisedWords
     * List contains at least one word.
     *
     * @param inputLine 
     */
    public void parse(String inputLine) {
        // Clear the words matched from last time.
        state.recognisedWords.clear();
        this.recognisedWordNumbers.clear();

        // Remove ignored characters and collapse separators into a single space char.
        String sanitisedInputLine = inputLine.toLowerCase().replaceAll(IGNORE_CHARS, "").replaceAll(SEPARATORS, " ").trim();

        if (sanitisedInputLine.length() > 0) {
            int inputLineStartPos = 0;

            while (inputLineStartPos < sanitisedInputLine.length()) {
                // Scan backwards from the end of the input line, to the current input line start pos, to find the longest word match.
                for (int inputLineEndPos = sanitisedInputLine.length(); inputLineEndPos >= inputLineStartPos; inputLineEndPos--) {
                    if ((inputLineEndPos == sanitisedInputLine.length()) || (sanitisedInputLine.charAt(inputLineEndPos) == ' ')) {
                        // This is the end of a word in the input line. Check if we have a match.
                        String wordToMatch = sanitisedInputLine.substring(inputLineStartPos, inputLineEndPos);

                        if (state.words.wordToNumber.containsKey(wordToMatch)) {
                            // The word is recognised. This is the longest match possible, so let's get the word number for it.
                            int matchedWordNum = state.words.wordToNumber.get(wordToMatch);

                            // If the word number is 0, it is ignored.
                            if (matchedWordNum > 0) {
                                // Otherwise store matched word details.
                                state.recognisedWords.add(wordToMatch);
                                this.recognisedWordNumbers.add(matchedWordNum);
                            }

                            // Set the next start position to character after the separator that ended the matched word
                            // so that we can continue scanning the rest of the input line for more words.
                            inputLineStartPos = inputLineEndPos + 1;
                            break;
                        }
                        else if (wordToMatch.equals("a") || wordToMatch.equals("i")) {
                            // Skip "a" and "i". Move input line start position beyond it.
                            inputLineStartPos = inputLineEndPos + 1;
                            break;
                        }
                        else if (!wordToMatch.contains(" ")) {
                            // Unrecognised single word. Stores the word, use ANYWORD (word number 1, place holder for any word)
                            state.recognisedWords.add(wordToMatch);
                            this.recognisedWordNumbers.add(ANYWORD);
                            state.setVar(Defines.UNKNOWN_WORD, (byte)(state.recognisedWords.size()));
                            inputLineStartPos = sanitisedInputLine.length();
                            break;
                        }
                    }
                }
            }
        }

        if (state.recognisedWords.size() > 0) {
            state.flags[Defines.INPUT] = true;
        }
    }

    /**
     * Implements the 'parse' AGI command. What it does is to parse a string as if it
     * was the normal user input line. It does this simply by calling the Parse method 
     * above with the value from the identified AGI string. It resets both the INPUT
     * and HADMATCH flags prior to calling it so that the normal user input parsing
     * state is cleared. The words will be available to all said() tests for the 
     * remainder of the current logic scan.
     *
     * @param strNum The number of the AGI string to parse the value of.
     */
    public void parseString(int strNum) {
        // Clear the state from the most recent parse.
        state.flags[Defines.INPUT] = false;
        state.flags[Defines.HADMATCH] = false;

        // If the given string number is less that the total number of strings.
        if (strNum < Defines.NUMSTRINGS) {
            // Parse the value of the string as if it was user input.
            parse(state.strings[strNum]);
        }
    }

    /**
     * Returns true if the number of non-ignored words in the input line is the same
     * as that in the word list and the non-ignored words in the input match, in order, 
     * the words in the word list. The special word 'anyword' (or whatever is defined 
     * word list as word 1 in 'WORDS.TOK') matches any non-ignored word in the input.
     *
     * @param words The List of words to test if the user has said.
     * 
     * @param true if the user has said the given words; otherwise false.
     */
    public boolean said(List<Integer> wordNumbers) {
        // If there are no recognised words then we obviously didn't say what we're testing against.
        if (this.recognisedWordNumbers.size() == 0) return false;

        // We should only perform the check if we have input, and there hasn't been a match already.
        if (!state.flags[Defines.INPUT] || state.flags[Defines.HADMATCH]) return false;

        // Compare each word number in order.
        for (int i=0; i < wordNumbers.size(); i++) {
            int testWordNumber = wordNumbers.get(i);

            // If test word number matches the rest of the line, then it's a match.
            if (testWordNumber == REST_OF_LINE) {
                state.flags[Defines.HADMATCH] = true;
                return true;
            }

            // Exit if we have reached the end of the user entered words. No match.
            if (i >= recognisedWordNumbers.size()) return false;

            int inputWordNumber = this.recognisedWordNumbers.get(i);

            // If word numbers don't match, and test word number doesn't represent anyword, then no match.
            if ((testWordNumber != inputWordNumber) && (testWordNumber != ANYWORD)) return false;
        }

        // If more words were entered than in the said, and there obviously wasn't a REST_OF_LINE, then no match.
        if (state.recognisedWords.size() > wordNumbers.size()) return false;

        // Otherwise if we get this far without having exited already, it is a match.
        state.flags[Defines.HADMATCH] = true;
        return true;
    }
}