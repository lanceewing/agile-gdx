package com.agifans.agile.lwjgl3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.agifans.agile.SavedGameStore;
import com.agifans.agile.SavedGames.SavedGame;
import com.agifans.agile.util.StringUtils;

public class DesktopSavedGameStore implements SavedGameStore {

    @Override
    public boolean createFolderForGame(String gameId) {
        try {
            Files.createDirectories(Paths.get(getFolderNameForGame(gameId)));
            return true;
        } catch (Exception e) {
            // Failed to create the folder.
            return false;
        }
    }

    public String getFolderNameForGame(String gameId) {
        StringBuilder savedGamesPath = new StringBuilder();
        savedGamesPath.append(System.getProperty("user.home"));
        savedGamesPath.append(System.getProperty("file.separator"));
        savedGamesPath.append("Saved Games");
        savedGamesPath.append(System.getProperty("file.separator"));
        savedGamesPath.append(gameId);
        return savedGamesPath.toString();
    }

    @Override
    public SavedGame getSavedGameByNumber(String gameId, int num) {
        SavedGame theGame = new SavedGame();
        theGame.num = num;

        // Build full path to the saved game of this number for this game ID.
        theGame.fileName = StringUtils.format(
                "{0}{1}{2}SG.{3}",
                getFolderNameForGame(gameId), 
                System.getProperty("file.separator"),
                gameId, num);

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
        return theGame;
    }

    @Override
    public boolean saveGame(SavedGame savedGame, byte[] savedGameData, int length) {
        try {
            try (FileOutputStream outputStream = new FileOutputStream(savedGame.fileName)) {
                outputStream.write(savedGameData, 0, length);
            }
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
