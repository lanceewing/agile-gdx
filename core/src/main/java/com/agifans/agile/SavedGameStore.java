package com.agifans.agile;

import com.agifans.agile.SavedGames.SavedGame;

/**
 * An interface for saving and reading saved game files. This interface deals only
 * with the writing and reading of the raw data and does not know how to interpret
 * the game state data. That part is handled by the core part of AGILE and is not
 * platform dependent. Storage of files though is very much dependent on the platform.
 */
public interface SavedGameStore {

    /**
     * Creates a folder for the given AGI game ID (e.g. SQ2, KQ1, etc). Does nothing
     * if the folder already exists. "Folder" is intended to be a generic name in this
     * context and doesn't imply a DOS or Unix style file system. For Android and GWT,
     * "files" may be stored in a quite different way where it may instead be called
     * by a different name.
     * 
     * @param gameId The id of the AGI game to create the folder form.
     * 
     * @return true if the folder was successfully created.
     */
    public boolean createFolderForGame(String gameId);
    
    /**
     * Gets the full name of the folder. This is used by AGILE only for display purposes,
     * e.g. when it couldn't find any saved games to restore, in which case it tells the
     * player that it couldn't find any in the game's saved game "Folder".
     * 
     * @param gameId The id of the AGI game to get the full folder name for.
     * 
     * @return The full folder name for the given AGI game ID.
     */
    public String getFolderNameForGame(String gameId);
    
    /**
     * Attempts to get the saved game identified by the given AGI game ID and saved 
     * game number (saved games are numbered within each AGI game). If it couldn't 
     * find a saved game file then it still returns a SavedGame instead but the fields
     * within that object will indicate that it doesn't exist.
     * 
     * @param gameId The ID of the AGI game to get the saved game for.
     * @param num The number of the saved game to get.
     * 
     * @return SavedGame containing details of the saved game file requested.
     */
    public SavedGame getSavedGameByNumber(String gameId, int num);

    /**
     * Saves the given saved game data to the saved game file identified in the SavedGame object.
     * 
     * @param savedGame SavedGame object containing details about the saved game to save.
     * @param savedGameData The byte array containing the data to save to the saved game file.
     * @param length The number of bytes from the byte array to write out to the saved game file.
     * 
     * @return true if the saved game file was successfully written.
     */
    public boolean saveGame(SavedGame savedGame, byte[] savedGameData, int length);
}
