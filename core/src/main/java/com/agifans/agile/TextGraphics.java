package com.agifans.agile;

import java.util.ArrayList;
import java.util.List;

import com.agifans.agile.util.StringUtils;

import static com.agifans.agile.util.StringUtils.*;

/**
 * Provides methods for drawing text on to the AGI screen.
 */
public class TextGraphics {

    private static final int WINTOP = 1;
    private static final int WINBOT = 20;
    private static final int WINWIDTH = 30;
    private static final int VMARGIN = 5;
    private static final int HMARGIN = 5;
    private static final int CHARWIDTH = 4;        /* in our coordinates */
    private static final int CHARHEIGHT = 8;
    private static final int INVERSE = 0x8f;       /* inverse video, i.e. black on white */
    private static final int UNASSIGNED = -1;

    /**
     * Stores details about the currently displayed text window.
     */
    public static class TextWindow {
        
        // Mandatory items required by OpenWindow.
        public int position;
        public int dimensions;
        public int x() { return ((((position >> 8) & 0xFF) << 1)); }
        public int y() { return ((position & 0xFF) - (((dimensions >> 8) & 0xFF) - 1) + 8); }
        public int width() { return ((dimensions & 0xFF) << 1); }
        public int height() { return ((dimensions >> 8) & 0xFF); }
        public int backgroundColour;
        public int borderColour;

        // Items set by OpenWindow.
        public int[] backPixels;

        // Items always set by WindowNoWait.
        public int top;
        public int left;
        public int bottom;
        public int right;
        public String[] textLines;
        public int textColour;

        // Items optionally set by WindowNoWait.
        public AnimatedObject aniObj;

        public TextWindow(int position, int dimensions, int backgroundColour, int borderColour) {
            this(position, dimensions, backgroundColour, borderColour, 0, 0, 0, 0, null, 0, null);
        }
    
        public TextWindow(
            int position, int dimensions, int backgroundColour, int borderColour, int top, int left, 
            int bottom, int right, String[] textLines, int textColour, AnimatedObject aniObj) {
            this.position = position;
            this.dimensions = dimensions;
            this.backgroundColour = backgroundColour;
            this.borderColour = borderColour;
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
            this.textLines = textLines;
            this.textColour = textColour;
            this.aniObj = aniObj;
        }
    }

    /**
     * Stores details about the currently displayed text window.
     */
    private TextWindow openWindow;

    private int winWidth = -1;
    private int winULRow = -1;
    private int winULCol = -1;
    private int maxLength;

    private char escapeChar = '\\';         /* the escape character */

    /**
     * The GameState class holds all of the data and state for the Game currently 
     */
    private GameState state;

    /**
     * Holds the data and state for the user input, i.e. keyboard and mouse input.
     */
    private UserInput userInput;

    /**
     * The pixel data for the AGI screen, in which the text will be drawn.
     */
    private PixelData pixelData;

    /**
     * Constructor for TextGraphics.
     * 
     * @param pixelData The GameScreen pixels. This is what TextGraphics draws windows (and indirectly menus) to.
     * @param state The GameState class holds all of the data and state for the Game currently running.
     * @param userInput Holds the data and state for the user input, i.e. keyboard and mouse input.
     */
    public TextGraphics(PixelData pixelData, GameState state, UserInput userInput) {
        this.state = state;
        this.userInput = userInput;
        this.pixelData = pixelData;
        this.openWindow = null;
        this.clearLines(0, 24, 0);
    }

    /**
     * Sets the text colour attributes used when drawing text characters.
     * 
     * @param foregroundColour
     * @param backgroundColour
     */
    public void setTextAttribute(int foregroundColour, int backgroundColour) {
        state.foregroundColour = (foregroundColour & 0xFF);
        state.backgroundColour = makeBackgroundColour(backgroundColour);
        state.textAttribute = makeTextAttribute(foregroundColour, backgroundColour);
    }

    /**
     * Return the requested text attribute in it's internal representation.
     * 
     * @param foregroundColour
     * @param backgroundColour
     * 
     * @return
     */
    private int makeTextAttribute(int foregroundColour, int backgroundColour) {
        if (!state.graphicsMode) {
            // For text mode, put background in high nibble, fore in low.
            return (((backgroundColour << 4) | foregroundColour) & 0xFF);
        }
        else {
            // In graphics mode, if back is not black, approximate with inverse text (black on white).
            return ((backgroundColour == 0? foregroundColour : INVERSE) & 0xFF);
        }
    }

    /**
     * Return the internal representation for the requested background color.
     * 
     * @param backgroundColour
     * 
     * @return The internal representation for the requested background color.
     */
    public int makeBackgroundColour(int backgroundColour) {
        if (state.graphicsMode && (backgroundColour != 0)) {
            // In graphics if back is not black, approximate with inverse text (black on white).
            return (0xff);  /* mask off inverse */
        }
        else {
            // This is rather strange, but for clear.lines and clear.text.rect, in text mode the
            // background colour is black regardless of the colour parameter value.
            return (0);
        }
    }

    /**
     * Clears the lines from the specified top line to the specified bottom line using the
     * 
     * @param top
     * @param bottom
     * @param backgroundColour
     */
    public void clearLines(int top, int bottom, int backgroundColour) {
        int startPos = top * 8 * 320;
        int endPos = ((bottom + 1) * 8 * 320) - 1;
        int colour = EgaPalette.colours[backgroundColour & 0x0F];
        
        for (int i=startPos; i <= endPos; i++) {
            pixelData.putPixel(i, colour);
        }
    }

    /**
     * Clears a text rectangle as specified by the top, left, bottom and right values. The top and
     * 
     * @param top
     * @param left
     * @param bottom
     * @param right
     * @param backgroundColour
     */
    public void clearRect(int top, int left, int bottom, int right, int backgroundColour) {
        int backgroundRGB565 = EgaPalette.colours[backgroundColour & 0x0F];
        int height = ((bottom - top) + 1) * 8;
        int width = ((right - left) + 1) * 8;
        int startY = (top * 8);
        int startX = (left * 8);
        int startScreenPos = ((startY * 320) + startX);
        int screenYAdd = 320 - width;

        for (int y = 0, screenPos = startScreenPos; y < height; y++, screenPos += screenYAdd) {
            for (int x = 0; x < width; x++, screenPos++) {
                pixelData.putPixel(screenPos, backgroundRGB565);
            }
        }
    }

    public void textScreen() {
        textScreen(UNASSIGNED);
    }
    
    public void textScreen(int backgroundColour) {
        state.graphicsMode = false;

        if (backgroundColour == UNASSIGNED) {
            setTextAttribute((byte)state.foregroundColour, (byte)state.backgroundColour);
            // Note that the original AGI interpreter uses the background from the TextAttribute 
            // value rather than the current BackgroundColour.
            backgroundColour = ((state.textAttribute >> 4) & 0x0F);
        }

        // Clear the whole screen to the background colour.
        clearLines(0, 24, backgroundColour);
    }

    public void graphicsScreen() {
        state.graphicsMode = true;

        setTextAttribute((byte)state.foregroundColour, (byte)state.backgroundColour);

        // Clear whole screen to black.
        clearLines(0, 24, 0);

        // Copy VisualPixels to game screen.
        pixelData.pixelCopy(state.visualPixels, (8 * state.pictureRow) * 320, state.visualPixels.length);
        
        updateStatusLine();
        updateInputLine();
    }

