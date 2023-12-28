package com.agifans.agile.gwt;

import com.agifans.agile.SavedGameStore;
import com.agifans.agile.SavedGames.SavedGame;

public class GwtSavedGameStore implements SavedGameStore {

    @Override
    public boolean createFolderForGame(String gameId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getFolderNameForGame(String gameId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SavedGame getSavedGameByNumber(String gameId, int num) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean saveGame(SavedGame savedGame, byte[] savedGameData, int length) {
        // TODO Auto-generated method stub
        return false;
    }

}
