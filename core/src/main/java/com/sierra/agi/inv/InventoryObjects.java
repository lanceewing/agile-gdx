/*
 * InventoryObjects.java
 */

package com.sierra.agi.inv;

import com.sierra.agi.io.ByteCasterStream;
import com.sierra.agi.res.ResourceConfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Stores Objects of the game.
 * <p>
 * <B>Object File Format</B><BR>
 * The object file stores two bits of information about the inventory items used
 * in an AGI game. The starting room location and the name of the inventory item.
 * It also has a byte that determines the maximum number of animated objects.
 * </P><P>
 * <B>File Encryption</B><BR>
 * The first obstacle to overcome is the fact that most object files are
 * encrypted. I say most because some of the earlier AGI games were not, in
 * which case you can skip to the next section. Those that are encrypted are done
 * so with the string "Avis Durgan" (or, in case of AGDS games, "Alex Simkin").
 * The process of unencrypting the file is to simply taken every eleven bytes
 * from the file and XOR each element of those eleven bytes with the corresponding
 * element in the string "Avis Durgan". This sort of encryption is very easy to
 * crack if you know what you are doing and is simply meant to act as a shield
 * so as not to encourage cheating. In some games, however, the object names are
 * clearly visible in the saved game files even when the object file is encrypted,
 * so it's not a very effective shield.
 * <p>
 * <B>File Format</B><BR>
 * <TABLE BORDER=1>
 * <THEAD><TR><TD>Byte</TD><TD>Meaning</TD></TR></THEAD>
 * <TBODY>
 * <TR><TD>0-1</TD><TD>Offset of the start of inventory item names</TD></TR>
 * <TR><TD>2</TD><TD>Maximum number of animated objects</TD></TR>
 * </TBODY></TABLE>
 * <p>
 * Following the first three bytes as a section containing a three byte entry
 * for each inventory item all of which conform to the following format:
 * <p>
 * <TABLE BORDER=1>
 * <THEAD><TR><TD>Byte</TD><TD>Meaning</TD></TR></THEAD>
 * <TBODY>
 * <TR><TD>0-1</TD><TD>Offset of inventory item name i</TD></TR>
 * <TR><TD>2</TD><TD>Starting room number for inventory item i or 255 carried</TD></TR>
 * </TBODY></TABLE>
 * <p>
 * Where i is the entry number starting at 0. All offsets are taken from the
 * start of entry for inventory item 0 (not the start of the file).
 * <p>
 * Then comes the textual names themselves. This is simply a list of NULL
 * terminated strings. The offsets mentioned in the above section point to the
 * first character in the string and the last character is the one before the
 * 0x00.
 *
 * @author Dr. Z, Lance Ewing (Documentation)
 * @version 0.00.00.01
 */
public class InventoryObjects implements InventoryProvider {
    /**
     * Object list.
     */
    protected InventoryObject[] objects = null;
    protected int numOfAnimatedObjects;

    public InventoryObjects(ResourceConfiguration config) {

    }

    /**
     * Loads the String Table from a AGI Object file. Internal Uses only.
     *
     * @param stream AGI Object file's Stream.
     * @param offset Starting offset.
     * @return Returns a Hashtable containing the strings with their offset has
     * the Hash key.
     * @throws IOException Caller must handle IOException from his stream.
     */
    protected static Hashtable loadStringTable(InputStream stream, int offset) throws IOException {
        Hashtable h = new Hashtable(64);
        String o = "";
        int s = offset;

        while (true) {
            int c = stream.read();
            offset++;

            if (c < 0) {
                break;
            }

            if (c == 0) {
                h.put(Integer.valueOf(s), o);
                o = "";
                s = offset;
            } else {
                o += (char) c;
            }
        }

        return h;
    }

    /**
     * Loads a AGI Object File from a stream.
     *
     * @param stream Stream where the Objects are contained. Must be a AGI
     *               compliant format.
     * @return Returns the number of object contained in the stream.
     * @throws IOException Caller must handle IOException from his stream.
     */
    public InventoryObjects loadInventory(InputStream stream) throws IOException {
        ByteCasterStream rawStream = new ByteCasterStream(stream);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(rawStream.readAllBytes());
        ByteCasterStream bstream = new ByteCasterStream(byteArrayInputStream);

        /* Calculate Inventory Object Count */
        int padSize = 3;
        int nobject = bstream.lohiReadUnsignedShort();
        nobject /= padSize;

        this.objects = new InventoryObject[nobject];
        int[] offsets = new int[nobject];


        this.numOfAnimatedObjects = bstream.readUnsignedByte();

        int offset = 0;

        for (int i = 0; i < nobject; i++) {
            offsets[i] = bstream.lohiReadUnsignedShort();
            objects[i] = new InventoryObject(bstream.readUnsignedByte());
            offset += padSize;
        }

        Hashtable hash = loadStringTable(byteArrayInputStream, offset);

        for (int i = 0; i < nobject; i++) {
            objects[i].name = (String) hash.get(Integer.valueOf(offsets[i]));
        }

        byteArrayInputStream.close();
        rawStream.close();
        bstream.close();
        return this;
    }

    /**
     * Returns the number of objects contained in this object.
     *
     * @return Returns the number of objects.
     */
    public short getCount() {
        return (short) objects.length;
    }

    /**
     * Returns an Object contained in this object based on his index.
     *
     * @param index Index number of the wanted object.
     * @return Returns the wanted object.
     */
    public InventoryObject getObject(short index) {
        return objects[index];
    }

    public void resetLocationTable(short[] locations) {
        int i;

        for (i = 0; i < objects.length; i++) {
            locations[i] = objects[i].getLocation();
        }
    }

    public InventoryObject[] getObjects() {
        return objects;
    }

    public int getNumOfAnimatedObjects() {
        return numOfAnimatedObjects;
    }

    public byte[] encode(short[] locations) throws Exception{
        // Recreate the Item Entries
        // key = object name
        // value = offset
        Map<String, Integer> itemEntries = new HashMap<>();
        // We need to preserve the order of the objects in the list
        List<InventoryObject> itemList = new ArrayList<>();

        int count = objects.length;
        int num = count * 3;

        int offset = num;
        for (int i = 0; i < count; i++) {
            InventoryObject inventoryObject = objects[i];
            if (!itemEntries.containsKey(inventoryObject.name)) {
                itemEntries.put(inventoryObject.name, offset);
                itemList.add(inventoryObject);
                // 1 = NUL char
                offset = offset + (inventoryObject.name.length() + 1);
            }
        }

        // Dump of the in memory OBJECT file including updates made by get, put and drop commands
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write((num & 0xFF));
        outputStream.write((num >> 8) & 0xFF);
        outputStream.write(numOfAnimatedObjects);

        for (int i = 0; i < count; i++) {
            InventoryObject invObject = objects[i];
            short location = locations[i];

            int itemOffset = itemEntries.get(invObject.name);
            outputStream.write(itemOffset & 0xFF);
            outputStream.write((itemOffset >> 8) & 0xFF);
            outputStream.write(location);
        }

        for (InventoryObject inventoryObject : itemList) {
            byte[] nameBytes = inventoryObject.getName().getBytes();
            outputStream.write(nameBytes);
            outputStream.write(0);
        }

        byte[] buffer = outputStream.toByteArray();

        return buffer;
    }
}