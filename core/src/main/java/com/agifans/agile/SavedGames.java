package com.agifans.agile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map.Entry;

import com.agifans.agile.AnimatedObject.CycleType;
import com.agifans.agile.AnimatedObject.MotionType;
import com.agifans.agile.ScriptBuffer.ScriptBufferEvent;
import com.agifans.agile.ScriptBuffer.ScriptBufferEventType;
import com.agifans.agile.TextGraphics.TextWindow;
import com.agifans.agile.util.StringUtils;
import com.badlogic.gdx.Input.Keys;

/**
 * A class or saving and restoring saved games.
 */
public class SavedGames {
    
    private static final int SAVENAME_LEN = 30;
    private static final int NUM_GAMES = 12;
    private static final int GAME_INDENT = 3;
    private static final char POINTER_CHAR = (char)26;
    private static final char ERASE_CHAR = (char)32;

    // Keeps track of whether it is the first time a save/restore is happening in simple mode.
    private boolean firstTime = true;

    // Messages for the various window dialogs that are shown as part of the Save / Restore functionality.
    private String simpleFirstMsg = "Use the arrow keys to move\n     the pointer to your name.\nThen press ENTER\n";
    private String simpleSelectMsg = "   Sorry, this disk is full.\nPosition pointer and press ENTER\n    to overwrite a saved game\nor press ESC and try again \n    with another disk\n";
    private String selectSaveMsg = "Use the arrow keys to select the slot in which you wish to save the game. Press ENTER to save in the slot, ESC to not save a game.";
    private String selectRestoreMsg = "Use the arrow keys to select the game which you wish to restore. Press ENTER to restore the game, ESC to not restore a game.";
    private String newDescriptMsg = "How would you like to describe this saved game?\n\n";
    private String noGamesMsg = "There are no games to\nrestore in\n\n{0}\n\nPress ENTER to continue.";

    // Data type for storing data about a single saved game file.
    class SavedGame {
        public int num;
        public boolean exists;
        public String fileName;
        public long fileTime;
        public String description;
        public byte[] savedGameData;
    }
    
    /**
     * The GameState class holds all of the data and state for the Game currently 
     * being run by the interpreter.
     */
    private GameState state;

    /**
     * Holds the data and state for the user input, i.e. keyboard and mouse input.
     */
    private UserInput userInput;

    /**
     * Provides methods for drawing text on to the AGI screen.
     */
    private TextGraphics textGraphics;

    /**
     * The pixels array for the AGI screen on which the background Picture and 
     * AnimatedObjects will be drawn to.
     */
    private short[] pixels;
    
    /**
     * Constructor for SavedGames.
     * 
     * @param state
     * @param userInput
     * @param textGraphics
     * @param pixels
     */
    public SavedGames(GameState state, UserInput userInput, TextGraphics textGraphics, short[] pixels) {
        this.state = state;
        this.userInput = userInput;
        this.textGraphics = textGraphics;
        this.pixels = pixels;
    }

    /**
     * Chooses a saved game to either save to or restore from. The choice is either automatic
     * such as in the case of simple save, or by the user.
     *
     * @param function 's' for save, 'r' for restore.
     * 
     * @return 
     */
    private SavedGame chooseGame(char function) {
        SavedGame[] game = new SavedGame[NUM_GAMES];
        int gameNum, numGames, mostRecentGame = 0;
        long mostRecentTime = 0;
        boolean simpleSave = (state.simpleName.length() > 0);

        try {
            // Create saved game directory for this game if it doesn't yet exist.
            Files.createDirectories(Paths.get(getSavePath()));
        } catch (IOException ioe) {
            // TODO: Handle this exception.
        }

        // Look for the game files and get their data and meta data.
        if (function == 's') {
            // We're saving a game.
            for (gameNum = 0; gameNum < NUM_GAMES; gameNum++) {
                game[gameNum] = getGameByNumber(gameNum + 1);

                if (game[gameNum].exists && (game[gameNum].fileTime > mostRecentTime)) {
                    mostRecentTime = game[gameNum].fileTime;
                    mostRecentGame = gameNum;
                }
            }

            numGames = NUM_GAMES;
        }
        else {
            // We're restoring a game.
            for (gameNum = numGames = 0; gameNum < NUM_GAMES; gameNum++) {
                game[numGames] = getGameByNumber(gameNum + 1);
                
                if (game[numGames].exists) {
                    if (game[numGames].fileTime > mostRecentTime) {
                        mostRecentTime = game[numGames].fileTime;
                        mostRecentGame = numGames;
                    }

                    // Count how many saved games we currently have.
                    numGames++;
                }
            }

            if (numGames == 0) {
                if (!simpleSave) {
                    // For normal save, if there are no games to display, tell the user so.
                    textGraphics.windowPrint(StringUtils.format(noGamesMsg, getSavePath().replace("\\", "\\\\")));
                }

                // If there are no games to restore, exit at this point.
                return null;
            }
        }

        if (simpleSave && !firstTime) {
            // See if we have a slot for the current simple name value.
            for (gameNum = 0; gameNum < NUM_GAMES; gameNum++) {
                if (game[gameNum].description.equals(state.simpleName)) {
                    return (game[gameNum]);
                }
            }

            if (function == 's') {
                // For simple save, we automatically find an empty slot for new saved game.
                for (gameNum = 0; gameNum < NUM_GAMES; gameNum++) {
                    if ((game[gameNum].description == null) || (game[gameNum].description.equals(""))) {
                        // Description is automatically set to the SimpleName value if it is set.
                        game[gameNum].description = state.simpleName;
                        return (game[gameNum]);
                    }
                }
            }

            // If none available, fall thru to window.

            // We shouldn't be able to get to this point in restore mode, but just in case, return null.
            if (function == 'r') return null;
        }

        // Compute the height of the window desired and put it up
        int descriptTop = 5;
        int height = numGames + descriptTop;
        TextWindow textWin = textGraphics.windowNoWait(simpleSave ? (firstTime ? simpleFirstMsg : simpleSelectMsg) :
            (function == 's') ? selectSaveMsg : selectRestoreMsg,
            height, SAVENAME_LEN + GAME_INDENT + 1, true);

        descriptTop += textWin.top;
        firstTime = false;

        // Print the game descriptions within the open window..
        for (gameNum = 0; gameNum < numGames; gameNum++) {
            textGraphics.drawString(this.pixels, StringUtils.format(" - {0}", game[gameNum].description),
                textWin.left * 8, (descriptTop + gameNum) * 8, 0, 15);
        }

        // Put up the pointer, defaulting to most recently saved game, and then let the user start 
        // scrolling around with it to make a choice.
        gameNum = mostRecentGame;
        writePointer(textWin.left, descriptTop + gameNum);

        while (true) {
            switch (userInput.waitForKey()) {
                case (UserInput.ASCII | Character.ENTER):
                    if (simpleSave && (function == 'r')) {
                        // If this is a restore in simple save mode, it must be the first one, in which
                        // case we remember the selection in the SimpleName var so that it automatically 
                        // restores next time the user restores.
                        state.simpleName = game[gameNum].description;
                    }
                    if (!simpleSave && (function == 's')) {
                        // If this is a save in normal save mode, then we ask the user to confirm/enter 
                        // the description for the save game.
                        if ((game[gameNum].description = getWindowStr(newDescriptMsg, game[gameNum].description)) == null) {
                            // If they have pressed ESC, we return null to indicate not to continue.
                            return null;
                        }
                    }
                    textGraphics.closeWindow();
                    return (game[gameNum]);

                case (UserInput.ASCII | Character.ESC):
                    textGraphics.closeWindow();
                    return null;

                case Keys.UP:
                    erasePointer(textWin.left, descriptTop + gameNum);
                    gameNum = (gameNum == 0) ? numGames - 1 : gameNum - 1;
                    writePointer(textWin.left, descriptTop + gameNum);
                    break;

                case Keys.DOWN:
                    erasePointer(textWin.left, descriptTop + gameNum);
                    gameNum = (gameNum == numGames - 1) ? 0 : gameNum + 1;
                    writePointer(textWin.left, descriptTop + gameNum);
                    break;
            }
        }
    }

