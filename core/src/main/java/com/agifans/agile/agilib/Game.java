package com.agifans.agile.agilib;

import java.io.File;
import java.io.IOException;

import com.agifans.agile.agilib.AgileLogicProvider.AgileLogicWrapper;
import com.agifans.agile.agilib.AgileSoundProvider.AgileSoundWrapper;
import com.agifans.agile.agilib.jagi.res.ResourceCache;
import com.agifans.agile.agilib.jagi.res.ResourceCacheFile;
import com.agifans.agile.agilib.jagi.res.ResourceException;

/**
 * An adapter between the interface that AGILE expects and the JAGI library.
 */
public class Game {

    private ResourceCache resourceCache;
    
    public String gameFolder;

    public String v3GameSig;

    public String version;

    public Words words;

    public Objects objects;

    public Logic[] logics;

    public Picture[] pictures;

    public View[] views;

    public Sound[] sounds;
    
    /**
     * Constructor for Game.
     * 
     * @param gameFolder The folder to load the AGI game from.
     */
    public Game(String gameFolder) {
        this.gameFolder = gameFolder;
        
        // The aim is to try to use JAGI as untouched as possible to load resources 
        // for use in AGILE, i.e. JAGI becomes the AGI library for the Java version 
        // of AGILE.
        
        try {
            // We use our own LogicProvider & SoundProvider implementations, so that we can 
            // load LOGICs and SOUNDs directly in the form required by AGILE. The other 
            // types are converted from the JAGI types, after being loaded. It didn't make
            // sense to do that for the Logic and Sound types, as it is quite different. Luckily
            // JAGI already provided a way to plug in a custom implementations via properties.
            
            // Use JAGI to fully load the AGI game's files.
            resourceCache = new ResourceCacheFile(new File(gameFolder));
            resourceCache.setLogicProvider(new com.agifans.agile.agilib.AgileLogicProvider());
            resourceCache.setSoundProvider(new com.agifans.agile.agilib.AgileSoundProvider());
            version = resourceCache.getVersion();
            v3GameSig = resourceCache.getV3GameSig();
            logics = loadLogics();
            pictures = loadPictures();
            views = loadViews();
            sounds = loadSounds();
            objects = new Objects(resourceCache.getObjects());
            words = new Words(resourceCache.getWords());
            
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
                View view = new View(resourceCache.getView(i));
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