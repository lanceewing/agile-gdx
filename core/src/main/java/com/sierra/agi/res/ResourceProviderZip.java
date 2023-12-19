/*
 *  ResourceProviderZip.java
 *  Adventure Game Interpreter Resource Package
 *
 *  Created by Dr. Z.
 *  Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.sierra.agi.res;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class ResourceProviderZip implements ResourceProvider {
    protected ZipFile file;

    protected int[] counts;
    protected int[][] enums;

    public ResourceProviderZip(File file) throws IOException {
        this.file = new ZipFile(file);
    }

    public static void convert(File file, ResourceProvider provider) throws ResourceException, IOException {
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(file));
        DataOutputStream outData = new DataOutputStream(outZip);
        ResourceConfiguration config = provider.getConfiguration();

        outZip.setLevel(9);
        outZip.putNextEntry(new ZipEntry("info"));

        convert(outData, provider, TYPE_LOGIC);
        convert(outData, provider, TYPE_PICTURE);
        convert(outData, provider, TYPE_SOUND);
        convert(outData, provider, TYPE_VIEW);

        outData.writeLong(provider.getCRC());
        outData.writeShort(config.engineEmulation);
        outData.writeBoolean(config.amiga);
        outData.writeBoolean(config.agds);
        outData.writeUTF(config.name);

        outZip.close();
    }

    protected static void convert(DataOutputStream outData, ResourceProvider provider, byte resType) throws ResourceException, IOException {
        short[] en = provider.enumerate(resType);
        int index;

        outData.writeInt(en.length);

        for (index = 0; index < en.length; index++) {
            outData.writeShort(en[index]);
        }
    }
}