    /**
     * 
     * @param num 
     * 
     * @return
     */ 
    private SavedGame getGameByNumber(int num) {
        SavedGame theGame = new SavedGame();
        theGame.num = num;

        // Build full path to the saved game of this number for this game ID.
        theGame.fileName = StringUtils.format("{0}\\{1}SG.{2}", getSavePath(), state.gameId, num);

        File savedGameFile = new File(theGame.fileName);
        theGame.savedGameData = new byte[(int)savedGameFile.length()];

        try (FileInputStream fis = new FileInputStream(savedGameFile)) {
            int bytesRead = fis.read(theGame.savedGameData);
            if (bytesRead != savedGameFile.length()) {
                theGame.description = "";
                theGame.exists = false;
                return theGame;
            }
        }
        catch (FileNotFoundException fnfe) {
            // There is no saved game file of this name, so return false.
            theGame.description = "";
            theGame.exists = false;
            return theGame;
        }
        catch (Exception e) {
            // Something unexpected happened. Bad file I guess. Return false.
            theGame.description = "";
            theGame.exists = false;
            return theGame;
        }

        // Get last modified time as an epoch time, i.e. seconds since start of
        // 1970 (which I guess must have been when the big bang was).
        theGame.fileTime = ((new File(theGame.fileName)).lastModified() / 1000);

        // 0 - 30(31 bytes) SAVED GAME DESCRIPTION.
        int textEnd = 0;
        while (theGame.savedGameData[textEnd] != 0) textEnd++;
        String savedGameDescription = new String(theGame.savedGameData, 0, textEnd, Charset.forName("Cp437"));

        // 33 - 39(7 bytes) Game ID("SQ2", "KQ3", "LLLLL", etc.), NUL padded.
        textEnd = 33;
        while ((theGame.savedGameData[textEnd] != 0) && ((textEnd - 33) < 7)) textEnd++;
        String gameId = new String(theGame.savedGameData, 33, textEnd - 33, Charset.forName("Cp437"));

        // If the saved Game ID  doesn't match the current, don't use  this game.
        if (!gameId.equals(state.gameId)) {
            theGame.description = "";
            theGame.exists = false;
            return theGame;
        }

        // If we get this far, there is a valid saved game with this number for this game.
        theGame.description = savedGameDescription;
        theGame.exists = true;
        return theGame;
    }

    /**
     * Displays the pointer character at the specified screen position.
     * 
     * @param col 
     * @param row 
     */
    private void writePointer(int col, int row) {
        textGraphics.drawChar(this.pixels, (byte)POINTER_CHAR, col * 8, row * 8, 0, 15);
    }

    /**
     * Erases the pointer character from the specified screen position.
     * 
     * @param col 
     * @param row 
     */
    private void erasePointer(int col, int row) {
        textGraphics.drawChar(this.pixels, (byte)ERASE_CHAR, col * 8, row * 8, 0, 15);
    }

    /**
     * Gets a String from the user by opening a window dialog.
     * 
     * @param msg
     * 
     * @return The entered text.
     */
    private String getWindowStr(String msg) {
        return getWindowStr(msg, "");
    }
    
    /**
     * Gets a String from the user by opening a window dialog.
     * 
     * @param msg 
     * @param str 
     * 
     * @return The entered text.
     */
    private String getWindowStr(String msg, String str) {
        // Open a new window with the message text displayed. 
        TextWindow textWin = textGraphics.windowNoWait(msg, 0, SAVENAME_LEN+1, true);

        // Clear the input row to black on top of the window.
        textGraphics.clearRect(textWin.bottom, textWin.left, textWin.bottom, textWin.right - 1, 0);

        // Get the line of text from the user.
        String line = textGraphics.getLine(SAVENAME_LEN, (byte)textWin.bottom, (byte)textWin.left, str, 15, 0);

        textGraphics.closeWindow();

        return line;
    }

    /**
     * Gets the full path of the folder to use for reading and writing saved games.
     * 
     * @return The full path of the folder to use for reading and writing saved games.
     */
    private String getSavePath() {
        // TODO: Will need an alternative approach when GWT is supported.
        StringBuilder savedGamesPath = new StringBuilder();
        savedGamesPath.append(System.getProperty("user.home"));
        savedGamesPath.append(System.getProperty("file.separator"));
        savedGamesPath.append("Saved Games");
        savedGamesPath.append(System.getProperty("file.separator"));
        savedGamesPath.append(state.gameId);
        return savedGamesPath.toString();
    }

    /**
     * Returns the length of the save variables part of a saved game.
     * 
     * @param version The AGI interpreter version string.
     * 
     * @return The length of the save variables part of a saved game
     */
    private int getSaveVariablesLength(String version) {
        switch (version) {
            case "2.089":
            case "2.272":
            case "2.277":
                return 0x03DB;

            case "2.411":
            case "2.425":
            case "2.426":
            case "2.435":
            case "2.439":
            case "2.440":
                return 0x05DF;

            case "3.002.102":
            case "3.002.107":
                // TODO: Not yet sure what the additional 3 bytes are used for.
                return 0x05E4;

            case "3.002.149":
                // This difference between 3.002.107 and 3.002.149 is that the latter has only 12 strings (12x40=480=0x1E0)
                return 0x0404;

            // Default covers all the 2.9XX versions, 3.002.086 and 3.002.098.
            default:
                return 0x05E1;
        }
    }

    /**
     * Returns the number of strings for the given AGI version.
     * 
     * @param version The AGI version to return the number of strings for.
     * 
     * @return The number of strings for the given AGI version.
     */
    private int getNumberOfStrings(String version) {
        switch (version) {
            case "2.089":
            case "2.272":
            case "2.277":
            case "3.002.149":
                return 12;
            // Most versions have 24 strings, as defined in the Defines constant.
            default:
                return Defines.NUMSTRINGS;
        }
    }

    /**
     * Returns the number of controllers for the given AGI version.
     * 
     * @param version The AGI version to return the number of controllers for.
     * 
     * @return The number of controllers for the given AGI version.
     */
    private int getNumberOfControllers(String version) {
        switch (version) {
            case "2.089":
            case "2.272":
            case "2.277":
                return 40;
            // Most versions have a max of 50 controllers, as defined in the Defines constant.
            default:
                return Defines.NUMCONTROL;
        }
    }

    /**
     * Used to encrypt/decrypt the OBJECT section of the saved game file for AGI V3 games. Can't
     * reuse the Objects class to do the crypting, as the saved game encrypts it from a different
     * starting index, so the output is incompatible.
     * 
     * @param data The byte array to crypt part of.
     * @param start The start index to start crypting from.
     * @param end The end index (exclusive) to crypt to.
     */
    private void crypt(byte[] data, int start, int end) {
        for (int i=0, j=start; j<end;) data[j++] ^= (byte)"Avis Durgan".charAt(i++ % 11);
    }