    /**
     * Draws a character to the AGI screen. Depending on the usage, this may either be done
     * to the VisualPixels or directly to the GameScreen pixels. Windows and menu text is 
     * drawn directly to the GameScreen pixels, but Display action commands are drawn to the
     * VisualPixels array.
     * 
     * @param pixelData The pixel data to draw the character to.
     * @param charNum The ASCII code number of the character to draw.
     * @param x The X position of the character.
     * @param y The Y position of the character.
     * @param foregroundColour The foreground colour of the character.
     * @param backgroundColour The background colour of the character.
     */
    public void drawChar(PixelData pixelData, byte charNum, int x, int y, int foregroundColour, int backgroundColour) {
        drawChar(pixelData, charNum, x, y, foregroundColour, backgroundColour, false);
    }
    
    /**
     * Draws a character to the AGI screen. Depending on the usage, this may either be done
     * to the VisualPixels or directly to the GameScreen pixels. Windows and menu text is 
     * drawn directly to the GameScreen pixels, but Display action commands are drawn to the
     * VisualPixels array.
     * 
     * @param pixelData The pixel data to draw the character to.
     * @param charNum The ASCII code number of the character to draw.
     * @param x The X position of the character.
     * @param y The Y position of the character.
     * @param foregroundColour The foreground colour of the character.
     * @param backgroundColour The background colour of the character.
     * @param halfTone If true then character are only half drawn.
     */
    public void drawChar(PixelData pixelData, byte charNum, int x, int y, int foregroundColour, int backgroundColour, boolean halfTone) {
        for (int byteNum = 0; byteNum < 8; byteNum++) {
            int fontByte = (IBM_BIOS_FONT[(charNum << 3) + byteNum] & 0xFF);
            boolean halfToneState = ((byteNum % 2) == 0);

            for (int bytePos = 7; bytePos >= 0; bytePos--) {
                if (!halfTone || halfToneState) {
                    if ((fontByte & (1 << bytePos)) != 0) {
                        pixelData.putPixel(((y + byteNum) * 320) + x + (7 - bytePos), EgaPalette.colours[foregroundColour]);
                    }
                    else {
                        pixelData.putPixel(((y + byteNum) * 320) + x + (7 - bytePos), EgaPalette.colours[backgroundColour]);
                    }
                }

                halfToneState = !halfToneState;
            }
        }
    }

    /**
     * Draws the given string to the AGI screen, at the given x/y position, in the given colours.
     * 
     * @param pixelData The pixel data to draw the character to.
     * @param text The text to draw to the screen.
     * @param x The X position of the text.
     * @param y The Y position of the text.
     */
    public void drawString(PixelData pixelData, String text, int x, int y) {
        drawString(pixelData, text, x, y, UNASSIGNED, UNASSIGNED, false);
    }

    /**
     * Draws the given string to the AGI screen, at the given x/y position, in the given colours.
     * 
     * @param pixelData The pixel data to draw the character to.
     * @param text The text to draw to the screen.
     * @param x The X position of the text.
     * @param y The Y position of the text.
     * @param foregroundColour Optional foreground colour. Defaults to currently active foreground colour if not specified.
     * @param backgroundColour Optional background colour. Defaults to currently active background colour if not specified.
     */
    public void drawString(PixelData pixelData, String text, int x, int y, int foregroundColour, int backgroundColour) {
        drawString(pixelData, text, x, y, foregroundColour, backgroundColour, false);
    }
    
    /**
     * Draws the given string to the AGI screen, at the given x/y position, in the given colours.
     * 
     * @param pixelData The pixel data to draw the character to.
     * @param text The text to draw to the screen.
     * @param x The X position of the text.
     * @param y The Y position of the text.
     * @param foregroundColour Optional foreground colour. Defaults to currently active foreground colour if not specified.
     * @param backgroundColour Optional background colour. Defaults to currently active background colour if not specified.
     * @param halfTone If true then character are only half drawn.
     */
    public void drawString(PixelData pixelData, String text, int x, int y, int foregroundColour, int backgroundColour, boolean halfTone) {
        // This method is used as both a general text drawing method, for things like the menu 
        // and inventory, and also for the print and display commands. The print and display
        // commands will operate using the currently set text attribute, foreground and background
        // values. The more general use cases would pass in the exact colours that they want to
        // use, no questions asked.

        // Foreground colour.
        if (foregroundColour == UNASSIGNED) {
            if (state.graphicsMode) {
                // In graphics mode, if background is not black, foreground is black; otherwise as is.
                foregroundColour = (state.backgroundColour == 0? state.foregroundColour : 0);
            }
            else {
                // In text mode, we use the text attribute foreground colour as is.
                foregroundColour = (state.textAttribute & 0x0F);
            }
        }

        // Background colour.
        if (backgroundColour == UNASSIGNED) {
            if (state.graphicsMode) {
                // In graphics mode, background can only be black or white.
                backgroundColour = (state.backgroundColour == 0 ? 0 : 15);
            }
            else {
                // In text mode, we use the text attribute background colour as is.
                backgroundColour = ((state.textAttribute >> 4) & 0x0F);
            }
        }

        // GWT backend doesn't support IBM437/CP437.
        byte[] textBytes = StringUtils.getBytesFromString(text);

        for (int charPos = 0; charPos < textBytes.length; charPos++) {
            drawChar(pixelData, textBytes[charPos], x + (charPos * 8), y, foregroundColour, backgroundColour, halfTone);
        }
    }

    /**
     * Display the given string at the given row and col. This method renders only the text and 
     * does not pop up a message window.
     * 
     * @param str
     * @param row
     * @param col
     */
    public void display(String str, int row, int col) {
        // Expand references and split on new lines.
        String[] lines = buildMessageLines(str, Defines.TEXTCOLS + 1, col);

        for (int i = 0; i < lines.length; i++) {
            drawString(this.pixelData, lines[i], col * 8, (row + i) * 8);

            // For subsequent lines, we start at column 0 and ignore what was passed in.
            col = 0;
        }
    }

    /**
     * Print the given string in an AGI message window.
     * 
     * @param str The text to include in the message window.
     */
    public void print(String str) {
        windowPrint(str);
    }

    /**
     * Print the given string in an AGI message window, the window positioned at the given row
     * and col, and of the given width.
     * 
     * @param str 
     * @param row
     * @param col
     * @param width
     */
    public void printAt(String str, int row, int col, int width) {
        winULRow = row;
        winULCol = col;

        if ((winWidth = width) == 0) {
            winWidth = WINWIDTH;
        }

        windowPrint(str);

        winWidth = winULRow = winULCol = -1;
    }

    /**
     * Updates the status line with the score and sound status.
     */
    public void updateStatusLine() {
        if (state.showStatusLine) {
            clearLines(state.statusLineRow, state.statusLineRow, 15);

            StringBuilder scoreStatus = new StringBuilder();
            scoreStatus.append(" Score:");
            scoreStatus.append(state.vars[Defines.SCORE]);
            scoreStatus.append(" of ");
            scoreStatus.append(state.vars[Defines.MAXSCORE]);
            drawString(this.pixelData, padRightSpaces(scoreStatus.toString(), 30), 0, state.statusLineRow * 8, 0, 15);
            StringBuilder soundStatus = new StringBuilder();
            soundStatus.append("Sound:");
            soundStatus.append(state.flags[Defines.SOUNDON] ? "on" : "off");
            drawString(this.pixelData, padRightSpaces(soundStatus.toString(), 10), 30 * 8, state.statusLineRow * 8, 0, 15);
        }
    }

    public void updateInputLine() {
        updateInputLine(true);
    }
    
