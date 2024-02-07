/*
 * ResourceProviderV3.java
 */

package com.agifans.agile.agilib.jagi.res.v3;

import com.agifans.agile.agilib.jagi.io.*;
import com.agifans.agile.agilib.jagi.res.*;
import com.agifans.agile.agilib.jagi.res.dir.ResourceDirectory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Provide access to resources via the standard storage methods.
 * It reads unmodified sierra's resource files.
 * <p>
 * AGIv3 stores resources in a slightly different way from AGIv2. The first
 * significant difference is in the length of the resource header which is
 * now seven bytes.
 * </P>
 * <TABLE BORDER=1>
 * <THEAD><TR><TD>Byte</TD><TD>Meaning</TD></TR></THEAD>
 * <TR><TD>0-1</TD><TD>Signature (0x12--0x34)</TD></TR>
 * <TR><TD>2</TD><TD>Vol number that the resource is contained in</TD></TR>
 * <TR><TD>3-4</TD><TD>Uncompressed resource size (LO-HI)</TD></TR>
 * <TR><TD>5-6</TD><TD>Compressed resource size (LO-HI)</TD></TR>
 * </TABLE>
 * <p>
 * Instead of one resource size as in AGIv2, there are now two sizes. Most of
 * the resources in AGIv3 games are compressed with a form of LZW. Some of them
 * are not though. The interpreter determines whether the resource is compressed
 * by comparing the values of the two sizes given in the header information. If
 * they are equal, then it knows that the resource is stored uncompressed.
 * However, if the sizes do not match, this does not mean that the file is
 * compressed with LZW. If the file is a PICTURE file, then it is stored with
 * its own limited form of compression. This is why the top bit of the third
 * byte in the header is used to tell the interpreter that the resource is a
 * PICTURE file, otherwise it would think that the resource was compressed with
 * LZW.
 * </P><P>
 * As far as I can tell, none of the PICTUREs are compressed with LZW. This may
 * well be possible though. It could also be possible for the PICTURE to be
 * totally uncompressed (i.e. it wouldn't use the PICTURE compression method),
 * but I haven't seen any examples of either of the above two cases.
 *
 * </P><P><B>LZW compression</B>
 * </P><P>
 * The compression used with version 3 games is an adaptive form of LZW. The LZW
 * algorithm is not explained here, but it basically compresses data by
 * representing previous strings by single codes. When these strings are
 * encountered again, the code can be stored instead. The following information
 * states how the AGIv3 algorithm differs from the standard LZW algorithm. There
 * are plenty of places on the net where you can find a description of the LZW
 * algorithm if you are not familiar with it.
 * </P><P>
 * AGIv3 uses an adaptive form of LZW that starts by using 9 bit codes and when
 * the code space is full, it progresses on to 10 bits and so on. As with normal
 * LZW, codes 0-255 represent the standard ASCII characters. The next two codes
 * have a special meaning:
 * </P><P>
 * 256 is used as a start over code. The table is cleared, the number of bits
 * set back to 9, and the process begins again with the next code being 258.
 * </P><P>
 * 257 tells the interpreter that it has reached the end of the resource.
 * </P><P>
 * Code 256 seems to be the first code stored in all compressed resources. This
 * is probably just to make sure everything is initialized for beginning the
 * compression process. As was mentioned above, the first code used for the LZW
 * table itself is code 258. From there it stores pairs of prefix codes and
 * appended characters for each table entry until it reaches code 512 at which
 * stage it switches to storing the codes using 10 bits and then 11 and so on.
 * It appears that it will never get to 12 bits because code 256 always seems
 * to turn up just before it needs to switch up to 12 bits, i.e. when code 2048
 * is required. Carl Muckenhoupt's decrypt routine for SCI games specifically
 * prevents it from switching to 12 bits anyway. Whether there is ever a case
 * where code 256 does not intervene, it has not yet been determined.
 * </P><P>
 * Note: I should point out that Carl and myself both arrived at the above
 * algorithm independently which confirms that the compression used in the early
 * SCI games was identical to that used in AGIv3.
 * </P>
 *
 * @author Dr. Z
 * @version 0.00.00.01
 */
public class ResourceProviderV3 extends com.agifans.agile.agilib.jagi.res.v2.ResourceProviderV2 {

    protected byte[][] vols;
    protected byte[][] dirs;

    private String v3GameSignature;
    
    /**
     * Initialize the ResourceProvider implementation to access
     * resource on the file system.
     *
     * @param gamesFilesMap Map of the AGI game's data file content.
     */
    public ResourceProviderV3(Map<String, byte[]> gameFilesMap) throws IOException, ResourceException {
        super(gameFilesMap);
    }

    /**
     * Find volumes files
     */
    protected void readVolumes() {
        this.vols = new byte[16][];
        
        // In order to cater for games like GR, where there are gaps in the VOL
        // numbering, we use the actual number from the file extension as the index
        // when putting a vol into the vols array.
        for (String fileName : gameFilesMap.keySet()) {
            if (fileName.matches("^[a-z0-9]*vol.[0-9]+$")) {
                try {
                    String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                    int volNumber = Integer.parseInt(extension);
                    this.vols[volNumber] = gameFilesMap.get(fileName);
                } catch(Exception e) {
                    // Ignore. Must not be a proper VOL file.
                }
            }
        }
    }

    /**
     * Read directory files
     */
    protected void readDirectories() throws NoDirectoryAvailableException, IOException {
        byte[] b = new byte[8];
        int[] o = new int[8];
        InputStream stream;
        RandomAccessFile dirfile;
        int i, j, ax;

        findDirectories();
        stream = new ByteArrayInputStream(dirs[0]);
        stream.read(b, 0, 8);
        stream.close();

        for (i = 0; i < 4; i++) {
            o[i] = ByteCaster.lohiUnsignedShort(b, i * 2);
        }

        for (i = 0; i < 4; i++) {
            ax = 0xffffff;

            for (j = 0; j < 4; j++) {
                if ((o[j] > o[i]) && (o[j] < ax)) {
                    ax = o[j];
                }
            }

            if (ax == 0xffffff) {
                ax = (int) dirs[0].length;
            }

            o[i + 4] = ax;
        }

        dirfile = new RandomAccessFile(new ByteArrayInputStream(dirs[0]));
        o[4] -= o[0];
        entries[0] = new ResourceDirectory(new SegmentedInputStream(dirfile, o[0], o[4]));

        o[5] -= o[1];
        entries[1] = new ResourceDirectory(new SegmentedInputStream(dirfile, o[1], o[5]));

        o[6] -= o[2];
        entries[3] = new ResourceDirectory(new SegmentedInputStream(dirfile, o[2], o[6]));

        o[7] -= o[3];
        entries[2] = new ResourceDirectory(new SegmentedInputStream(dirfile, o[3], o[7]));
    }

    /**
     * Find all directory files
     */
    protected void findDirectories() throws NoDirectoryAvailableException {
        dirs = new byte[3][];
        
        for (String fileName : gameFilesMap.keySet()) {
            if (fileName.equalsIgnoreCase("object")) {
                dirs[1] = gameFilesMap.get(fileName);
            }
            else if (fileName.equalsIgnoreCase("words.tok")) {
                dirs[2] = gameFilesMap.get(fileName);
            }
            else if (fileName.toLowerCase().endsWith("dir")) {
                dirs[0] = gameFilesMap.get(fileName);
                
                v3GameSignature = fileName.toUpperCase().replaceAll("DIR$", "");
            }
        }
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
    public InputStream open(byte resType, short resNumber) throws IOException, ResourceException {
        if (resType > TYPE_WORD) {
            throw new ResourceTypeInvalidException();
        }

        switch (resType) {
            case ResourceProvider.TYPE_OBJECT:
                if (isCrypted(dirs[1])) {
                    return new CryptedInputStream(new ByteArrayInputStream(dirs[1]), getKey());
                }

                return new ByteArrayInputStream(dirs[1]);

            case ResourceProvider.TYPE_WORD:
                return new ByteArrayInputStream(dirs[2]);
        }

        try {
            if (entries[resType] != null) {
                int vol, offset, compressed, uncompressed;

                vol = entries[resType].getVolume(resNumber);
                offset = entries[resType].getOffset(resNumber);

                if ((vol != -1) && (offset != -1)) {
                    byte[] b;
                    RandomAccessFile file;
                    InputStream in;

                    try {
                        b = new byte[7];
                        file = new RandomAccessFile(new ByteArrayInputStream(vols[vol]));
                        file.seek(offset);
                        file.read(b, 0, 7);
                    } catch (IndexOutOfBoundsException ioobex) {
                        throw new ResourceNotExistingException();
                    }

                    if ((b[0] != 0x12) || (b[1] != 0x34)) {
                        throw new CorruptedResourceException();
                    }

                    uncompressed = ByteCaster.lohiUnsignedShort(b, 3);
                    compressed = ByteCaster.lohiUnsignedShort(b, 5);
                    in = new SegmentedInputStream(file, offset + 7, compressed);
                    
                    if ((resType == TYPE_PICTURE) && ((b[2] & 0x80) == 0x80)) {
                        // AGI V3 PICTURE compression is used only if the third byte
                        // of the header, i.e. vol number, has the top bit set.
                        in = new PictureInputStream(in);
                    } else {
                        if (compressed != uncompressed) {
                            in = new LZWInputStream(in);
                        }
                    }

                    return in;
                }
            }
        } catch (IndexOutOfBoundsException ioobex) {
            throw new ResourceTypeInvalidException();
        }

        throw new ResourceNotExistingException();
    }

    protected byte[] getVolumeFile(int vol) throws VolumeNotFoundException {
        byte[] fileData = vols[vol];

        if (fileData == null) {
            throw new VolumeNotFoundException();
        }

        return fileData;
    }

    protected byte[] getDirectoryFile(int resType) throws IOException, VolumeNotFoundException {
        byte[] fileData;

        switch (resType) {
            case ResourceProvider.TYPE_OBJECT:
                fileData = dirs[1];
                break;
            case ResourceProvider.TYPE_WORD:
                fileData = dirs[2];
                break;
            case ResourceProvider.TYPE_LOGIC:
            case ResourceProvider.TYPE_PICTURE:
            case ResourceProvider.TYPE_SOUND:
            case ResourceProvider.TYPE_VIEW:
                fileData = dirs[0];
            default:
                fileData = null;
        }

        if (fileData == null) {
            throw new VolumeNotFoundException();
        }

        return fileData;
    }

    public String getV3GameSig() {
        return v3GameSignature;
    }
}