    /**
     * Saves the GameState of the Interpreter to a saved game file.
     */
    public void saveGameState() {
        boolean simpleSave = (state.simpleName.length() > 0);
        SavedGame savedGame = null;

        // Get the saved game file to save.
        if ((savedGame = chooseGame('s')) == null) return;

        // If it is Simple Save mode then we skip asking them if they want to save.
        if (!simpleSave) {
            // Otherwise we prompt the user to confirm.
            String msg = StringUtils.format(
                "About to save the game\ndescribed as:\n\n{0}\n\nin file:\n{1}\n\n{2}",
                savedGame.description, savedGame.fileName.replace("\\", "\\\\"),
                "Press ENTER to continue.\nPress ESC to cancel.");
            textGraphics.windowNoWait(msg, 0, 35, false);
            boolean abort = (userInput.waitAcceptAbort() == UserInput.ABORT);
            textGraphics.closeWindow();
            if (abort) return;
        }

        // No saved game will ever be as big as 20000, but we put that as a theoretical lid
        // on the size based on rough calculations with all parts set to maximum size. We'll
        // only write the bytes that use when created the file.
        byte[] savedGameData = new byte[20000];
        int pos = 0;

        // 0 - 30(31 bytes) SAVED GAME DESCRIPTION.
        for (byte b : savedGame.description.getBytes(Charset.forName("Cp437"))) {
            savedGameData[pos++] = b;
        }
        
        // FIRST PIECE: SAVE VARIABLES
        // [0] 31 - 32(2 bytes) Length of save variables piece. Length depends on AGI interpreter version.
        int saveVarsLength = getSaveVariablesLength(state.version);
        int aniObjsOffset = 33 + saveVarsLength;
        savedGameData[31] = (byte)(saveVarsLength & 0xFF);
        savedGameData[32] = (byte)((saveVarsLength >> 8) & 0xFF);

        // [2] 33 - 39(7 bytes) Game ID("SQ2", "KQ3", "LLLLL", etc.), NUL padded.
        pos = 33;
        for (byte b : state.gameId.getBytes(Charset.forName("Cp437"))) {
            savedGameData[pos++] = b;
        }
        
        // [9] 40 - 295(256 bytes) Variables, 1 variable per byte
        for (int i = 0; i < 256; i++) savedGameData[40 + i] = (byte)state.vars[i];

        // [265] 296 - 327(32 bytes) Flags, 8 flags per byte
        pos = 296;
        for (int i = 0; i < 256; i+=8) {
            savedGameData[pos++] = (byte)(
                (state.flags[i + 0] ? 0x80 : 0x00) | (state.flags[i + 1] ? 0x40 : 0x00) |
                (state.flags[i + 2] ? 0x20 : 0x00) | (state.flags[i + 3] ? 0x10 : 0x00) | 
                (state.flags[i + 4] ? 0x08 : 0x00) | (state.flags[i + 5] ? 0x04 : 0x00) | 
                (state.flags[i + 6] ? 0x02 : 0x00) | (state.flags[i + 7] ? 0x01 : 0x00));
        }

        // [297] 328 - 331(4 bytes) Clock ticks since game started. 1 clock tick == 50ms.
        int saveGameTicks = (int)(state.totalTicks / 3);
        savedGameData[328] = (byte)(saveGameTicks & 0xFF);
        savedGameData[329] = (byte)((saveGameTicks >> 8) & 0xFF);
        savedGameData[330] = (byte)((saveGameTicks >> 16) & 0xFF);
        savedGameData[331] = (byte)((saveGameTicks >> 24) & 0xFF);

        // [301] 332 - 333(2 bytes) Horizon
        savedGameData[332] = (byte)(state.horizon & 0xFF);
        savedGameData[333] = (byte)((state.horizon >> 8) & 0xFF);

        // [303] 334 - 335(2 bytes) Key Dir
        // TODO: Not entirely sure what this is for, so not currently saving this.

        // Currently active block.
        // [305] 336 - 337(2 bytes) Upper left X position for active block.
        savedGameData[336] = (byte)(state.blockUpperLeftX & 0xFF);
        savedGameData[337] = (byte)((state.blockUpperLeftX >> 8) & 0xFF);
        // [307] 338 - 339(2 bytes) Upper Left Y position for active block.
        savedGameData[338] = (byte)(state.blockUpperLeftY & 0xFF);
        savedGameData[339] = (byte)((state.blockUpperLeftY >> 8) & 0xFF);
        // [309] 340 - 341(2 bytes) Lower Right X position for active block.
        savedGameData[340] = (byte)(state.blockLowerRightX & 0xFF);
        savedGameData[341] = (byte)((state.blockLowerRightX >> 8) & 0xFF);
        // [311] 342 - 343(2 bytes) Lower Right Y position for active block.
        savedGameData[342] = (byte)(state.blockLowerRightY & 0xFF);
        savedGameData[343] = (byte)((state.blockLowerRightY >> 8) & 0xFF);

        // [313] 344 - 345(2 bytes) Player control (1) / Program control (0)
        savedGameData[344] = (byte)(state.userControl ? 1 : 0);
        // [315] 346 - 347(2 bytes) Current PICTURE number
        savedGameData[346] = (byte)state.currentPicture.index;
        // [317] 348 - 349(2 bytes) Blocking flag (1 = true, 0 = false)
        savedGameData[348] = (byte)(state.blocking ? 1 : 0);

        // [319] 350 - 351(2 bytes) Max drawn. Always set to 15. Maximum number of animated objects that can be drawn at a time. Set by old max.drawn command in AGI v2.001.
        savedGameData[350] = (byte)state.maxDrawn;
        // [321] 352 - 353(2 bytes) Script size. Set by script.size. Max number of script event items. Default is 50.
        savedGameData[352] = (byte)state.scriptBuffer.scriptSize;
        // [323] 354 - 355(2 bytes) Current number of script event entries.
        savedGameData[354] = (byte)state.scriptBuffer.scriptEntries();

        // [325] 356 - 555(200 or 160 bytes) ? Key to controller map (4 bytes each). Earlier versions had less entries.
        pos = 356;
        int keyMapSize = getNumberOfControllers(state.version);
        for (Entry<Integer, Integer> entry : state.keyToControllerMap.entrySet()) {
            if (entry.getKey() != 0) {
                int keyCode = userInput.reverseKeyCodeMap.get(entry.getKey());
                int controllerNum = entry.getValue();
                savedGameData[pos++] = (byte)(keyCode & 0xFF);
                savedGameData[pos++] = (byte)((keyCode >> 8) & 0xFF);
                savedGameData[pos++] = (byte)(controllerNum & 0xFF);
                savedGameData[pos++] = (byte)((controllerNum >> 8) & 0xFF);
            }
        }

        int postKeyMapOffset = 356 + (keyMapSize << 2);

        // [525] 556 - 1515(480 or 960 bytes) 12 or 24 strings, each 40 bytes long. For 2.4XX to 2.9XX, it was 24 strings.
        int numOfStrings = getNumberOfStrings(state.version);
        for (int i = 0; i < numOfStrings; i++) {
            pos = postKeyMapOffset + (i * Defines.STRLENGTH);
            if ((state.strings[i] != null) && (state.strings[i].length() > 0)) {
                for (byte b : state.strings[i].getBytes(Charset.forName("Cp437"))) {
                    savedGameData[pos++] = b;
                }
            }
        }

        int postStringsOffset = postKeyMapOffset + (numOfStrings * Defines.STRLENGTH);

        // [1485] 1516(2 bytes) Foreground colour
        savedGameData[postStringsOffset + 0] = (byte)state.foregroundColour;

        // TODO: Need to fix the foreground and background colour storage.

        // [1487] 1518(2 bytes) Background colour
        //int backgroundColour = (savedGameData[postStringsOffset + 2] + (savedGameData[postStringsOffset + 3] << 8));
        // TODO: Interpreter doesn't yet properly handle AGI background colour.

        // [1489] 1520(2 bytes) Text Attribute value (combined foreground/background value)
        //int textAttribute = (savedGameData[postStringsOffset + 4] + (savedGameData[postStringsOffset + 5] << 8));

        // [1491] 1522(2 bytes) Accept input = 1, Prevent input = 0
        savedGameData[postStringsOffset + 6] = (byte)(state.acceptInput ? 1 : 0);

        // [1493] 1524(2 bytes) User input row on the screen
        savedGameData[postStringsOffset + 8] = (byte)state.inputLineRow;

        // [1495] 1526(2 bytes) Cursor character
        savedGameData[postStringsOffset + 10] = (byte)state.cursorCharacter;

        // [1497] 1528(2 bytes) Show status line = 1, Don't show status line = 0
        savedGameData[postStringsOffset + 12] = (byte)(state.showStatusLine ? 1 : 0);

        // [1499] 1530(2 bytes) Status line row on the screen
        savedGameData[postStringsOffset + 14] = (byte)state.statusLineRow;

        // [1501] 1532(2 bytes) Picture top row on the screen
        savedGameData[postStringsOffset + 16] = (byte)state.pictureRow;

        // [1503] 1534(2 bytes) Picture bottom row on the screen
        savedGameData[postStringsOffset + 18] = (byte)(state.pictureRow + 21);

        // [1505] 1536(2 bytes) Stores a pushed position within the script event list
        // Note: Depends on interpreter version. 2.4xx and below didn't have push.script/pop.script, so they didn't have this saved game field.
        if ((postStringsOffset + 20) < aniObjsOffset) {
            // The spec is 2 bytes, but as with the fields above, there shouldn't be more than 255.
            savedGameData[1536] = (byte)(state.scriptBuffer.savedScript);
        }

        // Some AGI V3 versions have 3 additional bytes at this point.
        // TODO: Work out what these 3 bytes are for and write them out here.

        // SECOND PIECE: ANIMATED OBJECT STATE
        // 1538 - 1539(2 bytes) Length of piece
        // Each ANIOBJ entry is 0x2B in length, i.e. 43 bytes.
        int aniObjectsLength = ((state.objects.numOfAnimatedObjects + 1) * 0x2B);
        savedGameData[aniObjsOffset + 0] = (byte)(aniObjectsLength & 0xFF);
        savedGameData[aniObjsOffset + 1] = (byte)((aniObjectsLength >> 8) & 0xFF);
        
        for (int i=0; i < (state.objects.numOfAnimatedObjects + 1); i++) {
            int aniObjOffset = aniObjsOffset + 2 + (i * 0x2B);
            AnimatedObject aniObj = state.animatedObjects[i];

            //UBYTE movefreq;     /* number of animation cycles between motion  */    e.g.   01
            savedGameData[aniObjOffset + 0] = (byte)aniObj.stepTime;
            //UBYTE moveclk;      /* number of cycles between moves of object   */    e.g.   01
            savedGameData[aniObjOffset + 1] = (byte)aniObj.stepTimeCount;
            //UBYTE num;          /* object number                              */    e.g.   00
            savedGameData[aniObjOffset + 2] = aniObj.objectNumber;
            //COORD x;            /* current x coordinate                       */    e.g.   6e 00 (0x006e = )
            savedGameData[aniObjOffset + 3] = (byte)(aniObj.x & 0xFF);
            savedGameData[aniObjOffset + 4] = (byte)((aniObj.x >> 8) & 0xFF);
            //COORD y;            /* current y coordinate                       */    e.g.   64 00 (0x0064 = )
            savedGameData[aniObjOffset + 5] = (byte)(aniObj.y & 0xFF);
            savedGameData[aniObjOffset + 6] = (byte)((aniObj.y >> 8) & 0xFF);
            //UBYTE view;         /* current view number                        */    e.g.   00
            savedGameData[aniObjOffset + 7] = (byte)aniObj.currentView;
            //VIEW* viewptr;      /* pointer to current view                    */    e.g.   17 6b (0x6b17 = ) IGNORE.
            //UBYTE loop;         /* current loop in view                       */    e.g.   00
            savedGameData[aniObjOffset + 10] = (byte)aniObj.currentLoop;
            //UBYTE loopcnt;      /* number of loops in view                    */    e.g.   04
            if (aniObj.view() != null) savedGameData[aniObjOffset + 11] = (byte)aniObj.numberOfLoops();
            //LOOP* loopptr;      /* pointer to current loop                    */    e.g.   24 6b (0x6b24 = ) IGNORE
            //UBYTE cel;          /* current cell in loop                       */    e.g.   00
            savedGameData[aniObjOffset + 14] = (byte)aniObj.currentCel;
            //UBYTE celcnt;       /* number of cells in current loop            */    e.g.   06
            if (aniObj.view() != null) savedGameData[aniObjOffset + 15] = (byte)aniObj.numberOfCels();
            //CEL* celptr;        /* pointer to current cell                    */    e.g.   31 6b (0x6b31 = ) IGNORE
            //CEL* prevcel;       /* pointer to previous cell                   */    e.g.   31 6b (0x6b31 = ) IGNORE
            //STRPTR save;        /* pointer to background save area            */    e.g.   2f 9c (0x9c2f = ) IGNORE
            //COORD prevx;        /* previous x coordinate                      */    e.g.   6e 00 (0x006e = )
            savedGameData[aniObjOffset + 22] = (byte)(aniObj.prevX & 0xFF);
            savedGameData[aniObjOffset + 23] = (byte)((aniObj.prevX >> 8) & 0xFF);
            //COORD prevy;        /* previous y coordinate                      */    e.g.   64 00 (0x0064 = )
            savedGameData[aniObjOffset + 24] = (byte)(aniObj.prevY & 0xFF);
            savedGameData[aniObjOffset + 25] = (byte)((aniObj.prevY >> 8) & 0xFF);
            //COORD xsize;        /* x dimension of current cell                */    e.g.   06 00 (0x0006 = )
            if (aniObj.view() != null) savedGameData[aniObjOffset + 26] = (byte)(aniObj.xSize() & 0xFF);
            if (aniObj.view() != null) savedGameData[aniObjOffset + 27] = (byte)((aniObj.xSize() >> 8) & 0xFF);
            //COORD ysize;        /* y dimension of current cell                */    e.g.   20 00 (0x0020 = )
            if (aniObj.view() != null) savedGameData[aniObjOffset + 28] = (byte)(aniObj.ySize() & 0xFF);
            if (aniObj.view() != null) savedGameData[aniObjOffset + 29] = (byte)((aniObj.ySize() >> 8) & 0xFF);
            //UBYTE stepsize;     /* distance object can move                   */    e.g.   01
            savedGameData[aniObjOffset + 30] = (byte)aniObj.stepSize;
            //UBYTE cyclfreq;     /* time interval between cells of object      */    e.g.   01
            savedGameData[aniObjOffset + 31] = (byte)aniObj.cycleTime;
            //UBYTE cycleclk;     /* counter for determining when object cycles */    e.g.   01
            savedGameData[aniObjOffset + 32] = (byte)aniObj.cycleTimeCount;
            //UBYTE dir;          /* object direction                           */    e.g.   00
            savedGameData[aniObjOffset + 33] = aniObj.direction;
            //UBYTE motion;       /* object motion type                         */    e.g.   00
            // #define    WANDER    1        /* random movement */
            // #define    FOLLOW    2        /* follow an object */
            // #define    MOVETO    3        /* move to a given coordinate */
            savedGameData[aniObjOffset + 34] = (byte)aniObj.motionType.ordinal();
            //UBYTE cycle;        /* cell cycling type                          */    e.g.   00
            // #define NORMAL    0        /* normal repetative cycling of object */
            // #define ENDLOOP    1        /* animate to end of loop and stop */
            // #define RVRSLOOP    2        /* reverse of ENDLOOP */
            // #define REVERSE    3        /* cycle continually in reverse */
            savedGameData[aniObjOffset + 35] = (byte)aniObj.cycleType.ordinal();
            //UBYTE pri;          /* priority of object                         */    e.g.   09
            savedGameData[aniObjOffset + 36] = aniObj.priority;

            //UWORD control;      /* object control flag (bit mapped)           */    e.g.   53 40 (0x4053 = )
            int controlBits =
                (aniObj.drawn ? 0x0001 : 0x00) |
                (aniObj.ignoreBlocks ? 0x0002 : 0x00) |
                (aniObj.fixedPriority ? 0x0004 : 0x00) |
                (aniObj.ignoreHorizon ? 0x0008 : 0x00) |
                (aniObj.update ? 0x0010 : 0x00) |
                (aniObj.cycle ? 0x0020 : 0x00) |
                (aniObj.animated ? 0x0040 : 0x00) |
                (aniObj.blocked ? 0x0080 : 0x00) |
                (aniObj.stayOnWater ? 0x0100 : 0x00) |
                (aniObj.ignoreObjects ? 0x0200 : 0x00) |
                (aniObj.repositioned ? 0x0400 : 0x00) |
                (aniObj.stayOnLand ? 0x0800 : 0x00) |
                (aniObj.noAdvance ? 0x1000 : 0x00) |
                (aniObj.fixedLoop ? 0x2000 : 0x00) |
                (aniObj.stopped ? 0x4000 : 0x00);
            savedGameData[aniObjOffset + 37] = (byte)(controlBits & 0xFF);
            savedGameData[aniObjOffset + 38] = (byte)((controlBits >> 8) & 0xFF);

            //UBYTE parms[4];     /* space for various motion parameters        */    e.g.   00 00 00 00
            savedGameData[aniObjOffset + 39] = (byte)aniObj.motionParam1;
            savedGameData[aniObjOffset + 40] = (byte)aniObj.motionParam2;
            savedGameData[aniObjOffset + 41] = (byte)aniObj.motionParam3;
            savedGameData[aniObjOffset + 42] = (byte)aniObj.motionParam4;
        }

        // THIRD PIECE: OBJECTS
        // Almost an exact copy of the OBJECT file, but with the 3 byte header removed, and room
        // numbers reflecting the current location of each object.
        byte[] objectData = state.objects.encode();
        int objectsOffset = aniObjsOffset + 2 + aniObjectsLength;
        int objectsLength = objectData.length - 3;
        savedGameData[objectsOffset + 0] = (byte)(objectsLength & 0xFF);
        savedGameData[objectsOffset + 1] = (byte)((objectsLength >> 8) & 0xFF);
        pos = objectsOffset + 2;
        if (state.isAGIV3()) {
            // AGI V3 games xor encrypt the data with Avis Durgan. Note that unlike the OBJECT 
            // file itself, the saved game OBJECT section crypts from index 3, since it does 
            // not output the 3 byte header, so starts the crypting after that.
            crypt(objectData, 3, objectData.length);
        }
        for (int i=3; i<objectData.length; i++) {
            savedGameData[pos++] = objectData[i];
        }

        // FOURTH PIECE: SCRIPT BUFFER EVENTS
        // A transcript of events leading to the current state in the current room.
        int scriptsOffset = objectsOffset + 2 + objectsLength;
        byte[] scriptEventData = state.scriptBuffer.encode();
        int scriptsLength = scriptEventData.length;
        savedGameData[scriptsOffset + 0] = (byte)(scriptsLength & 0xFF);
        savedGameData[scriptsOffset + 1] = (byte)((scriptsLength >> 8) & 0xFF);
        pos = scriptsOffset + 2;
        for (int i = 0; i < scriptEventData.length; i++) {
            savedGameData[pos++] = scriptEventData[i];
        }

        // FIFTH PIECE: SCAN OFFSETS
        int scanOffsetsOffset = scriptsOffset + 2 + scriptsLength;
        int loadedLogicCount = 0;
        // There is a scan offset for each loaded logic.
        for (ScriptBufferEvent e : state.scriptBuffer.events) if (e.type == ScriptBufferEventType.LOAD_LOGIC) loadedLogicCount++;
        // The scan offset data contains the offsets for loaded logics plus a 4 byte header, 4 bytes for logic 0, and 4 byte trailer.
        int scanOffsetsLength = (loadedLogicCount * 4) + 12;
        savedGameData[scanOffsetsOffset + 0] = (byte)(scanOffsetsLength & 0xFF);
        savedGameData[scanOffsetsOffset + 1] = (byte)((scanOffsetsLength >> 8) & 0xFF);
        pos = scanOffsetsOffset + 2;
        // The scan offsets start with 00 00 00 00.
        savedGameData[pos++] = 0;
        savedGameData[pos++] = 0;
        savedGameData[pos++] = 0;
        savedGameData[pos++] = 0;
        // And this is then always followed by an entry for Logic 0
        savedGameData[pos++] = 0;
        savedGameData[pos++] = 0;
        savedGameData[pos++] = (byte)(state.scanStart[0] & 0xFF);
        savedGameData[pos++] = (byte)((state.scanStart[0] >> 8) & 0xFF);
        // The scan offsets for the rest are stored in the order in which the logics were loaded.
        for (ScriptBufferEvent e : state.scriptBuffer.events) {
            if (e.type == ScriptBufferEventType.LOAD_LOGIC) {
                int logicNum = e.resourceNumber;
                int scanOffset = state.scanStart[logicNum];
                savedGameData[pos++] = (byte)(logicNum & 0xFF);
                savedGameData[pos++] = (byte)((logicNum >> 8) & 0xFF);
                savedGameData[pos++] = (byte)(scanOffset & 0xFF);
                savedGameData[pos++] = (byte)((scanOffset >> 8) & 0xFF);
            }
        }
        // The scan offset section ends with FF FF 00 00.
        savedGameData[pos++] = (byte)0xFF;
        savedGameData[pos++] = (byte)0xFF;
        savedGameData[pos++] = 0;
        savedGameData[pos++] = 0;

        // Write out the saved game data to the file.
        try {
            try (FileOutputStream outputStream = new FileOutputStream(savedGame.fileName)) {
                outputStream.write(savedGameData, 0, pos);
            }
        }
        catch (Exception e) {
            this.textGraphics.print("Error in saving game.\nPress ENTER to continue.");
        }
    }

