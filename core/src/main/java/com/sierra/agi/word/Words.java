/*
 *  Words.java
 *  Adventure Game Interpreter Word Package
 *
 *  Created by Dr. Z
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.word;

import com.sierra.agi.io.ByteCasterStream;
import com.sierra.agi.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Stores Words of the game.
 * <p>
 * <B>Word File Format</B><BR>
 * The words.tok file is used to store the games vocabulary, i.e. the dictionary
 * of words that the interpreter understands. These words are stored along with
 * a word number which is used by the said test commands as argument values for
 * that command. Many words can have the same word number which basically means
 * that these words are synonyms for each other as far as the game is concerned.
 * </P><P>
 * The file itself is both packed and encrypted. Words are stored in alphabetic
 * order which is required for the compression method to work.
 * </P><P>
 * <B>The first section</B><BR>
 * At the start of the file is a section that is always 26x2 bytes long. This
 * section contains a two byte entry for every letter of the alphabet. It is
 * essentially an index which gives the starting location of the words beginning
 * with the corresponding letter.
 * </P><P>
 * <TABLE BORDER=1>
 * <THEAD><TR><TD>Byte</TD><TD>Meaning</TD></TR></THEAD>
 * <TBODY>
 * <TR><TD>0-1</TD><TD>Hi and then Lo byte for 'A' offset</TD></TR>
 * <TR><TD COLSPAN=2>...</TD></TR>
 * <TR><TD>50-51</TD><TD>Hi and then Lo byte for 'Z' offset</TD></TR>
 * <TR><TD>52</TD><TD>Words section</TD></TR>
 * </TBODY></TABLE>
 * </P><P>
 * The important thing to note from the above is that the 16 bit words are
 * big-endian (HI-LO). The little endian (LO-HI) byte order convention used
 * everywhere else in the AGI system is not used here. For example, 0x00 and
 * 0x24 means 0x0024, not 0x2400. Big endian words are used later on for word
 * numbers as well.
 * </P><P>
 * All offsets are taken from the beginning of the file. If no words start with
 * a particular letter, then the offset in that field will be 0x0000.
 * </P><P>
 * <B>The words section</B><BR>
 * Words are stored in a compressed way in which each word will use part of the
 * previous word as a starting point for itself. For example, "forearm" and
 * "forest" both have the prefix "fore". If "forest" comes immediately after
 * "forearm", then the data for "forest" will specify that it will start with
 * the first four characters of the previous word. Whether this method is used
 * for further confusion for would be cheaters or whether it is to help in the
 * searching process, I don't yet know, but it most certainly isn't purely for
 * compression since the words.tok file is usally quite small and no attempt is
 * made to compress any of the larger files (before AGI version 3 that is).
 * </P><P>
 * <TABLE BORDER=1>
 * <THEAD><TR><TD>Byte</TD><TD>Meaning</TD></TR></THEAD>
 * <TBODY>
 * <TR><TD>0</TD><TD>Number of characters to include from start of prevous word</TD></TR>
 * <TR><TD>1</TD><TD>Char 1 (xor 0x7F gives the ASCII code for the character)</TD></TR>
 * <TR><TD>2</TD><TD>Char 2</TD></TR>
 * <TR><TD COLSPAN=2>...</TD></TR>
 * <TR><TD>n</TD><TD>Last char</TD></TR>
 * <TR><TD>n + 1</TD><TD>Wordnum (LO-HI) -- see below</TD></TR>
 * </TBODY></TABLE>
 * </P><P>
 * If a word does not use any part of the previous word, then the prefix field
 * is equal to zero. This will always be the case for the first word starting
 * with a new letter. There is nothing to indicate where the words starting with
 * one letter finish and the next set starts, infact the words section is just
 * one continuous chain of words conforming to the above format. The index
 * section mentioned earlier is not needed to read the words in which suggests
 * that the whole words.tok format is organised to find words quickly.
 * </P><P>
 * <B>A note about word numbers</B><BR>
 * Some word numbers have special meaning. They are listed below:
 * </P><P>
 * <TABLE BORDER=1>
 * <THEAD><TR><TD>Word #</TD><TD>Meaning</TD></TR></THEAD>
 * <TBODY>
 * <TR><TD>0</TD><TD>Words are ignored (e.g. the, at)</TD></TR>
 * <TR><TD>1</TD><TD>Anyword</TD></TR>
 * <TR><TD>9999</TD><TD>ROL (Rest Of Line) -- it does matter what the rest of the input list is</TD></TR>
 * </TBODY></TABLE>
 * </P>
 *
 * @author Dr. Z, Lance Ewing (Documentation)
 * @version 0.00.00.01
 */