    /**
     *  Updates the user input line based on current state.
     * 
     * @param clearWhenNotEnabled
     */
    public void updateInputLine(boolean clearWhenNotEnabled) {
        if (state.graphicsMode) {
            if (state.acceptInput) {
                // Input line has the prompt string at the start, then the user input.
                StringBuilder inputLine = new StringBuilder();
                if (state.strings[0] != null) {
                    inputLine.append(expandReferences(state.strings[0]));
                }
                inputLine.append(state.currentInput.toString());
                if (state.cursorCharacter > 0) {
                    // Cursor character is optional. There isn't one at the start of the game.
                    inputLine.append(state.cursorCharacter);
                }
                drawString(this.pixelData, padRightSpaces(inputLine.toString(), Defines.MAXINPUT), 0, state.inputLineRow * 8);
            }
            else if (clearWhenNotEnabled) {
                // If not accepting input, clear the prompt and text input.
                clearLines(state.inputLineRow, state.inputLineRow, 0);
            }
        }
    }

    /**
     * Prints the message as a prompt at column 0 of the current input row, then allows the user to
     * enter some text. The entered text will have everything other than digits stripped from it, then 
     * it is converted into a number and returned.
     * 
     * @param message The message to display to the player instructing them what to enter.
     * 
     * @returns The entered number as a byte, or 0 if it can't be converted.
     */
    public byte getNum(String message) {
        clearLines(state.inputLineRow, state.inputLineRow, 0);

        // Show the prompt message to the user at the specified position.
        display(message, state.inputLineRow, 0);

        // Get a line of text from the user.
        String line = getLine(4, (byte)state.inputLineRow, (byte)message.length());

        // Strip out everything that isn't a digit. A little more robust than the original AGI interpreter.
        String digitsInLine = line.replaceAll("[^\\d]", "");
        
        updateInputLine();

        return (byte)(digitsInLine.length() > 0? Integer.parseInt(digitsInLine) : 0);
    }

    /**
     * Prints the message as a prompt at the given screen position, then allows the user to enter
     * the string for string number.
     * 
     * @param strNum The number of the user string to put the entered value in to.
     * @param message A message to display to the player instructing them what to enter.
     * @param row The row to display the message at.
     * @param col The column to display the message at.
     * @param length The maximum length of the string to get.
     */
    public void getString(int strNum, String message, int row, int col, int length) {
        // The string cannot be longer than the maximum length for a user string.
        length = (byte)(length > Defines.STRLENGTH? Defines.STRLENGTH : length);

        // Show the prompt message to the user at the specified position.
        display(message, row, col);

        // Position the input area immediately after the message.
        col += (byte)message.length();

        // Get a line of text from the user.
        String line = getLine(length, row, col);

        // If it is not null, i.e. the user didn't hit ESC, then store in user string.
        if (line != null) state.strings[strNum] = line;
    }

    /**
     * Gets a line of user input, echoing the prompt char and entered text at the specified position.
     *
     * @param length The maximum length of the line of text to get.
     * @param row The row on the screen to position the text entry field.
     * @param col The column on the screen to position the start of the text entry field.
     */
    public String getLine(int length, int row, int col) {
        return getLine(length, row, col, "", -1, -1);
    }
    
    /**
     * Gets a line of user input, echoing the prompt char and entered text at the specified position.
     *
     * @param length The maximum length of the line of text to get.
     * @param row The row on the screen to position the text entry field.
     * @param col The column on the screen to position the start of the text entry field.
     * @param str The value to initialise the text entry field with; defaults to empty.
     * @param foregroundColour The foreground colour of the text in the text entry field.
     * @param backgroundColour The background colour of the text in the text entry field.
     * 
     * @return The entered string if ENTER was hit, otherwise null if ESC was hit.
     */
    public String getLine(int length, int row, int col, String str, int foregroundColour, int backgroundColour) {
        StringBuilder line = new StringBuilder(str);

        // The string cannot be longer than the maximum length for a GetLine call.
        length = (byte)(length > Defines.GLSIZE ? Defines.GLSIZE : length);

        // Process entered keys until either ENTER or ESC is pressed.
        while (true) {
            // Show the currently entered text.
            drawString(this.pixelData, (line.toString() + state.cursorCharacter), col * 8, row * 8, foregroundColour, backgroundColour);

            int key = userInput.waitForKey(false);
            
            if ((key & 0xF0000) == UserInput.ASCII) {
                char character = (char)(key & 0xFF);
                
                if (character == Character.ESC) {
                    // Exits without returning any entered text.
                    return null;
                }
                else if (character == Character.ENTER) {
                    // If ENTER is hit, we break out of the loop and return the entered line of text.
                    // Render Line without the cursor by replacing the cursor with empty string
                    drawString(this.pixelData, line.toString() + " ", col * 8, row * 8, foregroundColour, backgroundColour);
                    break;
                }
                else if (character == Character.BACKSPACE) {
                    // Removes one from the end of the currently entered input.
                    if (line.length() > 0) line.delete(line.length() - 1, line.length());

                    // Render Line with a space overwriting the previous position of the cursor.
                    drawString(this.pixelData, (line.toString() + state.cursorCharacter + " "), col * 8, row * 8, foregroundColour, backgroundColour);
                }
                else { // Standard char from a keypress event.
                    // If we haven't reached the max length, add the char to the line of text.
                    if (line.length() < length) line.append((char)(key & 0xff));
                }
            }
        }

        return line.toString();
    }

    /**
     * Print the string 'str' in a window on the screen and wait for ACCEPT or ABORT 
     * before disposing of it.Return TRUE for ACCEPT, FALSE for ABORT.
     *
     * @param str 
     * 
     * @return true for ACCEPT, false for ABORT.
     */
    public boolean windowPrint(String str) {
        return windowPrint(str, null);
    }
    
    /**
     * Print the string 'str' in a window on the screen and wait for ACCEPT or ABORT 
     * before disposing of it.Return TRUE for ACCEPT, FALSE for ABORT.
     *
     * @param str 
     * @param aniObj Optional AnimatedObject to draw when the window is opened.
     * 
     * @return true for ACCEPT, false for ABORT.
     */
    public boolean windowPrint(String str, AnimatedObject aniObj) {
        boolean retVal;
        long timeOut;

        // Display the window.
        windowNoWait(str, 0, 0, false, aniObj);

        // If we're to leave the window up, just return.
        if (state.flags[Defines.LEAVE_WIN] == true) {
            state.flags[Defines.LEAVE_WIN] = false;
            return true;
        }

        // Get the response.
        if (state.vars[Defines.PRINT_TIMEOUT] == 0) {
            retVal = (userInput.waitAcceptAbort() == UserInput.ACCEPT);
        }
        else {
            // The timeout value is given in half seconds and the TotalTicks in 1/60ths of a second.
            timeOut = state.totalTicks + state.vars[Defines.PRINT_TIMEOUT] * 30;

            while ((state.totalTicks < timeOut) && (userInput.checkAcceptAbort() == -1))  {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // Interrupt indicates AGILE is stopping, so throw QuitAction.
                    QuitAction.exit();
                }
            }

            retVal = true;

            state.vars[Defines.PRINT_TIMEOUT] = 0;
        }

        // Close the window.
        closeWindow();

