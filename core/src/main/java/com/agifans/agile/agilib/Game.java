package com.agifans.agile.agilib;

import java.io.IOException;
import java.util.Map;

import com.agifans.agile.agilib.AgileLogicProvider.AgileLogicWrapper;
import com.agifans.agile.agilib.AgileSoundProvider.AgileSoundWrapper;
import com.agifans.agile.agilib.AgileViewProvider.AgileViewWrapper;
import com.agifans.agile.agilib.jagi.pic.CorruptedPictureException;
import com.agifans.agile.agilib.jagi.res.ResourceCache;
import com.agifans.agile.agilib.jagi.res.ResourceException;
import com.agifans.agile.agilib.jagi.res.ResourceProvider;

/**
 * An adapter between the interface that AGILE expects and the JAGI library.
 */
public class Game {

    private ResourceCache resourceCache;
    
    public Map<String, byte[]> gameFilesMap;
    
    public String gameId;
    
    public String v3GameSig;

    public String version;
    
    public boolean hasAGIMouse;
    
    public boolean hasAGIPal;
    
    public boolean hasAGI256;

    public Words words;

    public Objects objects;

    public Logic[] logics;

    public Picture[] pictures;

    public View[] views;

    public Sound[] sounds;
    
    public int[][] palettes;
    
    /**
     * Constructor for Game.
     * 
     * @param gameFilesMap Map containing the data files for the AGI game.
     */
    public Game(Map<String, byte[]> gameFilesMap) {
        try {
            this.gameFilesMap = gameFilesMap;
            
            // Use JAGI to fully load the AGI game's files.
            resourceCache = new ResourceCache(gameFilesMap);
            resourceCache.setLogicProvider(new AgileLogicProvider());
            resourceCache.setSoundProvider(new AgileSoundProvider());
            resourceCache.setViewProvider(new AgileViewProvider());
            version = resourceCache.getVersion();
            v3GameSig = resourceCache.getV3GameSig();
            objects = new Objects(resourceCache.getObjects());
            words = new Words(resourceCache.getWords());
            logics = loadLogics();
            pictures = loadPictures();
            views = loadViews();
            sounds = loadSounds();
            palettes = resourceCache.getPalettes();
            
        } catch (ResourceException | IOException e) {
            throw new RuntimeException("Decode of game failed.", e);
        }
    }
    
    private Logic[] loadLogics() {
        Logic[] logics = new Logic[256];
        for (short i=0; i<256; i++) {
            try {
                Logic logic = ((AgileLogicWrapper)resourceCache.getLogic(i)).getAgileLogic();
                logic.index = i;
                logics[i] = logic;
                // If this LOGIC sets the game id, then capture it.
                if (logic.getGameId() != null) {
                    gameId = logic.getGameId();
                }
            } catch (Exception rnee) { 
                // Ignore. The LOGIC doesn't exist.
            }
        }
        return logics;
    }
    
    private Picture[] loadPictures() {
        Picture[] pictures = new Picture[256];
        for (short i=0; i<256; i++) {
            try {
                Picture picture = new Picture(resourceCache.getPicture(i));
                picture.index = i;
                pictures[i] = picture;
            } catch (CorruptedPictureException cpe) {
                // This probably means that it is an AGI256 picture, so let's load
                // the raw data instead, so that the AGILE interpreter can use it
                // directly.
                try {
                    Picture picture = new Picture(resourceCache.getResourceProvider().open(ResourceProvider.TYPE_PICTURE, i));
                    picture.index = i;
                    pictures[i] = picture;
                } catch (Exception e) {
                    // Ignore. Perhaps it really is a PICTURE we can't deal with.
                }
            } catch (Exception e) {
                // Ignore. The PICTURE doesn't exist.
            }
        }
        return pictures;
    }
    
    private View[] loadViews() {
        View[] views = new View[256];
        for (short i=0; i<256; i++) {
            try {
                View view = ((AgileViewWrapper)resourceCache.getView(i)).getAgileView();
                view.index = i;
                views[i] = view;
            } catch (Exception e) {
                // Ignore. The VIEW doesn't exist.
            }
        }
        return views;
    }
    
    private Sound[] loadSounds() {
        Sound[] sounds = new Sound[256];
        for (short i=0; i<256; i++) {
            try {
                Sound sound = ((AgileSoundWrapper)resourceCache.getSound(i)).getAgileSound();
                sound.index = i;
                sounds[i] = sound;
            } catch (Exception e) {
                // Ignore. The SOUND doesn't exist.
            }
        }
        return sounds;
    }
}