public class Words implements WordsProvider {
    protected Map<String, Word> wordHash = new HashMap(800);

    protected Map<Integer, Word> wordNumToWordMap = new HashMap<Integer, Word>();

    /**
     * Creates a new Word container.
     */
    public Words() {
    }

    private static String removeSpaces(String inputString) {
        StringBuffer buff = new StringBuffer(inputString.length());
        StringTokenizer token = new StringTokenizer(inputString.trim(), " ");

        while (token.hasMoreTokens()) {
            buff.append(token.nextToken());

            if (token.hasMoreTokens()) {
                buff.append(" ");
            }
        }

        return buff.toString();
    }

    private static int findChar(String str, int begin) {
        int ch = str.indexOf(' ', begin);

        if (ch < 0) {
            ch = str.length();
        }

        return ch;
    }

    public Words loadWords(InputStream stream) throws IOException {
        loadWordTable(stream);
        return this;
    }

    /**
     * Read a AGI word table.
     *
     * @param stream Stream from where to read the words.
     * @return Returns the number of words readed.
     */
    protected int loadWordTable(InputStream stream) throws IOException {
        ByteCasterStream bstream = new ByteCasterStream(stream);
        String prev = null;
        String curr;
        int i, wordNum, wordCount;

        IOUtils.skip(stream, 52);
        wordCount = 0;

        while (true) {
            i = stream.read();

            if (i < 0) {
                break;
            } else if (i > 0) {
                curr = prev.substring(0, i);
            } else {
                curr = "";
            }

            while (true) {
                i = stream.read();

                if (i <= 0) {
                    break;
                } else {
                    curr += (char) ((i ^ 0x7F) & 0x7F);

                    if (i >= 0x7F) {
                        break;
                    }
                }
            }

            if (i <= 0) {
                break;
            }

            wordNum = bstream.hiloReadUnsignedShort();
            prev = curr;

            addWord(wordNum, curr);
            wordCount++;
        }

        return wordCount;
    }

    private boolean addWord(int wordNum, String word) {
        Word w = wordHash.get(word);

        if (w != null) {
            return false;
        }

        w = new Word();
        w.number = wordNum;
        w.text = word;

        // Map of word text to the Word object.
        wordHash.put(word, w);

        // Map of word number to the Word object.
        wordNumToWordMap.put(wordNum, w);

        return true;
    }

    public Word getWordByNumber(int wordNum) {
        return wordNumToWordMap.get(wordNum);
    }

    public Word findWord(String word) {
        return wordHash.get(word);
    }

    public int getWordCount() {
        return wordHash.size();
    }

    public Collection<Word> words() {
        return wordHash.values();
    }

    public List parse(String inputString) {
        List vector = new ArrayList(5);
        int begin, end;
        Word word;

        inputString = inputString.toLowerCase();
        inputString = removeSpaces(inputString);
        begin = 0;

        while (inputString.length() > 0) {
            end = findChar(inputString, begin);
            word = findWord(inputString.substring(0, end));

            if (word != null) {
                begin = 0;

                try {
                    inputString = inputString.substring(end + 1);
                } catch (StringIndexOutOfBoundsException sioobex) {
                    inputString = "";
                }

                if (word.number == 9999) {
                    return vector;
                }

                if (word.number != 0) {
                    vector.add(word);
                }

                continue;
            }

            if (end >= inputString.length()) {
                begin = 0;
                end = findChar(inputString, 0);

                word = new Word();
                word.number = -1;
                word.text = inputString.substring(0, end);
                vector.add(word);

                if (end >= inputString.length()) {
                    break;
                }

                inputString = inputString.substring(end + 1);
                continue;
            }

            begin = end + 1;
        }

        System.out.println("Words.java = " + vector);
        return vector;
    }
}