        return retVal;
    }

    /**
     * 
     *
     * @param str 
     * @param height 
     * @param width 
     * @param fixedSize 
     * 
     * @return TextWindow
     */
    public TextWindow windowNoWait(String str, int height, int width, boolean fixedSize) {
        return windowNoWait(str, height, width, fixedSize, null);
    }
    
    /**
     * 
     *
     * @param str 
     * @param height 
     * @param width 
     * @param fixedSize 
     * @param aniObj Optional AnimatedObject to draw when the window is opened.
     * 
     * @return TextWindow
     */
    public TextWindow windowNoWait(String str, int height, int width, boolean fixedSize, AnimatedObject aniObj) {
        String[] lines;
        int numLines = 0;

        if (openWindow != null) {
            closeWindow();
        }

        if ((winWidth == -1) && (width == 0)) {
            width = WINWIDTH;
        }
        else if (winWidth != -1) {
            width = winWidth;
        }

        while (true) {
            // First make a formatting pass through the message, getting maximum line length and number of lines.
            lines = buildMessageLines(str, width);
            numLines = lines.length;

            if (fixedSize) {
                maxLength = width;
                if (height != 0) {
                    numLines = height;
                }
            }

            if (numLines > (WINBOT - WINTOP)) {
                str = format("Message too verbose:\n\n\"{0}...\"\n\nPress ESC to continue.", str.substring(0, 20));
            }
            else {
                break;
            }
        }

        int top = (winULRow == -1 ? WINTOP + (WINBOT - WINTOP - numLines) / 2 : winULRow) + state.pictureRow;
        int bottom = top + numLines - 1;
        int left = (winULCol == -1 ? (Defines.TEXTCOLS - maxLength) / 2 : winULCol);
        int right = left + maxLength;

        // Compute window size and position and put them into the appropriate bytes of the words.
        int windowDim = ((numLines * CHARHEIGHT + 2 * VMARGIN) << 8) | (maxLength * CHARWIDTH + 2 * HMARGIN);
        int windowPos = ((left * CHARWIDTH - HMARGIN) << 8) | (bottom * CHARHEIGHT + VMARGIN - 1);

        // Open the window, white with a red border and black text.
        return openWindow(new TextWindow(windowPos, windowDim, 15, 4, top, left, bottom, right, lines, 0, aniObj));
    }

    /**
     * Builds the array of message lines to be included in a message window. The str parameter
     * provides the message text, which may contain special % command references that need 
     * expanding first. After that substitution, the resulting message text is split up on to
     * lines that are no longer than the given width, words wrapping down a line if required.
     *
     * @param str The message text to expand references and split in to lines.
     * @param width The maximum width that a message line can be.
     * 
     * @return A String array containing the message lines.
     */
    private String[] buildMessageLines(String str, int width) {
        return buildMessageLines(str, width, 0);
    }
    
    /**
     * Builds the array of message lines to be included in a message window. The str parameter
     * provides the message text, which may contain special % command references that need 
     * expanding first. After that substitution, the resulting message text is split up on to
     * lines that are no longer than the given width, words wrapping down a line if required.
     *
     * @param str The message text to expand references and split in to lines.
     * @param width The maximum width that a message line can be.
     * @param startColumn Optional starting column value.
     * 
     * @return A String array containing the message lines.
     */
    private String[] buildMessageLines(String str, int width, int startColumn) {
        List<String> lines = new ArrayList<String>();

        maxLength = 0;

        if (str != null) {
            // Recursively expand/substitute references to other strings.
            String processedMessage = expandReferences(str);

            // Now that we have the processed message text, split it in to lines.
            StringBuilder currentLine = new StringBuilder();

            // Pad the first line with however many spaces required to begin at starting column.
            if (startColumn > 0) currentLine.append(padRightSpaces("", startColumn));
            
            for (int i = 0; i < processedMessage.length(); i++) {
                int addLines = (i == (processedMessage.length() - 1)) ? 1 : 0;

                if (processedMessage.charAt(i) == 0x0A) {
                    addLines++;
                }
                else {
                    // Add the character to the current line.
                    currentLine.append(processedMessage.charAt(i));

                    // If the current line has reached the width, then word wrap.
                    if (currentLine.length() >= width) {
                        i = wrapWord(currentLine, i);

                        addLines = 1;
                    }
                }

                while (addLines-- > 0) {
                    if ((startColumn > 0) && (lines.size() == 0)) {
                        // Remove the extra padding that we added at the start of first line.
                        currentLine.delete(0, startColumn);
                        startColumn = 0;
                    }

                    lines.add(currentLine.toString());

                    if (currentLine.length() > maxLength) {
                        maxLength = currentLine.length();
                    }

                    currentLine.setLength(0);
                }
            }
        }

        return (String[])lines.toArray(new String[0]);
    }

    /**
     * Winds back the given StringBuilder to the last word separate (i.e. space) and adjusts the
     * pos index value so that the word that overlapped the max line length is wrapped to the
     * next line.
     *
     * @param str 
     * 
     * @return The new position. 
     */
    private int wrapWord(StringBuilder str, int pos) {
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) == ' ') {
                pos -= (str.length() - i - 1);
                str.delete(i, str.length());
                return pos;
            }
        }
        return pos;
    }

    /**
     * Scans the given string from the given position for a consecutive sequence of digits. When
     * the end is reached, the string of digits is converted in to numeric form and returned. Any
     * characters before the given position, and after the end of the sequence of digits, is 
     * ignored.
     *
     * @param str 
     * @param startPos 
     * 
     * @return An array containing the number in the first slot and new position in the second.
     */
    private int[] numberFromString(String str, int pos) {
        int startPos = pos;
        while ((pos < str.length()) && (str.charAt(pos) >= '0') && (str.charAt(pos) <= '9')) pos++;
        int number = Integer.parseInt(str.substring(startPos, pos--));
        return new int[] { number, pos };
    }

    /**
     * Expands the special commands that reference other types of text, such as 
     * object names, words, other messages, etc.
     * 
     * Messages are strings of fewer than 255 characters which may contain 
     * the following special commands:
     * 
     *   \         Take the next character(except '\n' below) literally
     *   \n        Begin a new line
     *   %wn       Include word number n from the parsed line (1 &lt; = n &lt;= 255)
     *   %sn       Include user defined string number n (0 &lt;= n &lt;= 255)
     *   %mn       Include message number n from this room (0 &lt;= n &lt;= 255)
     *   %gn       Include global message number n from room 0 (0 &lt;= n &lt;= 255)
     *   %vn|m     Print the value of var #n. If the optional '|m' is present, print in a field of width m with leading zeros.
     *   %on       Print the name of the object whose number is in var number n.
     *   
     *
     * @param str The string to expand the references of.
     * 
     * @return 
     */
    private String expandReferences(String str) {
        StringBuilder output = new StringBuilder();

        // Iterate over each character in the message string looking for % codes.
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == escapeChar) {
                // The '\' character escapes the next character (e.g. \%)
                output.append(str.charAt(++i));
            }
            else if (str.charAt(i) == '%') {
                int num, width;
                int[] numPos;

                i++;

                switch (str.charAt(i++)) {
                    case 'v':
                        numPos = numberFromString(str, i);
                        num = numPos[0];
                        i = numPos[1];
                        if ((i < (str.length() - 1)) && (str.charAt(i + 1) == '|')) {
                            i += 2;
                            numPos = numberFromString(str, i);
                            width = numPos[0];
                            i = numPos[1];
                            output.append(padLeftZeros(Integer.toString(state.vars[num]), width));
                        }
                        else {
                            output.append(state.vars[num]);
                        }
                        break;

                    case 'm':
                        numPos = numberFromString(str, i);
                        num = numPos[0];
                        i = numPos[1];
                        output.append(state.logics[state.currentLogNum].messages.get(num));
                        break;

                    case 'g':
                        numPos = numberFromString(str, i);
                        num = numPos[0];
                        i = numPos[1];
                        output.append(state.logics[0].messages.get(num));
                        break;

                    case 'w':
                        numPos = numberFromString(str, i);
                        num = numPos[0];
                        i = numPos[1];
                        if (num <= state.recognisedWords.size()) {
                            output.append(state.recognisedWords.get(num - 1));
                        }
                        break;

                    case 's':
                        numPos = numberFromString(str, i);
                        num = numPos[0];
                        i = numPos[1];
                        output.append(state.strings[num]);
                        break;

                    case 'o':
                        numPos = numberFromString(str, i);
                        num = numPos[0];
                        i = numPos[1];
                        output.append(state.objects.objects.get(num).name);
                        break;

                    default: // ignore the second character.
                        break;
                }
            }
            else {
                // Default is simply to append the character.
                output.append(str.charAt(i));
            }
        }

        // Recursive part to make sure all % formatting codes are dealt with.
        if (output.toString().contains("%")) {
            return expandReferences(output.toString());
        }
        else {
            return output.toString();
        }
    }

    /**
     * Opens an AGI window on the game screen.
     *
     * @param textWindow
     *  
     * @return The same TextWindow with the BackPixels populated.</returns>
     */
    public TextWindow openWindow(TextWindow textWindow) {
        drawWindow(textWindow);

        // Remember this as the currently open window.
        this.openWindow = textWindow;

        return textWindow;
    }

    /**
     * 
     */
    public void drawWindow() {
        drawWindow(null);
    }
    
    /**
     * 
     *
     * @param textWindow 
     */
    public void drawWindow(TextWindow textWindow) {
        // Defaults to the currently open window if one was not provided by the caller.
        textWindow = (textWindow == null ? openWindow : textWindow);

        if (textWindow != null) {
            int backgroundRGB565 = EgaPalette.colours[textWindow.backgroundColour];
            int borderRGB565 = EgaPalette.colours[textWindow.borderColour];
            int startScreenPos = (textWindow.y() * 320) + textWindow.x();
            int screenYAdd = (320 - textWindow.width());

            // The first time that DrawWindow is invoke for a TextWindow, we store the back pixels.
            boolean storeBackPixels = (textWindow.backPixels == null);
            if (storeBackPixels) textWindow.backPixels = new int[textWindow.width() * textWindow.height()];

            // Draw a box in the background colour and store the pixels that were behind it.
            int backPixelsPos = 0;
            for (int y = 0, screenPos = startScreenPos; y < textWindow.height(); y++, screenPos += screenYAdd) {
                for (int x = 0; x < textWindow.width(); x++, screenPos++) {
                    // Store the pixel currently at this position (if applicable).
                    if (storeBackPixels) textWindow.backPixels[backPixelsPos++] = pixelData.getPixel(screenPos);

                    // Overwrite the pixel with the window's background colour.
                    pixelData.putPixel(screenPos, backgroundRGB565);
                }
            }

            // Draw a line just in a bit from the edge of the box in the border colour.
            for (int x = 0, screenPos = (startScreenPos + 320 + 2); x < (textWindow.width() - 4); x++, screenPos++) {
                pixelData.putPixel(screenPos, borderRGB565);
            }
            for (int x = 0, screenPos = (startScreenPos + (320 * (textWindow.height() - 2) + 2)); x < (textWindow.width() - 4); x++, screenPos++) {
                pixelData.putPixel(screenPos, borderRGB565);
            }
            for (int y = 1, screenPos = (startScreenPos + 640 + 2); y < (textWindow.height() - 2); y++, screenPos += 320) {
                pixelData.putPixel(screenPos, borderRGB565);
                pixelData.putPixel(screenPos + 1, borderRGB565);
                pixelData.putPixel(screenPos + (textWindow.width() - 6), borderRGB565);
                pixelData.putPixel(screenPos + (textWindow.width() - 5), borderRGB565);
            }

            // Draw the text lines (if applicable).
            if (textWindow.textLines != null) {
                // Draw the text black on white.
                for (int i = 0; i < textWindow.textLines.length; i++) {
                    drawString(pixelData, textWindow.textLines[i], (textWindow.left << 3), ((textWindow.top + i) << 3), textWindow.textColour, textWindow.backgroundColour);
                }
            }

            // Draw the embedded AnimatedObject (if applicable). Supports inventory item description windows.
            if (textWindow.aniObj != null) {
                textWindow.aniObj.draw();
                textWindow.aniObj.show(pixelData);
            }
        }
    }

    /**
     * Checks if there is a text window currently open.
     *
     * @return true if there is a window open; otherwise false.
     */
    public boolean isWindowOpen() {
        return (this.openWindow != null);
    }

    /**
     * Closes the current message window.
     */
    public void closeWindow() {
        closeWindow(true);
    }
    
    /**
     * Closes the current message window.
     * 
     * @param restoreBackPixels Whether to restore back pixels or not (defaults to true)
     */
    public void closeWindow(boolean restoreBackPixels) {
        if (this.openWindow != null) {
            if (restoreBackPixels) {
                int startScreenPos = (openWindow.y() * 320) + openWindow.x();
                int screenYAdd = (320 - openWindow.width());

                // Copy each of the stored background pixels back in to their original places.
                int backPixelsPos = 0;
                for (int y = 0, screenPos = startScreenPos; y < openWindow.height(); y++, screenPos += screenYAdd) {
                    for (int x = 0; x < openWindow.width(); x++, screenPos++) {
                        pixelData.putPixel(screenPos, openWindow.backPixels[backPixelsPos++]);
                    }
                }
            }

            // Clear the currently open window variable.
            this.openWindow = null;
        }
    }

    /**
     * The raw bitmap data for the original IBM PC/PCjr BIOS 8x8 font.
     */
    private static final int[] IBM_BIOS_FONT = {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x7E, 0x81, 0xA5, 0x81, 0xBD, 0x99, 0x81, 0x7E,
        0x7E, 0xFF, 0xDB, 0xFF, 0xC3, 0xE7, 0xFF, 0x7E,
        0x6C, 0xFE, 0xFE, 0xFE, 0x7C, 0x38, 0x10, 0x00,
        0x10, 0x38, 0x7C, 0xFE, 0x7C, 0x38, 0x10, 0x00,
        0x38, 0x7C, 0x38, 0xFE, 0xFE, 0x7C, 0x38, 0x7C,
        0x10, 0x10, 0x38, 0x7C, 0xFE, 0x7C, 0x38, 0x7C,
        0x00, 0x00, 0x18, 0x3C, 0x3C, 0x18, 0x00, 0x00,
        0xFF, 0xFF, 0xE7, 0xC3, 0xC3, 0xE7, 0xFF, 0xFF,
        0x00, 0x3C, 0x66, 0x42, 0x42, 0x66, 0x3C, 0x00,
        0xFF, 0xC3, 0x99, 0xBD, 0xBD, 0x99, 0xC3, 0xFF,
        0x0F, 0x07, 0x0F, 0x7D, 0xCC, 0xCC, 0xCC, 0x78,
        0x3C, 0x66, 0x66, 0x66, 0x3C, 0x18, 0x7E, 0x18,
        0x3F, 0x33, 0x3F, 0x30, 0x30, 0x70, 0xF0, 0xE0,
        0x7F, 0x63, 0x7F, 0x63, 0x63, 0x67, 0xE6, 0xC0,
        0x99, 0x5A, 0x3C, 0xE7, 0xE7, 0x3C, 0x5A, 0x99,
        0x80, 0xE0, 0xF8, 0xFE, 0xF8, 0xE0, 0x80, 0x00,
        0x02, 0x0E, 0x3E, 0xFE, 0x3E, 0x0E, 0x02, 0x00,
        0x18, 0x3C, 0x7E, 0x18, 0x18, 0x7E, 0x3C, 0x18,
        0x66, 0x66, 0x66, 0x66, 0x66, 0x00, 0x66, 0x00,
        0x7F, 0xDB, 0xDB, 0x7B, 0x1B, 0x1B, 0x1B, 0x00,
        0x3E, 0x63, 0x38, 0x6C, 0x6C, 0x38, 0xCC, 0x78,
        0x00, 0x00, 0x00, 0x00, 0x7E, 0x7E, 0x7E, 0x00,
        0x18, 0x3C, 0x7E, 0x18, 0x7E, 0x3C, 0x18, 0xFF,
        0x18, 0x3C, 0x7E, 0x18, 0x18, 0x18, 0x18, 0x00,
        0x18, 0x18, 0x18, 0x18, 0x7E, 0x3C, 0x18, 0x00,
        0x00, 0x18, 0x0C, 0xFE, 0x0C, 0x18, 0x00, 0x00,
        0x00, 0x30, 0x60, 0xFE, 0x60, 0x30, 0x00, 0x00,
        0x00, 0x00, 0xC0, 0xC0, 0xC0, 0xFE, 0x00, 0x00,
        0x00, 0x24, 0x66, 0xFF, 0x66, 0x24, 0x00, 0x00,
        0x00, 0x18, 0x3C, 0x7E, 0xFF, 0xFF, 0x00, 0x00,
        0x00, 0xFF, 0xFF, 0x7E, 0x3C, 0x18, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x30, 0x78, 0x78, 0x30, 0x30, 0x00, 0x30, 0x00,
        0x6C, 0x6C, 0x6C, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x6C, 0x6C, 0xFE, 0x6C, 0xFE, 0x6C, 0x6C, 0x00,
        0x30, 0x7C, 0xC0, 0x78, 0x0C, 0xF8, 0x30, 0x00,
        0x00, 0xC6, 0xCC, 0x18, 0x30, 0x66, 0xC6, 0x00,
        0x38, 0x6C, 0x38, 0x76, 0xDC, 0xCC, 0x76, 0x00,
        0x60, 0x60, 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x18, 0x30, 0x60, 0x60, 0x60, 0x30, 0x18, 0x00,
        0x60, 0x30, 0x18, 0x18, 0x18, 0x30, 0x60, 0x00,
        0x00, 0x66, 0x3C, 0xFF, 0x3C, 0x66, 0x00, 0x00,
        0x00, 0x30, 0x30, 0xFC, 0x30, 0x30, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x30, 0x30, 0x60,
        0x00, 0x00, 0x00, 0xFC, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x30, 0x30, 0x00,
        0x06, 0x0C, 0x18, 0x30, 0x60, 0xC0, 0x80, 0x00,
        0x7C, 0xC6, 0xCE, 0xDE, 0xF6, 0xE6, 0x7C, 0x00,
        0x30, 0x70, 0x30, 0x30, 0x30, 0x30, 0xFC, 0x00,
        0x78, 0xCC, 0x0C, 0x38, 0x60, 0xCC, 0xFC, 0x00,
        0x78, 0xCC, 0x0C, 0x38, 0x0C, 0xCC, 0x78, 0x00,
        0x1C, 0x3C, 0x6C, 0xCC, 0xFE, 0x0C, 0x1E, 0x00,
        0xFC, 0xC0, 0xF8, 0x0C, 0x0C, 0xCC, 0x78, 0x00,
        0x38, 0x60, 0xC0, 0xF8, 0xCC, 0xCC, 0x78, 0x00,
        0xFC, 0xCC, 0x0C, 0x18, 0x30, 0x30, 0x30, 0x00,
        0x78, 0xCC, 0xCC, 0x78, 0xCC, 0xCC, 0x78, 0x00,
        0x78, 0xCC, 0xCC, 0x7C, 0x0C, 0x18, 0x70, 0x00,
        0x00, 0x30, 0x30, 0x00, 0x00, 0x30, 0x30, 0x00,
        0x00, 0x30, 0x30, 0x00, 0x00, 0x30, 0x30, 0x60,
        0x18, 0x30, 0x60, 0xC0, 0x60, 0x30, 0x18, 0x00,
        0x00, 0x00, 0xFC, 0x00, 0x00, 0xFC, 0x00, 0x00,
        0x60, 0x30, 0x18, 0x0C, 0x18, 0x30, 0x60, 0x00,
        0x78, 0xCC, 0x0C, 0x18, 0x30, 0x00, 0x30, 0x00,
        0x7C, 0xC6, 0xDE, 0xDE, 0xDE, 0xC0, 0x78, 0x00,
        0x30, 0x78, 0xCC, 0xCC, 0xFC, 0xCC, 0xCC, 0x00,
        0xFC, 0x66, 0x66, 0x7C, 0x66, 0x66, 0xFC, 0x00,
        0x3C, 0x66, 0xC0, 0xC0, 0xC0, 0x66, 0x3C, 0x00,
        0xF8, 0x6C, 0x66, 0x66, 0x66, 0x6C, 0xF8, 0x00,
        0xFE, 0x62, 0x68, 0x78, 0x68, 0x62, 0xFE, 0x00,
        0xFE, 0x62, 0x68, 0x78, 0x68, 0x60, 0xF0, 0x00,
        0x3C, 0x66, 0xC0, 0xC0, 0xCE, 0x66, 0x3E, 0x00,
        0xCC, 0xCC, 0xCC, 0xFC, 0xCC, 0xCC, 0xCC, 0x00,
        0x78, 0x30, 0x30, 0x30, 0x30, 0x30, 0x78, 0x00,
        0x1E, 0x0C, 0x0C, 0x0C, 0xCC, 0xCC, 0x78, 0x00,
        0xE6, 0x66, 0x6C, 0x78, 0x6C, 0x66, 0xE6, 0x00,
        0xF0, 0x60, 0x60, 0x60, 0x62, 0x66, 0xFE, 0x00,
        0xC6, 0xEE, 0xFE, 0xFE, 0xD6, 0xC6, 0xC6, 0x00,
        0xC6, 0xE6, 0xF6, 0xDE, 0xCE, 0xC6, 0xC6, 0x00,
        0x38, 0x6C, 0xC6, 0xC6, 0xC6, 0x6C, 0x38, 0x00,
        0xFC, 0x66, 0x66, 0x7C, 0x60, 0x60, 0xF0, 0x00,
        0x78, 0xCC, 0xCC, 0xCC, 0xDC, 0x78, 0x1C, 0x00,
        0xFC, 0x66, 0x66, 0x7C, 0x6C, 0x66, 0xE6, 0x00,
        0x78, 0xCC, 0xE0, 0x70, 0x1C, 0xCC, 0x78, 0x00,
        0xFC, 0xB4, 0x30, 0x30, 0x30, 0x30, 0x78, 0x00,
        0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0xFC, 0x00,
        0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0x78, 0x30, 0x00,
        0xC6, 0xC6, 0xC6, 0xD6, 0xFE, 0xEE, 0xC6, 0x00,
        0xC6, 0xC6, 0x6C, 0x38, 0x38, 0x6C, 0xC6, 0x00,
        0xCC, 0xCC, 0xCC, 0x78, 0x30, 0x30, 0x78, 0x00,
        0xFE, 0xC6, 0x8C, 0x18, 0x32, 0x66, 0xFE, 0x00,
        0x78, 0x60, 0x60, 0x60, 0x60, 0x60, 0x78, 0x00,
        0xC0, 0x60, 0x30, 0x18, 0x0C, 0x06, 0x02, 0x00,
        0x78, 0x18, 0x18, 0x18, 0x18, 0x18, 0x78, 0x00,
        0x10, 0x38, 0x6C, 0xC6, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF,
        0x30, 0x30, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x78, 0x0C, 0x7C, 0xCC, 0x76, 0x00,
        0xE0, 0x60, 0x60, 0x7C, 0x66, 0x66, 0xDC, 0x00,
        0x00, 0x00, 0x78, 0xCC, 0xC0, 0xCC, 0x78, 0x00,
        0x1C, 0x0C, 0x0C, 0x7C, 0xCC, 0xCC, 0x76, 0x00,
        0x00, 0x00, 0x78, 0xCC, 0xFC, 0xC0, 0x78, 0x00,
        0x38, 0x6C, 0x60, 0xF0, 0x60, 0x60, 0xF0, 0x00,
        0x00, 0x00, 0x76, 0xCC, 0xCC, 0x7C, 0x0C, 0xF8,
        0xE0, 0x60, 0x6C, 0x76, 0x66, 0x66, 0xE6, 0x00,
        0x30, 0x00, 0x70, 0x30, 0x30, 0x30, 0x78, 0x00,
        0x0C, 0x00, 0x0C, 0x0C, 0x0C, 0xCC, 0xCC, 0x78,
        0xE0, 0x60, 0x66, 0x6C, 0x78, 0x6C, 0xE6, 0x00,
        0x70, 0x30, 0x30, 0x30, 0x30, 0x30, 0x78, 0x00,
        0x00, 0x00, 0xCC, 0xFE, 0xFE, 0xD6, 0xC6, 0x00,
        0x00, 0x00, 0xF8, 0xCC, 0xCC, 0xCC, 0xCC, 0x00,
        0x00, 0x00, 0x78, 0xCC, 0xCC, 0xCC, 0x78, 0x00,
        0x00, 0x00, 0xDC, 0x66, 0x66, 0x7C, 0x60, 0xF0,
        0x00, 0x00, 0x76, 0xCC, 0xCC, 0x7C, 0x0C, 0x1E,
        0x00, 0x00, 0xDC, 0x76, 0x66, 0x60, 0xF0, 0x00,
        0x00, 0x00, 0x7C, 0xC0, 0x78, 0x0C, 0xF8, 0x00,
        0x10, 0x30, 0x7C, 0x30, 0x30, 0x34, 0x18, 0x00,
        0x00, 0x00, 0xCC, 0xCC, 0xCC, 0xCC, 0x76, 0x00,
        0x00, 0x00, 0xCC, 0xCC, 0xCC, 0x78, 0x30, 0x00,
        0x00, 0x00, 0xC6, 0xD6, 0xFE, 0xFE, 0x6C, 0x00,
        0x00, 0x00, 0xC6, 0x6C, 0x38, 0x6C, 0xC6, 0x00,
        0x00, 0x00, 0xCC, 0xCC, 0xCC, 0x7C, 0x0C, 0xF8,
        0x00, 0x00, 0xFC, 0x98, 0x30, 0x64, 0xFC, 0x00,
        0x1C, 0x30, 0x30, 0xE0, 0x30, 0x30, 0x1C, 0x00,
        0x18, 0x18, 0x18, 0x00, 0x18, 0x18, 0x18, 0x00,
        0xE0, 0x30, 0x30, 0x1C, 0x30, 0x30, 0xE0, 0x00,
        0x76, 0xDC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x10, 0x38, 0x6C, 0xC6, 0xC6, 0xFE, 0x00,
        0x78, 0xCC, 0xC0, 0xCC, 0x78, 0x18, 0x0C, 0x78,
        0x00, 0xCC, 0x00, 0xCC, 0xCC, 0xCC, 0x7E, 0x00,
        0x1C, 0x00, 0x78, 0xCC, 0xFC, 0xC0, 0x78, 0x00,
        0x7E, 0xC3, 0x3C, 0x06, 0x3E, 0x66, 0x3F, 0x00,
        0xCC, 0x00, 0x78, 0x0C, 0x7C, 0xCC, 0x7E, 0x00,
        0xE0, 0x00, 0x78, 0x0C, 0x7C, 0xCC, 0x7E, 0x00,
        0x30, 0x30, 0x78, 0x0C, 0x7C, 0xCC, 0x7E, 0x00,
        0x00, 0x00, 0x78, 0xC0, 0xC0, 0x78, 0x0C, 0x38,
        0x7E, 0xC3, 0x3C, 0x66, 0x7E, 0x60, 0x3C, 0x00,
        0xCC, 0x00, 0x78, 0xCC, 0xFC, 0xC0, 0x78, 0x00,
        0xE0, 0x00, 0x78, 0xCC, 0xFC, 0xC0, 0x78, 0x00,
        0xCC, 0x00, 0x70, 0x30, 0x30, 0x30, 0x78, 0x00,
        0x7C, 0xC6, 0x38, 0x18, 0x18, 0x18, 0x3C, 0x00,
        0xE0, 0x00, 0x70, 0x30, 0x30, 0x30, 0x78, 0x00,
        0xC6, 0x38, 0x6C, 0xC6, 0xFE, 0xC6, 0xC6, 0x00,
        0x30, 0x30, 0x00, 0x78, 0xCC, 0xFC, 0xCC, 0x00,
        0x1C, 0x00, 0xFC, 0x60, 0x78, 0x60, 0xFC, 0x00,
        0x00, 0x00, 0x7F, 0x0C, 0x7F, 0xCC, 0x7F, 0x00,
        0x3E, 0x6C, 0xCC, 0xFE, 0xCC, 0xCC, 0xCE, 0x00,
        0x78, 0xCC, 0x00, 0x78, 0xCC, 0xCC, 0x78, 0x00,
        0x00, 0xCC, 0x00, 0x78, 0xCC, 0xCC, 0x78, 0x00,
        0x00, 0xE0, 0x00, 0x78, 0xCC, 0xCC, 0x78, 0x00,
        0x78, 0xCC, 0x00, 0xCC, 0xCC, 0xCC, 0x7E, 0x00,
        0x00, 0xE0, 0x00, 0xCC, 0xCC, 0xCC, 0x7E, 0x00,
        0x00, 0xCC, 0x00, 0xCC, 0xCC, 0x7C, 0x0C, 0xF8,
        0xC3, 0x18, 0x3C, 0x66, 0x66, 0x3C, 0x18, 0x00,
        0xCC, 0x00, 0xCC, 0xCC, 0xCC, 0xCC, 0x78, 0x00,
        0x18, 0x18, 0x7E, 0xC0, 0xC0, 0x7E, 0x18, 0x18,
        0x38, 0x6C, 0x64, 0xF0, 0x60, 0xE6, 0xFC, 0x00,
        0xCC, 0xCC, 0x78, 0xFC, 0x30, 0xFC, 0x30, 0x30,
        0xF8, 0xCC, 0xCC, 0xFA, 0xC6, 0xCF, 0xC6, 0xC7,
        0x0E, 0x1B, 0x18, 0x3C, 0x18, 0x18, 0xD8, 0x70,
        0x1C, 0x00, 0x78, 0x0C, 0x7C, 0xCC, 0x7E, 0x00,
        0x38, 0x00, 0x70, 0x30, 0x30, 0x30, 0x78, 0x00,
        0x00, 0x1C, 0x00, 0x78, 0xCC, 0xCC, 0x78, 0x00,
        0x00, 0x1C, 0x00, 0xCC, 0xCC, 0xCC, 0x7E, 0x00,
        0x00, 0xF8, 0x00, 0xF8, 0xCC, 0xCC, 0xCC, 0x00,
        0xFC, 0x00, 0xCC, 0xEC, 0xFC, 0xDC, 0xCC, 0x00,
        0x3C, 0x6C, 0x6C, 0x3E, 0x00, 0x7E, 0x00, 0x00,
        0x38, 0x6C, 0x6C, 0x38, 0x00, 0x7C, 0x00, 0x00,
        0x30, 0x00, 0x30, 0x60, 0xC0, 0xCC, 0x78, 0x00,
        0x00, 0x00, 0x00, 0xFC, 0xC0, 0xC0, 0x00, 0x00,
        0x00, 0x00, 0x00, 0xFC, 0x0C, 0x0C, 0x00, 0x00,
        0xC3, 0xC6, 0xCC, 0xDE, 0x33, 0x66, 0xCC, 0x0F,
        0xC3, 0xC6, 0xCC, 0xDB, 0x37, 0x6F, 0xCF, 0x03,
        0x18, 0x18, 0x00, 0x18, 0x18, 0x18, 0x18, 0x00,
        0x00, 0x33, 0x66, 0xCC, 0x66, 0x33, 0x00, 0x00,
        0x00, 0xCC, 0x66, 0x33, 0x66, 0xCC, 0x00, 0x00,
        0x22, 0x88, 0x22, 0x88, 0x22, 0x88, 0x22, 0x88,
        0x55, 0xAA, 0x55, 0xAA, 0x55, 0xAA, 0x55, 0xAA,
        0xDB, 0x77, 0xDB, 0xEE, 0xDB, 0x77, 0xDB, 0xEE,
        0x18, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18,
        0x18, 0x18, 0x18, 0x18, 0xF8, 0x18, 0x18, 0x18,
        0x18, 0x18, 0xF8, 0x18, 0xF8, 0x18, 0x18, 0x18,
        0x36, 0x36, 0x36, 0x36, 0xF6, 0x36, 0x36, 0x36,
        0x00, 0x00, 0x00, 0x00, 0xFE, 0x36, 0x36, 0x36,
        0x00, 0x00, 0xF8, 0x18, 0xF8, 0x18, 0x18, 0x18,
        0x36, 0x36, 0xF6, 0x06, 0xF6, 0x36, 0x36, 0x36,
        0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36,
        0x00, 0x00, 0xFE, 0x06, 0xF6, 0x36, 0x36, 0x36,
        0x36, 0x36, 0xF6, 0x06, 0xFE, 0x00, 0x00, 0x00,
        0x36, 0x36, 0x36, 0x36, 0xFE, 0x00, 0x00, 0x00,
        0x18, 0x18, 0xF8, 0x18, 0xF8, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0xF8, 0x18, 0x18, 0x18,
        0x18, 0x18, 0x18, 0x18, 0x1F, 0x00, 0x00, 0x00,
        0x18, 0x18, 0x18, 0x18, 0xFF, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0xFF, 0x18, 0x18, 0x18,
        0x18, 0x18, 0x18, 0x18, 0x1F, 0x18, 0x18, 0x18,
        0x00, 0x00, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00,
        0x18, 0x18, 0x18, 0x18, 0xFF, 0x18, 0x18, 0x18,
        0x18, 0x18, 0x1F, 0x18, 0x1F, 0x18, 0x18, 0x18,
        0x36, 0x36, 0x36, 0x36, 0x37, 0x36, 0x36, 0x36,
        0x36, 0x36, 0x37, 0x30, 0x3F, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x3F, 0x30, 0x37, 0x36, 0x36, 0x36,
        0x36, 0x36, 0xF7, 0x00, 0xFF, 0x00, 0x00, 0x00,
        0x00, 0x00, 0xFF, 0x00, 0xF7, 0x36, 0x36, 0x36,
        0x36, 0x36, 0x37, 0x30, 0x37, 0x36, 0x36, 0x36,
        0x00, 0x00, 0xFF, 0x00, 0xFF, 0x00, 0x00, 0x00,
        0x36, 0x36, 0xF7, 0x00, 0xF7, 0x36, 0x36, 0x36,
        0x18, 0x18, 0xFF, 0x00, 0xFF, 0x00, 0x00, 0x00,
        0x36, 0x36, 0x36, 0x36, 0xFF, 0x00, 0x00, 0x00,
        0x00, 0x00, 0xFF, 0x00, 0xFF, 0x18, 0x18, 0x18,
        0x00, 0x00, 0x00, 0x00, 0xFF, 0x36, 0x36, 0x36,
        0x36, 0x36, 0x36, 0x36, 0x3F, 0x00, 0x00, 0x00,
        0x18, 0x18, 0x1F, 0x18, 0x1F, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x1F, 0x18, 0x1F, 0x18, 0x18, 0x18,
        0x00, 0x00, 0x00, 0x00, 0x3F, 0x36, 0x36, 0x36,
        0x36, 0x36, 0x36, 0x36, 0xFF, 0x36, 0x36, 0x36,
        0x18, 0x18, 0xFF, 0x18, 0xFF, 0x18, 0x18, 0x18,
        0x18, 0x18, 0x18, 0x18, 0xF8, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x1F, 0x18, 0x18, 0x18,
        0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
        0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF,
        0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xF0,
        0x0F, 0x0F, 0x0F, 0x0F, 0x0F, 0x0F, 0x0F, 0x0F,
        0xFF, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x76, 0xDC, 0xC8, 0xDC, 0x76, 0x00,
        0x00, 0x78, 0xCC, 0xF8, 0xCC, 0xF8, 0xC0, 0xC0,
        0x00, 0xFC, 0xCC, 0xC0, 0xC0, 0xC0, 0xC0, 0x00,
        0x00, 0xFE, 0x6C, 0x6C, 0x6C, 0x6C, 0x6C, 0x00,
        0xFC, 0xCC, 0x60, 0x30, 0x60, 0xCC, 0xFC, 0x00,
        0x00, 0x00, 0x7E, 0xD8, 0xD8, 0xD8, 0x70, 0x00,
        0x00, 0x66, 0x66, 0x66, 0x66, 0x7C, 0x60, 0xC0,
        0x00, 0x76, 0xDC, 0x18, 0x18, 0x18, 0x18, 0x00,
        0xFC, 0x30, 0x78, 0xCC, 0xCC, 0x78, 0x30, 0xFC,
        0x38, 0x6C, 0xC6, 0xFE, 0xC6, 0x6C, 0x38, 0x00,
        0x38, 0x6C, 0xC6, 0xC6, 0x6C, 0x6C, 0xEE, 0x00,
        0x1C, 0x30, 0x18, 0x7C, 0xCC, 0xCC, 0x78, 0x00,
        0x00, 0x00, 0x7E, 0xDB, 0xDB, 0x7E, 0x00, 0x00,
        0x06, 0x0C, 0x7E, 0xDB, 0xDB, 0x7E, 0x60, 0xC0,
        0x38, 0x60, 0xC0, 0xF8, 0xC0, 0x60, 0x38, 0x00,
        0x78, 0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0xCC, 0x00,
        0x00, 0xFC, 0x00, 0xFC, 0x00, 0xFC, 0x00, 0x00,
        0x30, 0x30, 0xFC, 0x30, 0x30, 0x00, 0xFC, 0x00,
        0x60, 0x30, 0x18, 0x30, 0x60, 0x00, 0xFC, 0x00,
        0x18, 0x30, 0x60, 0x30, 0x18, 0x00, 0xFC, 0x00,
        0x0E, 0x1B, 0x1B, 0x18, 0x18, 0x18, 0x18, 0x18,
        0x18, 0x18, 0x18, 0x18, 0x18, 0xD8, 0xD8, 0x70,
        0x30, 0x30, 0x00, 0xFC, 0x00, 0x30, 0x30, 0x00,
        0x00, 0x76, 0xDC, 0x00, 0x76, 0xDC, 0x00, 0x00,
        0x38, 0x6C, 0x6C, 0x38, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x18, 0x18, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x18, 0x00, 0x00, 0x00,
        0x0F, 0x0C, 0x0C, 0x0C, 0xEC, 0x6C, 0x3C, 0x1C,
        0x78, 0x6C, 0x6C, 0x6C, 0x6C, 0x00, 0x00, 0x00,
        0x70, 0x18, 0x30, 0x60, 0x78, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x3C, 0x3C, 0x3C, 0x3C, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
}
