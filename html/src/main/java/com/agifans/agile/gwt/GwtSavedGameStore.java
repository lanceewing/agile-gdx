package com.agifans.agile.gwt;

import java.nio.charset.StandardCharsets;

import com.agifans.agile.SavedGameStore;
import com.agifans.agile.SavedGames.SavedGame;
import com.agifans.agile.util.StringUtils;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

/**
 * The GWT/HTML5 implementation of the SavedGameStore. Although web browsers have
 * JavaScript APIs these days for working with the real file system, those APIs involve
 * dialogs, permissions, and security checks that are better to avoid for the purposes
 * of saving games from a web site. There is a much better alternative available 
 * though, which is the Origin Private File System (OPFS), which is a new storage 
 * mechanism that is specific to the website, is not visible on the real file system,
 * but acts very much like a real file system. It also happens to be much faster than
 * other web browser storage mechanism, such as local storage and IndexedDB. It is 
 * therefore a no-brainer really. We could support exporting them in the future but
 * for now it seems nice to use the OPFS mechanism. GWT doesn't support this directly
 * though, so we'll need to use native JS methods. Another big advantage is that it 
 * is usable in a web worker, which is where our saved game class runs. If we were to
 * rely on the real file system, we'd need the UI thread to handle the file dialogs,
 * but this isn't required with the origin private file system. One big complications
 * is that most of the OPFS API is asynchronous (the same is true of IndexedDB). An
 * asynchronous API will not work within the confines of an AGI interpreter, since the
 * interpreter is literally blocked on the saved game functions, so the saved game
 * functions cannot (or should not) return before they've completed their file access.
 * Luckily there is a way to get hold of a synchronous file handle, which is what we
 * may need to do, but we'll need to do it ahead of time and keep a reference to handles
 * for all existing files open and ready, since it only becomes synchronous once we 
 * have the synchronous handle.
 */
public class GwtSavedGameStore implements SavedGameStore {
    
    private OPFSSavedGames opfsSavedGames;
    
    /**
     * Constructor for GwtSavedGameStore.
     */
    public GwtSavedGameStore() {
    }
    
    /**
     * Initialises the underlying OPFS synchronous access handles for the saved
     * game files for the given AGI game ID. This is handled via an initialise 
     * method since we can't perform this step until we know what the game ID 
     * will be, which is determined by the set.game.id command.
     * 
     * @param gameId The Game ID to initialise the saved game files for.
     */
    public void initialise(String gameId) {
        // This init call will perform the asynchronous set up of the OPFS to
        // be used for saving games. As this is called before the Interpreter is
        // instantiated, and within a web worker message prior to the first "Tick",
        // then it should be ample time for the OPFS setup to complete.
        opfsSavedGames = OPFSSavedGames.newOPFSSavedGames();
        opfsSavedGames.init(gameId);
    }
    
    @Override
    public boolean createFolderForGame(String gameId) {
        // Nothing to do, since we do this before the interpreter starts up, i.e.
        // in the initialise method above.
        return true;
    }

    @Override
    public String getFolderNameForGame(String gameId) {
        // OPFS is a virtual file system, so doesn't really have a defined file
        // separator. We'll use the DOS standard for display purposes.
        return "\\Saved Games\\" + gameId;
    }

    @Override
    public SavedGame getSavedGameByNumber(String gameId, int num) {
        SavedGame theGame = new SavedGame();
        theGame.num = num;

        // Build full path to the saved game of this number for this game ID.
        theGame.fileName = StringUtils.format("{0}\\{1}SG.{2}", 
                getFolderNameForGame(gameId), gameId, num);

        // Read the data from the OPFS saved game file of the given number.
        Int8Array savedGameDataInt8Array = opfsSavedGames.readSavedGameData(num);
        
        // Convert to byte array.
        byte[] savedGameData = new byte[savedGameDataInt8Array.byteLength()];
        for (int index=0; index<savedGameDataInt8Array.byteLength(); index++) {
            savedGameData[index] = savedGameDataInt8Array.get(index);
        }
        
        theGame.savedGameData = savedGameData;
        
        if (savedGameData.length > 0) {
            theGame.fileTime = opfsSavedGames.getSavedGameTimestamp(num);
            
            // 0 - 30(31 bytes) SAVED GAME DESCRIPTION.
            int textEnd = 0;
            while (theGame.savedGameData[textEnd] != 0) textEnd++;
            String savedGameDescription = new String(theGame.savedGameData, 0, textEnd, StandardCharsets.ISO_8859_1);

            // 33 - 39(7 bytes) Game ID("SQ2", "KQ3", "LLLLL", etc.), NUL padded.
            textEnd = 33;
            while ((theGame.savedGameData[textEnd] != 0) && ((textEnd - 33) < 7)) textEnd++;
            String gameIdFromFile = new String(theGame.savedGameData, 33, textEnd - 33, StandardCharsets.ISO_8859_1);

            // If the saved Game ID  doesn't match the current, don't use  this game.
            if (!gameIdFromFile.equals(gameId)) {
                theGame.description = "";
                theGame.exists = false;
                return theGame;
            }

            // If we get this far, there is a valid saved game with this number for this game.
            theGame.description = savedGameDescription;
            theGame.exists = true;
        }
        else {
            // An empty file means that the saved game doesn't exist yet. We create
            // all the files up front though, so that the synchronous acces handles
            // are immediately available. This is why we see empty files and interpret
            // these as files that don't yet exist.
            theGame.exists = false;
            theGame.description = "";
        }
        
        return theGame;
    }

    @Override
    public boolean saveGame(SavedGame savedGame, byte[] savedGameData, int length) {
        // Convert the Java byte array into an Int8Array.
        ArrayBuffer buffer = TypedArrays.createArrayBuffer(length);
        Int8Array savedGameDataInt8Array = TypedArrays.createInt8Array(buffer);
        
        // We don't use the Int8Array set method since we only copy up to given length.
        for (int index=0; index<length; index++) {
            savedGameDataInt8Array.set(index, savedGameData[index]);
        }
        
        // Then write the Int8Array into the OPFS saved game file of the given number.
        opfsSavedGames.writeSavedGameData(savedGame.num, savedGameDataInt8Array);
        
        return true;
    }
}
