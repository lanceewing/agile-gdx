/**
 * ResourceProviderV2.java
 * Adventure Game Interpreter Resource Package
 * <p>
 * Created by Dr. Z
 * Copyright (c) 2001 Dr. Z. All rights reserved.
 */

package com.agifans.agile.agilib.jagi.res.v2;

import com.agifans.agile.agilib.jagi.io.ByteCaster;
import com.agifans.agile.agilib.jagi.io.ByteCasterStream;
import com.agifans.agile.agilib.jagi.io.CryptedInputStream;
import com.agifans.agile.agilib.jagi.io.RandomAccessFile;
import com.agifans.agile.agilib.jagi.io.SegmentedInputStream;
import com.agifans.agile.agilib.jagi.res.CorruptedResourceException;
import com.agifans.agile.agilib.jagi.res.DirectoryNotFoundException;
import com.agifans.agile.agilib.jagi.res.NoDirectoryAvailableException;
import com.agifans.agile.agilib.jagi.res.ResourceException;
import com.agifans.agile.agilib.jagi.res.ResourceNotExistingException;
import com.agifans.agile.agilib.jagi.res.ResourceProvider;
import com.agifans.agile.agilib.jagi.res.ResourceTypeInvalidException;
import com.agifans.agile.agilib.jagi.res.VolumeNotFoundException;
import com.agifans.agile.agilib.jagi.res.dir.ResourceDirectory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Provide access to resources via the standard storage methods.
 * It reads unmodified sierra's resource files.
 * <p>
 * All AGI games have either one directory file, or more commonly, four.
 * AGI version 2 games will have the files LOGDIR, PICDIR, VIEWDIR, and SNDDIR.
 * This single file is basically the four version 2 files joined together
 * except that it has an 8 byte header giving the position of each directory
 * within the single file.
 * <p>
 * The directory files give the location of the data types within the VOL
 * files. The type of directory determines the type of data. For example, the
 * LOGDIR gives the locations of the LOGIC files.
 *
 * <I>Note</I>: In this description and elsewhere in documents written by me,
 * the AGI data called LOGIC, PICTURE, VIEW, and SOUND data are referred to by
 * me as files even though they are part of a single VOL file. I think of
 * the VOL file as sort of a virtual storage device in itself that holds many
 * files. Some documents call the files contains in VOL files "resources".
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class ResourceProviderV2 implements ResourceProvider {

    /**
     * Sierra's Decryption Key. This key used to decrypt
     * original sierra games.
     */
    private static final String SIERRA_KEY = "Avis Durgan";
    
    /**
     * AGDS's Decryption Key. This key is used to decrypt AGDS games.
     */
    private static final String GROZA_KEY = "Alex Simkin";
    
    /**
     * Whether the game is GROZA or not. It uses a different decrytion key.
     */
    protected boolean isGroza;
    
    /**
     * Resource's Entries Tables.
     */
    protected ResourceDirectory[] entries = new ResourceDirectory[4];
    
    /**
     * A Map of AGI game data file name to byte array content of that AGI file.
     */
    protected Map<String, byte[]> gameFilesMap;
    
    /**
     * AGIPAL palettes. Max of 10.
     */
    protected byte[][] palettes;
    
    protected String version = "unknown";

    /**
     * Initialize the ResourceProvider implementation to access
     * resource on the file system.
     *
     * @param gamesFilesMap Map of the AGI game's data file content.
     */
    public ResourceProviderV2(Map<String, byte[]> gameFilesMap) throws IOException, ResourceException {
        this.gameFilesMap = gameFilesMap;
        
        readVolumes();
        readDirectories();
        readVersion();
        readPalettes();
    }

    protected String getKey() {
        return (isGroza? GROZA_KEY : SIERRA_KEY);
    }
    
    public boolean isGroza(byte[] fileData) {
        // Groza stores 6 bytes, i.e. 2A 2A 2A 2A 2A 1A, in both OBJECT and WORDS.TOK
        isGroza = ((fileData[0] == 0x2A) && 
                   (fileData[1] == 0x2A) && 
                   (fileData[2] == 0x2A) && 
                   (fileData[3] == 0x2A) && 
                   (fileData[4] == 0x2A) && 
                   (fileData[5] == 0x1A));
        return isGroza;
    }

    public static boolean isCrypted(byte[] fileData) {
        boolean b = false;

        try {
            ByteCasterStream bstream = new ByteCasterStream(new ByteArrayInputStream(fileData));

            if (bstream.lohiReadUnsignedShort() > fileData.length) {
                b = true;
            }

            bstream.close();
            return b;
        } catch (Throwable t) {
            return false;
        }
    }

    protected void validateType(byte resType) throws ResourceTypeInvalidException {
        if ((resType > TYPE_WORD) || (resType < TYPE_LOGIC)) {
            throw new ResourceTypeInvalidException();
        }
    }

    /**
     * Retreive the count of resources of the specified type.
     * Only valid with Locic, Picture, Sound and View resource
     * types.
     *
     * @param resType Resource type
     * @return Resource count.
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_LOGIC
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_PICTURE
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_SOUND
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_VIEW
     */
    public int count(byte resType) throws ResourceException {
        validateType(resType);

        if (resType >= TYPE_OBJECT) {
            return 1;
        }

        return entries[resType].getCount();
    }

    /**
     * Enumerate the resource numbers of the specified type.
     * Only valid with Locic, Picture, Sound and View resource
     * types.
     *
     * @param resType Resource type
     * @return Array containing the resource numbers.
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_LOGIC
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_PICTURE
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_SOUND
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_VIEW
     */
    public short[] enumerate(byte resType) throws ResourceException {
        validateType(resType);

        return entries[resType].getNumbers();
    }

    /**
     * Open the specified resource and return a pointer
     * to the resource. The InputStream is decrypted/decompressed,
     * if neccessary, by this function. (So you don't have to care
     * about them.)
     *
     * @param resType   Resource type
     * @param resNumber Resource number. Ignored if resource type
     *                  is <CODE>TYPE_OBJECT</CODE> or
     *                  <CODE>TYPE_WORD</CODE>
     * @return InputStream linked to the specified resource.
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_LOGIC
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_OBJECT
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_PICTURE
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_SOUND
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_VIEW
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_WORD
     */
    public InputStream open(byte resType, short resNumber) throws ResourceException, IOException {
        byte[] volf;

        switch (resType) {
            case ResourceProvider.TYPE_OBJECT:
                volf = getDirectoryFile(resType);
                
                if (isGroza(volf)) {
                    // Replace with a minimal encrypted OBJECT file (8 bytes)
                    volf = new byte[] { 0x42, 0x76, 0x69, 0x70, 0x20, 0x44, 0x4A, 0x72 };
                }

                if (isCrypted(volf)) {
                    return new CryptedInputStream(new ByteArrayInputStream(volf), SIERRA_KEY);
                } else {
                    return new ByteArrayInputStream(volf);
                }

            case ResourceProvider.TYPE_WORD:
                volf = getDirectoryFile(resType);
                
                if (isGroza(volf)) {
                    // Replace with a minimal WORDS.TOK file (63 bytes)
                    volf = new byte[] {
                        0x00, 0x34, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x1E, 0x11, 0x06, 
                        0x08, 0x10, 0x0D, (byte)0x9B, 0x00, 0x01, 0x00
                    };
                }
                
                return new ByteArrayInputStream(volf);
        }

        try {
            if (entries[resType] != null) {
                int vol, offset, length;

                vol = entries[resType].getVolume(resNumber);
                offset = entries[resType].getOffset(resNumber);

                if ((vol != -1) && (offset != -1)) {
                    byte[] b;
                    RandomAccessFile file;
                    InputStream in;

                    b = new byte[5];
                    file = new RandomAccessFile(new ByteArrayInputStream(getVolumeFile(vol)));
                    file.seek(offset);
                    file.read(b, 0, 5);

                    if ((b[0] != 0x12) || (b[1] != 0x34)) {
                        throw new CorruptedResourceException();
                    }

                    length = ByteCaster.lohiUnsignedShort(b, 3);
                    in = new SegmentedInputStream(file, offset + 5, length);

                    if (resType == TYPE_LOGIC) {
                        int startPos, numMessages, offsetCrypted;

                        // Calculate the Messages Offset
                        file.read(b, 0, 2);
                        startPos = ByteCaster.lohiUnsignedShort(b, 0) + 2;
                        file.seek(offset + startPos + 5);
                        file.read(b, 0, 3);
                        numMessages = ByteCaster.lohiUnsignedByte(b, 0);
                        offsetCrypted = startPos + 3 + (numMessages * 2);
                        file.seek(offset + 5);

                        in = new CryptedInputStream(in, getKey(), offsetCrypted);
                    }

                    return in;
                }
            }

            throw new ResourceNotExistingException();
        } catch (IndexOutOfBoundsException e) {
            throw new ResourceTypeInvalidException();
        }
    }

    protected byte[] getVolumeFile(int vol) throws VolumeNotFoundException {
        byte[] fileData = getGameFile("vol." + vol);

        if (fileData == null) {
            throw new VolumeNotFoundException("File vol." + vol + " can't be found.");
        }

        return fileData;
    }

    /**
     * Gets the content of the AGI game data file as a byte array.
     *
     * @param fileName The name of the file to get.
     * 
     * @return A byte array representing the game file.
     */
    protected byte[] getGameFile(String fileName) {
        return gameFilesMap.get(fileName);
    }

    protected byte[] getDirectoryFile(byte resType) throws IOException, DirectoryNotFoundException {
        byte[] fileData;

        switch (resType) {
            case ResourceProvider.TYPE_OBJECT:
                fileData = getGameFile("object");
                break;
            case ResourceProvider.TYPE_WORD:
                fileData = getGameFile("words.tok");
                break;
            case ResourceProvider.TYPE_LOGIC:
                fileData = getGameFile("logdir");
                break;
            case ResourceProvider.TYPE_PICTURE:
                fileData = getGameFile("picdir");
                break;
            case ResourceProvider.TYPE_SOUND:
                fileData = getGameFile("snddir");
                break;
            case ResourceProvider.TYPE_VIEW:
                fileData = getGameFile("viewdir");
                break;
            default:
                // This game is missing a key AGI file!!!
                throw new DirectoryNotFoundException();
        }

        return fileData;
    }

    /**
     * Retrieve the size in bytes of the specified resource.
     *
     * @param resType   Resource type
     * @param resNumber Resource number. Ignored if resource type
     *                  is <CODE>TYPE_OBJECT</CODE> or
     *                  <CODE>TYPE_WORD</CODE>
     * @return Size in bytes of the specified resource.
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_LOGIC
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_OBJECT
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_PICTURE
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_SOUND
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_VIEW
     * @see com.agifans.agile.agilib.jagi.res.ResourceProvider#TYPE_WORD
     */
    public int getSize(byte resType, short resNumber) throws ResourceException, IOException {
        switch (resType) {
            case ResourceProvider.TYPE_OBJECT:
            case ResourceProvider.TYPE_WORD:
                return (int) getDirectoryFile(resType).length;
        }

        try {
            if (entries[resType] != null) {
                int vol, offset;

                vol = entries[resType].getVolume(resNumber);
                offset = entries[resType].getOffset(resNumber);

                if ((vol != -1) && (offset != -1)) {
                    byte[] b;
                    RandomAccessFile file;

                    b = new byte[5];
                    file = new RandomAccessFile(new ByteArrayInputStream(getVolumeFile(vol)));
                    file.seek(offset);
                    file.read(b);
                    file.close();

                    if ((b[0] != 0x12) || (b[1] != 0x34)) {
                        throw new CorruptedResourceException();
                    }

                    return ByteCaster.lohiUnsignedShort(b, 3);
                }
            }

            throw new ResourceNotExistingException();
        } catch (IndexOutOfBoundsException e) {
            throw new ResourceTypeInvalidException();
        }
    }

    protected void readVolumes() {
        // Template method. Not implemented by V2 provider.
    }
    
    /**
     * Read all directory files
     * 
     * @throws NoDirectoryAvailableException
     * @throws IOException
     * @throws DirectoryNotFoundException
     */
    protected void readDirectories() throws NoDirectoryAvailableException, IOException, DirectoryNotFoundException {
        byte i;
        int j;
        byte[] dir;
        InputStream stream;

        for (i = 0, j = 0; i < 4; i++) {
            dir = getDirectoryFile(i);

            if (dir != null) {
                stream = new ByteArrayInputStream(dir);
                entries[i] = new ResourceDirectory(stream);
                stream.close();
                j++;
            }
        }

        if (j == 0) {
            throw new NoDirectoryAvailableException();
        }
    }
    
    private void readPalettes() {
        this.palettes = new byte[10][];
        
        for (String fileName : gameFilesMap.keySet()) {
            if (fileName.matches("^pal[.]10[0-9]$")) {
                try {
                    String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                    int palNumber = Integer.parseInt(extension) - 100;
                    this.palettes[palNumber] = gameFilesMap.get(fileName);
                } catch(Exception e) {
                    // Ignore. Must not be a proper PAL file.
                }
            }
        }
    }

    private void readVersion() throws IOException {
        byte[] fileContent = getGameFile("agidata.ovl");

        if (fileContent == null) {
            return;
        }

        for (int i = 0; i < fileContent.length; i++) {
            if (fileContent[i] == 86 && fileContent[i + 1] == 101 && fileContent[i + 2] == 114 && fileContent[i + 3] == 115 && fileContent[i + 4] == 105 && fileContent[i + 5] == 111 && fileContent[i + 6] == 110 && fileContent[i + 7] == 32) {
                int j;
                for (j = i + 8; fileContent[j] != 0; j++) {
                }
                // Note: ISO_8859_1 used as it is supported by GWT. In this case, we only need 7-bits, so it works.
                this.version = new String(fileContent, i + 8, j - (i + 8), StandardCharsets.ISO_8859_1);
                break;
            }
        }
    }

    public byte[][] getPalettes() {
        return this.palettes;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public String getV3GameSig() {
        // This is V2, so it doesn't apply. Return null;
        return null;
    }
}