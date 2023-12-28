/**
 * ResourceCache.java
 * Adventure Game Interpreter Resource Package
 * <p>
 * Created by Dr. Z.
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.res;

import com.agifans.agile.agilib.jagi.inv.InventoryObjects;
import com.agifans.agile.agilib.jagi.inv.InventoryProvider;
import com.agifans.agile.agilib.jagi.logic.Logic;
import com.agifans.agile.agilib.jagi.logic.LogicException;
import com.agifans.agile.agilib.jagi.logic.LogicProvider;
import com.agifans.agile.agilib.jagi.pic.Picture;
import com.agifans.agile.agilib.jagi.pic.PictureException;
import com.agifans.agile.agilib.jagi.pic.PictureProvider;
import com.agifans.agile.agilib.jagi.sound.Sound;
import com.agifans.agile.agilib.jagi.sound.SoundProvider;
import com.agifans.agile.agilib.jagi.view.View;
import com.agifans.agile.agilib.jagi.view.ViewException;
import com.agifans.agile.agilib.jagi.view.ViewProvider;
import com.agifans.agile.agilib.jagi.word.Words;
import com.agifans.agile.agilib.jagi.word.WordsProvider;

import java.io.IOException;
import java.util.Map;

public class ResourceCache {
    
    protected LogicProvider logicProvider;
    protected InventoryProvider inventoryProvider;
    protected PictureProvider pictureProvider;
    protected ResourceProvider resourceProvider;
    protected SoundProvider soundProvider;
    protected ViewProvider viewProvider;
    protected WordsProvider wordsProvider;
    
    protected Logic[] logics;
    protected Picture[] pictures;
    protected Sound[] sounds;
    protected View[] views;
    protected Words words;
    protected InventoryObjects objects;

    public ResourceCache(Map<String, byte[]> gameFilesMap) throws IOException, ResourceException {
        try {
            resourceProvider = new com.agifans.agile.agilib.jagi.res.v2.ResourceProviderV2(gameFilesMap);
        } catch (ResourceException e) {
            resourceProvider = new com.agifans.agile.agilib.jagi.res.v3.ResourceProviderV3(gameFilesMap);
        }
    }

    public ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    public SoundProvider getSoundProvider() {
        return soundProvider;
    }

    public void setSoundProvider(SoundProvider sndProvider) {
        this.soundProvider = sndProvider;
    }

    public InventoryProvider getInventoryProvider() {
        if (inventoryProvider == null) {
            inventoryProvider = new com.agifans.agile.agilib.jagi.inv.InventoryObjects();
        }
        return inventoryProvider;
    }

    public void setInventoryProvider(InventoryProvider invProvider) {
        this.inventoryProvider = invProvider;
    }

    public LogicProvider getLogicProvider() {
        return logicProvider;
    }

    public void setLogicProvider(LogicProvider logProvider) {
        this.logicProvider = logProvider;
    }

    public ViewProvider getViewProvider() {
        return viewProvider;
    }

    public void setViewProvider(ViewProvider viwProvider) {
        this.viewProvider = viwProvider;
    }

    public WordsProvider getWordsProvider() {
        if (wordsProvider == null) {
            wordsProvider = new com.agifans.agile.agilib.jagi.word.Words();
        }
        return wordsProvider;
    }

    public void setWordsProvider(WordsProvider wrdProvider) {
        this.wordsProvider = wrdProvider;
    }

    public PictureProvider getPictureProvider() {
        if (pictureProvider == null) {
            pictureProvider = new com.agifans.agile.agilib.jagi.pic.StandardPictureProvider();
        }
        return pictureProvider;
    }

    public void setPictureProvider(PictureProvider picProvider) {
        this.pictureProvider = picProvider;
    }

    public Sound getSound(short resNumber) throws IOException, ResourceException {
        Sound sound;

        if (sounds == null) {
            sounds = new Sound[256];
        }

        sound = sounds[resNumber];

        if (sound == null) {
            sound = getSoundProvider().loadSound(resourceProvider.open(ResourceProvider.TYPE_SOUND, resNumber));
            sounds[resNumber] = sound;
        }

        return sound;
    }

    public Logic getLogic(short resNumber) throws IOException, ResourceException, LogicException {
        Logic logic;

        if (logics == null) {
            logics = new Logic[256];
        }

        logic = logics[resNumber];

        if (logic == null) {
            logic = getLogicProvider().loadLogic(resNumber, resourceProvider.open(ResourceProvider.TYPE_LOGIC, resNumber), resourceProvider.getSize(ResourceProvider.TYPE_LOGIC, resNumber));
            logics[resNumber] = logic;
        }

        return logic;
    }

    public Picture getPicture(short resNumber) throws IOException, ResourceException, PictureException {
        Picture picture;

        if (pictures == null) {
            pictures = new Picture[256];
        }

        picture = pictures[resNumber];

        if (picture == null) {
            picture = getPictureProvider().loadPicture(resourceProvider.open(ResourceProvider.TYPE_PICTURE, resNumber));
            pictures[resNumber] = picture;
        }

        return picture;
    }

    public View getView(short resNumber) throws IOException, ResourceException, ViewException {
        View view;
        
        if (views == null) {
            views = new View[256];
        }

        view = views[resNumber];

        if (view == null) {
            view = getViewProvider().loadView(resourceProvider.open(ResourceProvider.TYPE_VIEW, resNumber), resourceProvider.getSize(ResourceProvider.TYPE_VIEW, resNumber));
            views[resNumber] = view;
        }

        return view;
    }

    public Words getWords() throws IOException, ResourceException {
        if (words == null) {
            words = getWordsProvider().loadWords(resourceProvider.open(ResourceProvider.TYPE_WORD, (short) 0));
        }
        return words;
    }

    public InventoryObjects getObjects() throws IOException, ResourceException {
        if (objects == null) {
            objects = getInventoryProvider().loadInventory(resourceProvider.open(ResourceProvider.TYPE_OBJECT, (short) 0));
        }
        return objects;
    }

    public String getVersion() {
        return resourceProvider.getVersion();
    }
    
    public String getV3GameSig() {
        return resourceProvider.getV3GameSig();
    }
}
