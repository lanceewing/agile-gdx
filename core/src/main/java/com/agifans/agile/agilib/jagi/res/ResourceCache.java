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
    
    protected int[][] palettes;

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
    
    public int[][] getPalettes() {
        if (palettes == null) {
            palettes = new int[10][];
            
            for (int palNum=0; palNum < 10; palNum++) {
                byte[] palBytes = resourceProvider.getPalettes()[palNum];
                if ((palBytes != null) && (palBytes.length == 192)) {
                    // We have a valid AGIPAL file, so decode it into RGBA8888 values, the
                    // same as is used in AGILE.
                    int[] colours = new int[16];
                    // First chunk.
                    colours[0]  = getRGBAColour(palBytes[0],  palBytes[1],  palBytes[2]);
                    colours[1]  = getRGBAColour(palBytes[3],  palBytes[4],  palBytes[5]);
                    colours[2]  = getRGBAColour(palBytes[6],  palBytes[7],  palBytes[8]);
                    colours[3]  = getRGBAColour(palBytes[9],  palBytes[10], palBytes[11]);
                    colours[4]  = getRGBAColour(palBytes[12], palBytes[13], palBytes[14]);
                    colours[5]  = getRGBAColour(palBytes[15], palBytes[16], palBytes[17]);
                    colours[6]  = getRGBAColour(palBytes[18], palBytes[19], palBytes[20]);
                    colours[7]  = getRGBAColour(palBytes[21], palBytes[22], palBytes[23]);
                    // Second chunk.
                    colours[8]  = getRGBAColour(palBytes[48], palBytes[49], palBytes[50]);
                    colours[9]  = getRGBAColour(palBytes[51], palBytes[52], palBytes[53]);
                    colours[10] = getRGBAColour(palBytes[54], palBytes[55], palBytes[56]);
                    colours[11] = getRGBAColour(palBytes[57], palBytes[58], palBytes[59]);
                    colours[12] = getRGBAColour(palBytes[60], palBytes[61], palBytes[62]);
                    colours[13] = getRGBAColour(palBytes[63], palBytes[64], palBytes[65]);
                    colours[14] = getRGBAColour(palBytes[66], palBytes[67], palBytes[68]);
                    colours[15] = getRGBAColour(palBytes[69], palBytes[70], palBytes[71]);
                    palettes[palNum] = colours;
                }
            }
        }
        
        return palettes;
    }
    
    private int getRGBAColour(byte r, byte g, byte b) {
        // 18-bit VGA palette, so top 2 bits of each byte are discarded.
        int red = (((int)r) << 2) & 0xFF;
        int green = (((int)g) << 2) & 0xFF;
        int blue = (((int)b) << 2) & 0xFF;
        
        int rgba8888Colour = 0;
        rgba8888Colour |= ((red << 24) & 0xFF000000);
        rgba8888Colour |= ((green << 16) & 0x00FF0000);
        rgba8888Colour |= ((blue <<  8) & 0x0000FF00);
        rgba8888Colour |= 0x000000FF;
        return rgba8888Colour;
    }

    public String getVersion() {
        return resourceProvider.getVersion();
    }
    
    public String getV3GameSig() {
        return resourceProvider.getV3GameSig();
    }
}
