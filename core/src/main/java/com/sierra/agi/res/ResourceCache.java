/**
 * ResourceCache.java
 * Adventure Game Interpreter Resource Package
 * <p>
 * Created by Dr. Z.
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.res;

import com.sierra.agi.inv.InventoryObjects;
import com.sierra.agi.inv.InventoryProvider;
import com.sierra.agi.logic.Logic;
import com.sierra.agi.logic.LogicException;
import com.sierra.agi.logic.LogicProvider;
import com.sierra.agi.pic.Picture;
import com.sierra.agi.pic.PictureException;
import com.sierra.agi.pic.PictureProvider;
import com.sierra.agi.sound.Sound;
import com.sierra.agi.sound.SoundProvider;
import com.sierra.agi.view.View;
import com.sierra.agi.view.ViewException;
import com.sierra.agi.view.ViewProvider;
import com.sierra.agi.word.Words;
import com.sierra.agi.word.WordsProvider;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;

public class ResourceCache {
    protected static Class[] providerParameters = {ResourceConfiguration.class};
    protected LogicProvider logProvider;
    protected InventoryProvider invProvider;
    protected PictureProvider picProvider;
    protected ResourceProvider resProvider;
    protected SoundProvider sndProvider;
    protected ViewProvider viwProvider;
    protected WordsProvider wrdProvider;
    protected Object[] logics;
    protected int[] logicsc;
    protected Object[] pictures;
    protected int[] picturesc;
    protected Object[] sounds;
    protected int[] soundsc;
    protected Object[] views;
    protected int[] viewsc;
    protected Words words;
    protected InventoryObjects objects;

    protected ResourceCache() {
    }

    public ResourceCache(ResourceProvider resProvider) {
        this.resProvider = resProvider;
    }

    public synchronized ResourceProvider getResourceProvider() {
        return resProvider;
    }

    protected Object getProvider(String clazzName) {
        try {
            try {
                return Class.forName(clazzName).newInstance();
            } catch (InstantiationException e) {
                Class clazz = Class.forName(clazzName);
                Constructor cons = clazz.getConstructor(providerParameters);
                Object[] o = new Object[1];

                o[0] = resProvider.getConfiguration();

                return cons.newInstance(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getClass().getName() + ": " + clazzName);
        }
    }

    public synchronized SoundProvider getSoundProvider() {
        if (sndProvider == null) {
            sndProvider = (SoundProvider) getProvider(System.getProperty("com.sierra.agi.sound.SoundProvider", "com.sierra.agi.sound.StandardSoundProvider"));
        }

        return sndProvider;
    }

    public synchronized void setSoundProvider(SoundProvider sndProvider) {
        this.sndProvider = sndProvider;
    }

    public synchronized InventoryProvider getInventoryProvider() {
        if (invProvider == null) {
            invProvider = (InventoryProvider) getProvider(System.getProperty("com.sierra.agi.inv.LogicProvider", "com.sierra.agi.inv.InventoryObjects"));
        }

        return invProvider;
    }

    public synchronized void setInventoryProvider(InventoryProvider invProvider) {
        this.invProvider = invProvider;
    }

    public synchronized LogicProvider getLogicProvider() {
        if (logProvider == null) {
            logProvider = (LogicProvider) getProvider(System.getProperty("com.sierra.agi.logic.LogicProvider", "com.sierra.agi.logic.StandardLogicProvider"));
        }

        return logProvider;
    }

    public synchronized void setLogicProvider(LogicProvider logProvider) {
        this.logProvider = logProvider;
    }

    public synchronized ViewProvider getViewProvider() {
        if (viwProvider == null) {
            viwProvider = (ViewProvider) getProvider(System.getProperty("com.sierra.agi.view.ViewProvider", "com.sierra.agi.view.StandardViewProvider"));
        }

        return viwProvider;
    }

    public synchronized void setViewProvider(ViewProvider viwProvider) {
        this.viwProvider = viwProvider;
    }

    public synchronized WordsProvider getWordsProvider() {
        if (wrdProvider == null) {
            wrdProvider = (WordsProvider) getProvider(System.getProperty("com.sierra.agi.word.WordsProvider", "com.sierra.agi.word.Words"));
        }

        return wrdProvider;
    }

    public synchronized void setWordsProvider(WordsProvider wrdProvider) {
        this.wrdProvider = wrdProvider;
    }

    public synchronized PictureProvider getPictureProvider() {
        if (picProvider == null) {
            picProvider = (PictureProvider) getProvider(System.getProperty("com.sierra.agi.pic.PictureProvider", "com.sierra.agi.pic.StandardPictureProvider"));
        }

        return picProvider;
    }

    public synchronized void setPictureProvider(PictureProvider picProvider) {
        this.picProvider = picProvider;
    }

    protected Object obtainResource(Object[] objects, int[] objectsc, short resNumber, boolean inc) {
        Object obj = objects[resNumber];

        if (obj != null) {
            if (obj instanceof Reference) {
                obj = ((Reference) obj).get();

                if (inc) {
                    objects[resNumber] = obj;
                }

                if (obj == null) {
                    return null;
                }
            }

            if (inc) {
                objectsc[resNumber]++;
            }

            return obj;
        }

        return null;
    }

    protected void flushResource(Object[] objects, int[] objectsc, short resNumber) {
        if (objectsc[resNumber] > 0) {
            objectsc[resNumber]--;
        }

        if (objects[resNumber] == null) {
            return;
        }

        if (objectsc[resNumber] <= 0) {
            if (!(objects[resNumber] instanceof Reference)) {
                objects[resNumber] = generateReference(objects[resNumber]);
            }
        }
    }

    protected synchronized Sound obtainSound(short resNumber, boolean inc) throws IOException, ResourceException {
        Object o;

        if (sounds == null) {
            sounds = new Object[256];
            soundsc = new int[256];
        }

        o = obtainResource(sounds, soundsc, resNumber, inc);

        if (o == null) {
            o = getSoundProvider().loadSound(resProvider.open(ResourceProvider.TYPE_SOUND, resNumber));

            if (inc) {
                sounds[resNumber] = o;
                soundsc[resNumber] = 1;
            } else {
                sounds[resNumber] = generateReference(o);
            }
        }

        return (Sound) o;
    }

    public void loadSound(short resNumber) throws IOException, ResourceException {
        obtainSound(resNumber, true);
    }

    public Sound getSound(short resNumber) throws IOException, ResourceException {
        return obtainSound(resNumber, false);
    }

    public synchronized void unloadSound(short resNumber) {
        flushResource(sounds, soundsc, resNumber);
    }

    protected synchronized Logic obtainLogic(short resNumber, boolean inc) throws IOException, ResourceException, LogicException {
        Object o;

        if (logics == null) {
            logics = new Object[256];
            logicsc = new int[256];
        }

        o = obtainResource(logics, logicsc, resNumber, inc);

        if (o == null) {
            o = getLogicProvider().loadLogic(resNumber, resProvider.open(ResourceProvider.TYPE_LOGIC, resNumber), resProvider.getSize(ResourceProvider.TYPE_LOGIC, resNumber));

            if (inc) {
                logics[resNumber] = o;
                logicsc[resNumber] = 1;
            } else {
                logics[resNumber] = generateReference(o);
            }
        }

        return (Logic) o;
    }

    public void loadLogic(short resNumber) throws IOException, ResourceException, LogicException {
        obtainLogic(resNumber, true);
    }

    public Logic getLogic(short resNumber) throws IOException, ResourceException, LogicException {
        return obtainLogic(resNumber, false);
    }

    public synchronized void unloadLogic(short resNumber) {
        flushResource(logics, logicsc, resNumber);
    }

    protected synchronized Picture obtainPicture(short resNumber, boolean inc) throws IOException, ResourceException, PictureException {
        Object o;

        if (pictures == null) {
            pictures = new Object[256];
            picturesc = new int[256];
        }

        o = obtainResource(pictures, picturesc, resNumber, inc);

        if (o == null) {
            o = getPictureProvider().loadPicture(resProvider.open(ResourceProvider.TYPE_PICTURE, resNumber));

            if (inc) {
                pictures[resNumber] = o;
                picturesc[resNumber] = 1;
            } else {
                pictures[resNumber] = generateReference(o);
            }
        }

        return (Picture) o;
    }

    public void loadPicture(short resNumber) throws IOException, ResourceException, PictureException {
        obtainPicture(resNumber, true);
    }

    public Picture getPicture(short resNumber) throws IOException, ResourceException, PictureException {
        return obtainPicture(resNumber, false);
    }

    public synchronized void unloadPicture(short resNumber) {
        flushResource(pictures, picturesc, resNumber);
    }

    protected synchronized View obtainView(short resNumber, boolean inc) throws IOException, ResourceException, ViewException {
        if (views == null) {
            views = new Object[256];
            viewsc = new int[256];
        }

        Object o = obtainResource(views, viewsc, resNumber, inc);

        if (o == null) {
            o = getViewProvider().loadView(resProvider.open(ResourceProvider.TYPE_VIEW, resNumber), resProvider.getSize(ResourceProvider.TYPE_VIEW, resNumber));

            if (inc) {
                views[resNumber] = o;
                viewsc[resNumber] = 1;
            } else {
                views[resNumber] = generateReference(o);
            }
        }

        return (View) o;
    }

    public void loadView(short resNumber) throws IOException, ResourceException, ViewException {
        obtainView(resNumber, true);
    }

    public View getView(short resNumber) throws IOException, ResourceException, ViewException {
        return obtainView(resNumber, false);
    }

    public synchronized void unloadView(short resNumber) {
        flushResource(views, viewsc, resNumber);
    }

    public synchronized Words getWords() throws IOException, ResourceException {
        if (words == null) {
            words = getWordsProvider().loadWords(resProvider.open(ResourceProvider.TYPE_WORD, (short) 0));
        }

        return words;
    }

    public synchronized InventoryObjects getObjects() throws IOException, ResourceException {
        if (objects == null) {
            objects = getInventoryProvider().loadInventory(resProvider.open(ResourceProvider.TYPE_OBJECT, (short) 0));
        }

        return objects;
    }

    protected Reference generateReference(Object o) {
        return new WeakReference(o);
    }

    public File getPath() {
        return resProvider.getPath();
    }

    public String getVersion() {
        return this.resProvider.getVersion();
    }
    
    public String getV3GameSig() {
        return this.resProvider.getV3GameSig();
    }
}
