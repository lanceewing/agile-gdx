/*
 * ResourceProviderV3.java
 */

package com.sierra.agi.res.v3;

import com.sierra.agi.io.*;
import com.sierra.agi.res.*;
import com.sierra.agi.res.dir.ResourceDirectory;

import java.io.*;
import java.util.Arrays;

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
public class ResourceProviderV3 extends com.sierra.agi.res.v2.ResourceProviderV2 {
    protected File[] vols;
    protected File[] dirs;

    /**
     * Initialize the ResourceProvider implentation to access
     * resource on the file system.
     *
     * @param folder Resource's folder or File inside the resource's
     *               folder.
     */
    public ResourceProviderV3(File folder) throws IOException, ResourceException {
        super(folder);
    }

    /**
     * Find volumes files
     */
    protected void readVolumes() throws NoVolumeAvailableException {
        vols = path.listFiles(new VolumeFilenameFilter());

        if (vols == null) {
            throw new NoVolumeAvailableException();
        }

        if (vols.length == 0) {
            throw new NoVolumeAvailableException();
        }

        Arrays.sort(vols, new VolumeSorter());
    }

    protected int calculateCRCFromScratch() throws IOException {
        byte[] b = new byte[8];
        int c, i;
        InputStream stream;

        c = super.calculateCRCFromScratch();

        stream = new FileInputStream(dirs[0]);
        stream.read(b, 0, 8);
        stream.close();

        for (i = 0; i < 8; i++) {
            c += (b[i] & 0xff);
        }

        return c;
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
        stream = new FileInputStream(dirs[0]);
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
                ax = (int) dirs[0].length();
            }

            o[i + 4] = ax;
        }

        dirfile = new RandomAccessFile(dirs[0], "r");
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
        dirs = path.listFiles(new DirectoryFilenameFilter());

        if (dirs == null) {
            throw new NoDirectoryAvailableException();
        }

        if (dirs.length == 0) {
            throw new NoDirectoryAvailableException();
        }

        Arrays.sort(dirs, new DirectorySorter());
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
     * @see com.sierra.agi.res.ResourceProvider#TYPE_LOGIC
     * @see com.sierra.agi.res.ResourceProvider#TYPE_OBJECT
     * @see com.sierra.agi.res.ResourceProvider#TYPE_PICTURE
     * @see com.sierra.agi.res.ResourceProvider#TYPE_SOUND
     * @see com.sierra.agi.res.ResourceProvider#TYPE_VIEW
     * @see com.sierra.agi.res.ResourceProvider#TYPE_WORD
     */
    public InputStream open(byte resType, short resNumber) throws IOException, ResourceException {
        if (resType > TYPE_WORD) {
            throw new ResourceTypeInvalidException();
        }

        switch (resType) {
            case ResourceProvider.TYPE_OBJECT:
                if (isCrypted(dirs[1])) {
                    return new CryptedInputStream(new FileInputStream(dirs[1]), getKey(false));
                }

                return new FileInputStream(dirs[1]);

            case ResourceProvider.TYPE_WORD:
                return new FileInputStream(dirs[2]);
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
                        file = new RandomAccessFile(vols[vol], "r");
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

                    if (resType == TYPE_PICTURE) {
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

    protected File getVolumeFile(int vol) throws IOException {
        File file = vols[vol];

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        return file;
    }

    protected File getDirectoryFile(int resType) throws IOException {
        File file;

        switch (resType) {
            case ResourceProvider.TYPE_OBJECT:
                file = dirs[1];
                break;
            case ResourceProvider.TYPE_WORD:
                file = dirs[2];
                break;
            case ResourceProvider.TYPE_LOGIC:
            case ResourceProvider.TYPE_PICTURE:
            case ResourceProvider.TYPE_SOUND:
            case ResourceProvider.TYPE_VIEW:
                file = dirs[0];
            default:
                return null;
        }

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        return file;
    }

    public String getV3GameSig() {
        return dirs[0].getName().toUpperCase().replaceAll("DIR$", "");
    }
    
    protected static class VolumeFilenameFilter implements java.io.FilenameFilter {
        public boolean accept(File dir, String name) {
            int c;
            String s;

            c = name.lastIndexOf('.');

            if (c == -1) {
                return false;
            }

            if (!Character.isDigit(name.charAt(c + 1))) {
                return false;
            }

            s = name.substring(0, c);
            s = s.toLowerCase();

            return s.endsWith("vol");
        }
    }

    protected static class VolumeSorter implements java.util.Comparator {
        public int compare(Object o1, Object o2) {
            return ((File) o1).getName().compareToIgnoreCase(((File) o2).getName());
        }
    }

    protected static class DirectoryFilenameFilter implements java.io.FilenameFilter {
        public boolean accept(File dir, String name) {
            if (name.equalsIgnoreCase("object")) {
                return true;
            }

            if (name.equalsIgnoreCase("words.tok")) {
                return true;
            }

            return name.toLowerCase().endsWith("dir");
        }
    }

    protected static class DirectorySorter implements java.util.Comparator {
        public int compare(Object o1, Object o2) {
            String s1 = ((File) o1).getName();
            String s2 = ((File) o2).getName();

            if (s1.toLowerCase().endsWith("dir")) {
                if (!s2.toLowerCase().endsWith("dir")) {
                    return -1;
                }
            } else {
                if (s2.toLowerCase().endsWith("dir")) {
                    return 1;
                }
            }

            return s1.compareToIgnoreCase(s2);
        }
    }
}