    /**
     * Restores the GameState of the Interpreter from a saved game file.
     *
     * @return true if a game was restored; otherwise false
     */
    public boolean restoreGameState() {
        boolean simpleSave = (state.simpleName.length() > 0);
        SavedGame savedGame = null;

        // Get the saved game file to restore.
        if ((savedGame = chooseGame('r')) == null) return false;

        // If it is Simple Save mode then we skip asking them if they want to restore.
        if (!simpleSave) {
            // Otherwise we prompt the user to confirm.
            String msg = StringUtils.format(
                "About to restore the game\ndescribed as:\n\n{0}\n\nfrom file:\n{1}\n\n{2}",
                savedGame.description, savedGame.fileName.replace("\\", "\\\\"), 
                "Press ENTER to continue.\nPress ESC to cancel.");
            textGraphics.windowNoWait(msg, 0, 35, false);
            boolean abort = (userInput.waitAcceptAbort() == UserInput.ABORT);
            textGraphics.closeWindow();
            if (abort) return false;
        }

        byte[] savedGameData = savedGame.savedGameData;

        // 0 - 30(31 bytes) SAVED GAME DESCRIPTION.
        int textEnd = 0;
        while (savedGameData[textEnd] != 0) textEnd++;
        String savedGameDescription = new String(savedGameData, 0, textEnd, Charset.forName("Cp437"));

        // FIRST PIECE: SAVE VARIABLES
        // [0] 31 - 32(2 bytes) Length of save variables piece. Length depends on AGI interpreter version. [e.g. (0xE1 0x05) for some games, (0xDB 0x03) for some] 
        int saveVarsLength = (savedGameData[31] & 0xFF) + ((savedGameData[32] & 0xFF) << 8);
        int aniObjsOffset = 33 + saveVarsLength;

        // [2] 33 - 39(7 bytes) Game ID("SQ2", "KQ3", "LLLLL", etc.), NUL padded.
        textEnd = 33;
        while ((savedGameData[textEnd] != 0) && ((textEnd - 33) < 7)) textEnd++;
        String gameId = new String(savedGameData, 33, textEnd - 33, Charset.forName("Cp437"));
        if (!gameId.equals(state.gameId)) return false;

        // If we're sure that this saved game file is for this game, then continue.
        state.init();
        textGraphics.clearLines(0, 24, 0);

        // [9] 40 - 295(256 bytes) Variables, 1 variable per byte
        for (int i=0; i<256; i++) state.vars[i] = (savedGameData[40 + i] & 0xFF);

        // [265] 296 - 327(32 bytes) Flags, 8 flags per byte
        for (int i=0; i<256; i++) state.flags[i] = ((savedGameData[(i >> 3) + 296] & 0xFF) & (0x80 >> (i & 0x07))) > 0;

        // [297] 328 - 331(4 bytes) Clock ticks since game started. 1 clock tick == 50ms.
        state.totalTicks = ((savedGameData[328] & 0xFF) + ((savedGameData[329] & 0xFF) << 8) + ((savedGameData[330] & 0xFF) << 16) + ((savedGameData[331] & 0xFF) << 24)) * 3;

        // [301] 332 - 333(2 bytes) Horizon
        state.horizon = ((savedGameData[332] & 0xFF) + ((savedGameData[333] & 0xFF) << 8));

        // [303] 334 - 335(2 bytes) Key Dir
        // TODO: Not entirely sure what this is for.
        int keyDir = ((savedGameData[334] & 0xFF) + ((savedGameData[335] & 0xFF) << 8));

        // Currently active block.
        // [305] 336 - 337(2 bytes) Upper left X position for active block.
        state.blockUpperLeftX = (short)((savedGameData[336] & 0xFF) + ((savedGameData[337] & 0xFF) << 8));
        // [307] 338 - 339(2 bytes) Upper Left Y position for active block.
        state.blockUpperLeftY = (short)((savedGameData[338] & 0xFF) + ((savedGameData[339] & 0xFF) << 8));
        // [309] 340 - 341(2 bytes) Lower Right X position for active block.
        state.blockLowerRightX = (short)((savedGameData[340] & 0xFF) + ((savedGameData[341] & 0xFF) << 8));
        // [311] 342 - 343(2 bytes) Lower Right Y position for active block.
        state.blockLowerRightY = (short)((savedGameData[342] & 0xFF) + ((savedGameData[343] & 0xFF) << 8));

        // [313] 344 - 345(2 bytes) Player control (1) / Program control (0)
        state.userControl = ((savedGameData[344] & 0xFF) + ((savedGameData[345] & 0xFF) << 8)) == 1;
        // [315] 346 - 347(2 bytes) Current PICTURE number
        state.currentPicture = null; // Will be set via load.pic script entry later on.
        // [317] 348 - 349(2 bytes) Blocking flag (1 = true, 0 = false)
        state.blocking = ((savedGameData[348] & 0xFF) + ((savedGameData[349] & 0xFF) << 8)) == 1;

        // [319] 350 - 351(2 bytes) Max drawn. Always set to 15. Maximum number of animated objects that can be drawn at a time. Set by old max.drawn command in AGI v2.001.
        state.maxDrawn = ((savedGameData[350] & 0xFF) + ((savedGameData[351] & 0xFF) << 8));
        // [321] 352 - 353(2 bytes) Script size. Set by script.size. Max number of script event items. Default is 50.
        state.scriptBuffer.setScriptSize((savedGameData[352] & 0xFF) + ((savedGameData[353] & 0xFF) << 8));
        // [323] 354 - 355(2 bytes) Current number of script event entries.
        int scriptEntryCount = ((savedGameData[354] & 0xFF) + ((savedGameData[355] & 0xFF) << 8));

        // [325] 356 - 555(200 or 160 bytes) ? Key to controller map (4 bytes each)
        int keyMapSize = getNumberOfControllers(state.version);
        for (int i = 0; i < keyMapSize; i++) {
            int keyMapOffset = i << 2;
            int keyCode = ((savedGameData[356 + keyMapOffset] & 0xFF) + ((savedGameData[357 + keyMapOffset] & 0xFF) << 8));
            int controllerNum = ((savedGameData[358 + keyMapOffset] & 0xFF) + ((savedGameData[359 + keyMapOffset] & 0xFF) << 8));
            if (!((keyCode == 0) && (controllerNum == 0)) && userInput.keyCodeMap.containsKey(keyCode)) {
                int interKeyCode = userInput.keyCodeMap.get(keyCode);
                if (state.keyToControllerMap.containsKey(interKeyCode)) {
                    state.keyToControllerMap.remove(interKeyCode);
                }
                state.keyToControllerMap.put(userInput.keyCodeMap.get(keyCode), controllerNum);
            }
        }

        int postKeyMapOffset = 356 + (keyMapSize << 2);

        // [525] 556 - 1515(480 or 960 bytes) 12 or 24 strings, each 40 bytes long
        int numOfStrings = getNumberOfStrings(state.version);
        for (int i = 0; i < numOfStrings; i++) {
            int stringOffset = postKeyMapOffset + (i * Defines.STRLENGTH);
            textEnd = stringOffset;
            while (((savedGameData[textEnd] & 0xFF) != 0) && ((textEnd - stringOffset) < Defines.STRLENGTH)) textEnd++;
            state.strings[i] = new String(savedGameData, stringOffset, textEnd - stringOffset, Charset.forName("Cp437"));
        }

        int postStringsOffset = postKeyMapOffset + (numOfStrings * Defines.STRLENGTH);

        // [1485] 1516(2 bytes) Foreground colour
        state.foregroundColour = ((savedGameData[postStringsOffset + 0] & 0xFF) + ((savedGameData[postStringsOffset + 1] & 0xFF) << 8));

        // [1487] 1518(2 bytes) Background colour
        int backgroundColour = ((savedGameData[postStringsOffset + 2] & 0xFF) + ((savedGameData[postStringsOffset + 3] & 0xFF) << 8));
        // TODO: Interpreter doesn't yet properly handle AGI background colour.

        // [1489] 1520(2 bytes) Text Attribute value (combined foreground/background value)
        int textAttribute = ((savedGameData[postStringsOffset + 4] & 0xFF) + ((savedGameData[postStringsOffset + 5] & 0xFF) << 8));
        
        // [1491] 1522(2 bytes) Accept input = 1, Prevent input = 0
        state.acceptInput = ((savedGameData[postStringsOffset + 6] & 0xFF) + ((savedGameData[postStringsOffset + 7] & 0xFF) << 8)) == 1;

        // [1493] 1524(2 bytes) User input row on the screen
        state.inputLineRow = ((savedGameData[postStringsOffset + 8] & 0xFF) + ((savedGameData[postStringsOffset + 9] & 0xFF) << 8));

        // [1495] 1526(2 bytes) Cursor character
        state.cursorCharacter = (char)((savedGameData[postStringsOffset + 10] & 0xFF) + ((savedGameData[postStringsOffset + 11] & 0xFF) << 8));

        // [1497] 1528(2 bytes) Show status line = 1, Don't show status line = 0
        state.showStatusLine = ((savedGameData[postStringsOffset + 12] & 0xFF) + ((savedGameData[postStringsOffset + 13] & 0xFF) << 8)) == 1;

        // [1499] 1530(2 bytes) Status line row on the screen
        state.statusLineRow = ((savedGameData[postStringsOffset + 14] & 0xFF) + ((savedGameData[postStringsOffset + 15] & 0xFF) << 8));

        // [1501] 1532(2 bytes) Picture top row on the screen
        state.pictureRow = ((savedGameData[postStringsOffset + 16] & 0xFF) + ((savedGameData[postStringsOffset + 17] & 0xFF) << 8));

        // [1503] 1534(2 bytes) Picture bottom row on the screen
        // Note: Not needed by this intepreter.
        int picBottom = ((savedGameData[postStringsOffset + 18] & 0xFF) + ((savedGameData[postStringsOffset + 19] & 0xFF) << 8));

        if ((postStringsOffset + 20) < aniObjsOffset) {
            // [1505] 1536(2 bytes) Stores a pushed position within the script event list
            // Note: Depends on interpreter version. 2.4xx and below didn't have push.script/pop.script, so they didn't have this saved game field.
            state.scriptBuffer.savedScript = ((savedGameData[postStringsOffset + 20] & 0xFF) + ((savedGameData[postStringsOffset + 21] & 0xFF) << 8));
        }

        // SECOND PIECE: ANIMATED OBJECT STATE
        // 17 aniobjs = 0x02DB length, 18 aniobjs = 0x0306, 20 aniobjs = 0x035C, 21 aniobjs = 0x0387, 91 = 0x0F49] 2B, 2B, 2B, 2B, 2B
        // 1538 - 1539(2 bytes) Length of piece (ANIOBJ should divide evenly in to this length)
        int aniObjectsLength = ((savedGameData[aniObjsOffset + 0] & 0xFF) + ((savedGameData[aniObjsOffset + 1] & 0xFF) << 8));
        // Each ANIOBJ entry is 0x2B in length, i.e. 43 bytes.
        // 17 aniobjs = 0x02DB length, 18 aniobjs = 0x0306, 20 aniobjs = 0x035C, 21 aniobjs = 0x0387, 91 = 0x0F49] 2B, 2B, 2B, 2B, 2B
        int numOfAniObjs = (aniObjectsLength / 0x2B);

        for (int i = 0; i < numOfAniObjs; i++) {
            int aniObjOffset = aniObjsOffset + 2 + (i * 0x2B);
            AnimatedObject aniObj = state.animatedObjects[i];
            aniObj.reset();

            // Each ANIOBJ entry is 0x2B in length, i.e. 43 bytes.
            // Example: KQ1 - ego - starting position in room 1
            // 01 01 00 6e 00 64 00 00 17 6b 00 04 24 6b 00 06
            // 31 6b 31 6b 2f 9c 6e 00 64 00 06 00 20 00 01 01
            // 01 00 00 00 09 53 40 00 00 00 00
            
            //UBYTE movefreq;     /* number of animation cycles between motion  */    e.g.   01
            aniObj.stepTime = (savedGameData[aniObjOffset + 0] & 0xFF);
            //UBYTE moveclk;      /* number of cycles between moves of object   */    e.g.   01
            aniObj.stepTimeCount = (savedGameData[aniObjOffset + 1] & 0xFF);
            //UBYTE num;          /* object number                              */    e.g.   00
            aniObj.objectNumber = savedGameData[aniObjOffset + 2];
            //COORD x;            /* current x coordinate                       */    e.g.   6e 00 (0x006e = )
            aniObj.x = (short)((savedGameData[aniObjOffset + 3] & 0xFF) + ((savedGameData[aniObjOffset + 4] & 0xFF) << 8));
            //COORD y;            /* current y coordinate                       */    e.g.   64 00 (0x0064 = )
            aniObj.y = (short)((savedGameData[aniObjOffset + 5] & 0xFF) + ((savedGameData[aniObjOffset + 6] & 0xFF) << 8));
            //UBYTE view;         /* current view number                        */    e.g.   00
            aniObj.currentView = (savedGameData[aniObjOffset + 7] & 0xFF);
            //VIEW* viewptr;      /* pointer to current view                    */    e.g.   17 6b (0x6b17 = ) IGNORE.
            //UBYTE loop;         /* current loop in view                       */    e.g.   00
            aniObj.currentLoop = (savedGameData[aniObjOffset + 10] & 0xFF);
            //UBYTE loopcnt;      /* number of loops in view                    */    e.g.   04                IGNORE
            //LOOP* loopptr;      /* pointer to current loop                    */    e.g.   24 6b (0x6b24 = ) IGNORE
            //UBYTE cel;          /* current cell in loop                       */    e.g.   00
            aniObj.currentCel = (savedGameData[aniObjOffset + 14] & 0xFF);
            //UBYTE celcnt;       /* number of cells in current loop            */    e.g.   06                IGNORE
            //CEL* celptr;        /* pointer to current cell                    */    e.g.   31 6b (0x6b31 = ) IGNORE
            //CEL* prevcel;       /* pointer to previous cell                   */    e.g.   31 6b (0x6b31 = ) 
            if (aniObj.view() != null) aniObj.previousCel = aniObj.cel();
            //STRPTR save;        /* pointer to background save area            */    e.g.   2f 9c (0x9c2f = ) IGNORE
            //COORD prevx;        /* previous x coordinate                      */    e.g.   6e 00 (0x006e = )
            aniObj.prevX = (short)((savedGameData[aniObjOffset + 22] & 0xFF) + ((savedGameData[aniObjOffset + 23] & 0xFF) << 8));
            //COORD prevy;        /* previous y coordinate                      */    e.g.   64 00 (0x0064 = )
            aniObj.prevY = (short)((savedGameData[aniObjOffset + 24] & 0xFF) + ((savedGameData[aniObjOffset + 25] & 0xFF) << 8));
            //COORD xsize;        /* x dimension of current cell                */    e.g.   06 00 (0x0006 = ) IGNORE
            //COORD ysize;        /* y dimension of current cell                */    e.g.   20 00 (0x0020 = ) IGNORE
            //UBYTE stepsize;     /* distance object can move                   */    e.g.   01
            aniObj.stepSize = (savedGameData[aniObjOffset + 30] & 0xFF);
            //UBYTE cyclfreq;     /* time interval between cells of object      */    e.g.   01
            aniObj.cycleTime = (savedGameData[aniObjOffset + 31] & 0xFF);
            //UBYTE cycleclk;     /* counter for determining when object cycles */    e.g.   01
            aniObj.cycleTimeCount = (savedGameData[aniObjOffset + 32] & 0xFF);
            //UBYTE dir;          /* object direction                           */    e.g.   00
            aniObj.direction = savedGameData[aniObjOffset + 33];
            //UBYTE motion;       /* object motion type                         */    e.g.   00
            // #define    WANDER    1        /* random movement */
            // #define    FOLLOW    2        /* follow an object */
            // #define    MOVETO    3        /* move to a given coordinate */
            aniObj.motionType = MotionType.values()[savedGameData[aniObjOffset + 34]];
            //UBYTE cycle;        /* cell cycling type                          */    e.g.   00
            // #define NORMAL    0        /* normal repetative cycling of object */
            // #define ENDLOOP    1        /* animate to end of loop and stop */
            // #define RVRSLOOP    2        /* reverse of ENDLOOP */
            // #define REVERSE    3        /* cycle continually in reverse */
            aniObj.cycleType = CycleType.values()[savedGameData[aniObjOffset + 35]];
            //UBYTE pri;          /* priority of object                         */    e.g.   09
            aniObj.priority = savedGameData[aniObjOffset + 36];
            //UWORD control;      /* object control flag (bit mapped)           */    e.g.   53 40 (0x4053 = )
            int controlBits = ((savedGameData[aniObjOffset + 37] & 0xFF) + ((savedGameData[aniObjOffset + 38] & 0xFF) << 8));
            /* object control bits */
            // DRAWN     0x0001  /* 1 -> object is drawn on screen */
            aniObj.drawn = ((controlBits & 0x0001) > 0);
            // IGNRBLK   0x0002  /* 1 -> object ignores blocks */
            aniObj.ignoreBlocks = ((controlBits & 0x0002) > 0);
            // FIXEDPRI  0x0004  /* 1 -> object has fixed priority */
            aniObj.fixedPriority = ((controlBits & 0x0004) > 0);
            // IGNRHRZ   0x0008  /* 1 -> object ignores the horizon */
            aniObj.ignoreHorizon = ((controlBits & 0x0008) > 0);
            // UPDATE    0x0010  /* 1 -> update the object */
            aniObj.update = ((controlBits & 0x0010) > 0);
            // CYCLE     0x0020  /* 1 -> cycle the object */
            aniObj.cycle = ((controlBits & 0x0020) > 0);
            // ANIMATED  0x0040  /* 1 -> object can move */
            aniObj.animated = ((controlBits & 0x0040) > 0);
            // BLOCKED   0x0080  /* 1 -> object is blocked */
            aniObj.blocked = ((controlBits & 0x0080) > 0);
            // PRICTRL1  0x0100  /* 1 -> object must be on 'water' priority */
            aniObj.stayOnWater = ((controlBits & 0x0100) > 0);
            // IGNROBJ   0x0200  /* 1 -> object won't collide with objects */
            aniObj.ignoreObjects = ((controlBits & 0x0200) > 0);
            // REPOS     0x0400  /* 1 -> object being reposn'd in this cycle */
            aniObj.repositioned = ((controlBits & 0x0400) > 0);
            // PRICTRL2  0x0800  /* 1 -> object must not be entirely on water */
            aniObj.stayOnLand = ((controlBits & 0x0800) > 0);
            // NOADVANC  0x1000  /* 1 -> don't advance object's cel in this loop */
            aniObj.noAdvance = ((controlBits & 0x1000) > 0);
            // FIXEDLOOP 0x2000  /* 1 -> object's loop is fixed */
            aniObj.fixedLoop = ((controlBits & 0x2000) > 0);
            // STOPPED   0x4000  /* 1 -> object did not move during last animation cycle */
            aniObj.stopped = ((controlBits & 0x4000) > 0);
            //UBYTE parms[4];     /* space for various motion parameters        */    e.g.   00 00 00 00
            aniObj.motionParam1 = (short)(savedGameData[aniObjOffset + 39] & 0xFF);
            aniObj.motionParam2 = (short)(savedGameData[aniObjOffset + 40] & 0xFF);
            aniObj.motionParam3 = (short)(savedGameData[aniObjOffset + 41] & 0xFF);
            aniObj.motionParam4 = (short)(savedGameData[aniObjOffset + 42] & 0xFF);
            // If motion type is follow, then force a re-initialisation of the follow path.
            if (aniObj.motionType == MotionType.FOLLOW) aniObj.motionParam3 = -1;
        }

        // THIRD PIECE: OBJECTS
        // Almost an exact copy of the OBJECT file, but with the 3 byte header removed, and room
        // numbers reflecting the current location of each object.
        int objectsOffset = aniObjsOffset + 2 + aniObjectsLength;
        int objectsLength = (savedGameData[objectsOffset + 0] + (savedGameData[objectsOffset + 1] << 8));
        // The NumOfAnimatedObjects, as stored in OBJECT, should be 1 less than the number of animated object slots
        // (due to add.to.pic slot), otherwise this number increments by 1 on every save followed by restore.
        state.objects.numOfAnimatedObjects = (numOfAniObjs - 1);
        if (state.isAGIV3()) {
            // AGI V3 games xor encrypt the data with Avis Durgan.
            crypt(savedGameData, objectsOffset + 2, objectsOffset + objectsLength);
        }
        int numOfObjects = ((savedGameData[objectsOffset + 2] & 0xFF) + ((savedGameData[objectsOffset + 3] & 0xFF) << 8)) / 3;
        // Set the saved room number of each Object. 
        for (int objectNum = 0, roomPos = objectsOffset + 4; objectNum < numOfObjects; objectNum++, roomPos += 3) {
            state.objects.objects.get(objectNum).room = (savedGameData[roomPos] & 0xFF);
        }

        // FOURTH PIECE: SCRIPT BUFFER EVENTS
        // A transcript of events leading to the current state in the current room.
        int scriptsOffset = objectsOffset + 2 + objectsLength;
        int scriptsLength = ((savedGameData[scriptsOffset + 0] & 0xFF) + ((savedGameData[scriptsOffset + 1] & 0xFF) << 8));
        // Each script entry is two unsigned bytes long:
        // UBYTE action;
        // UBYTE who;
        //
        // Action byte is a code defined as follows:
        // S_LOADLOG       0
        // S_LOADVIEW      1
        // S_LOADPIC       2
        // S_LOADSND       3
        // S_DRAWPIC       4
        // S_ADDPIC        5
        // S_DSCRDPIC      6
        // S_DSCRDVIEW     7
        // S_OVERLAYPIC    8
        //
        // Example: 
        // c8 00 Length
        // 00 01 load.logic  0x01
        // 01 00 load.view   0x00
        // 00 66 load.logic  0x66
        // 01 4b load.view   0x4B
        // 01 57 load.view   0x57
        // 01 6e load.view   0x6e
        // 02 01 load.pic    0x01
        // 04 01 draw.pic    0x01
        // 06 01 discard.pic 0x01
        // 00 65 load.logic  0x65
        // 01 6b load.view   0x6B
        // 01 61 load.view   0x61
        // 01 5d load.view   0x5D
        // 01 46 load.view   0x46
        // 03 0d load.sound  0x0D
        // etc...
        state.scriptBuffer.initScript();
        for (int i = 0; i < scriptEntryCount; i++) {
            int scriptOffset = scriptsOffset + 2 + (i * 2);
            int action = (savedGameData[scriptOffset + 0] & 0xFF);
            ScriptBufferEventType eventType = ScriptBufferEventType.values()[action];
            int resourceNum = (savedGameData[scriptOffset + 1] & 0xFF);
            byte[] data = null;
            if (eventType == ScriptBufferEventType.ADD_TO_PIC) {
                // The add.to.pics are stored in the saved game file across 8 bytes, i.e. 4 separate script 
                // entries (that is also how the original AGI interpreter stored it in memory). 
                // What we do though is store these in an additional data array associated with
                // the script event since utilitising multiple event entries is a bit of a hack
                // really. I can understand why they did it though.
                data = new byte[] {
                    savedGameData[scriptOffset + 2], savedGameData[scriptOffset + 3], savedGameData[scriptOffset + 4],
                    savedGameData[scriptOffset + 5], savedGameData[scriptOffset + 6], savedGameData[scriptOffset + 7]
                };

                // Increase i to account for the fact that we've processed an additional 3 slots.
                i += 3;
            }
            state.scriptBuffer.restoreScript(eventType, resourceNum, data);
        }

        // FIFTH PIECE: SCAN OFFSETS
        // Note: Not every logic can set a scan offset, as there is a max of 30. But only
        // loaded logics can have this set and I'd imagine you'd run out of memory before 
        // loading that many logics at once. 
        int scanOffsetsOffset = scriptsOffset + 2 + scriptsLength;
        int scanOffsetsLength = ((savedGameData[scanOffsetsOffset + 0] & 0xFF) + ((savedGameData[scanOffsetsOffset + 1] & 0xFF) << 8));
        int numOfScanOffsets = (scanOffsetsLength / 4);
        // Each entry is 4 bytes long, made up of 2 16-bit words:
        // COUNT num;                                    /* logic number         */
        // COUNT ofs;                                    /* offset to scan start */ 
        //
        // Example:
        // 18 00 
        // 00 00 00 00  Start of list. Seems to always be 4 zeroes.
        // 00 00 00 00  Logic 0 - Offset 0
        // 01 00 00 00  Logic 1 - Offset 0
        // 66 00 00 00  Logic 102 - Offset 0
        // 65 00 00 00  Logic 101 - Offset 0
        // ff ff 00 00  End of list
        //
        // Quick Analysis of the above:
        // * Only logics that are current loaded are in the scan offset list, i.e. they're removed when the room changes.
        // * The order logics appear in this list is the order that they are loaded.
        // * Logics disappear from this list when they are unloaded (on new.room).
        // * The new.room command unloads all logics except for logic 0, so it never leaves this list.
        for (int i = 0; i < 256; i++) state.scanStart[i] = 0;
        for (int i = 1; i < numOfScanOffsets; i++) {
            int scanOffsetOffset = scanOffsetsOffset + 2 + (i * 4);
            int logicNumber = ((savedGameData[scanOffsetOffset + 0] & 0xFF) + ((savedGameData[scanOffsetOffset + 1] & 0xFF) << 8));
            if (logicNumber < 256) {
                state.scanStart[logicNumber] = ((savedGameData[scanOffsetOffset + 2] & 0xFF) + ((savedGameData[scanOffsetOffset + 3] & 0xFF) << 8));
            }
        }

        state.flags[Defines.RESTORE] = true;

        // Return true to say that we have successfully restored a saved game file.
        return true;
    }
}
